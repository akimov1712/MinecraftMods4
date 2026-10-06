package dev.mod.store.minecraft.domain.loadout

/** How many creation files are sitting on the phone right now. */
class CountDownloadedFilesUseCase(
    private val loadoutRepository: LoadoutRepository,
) {
    suspend operator fun invoke(): Int = loadoutRepository.countDownloaded()
}
