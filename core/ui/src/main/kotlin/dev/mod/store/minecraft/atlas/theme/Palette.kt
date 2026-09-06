package dev.mod.store.minecraft.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Ink-black behind everything and a hot red for anything you can act on. The accent never paints a
 * large area — it appears as small solid shapes (a key, a pill, an aura) so cover art stays the
 * brightest thing on screen.
 */
object Palette {

    // The action colour
    val Accent = Color(0xFFFF3B4E)
    val AccentSoft = Color(0xFFFF8A94)
    val AccentDeep = Color(0xFFB00F22)

    /** Text on artwork. */
    val OnAccent = Color(0xFFFFF3F4)

    /** Text and icons on a filled [Accent] surface. */
    val OnAccentDark = Color(0xFFFFF5F6)

    // Violet — the colour of feature surfaces (hero cards, sheets, empty artwork)
    val Violet = Color(0xFF8A5CFF)
    val VioletDeep = Color(0xFF4B23C7)
    val VioletSoft = Color(0xFFB79BFF)

    // Supporting colours
    val Ember = Color(0xFFFF9153)
    val Gold = Color(0xFFFFD54A)
    val Sky = Color(0xFF56CCF2)
    val Magenta = Color(0xFFFF5FA8)

    // Surfaces — ink, not grey
    val Canvas = Color(0xFF0A0B11)
    val Surface = Color(0xFF14161F)
    val SurfaceHigh = Color(0xFF1D2130)
    val Stroke = Color(0xFF2A2F42)

    // Text
    val TextPrimary = Color(0xFFF4F5FA)
    val TextMuted = Color(0xFFA0A6BC)
    val TextFaint = Color(0xFF6C7290)

    // Status
    val Positive = Color(0xFF3DE38B)
    val Negative = Color(0xFFFF6B6B)

    // Placeholder shimmer
    val ShimmerBase = Color(0xFF191C29)
    val ShimmerHighlight = Color(0xFF232839)

    // Category colours
    val CategoryLagoon = Sky
    val CategoryLime = Color(0xFFB4F04A)
    val CategoryRose = Magenta
    val CategoryAmber = Gold

    // Layers over artwork
    val Glass = Color(0x1FFFFFFF)
    val GlassStroke = Color(0x26FFFFFF)
    val Scrim = Color(0xB30A0B11)
}
