package dev.mod.store.minecraft.feature.loadout

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.domain.creation.FetchCreationUseCase
import dev.mod.store.minecraft.domain.creation.FetchFileSizeUseCase
import dev.mod.store.minecraft.domain.loadout.DownloadCreationUseCase
import dev.mod.store.minecraft.domain.loadout.DownloadStatus
import dev.mod.store.minecraft.domain.loadout.IsCreationDownloadedUseCase
import dev.mod.store.minecraft.domain.loadout.OpenCreationFileUseCase
import dev.mod.store.minecraft.domain.loadout.RecordDownloadUseCase
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.FileItem
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.FileStatus
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.Intent
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.Label
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder

private const val STALL_THRESHOLD_MS = 5_000L

interface LoadoutStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object Retry : Intent
        data class StartDownload(val url: String) : Intent
        data class CancelDownload(val url: String) : Intent
        data class Install(val url: String) : Intent
    }

    data class State(
        val title: String = "",
        val items: List<FileItem> = emptyList(),
        val stage: ScreenStage = ScreenStage.Loading,
    ) {
        val showVpnHint: Boolean get() = items.any { it.stalled }
    }

    data class FileItem(
        val url: String,
        val name: String,
        val sizeBytes: Long?,
        val status: FileStatus,
        val stalled: Boolean = false,
    )

    sealed interface FileStatus {
        data object Idle : FileStatus

        data class Downloading(val downloadedBytes: Long, val totalBytes: Long) : FileStatus {
            val hasKnownTotal: Boolean get() = totalBytes > 0L
            val fraction: Float
                get() = if (hasKnownTotal) {
                    (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                } else {
                    0f
                }
            val percent: Int get() = (fraction * 100f).toInt()
        }

        data object Ready : FileStatus
    }

    enum class Notice { DownloadFailed, OpenFailed }

    sealed interface Label {
        data class Notify(val notice: Notice) : Label
    }
}

private fun fileNameFor(url: String, index: Int, extension: String): String {
    val raw = url.substringAfterLast('/').substringBefore('?')
    val decoded = runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
    val name = decoded.ifBlank { "creation_${index + 1}$extension" }
    return if (name.endsWith(extension, ignoreCase = true)) name else "$name$extension"
}

