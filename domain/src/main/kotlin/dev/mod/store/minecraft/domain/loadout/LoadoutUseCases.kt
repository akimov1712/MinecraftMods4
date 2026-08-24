package dev.mod.store.minecraft.domain.loadout

import kotlinx.coroutines.flow.Flow

/** Streams a file download to storage. */
class DownloadCreationUseCase(
    private val loadoutRepository: LoadoutRepository,
) {
    operator fun invoke(url: String, fileName: String): Flow<DownloadStatus> =
        loadoutRepository.download(url, fileName)
}

/** Whether a creation file is already saved on the device. */
class IsCreationDownloadedUseCase(
    private val loadoutRepository: LoadoutRepository,
) {
    suspend operator fun invoke(fileName: String): Boolean = loadoutRepository.isDownloaded(fileName)
}

/** Hands a downloaded file to Minecraft via the system. */
class OpenCreationFileUseCase(
    private val loadoutRepository: LoadoutRepository,
) {
    suspend operator fun invoke(fileName: String): Boolean = loadoutRepository.openInMinecraft(fileName)
}
