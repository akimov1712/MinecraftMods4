package dev.mod.store.minecraft.core.ui.modifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.mod.store.minecraft.core.ui.theme.Palette

/** Clickable with the app's standard ripple, without the Material component boilerplate. */
@Composable
fun Modifier.pressable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction,
        indication = ripple(color = Palette.AccentSoft),
        enabled = enabled,
        onClick = onClick,
    )
}
