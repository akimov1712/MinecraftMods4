package dev.mod.store.minecraft.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Underground: wet stone in the dark, lit by one torch.
 *
 * The background is a cold green-black — the colour of a cave wall just outside the light — and the
 * one warm colour in the app is torchlight. It is used sparingly and never over a large area, so
 * cover art and the amber of an action are the only bright things on a screen. The cool glow of
 * cave lichen marks what is alive: something new, something downloaded, something that worked.
 */
object Palette {

    // Torchlight — everything you can act on
    val Accent = Color(0xFFFF9E3D)
    val AccentSoft = Color(0xFFFFC98A)
    val AccentDeep = Color(0xFFA3561A)

    /** Text on artwork. */
    val OnAccent = Color(0xFFFFF4E6)

    /** Text and icons on a filled [Accent] surface. */
    val OnAccentDark = Color(0xFF231202)

    // Lichen — the cold glow of something living in the dark
    val Glow = Color(0xFF7BE38F)
    val GlowDeep = Color(0xFF1F6B3C)
    val GlowSoft = Color(0xFFB6F0C2)

    // Supporting colours, all dug out of the same cave
    val Ember = Color(0xFFFF7A45)
    val Gold = Color(0xFFE8C76A)
    val Sky = Color(0xFF6FB7D9)
    val Amethyst = Color(0xFFC08ADB)

    // Surfaces — damp stone, never grey
    val Canvas = Color(0xFF080C0B)
    val Surface = Color(0xFF131A18)
    val SurfaceHigh = Color(0xFF1C2523)
    val Stroke = Color(0xFF2B3733)

    // Text — bone, not paper
    val TextPrimary = Color(0xFFEDF0EA)
    val TextMuted = Color(0xFFA2AEA7)
    val TextFaint = Color(0xFF6C7A74)

    // Status
    val Positive = Color(0xFF64D98A)
    val Negative = Color(0xFFE5564E)

    // Placeholder shimmer
    val ShimmerBase = Color(0xFF151D1B)
    val ShimmerHighlight = Color(0xFF203029)

    // Chart places — kept for anything that still ranks
    val Podium1 = Gold
    val Podium2 = Color(0xFFC3CBC6)
    val Podium3 = Color(0xFFC08A52)

    // Category colours — four minerals
    val CategoryLagoon = Color(0xFF6FD0C6)
    val CategoryLime = Color(0xFF9BD75B)
    val CategoryRose = Amethyst
    val CategoryAmber = Color(0xFFE8B45A)

    // Layers over artwork
    val Glass = Color(0x1FFFFFFF)
    val GlassStroke = Color(0x26FFFFFF)
    val Scrim = Color(0xB3080C0B)
}
