package dev.mod.store.minecraft.data.database.loadout

import com.russhwolf.settings.Settings
import dev.mod.store.minecraft.domain.loadout.DownloadLedger

/** The mods this device has already reported a download for, one flag per mod. */
internal class DownloadLedgerImpl(
    private val settings: Settings,
) : DownloadLedger {

    override fun isCounted(modId: Int): Boolean = settings.getBoolean(key(modId), false)

    override fun markCounted(modId: Int) {
        settings.putBoolean(key(modId), true)
    }

    override fun unmark(modId: Int) {
        settings.remove(key(modId))
    }

    private fun key(modId: Int) = "download.counted.$modId"
}
