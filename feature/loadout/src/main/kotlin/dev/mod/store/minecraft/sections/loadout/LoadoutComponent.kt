package dev.mod.store.minecraft.feature.loadout

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.domain.creation.FetchCreationUseCase
import dev.mod.store.minecraft.domain.creation.FetchFileSizeUseCase
import dev.mod.store.minecraft.domain.loadout.DownloadCreationUseCase
import dev.mod.store.minecraft.domain.loadout.IsCreationDownloadedUseCase
import dev.mod.store.minecraft.domain.loadout.OpenCreationFileUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface LoadoutComponent {
    val state: StateFlow<LoadoutStore.State>
    val labels: Flow<LoadoutStore.Label>
    fun onIntent(intent: LoadoutStore.Intent)
    fun back()
}

class DefaultLoadoutComponent(
    componentContext: ComponentContext,
    private val creationId: Int,
    private val onBack: () -> Unit,
) : LoadoutComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()
    private val fetchCreation: FetchCreationUseCase by inject()
    private val fetchFileSize: FetchFileSizeUseCase by inject()
    private val downloadCreation: DownloadCreationUseCase by inject()
    private val isDownloaded: IsCreationDownloadedUseCase by inject()
    private val openFile: OpenCreationFileUseCase by inject()
    private val faults: FaultMessages by inject()

    private val store = instanceKeeper.getStore {
        LoadoutStoreFactory(
            storeFactory = storeFactory,
            creationId = creationId,
            fetchCreation = fetchCreation,
            fetchFileSize = fetchFileSize,
            downloadCreation = downloadCreation,
            isDownloaded = isDownloaded,
            openFile = openFile,
            faults = faults,
        ).create()
    }

    override val state: StateFlow<LoadoutStore.State> = store.stateFlow(lifecycle)
    override val labels: Flow<LoadoutStore.Label> = store.labels

    override fun onIntent(intent: LoadoutStore.Intent) {
        store.accept(intent)
    }

    override fun back() = onBack()
}
