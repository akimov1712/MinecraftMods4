package dev.mod.store.minecraft.domain.loadout

/** Throws away every creation file this app has saved to the phone. */
class ClearDownloadedFilesUseCase(
    private val loadoutRepository: LoadoutRepository,
) {
    suspend operator fun invoke(): Int = loadoutRepository.clearDownloaded()
}
