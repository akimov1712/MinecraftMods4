package dev.mod.store.minecraft.data.database.reaction

import com.russhwolf.settings.Settings
import dev.mod.store.minecraft.domain.reaction.ReactionLedger
import dev.mod.store.minecraft.domain.reaction.ReactionType

/**
 * Reactions remembered in SharedPreferences, one pair of keys per mod: the chosen reaction by its
 * wire name, and when this device last wrote one. A name this build does not recognise — say the
 * backend drops a reaction — reads as no reaction rather than failing.
 */
internal class ReactionLedgerImpl(
    private val settings: Settings,
) : ReactionLedger {

    override fun selected(modId: Int): ReactionType? =
        settings.getStringOrNull(selectedKey(modId))
            ?.let { name -> ReactionType.entries.firstOrNull { it.name == name } }

    override fun remember(modId: Int, reaction: ReactionType?) {
        if (reaction == null) {
            settings.remove(selectedKey(modId))
        } else {
            settings.putString(selectedKey(modId), reaction.name)
        }
    }

    override fun lastWriteAt(modId: Int): Long = settings.getLong(writeKey(modId), 0L)

    override fun markWrite(modId: Int, atMillis: Long) {
        settings.putLong(writeKey(modId), atMillis)
    }

    private fun selectedKey(modId: Int) = "reaction.selected.$modId"
    private fun writeKey(modId: Int) = "reaction.write.$modId"
}
