package dev.mod.store.minecraft.data.database.loadout

import dev.mod.store.minecraft.domain.loadout.DownloadStatus
import dev.mod.store.minecraft.domain.loadout.LoadoutRepository
import kotlinx.coroutines.flow.Flow

internal class LoadoutRepositoryImpl(
    private val local: LoadoutLocalDataSource,
) : LoadoutRepository {

    override fun download(url: String, fileName: String): Flow<DownloadStatus> =
        local.download(url, fileName)

    override suspend fun isDownloaded(fileName: String): Boolean = local.isDownloaded(fileName)

    override suspend fun openInMinecraft(fileName: String): Boolean = local.openInMinecraft(fileName)
}
