package dev.mod.store.minecraft.data.database.loadout

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import dev.mod.store.minecraft.core.common.concurrency.DispatcherProvider
import dev.mod.store.minecraft.domain.loadout.DownloadStatus
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Streams a file to the public Downloads/Mods folder (MediaStore on Q+, direct file on older
 * devices), reports progress, and opens finished files via the system so Minecraft can import
 * them. Ported in spirit from the original save layer but built on HttpURLConnection.
 */
internal class LoadoutLocalDataSource(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
) {

    fun download(url: String, fileName: String): Flow<DownloadStatus> = flow {
        var target: SaveTarget? = null
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = true
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                emit(DownloadStatus.Failed)
                return@flow
            }

            val createdTarget = createTarget(fileName)
            if (createdTarget == null) {
                emit(DownloadStatus.Failed)
                return@flow
            }
            target = createdTarget

            val totalBytes = connection.contentLengthLong
            emit(DownloadStatus.Running(downloadedBytes = 0L, totalBytes = totalBytes))

            createdTarget.outputStream.use { output ->
                connection.inputStream.use { input ->
                    streamWithProgress(input, output, totalBytes) { status -> emit(status) }
                }
            }
            createdTarget.commit()
            emit(DownloadStatus.Finished)
        } catch (cancellation: CancellationException) {
            target?.delete()
            throw cancellation
        } catch (error: Exception) {
            Napier.e(message = "Download failed for $fileName", throwable = error, tag = "vault")
            target?.delete()
            emit(DownloadStatus.Failed)
        } finally {
            // Every early return above left the socket to the garbage collector.
            runCatching { connection?.disconnect() }
        }
    }.flowOn(dispatchers.io)

    suspend fun isDownloaded(fileName: String): Boolean = withContext(dispatchers.io) {
        savedFile(fileName).let { it.exists() && it.isFile }
    }

    suspend fun openInMinecraft(fileName: String): Boolean = withContext(dispatchers.io) {
        val file = savedFile(fileName)
        if (!file.exists() || !file.isFile) return@withContext false

        runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, MIME_TYPE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            Napier.e(message = "Failed to open $fileName", throwable = it, tag = "vault")
        }.isSuccess
    }

    @Suppress("DEPRECATION")
    private fun savedFile(name: String): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            SUBDIR,
        )
        return File(dir, name)
    }

    private suspend inline fun streamWithProgress(
        input: InputStream,
        output: OutputStream,
        totalBytes: Long,
        crossinline emit: suspend (DownloadStatus) -> Unit,
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var downloaded = 0L
        var lastEmitted = 0L
        while (true) {
            currentCoroutineContext().ensureActive()
            val read = input.read(buffer)
            if (read <= 0) break
            output.write(buffer, 0, read)
            downloaded += read
            if (downloaded - lastEmitted >= EMIT_THRESHOLD_BYTES) {
                lastEmitted = downloaded
                emit(DownloadStatus.Running(downloadedBytes = downloaded, totalBytes = totalBytes))
            }
        }
        if (lastEmitted != downloaded) {
            emit(DownloadStatus.Running(downloadedBytes = downloaded, totalBytes = totalBytes))
        }
    }

    private fun createTarget(name: String): SaveTarget? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createMediaStoreTarget(name)
        } else {
            createLegacyTarget(name)
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createMediaStoreTarget(name: String): SaveTarget? {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$SUBDIR")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values) ?: return null
        val output = resolver.openOutputStream(uri) ?: run {
            resolver.delete(uri, null, null)
            return null
        }
        return SaveTarget.MediaStoreTarget(
            outputStream = output,
            onFinalize = {
                val finalize = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                resolver.update(uri, finalize, null, null)
            },
            onDelete = { resolver.delete(uri, null, null) },
        )
    }

    @Suppress("DEPRECATION")
    private fun createLegacyTarget(name: String): SaveTarget? {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            SUBDIR,
        )
        if (!dir.exists() && !dir.mkdirs()) return null
        val file = File(dir, name)
        val output = runCatching { file.outputStream() }.getOrNull() ?: return null
        return SaveTarget.LegacyTarget(file = file, outputStream = output)
    }

    /**
     * Where a download is being written. Note the name: `finalize` would override
     * `Object.finalize`, so the garbage collector would publish a half-written MediaStore row on
     * the finalizer thread. It is [commit] for that reason and must stay that way.
     */
    private sealed class SaveTarget {
        abstract val outputStream: OutputStream
        abstract fun commit()
        abstract fun delete()

        class MediaStoreTarget(
            override val outputStream: OutputStream,
            private val onFinalize: () -> Unit,
            private val onDelete: () -> Unit,
        ) : SaveTarget() {
            override fun commit() = onFinalize()
            override fun delete() {
                runCatching { onDelete() }
            }
        }

        class LegacyTarget(
            private val file: File,
            override val outputStream: OutputStream,
        ) : SaveTarget() {
            override fun commit() = Unit
            override fun delete() {
                runCatching { if (file.exists()) file.delete() }
            }
        }
    }

    private companion object {
        const val SUBDIR = "Mods"
        const val MIME_TYPE = "application/octet-stream"
        const val EMIT_THRESHOLD_BYTES = 64L * 1024L
        const val CONNECT_TIMEOUT_MS = 30_000
        const val READ_TIMEOUT_MS = 30_000
    }
}
