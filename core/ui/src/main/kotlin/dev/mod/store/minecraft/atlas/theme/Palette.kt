package dev.mod.store.minecraft.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * A small, friendly palette: a soft dark background so bright mod covers pop, one warm orange for
 * everything you can tap, and a handful of clear colours for the categories. Nothing else — the
 * app is used by kids, so the fewer colours they have to learn, the better.
 */
object Palette {

    // The one action colour
    val Accent = Color(0xFFFF6A2B)
    val AccentSoft = Color(0xFFFF9460)
    val AccentDeep = Color(0xFFC94512)

    /** Text on artwork. */
    val OnAccent = Color(0xFFFFF6F1)

    /** Text on a filled [Accent] surface. */
    val OnAccentDark = Color(0xFF2A0C00)

    // Supporting colours
    val Ember = Color(0xFFFF8A3D)
    val Gold = Color(0xFFFFC53D)
    val Sky = Color(0xFF4FC3F7)
    val Magenta = Color(0xFFE879F9)

    // Surfaces — soft, not pitch black
    val Canvas = Color(0xFF14161B)
    val Surface = Color(0xFF1E2128)
    val SurfaceHigh = Color(0xFF272B34)
    val Stroke = Color(0xFF343945)

    // Text
    val TextPrimary = Color(0xFFF2F4F7)
    val TextMuted = Color(0xFFA2AAB8)
    val TextFaint = Color(0xFF6E7686)

    // Status
    val Positive = Color(0xFF5ED47C)
    val Negative = Color(0xFFFF6B6B)

    // Placeholder shimmer
    val ShimmerBase = Color(0xFF232730)
    val ShimmerHighlight = Color(0xFF2F3540)

    // Category colours
    val CategoryLagoon = Sky
    val CategoryLime = Color(0xFF8BD450)
    val CategoryRose = Magenta
    val CategoryAmber = Gold

    // Layers over artwork
    val Glass = Color(0x1AFFFFFF)
    val GlassStroke = Color(0x1FFFFFFF)
    val Scrim = Color(0xB3101218)
}