internal class LoadoutStoreFactory(
    private val storeFactory: StoreFactory,
    private val creationId: Int,
    private val fetchCreation: FetchCreationUseCase,
    private val fetchFileSize: FetchFileSizeUseCase,
    private val downloadCreation: DownloadCreationUseCase,
    private val isDownloaded: IsCreationDownloadedUseCase,
    private val openFile: OpenCreationFileUseCase,
    private val recordDownload: RecordDownloadUseCase,
    private val faults: FaultMessages,
) {

    fun create(): LoadoutStore =
        object : LoadoutStore, Store<Intent, State, Label> by storeFactory.create(
            name = "LoadoutStore",
            initialState = State(),
            bootstrapper = coroutineBootstrapper { dispatch(Action.Load) },
            executorFactory = { Executor() },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Action {
        data object Load : Action
    }

    private sealed interface Message {
        data object Loading : Message
        data class Loaded(val title: String, val items: List<FileItem>) : Message
        data class Failed(val message: String) : Message
        data class SizeSet(val url: String, val size: Long) : Message
        data class StatusSet(val url: String, val status: FileStatus) : Message
        data class Progress(val url: String, val downloaded: Long, val total: Long, val madeProgress: Boolean) : Message
        data class Stalled(val url: String) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        Message.Loading -> copy(stage = ScreenStage.Loading)
        is Message.Loaded -> copy(title = message.title, items = message.items, stage = ScreenStage.Ready)
        is Message.Failed -> copy(stage = ScreenStage.Failed(message.message))
        is Message.SizeSet -> patch(message.url) { it.copy(sizeBytes = message.size) }
        is Message.StatusSet -> patch(message.url) { it.copy(status = message.status, stalled = false) }
        is Message.Progress -> patch(message.url) {
            it.copy(
                status = FileStatus.Downloading(message.downloaded, message.total),
                stalled = if (message.madeProgress) false else it.stalled,
            )
        }
        is Message.Stalled -> patch(message.url) {
            if (it.status is FileStatus.Downloading) it.copy(stalled = true) else it
        }
    }

    private inline fun State.patch(url: String, transform: (FileItem) -> FileItem): State =
        copy(items = items.map { if (it.url == url) transform(it) else it })

    private inner class Executor :
        CoroutineExecutor<Intent, Action, State, Message, Label>() {

        private val downloadJobs = mutableMapOf<String, Job>()
        private val stallWatchers = mutableMapOf<String, Job>()

        override fun executeAction(action: Action) {
            when (action) {
                Action.Load -> load()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Retry -> load()
                is Intent.StartDownload -> startDownload(intent.url)
                is Intent.CancelDownload -> cancelDownload(intent.url)
                is Intent.Install -> install(intent.url)
            }
        }

        private fun load() {
            scope.launch {
                dispatch(Message.Loading)
                when (val outcome = fetchCreation(creationId)) {
                    is Outcome.Done -> {
                        val creation = outcome.value
                        val extension = creation.category.fileExtension
                        val items = creation.fileUrls.mapIndexed { index, url ->
                            FileItem(
                                url = url,
                                name = fileNameFor(url, index, extension),
                                sizeBytes = null,
                                status = FileStatus.Idle,
                            )
                        }
                        dispatch(Message.Loaded(creation.title, items))
                        items.forEach { item ->
                            resolveSize(item.url)
                            resolveExisting(item.url, item.name)
                        }
                    }
                    is Outcome.Failed -> dispatch(Message.Failed(faults.text(outcome.error)))
                }
            }
        }

        private fun resolveSize(url: String) {
            scope.launch {
                (fetchFileSize(url) as? Outcome.Done)?.let { dispatch(Message.SizeSet(url, it.value)) }
            }
        }

        private fun resolveExisting(url: String, name: String) {
            scope.launch {
                if (isDownloaded(name)) dispatch(Message.StatusSet(url, FileStatus.Ready))
            }
        }

        private fun startDownload(url: String) {
            val item = findItem(url) ?: return
            when (item.status) {
                is FileStatus.Downloading -> return
                FileStatus.Ready -> {
                    install(url)
                    return
                }
                FileStatus.Idle -> Unit
            }

            dispatch(Message.Progress(url, 0L, item.sizeBytes ?: 0L, madeProgress = false))
            scheduleStall(url)

            downloadJobs[url] = scope.launch {
                downloadCreation(url, item.name).collect { status ->
                    when (status) {
                        is DownloadStatus.Running -> {
                            val prev = (findItem(url)?.status as? FileStatus.Downloading)?.downloadedBytes ?: 0L
                            dispatch(
                                Message.Progress(
                                    url = url,
                                    downloaded = status.downloadedBytes,
                                    total = status.totalBytes,
                                    madeProgress = status.downloadedBytes > prev,
                                ),
                            )
                            scheduleStall(url)
                        }
                        DownloadStatus.Finished -> {
                            cancelStall(url)
                            dispatch(Message.StatusSet(url, FileStatus.Ready))
                            // Counted here and nowhere earlier: a cancelled or stalled download
                            // never reaches this branch, so only real downloads reach the stats.
                            // Fire and forget — the reader is not kept waiting on a statistic.
                            scope.launch { recordDownload(creationId) }
                        }
                        DownloadStatus.Failed -> {
                            cancelStall(url)
                            dispatch(Message.StatusSet(url, FileStatus.Idle))
                            publish(Label.Notify(LoadoutStore.Notice.DownloadFailed))
                        }
                    }
                }
                downloadJobs.remove(url)
            }
        }

        private fun cancelDownload(url: String) {
            downloadJobs.remove(url)?.cancel()
            cancelStall(url)
            dispatch(Message.StatusSet(url, FileStatus.Idle))
        }

        private fun install(url: String) {
            val item = findItem(url) ?: return
            scope.launch {
                if (!openFile(item.name)) {
                    publish(Label.Notify(LoadoutStore.Notice.OpenFailed))
                }
            }
        }

        private fun scheduleStall(url: String) {
            stallWatchers[url]?.cancel()
            stallWatchers[url] = scope.launch {
                delay(STALL_THRESHOLD_MS)
                dispatch(Message.Stalled(url))
            }
        }

        private fun cancelStall(url: String) {
            stallWatchers.remove(url)?.cancel()
        }

        private fun findItem(url: String): FileItem? = state().items.firstOrNull { it.url == url }
    }
}
