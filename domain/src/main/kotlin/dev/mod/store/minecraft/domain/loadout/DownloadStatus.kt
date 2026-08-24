package dev.mod.store.minecraft.domain.loadout

/** Progress of a single file download as it streams to disk. */
sealed interface DownloadStatus {

    data class Running(
        val downloadedBytes: Long,
        val totalBytes: Long,
    ) : DownloadStatus {

        val hasKnownTotal: Boolean get() = totalBytes > 0L

        val fraction: Float
            get() = if (hasKnownTotal) {
                (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
            } else {
                0f
            }

        val percent: Int get() = (fraction * 100f).toInt()
    }

    data object Finished : DownloadStatus

    data object Failed : DownloadStatus
}
