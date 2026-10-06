package dev.mod.store.minecraft.domain.loadout

import kotlinx.coroutines.flow.Flow

/** Downloads creation files to device storage and hands them off to Minecraft. */
interface LoadoutRepository {
    fun download(url: String, fileName: String): Flow<DownloadStatus>
    suspend fun isDownloaded(fileName: String): Boolean

    /** How many files this app has saved to the phone. */
    suspend fun countDownloaded(): Int

    /** Deletes every saved file and reports how many were removed. */
    suspend fun clearDownloaded(): Int
    suspend fun openInMinecraft(fileName: String): Boolean
}
