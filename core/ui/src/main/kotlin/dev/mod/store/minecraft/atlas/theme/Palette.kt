package dev.mod.store.minecraft.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Charcoal and lava. Surfaces are cold stone — flat, neutral, textured rather than gradient —
 * and every warm thing on screen (actions, badges, the podium) glows like molten rock.
 *
 * Components read these tokens directly; [AtlasTheme] also feeds them into the Material scheme.
 */
object Palette {

    // Primary — molten rock
    val Accent = Color(0xFFF04A21)
    val AccentSoft = Color(0xFFFF8A52)
    val AccentDeep = Color(0xFF8F2408)

    /** Text/icons drawn over artwork and dark scrims. */
    val OnAccent = Color(0xFFFFF3EC)

    /** Text/icons drawn on top of a filled [Accent] surface. */
    val OnAccentDark = Color(0xFF1A0500)

    // Secondary voices
    val Ember = Color(0xFFFF7A18)
    val Gold = Color(0xFFFFC04A)
    val Sky = Color(0xFF4FB6D6)
    val Magenta = Color(0xFFC05CE8)

    // Surfaces — cold stone, no colour cast
    val Canvas = Color(0xFF08090B)
    val Surface = Color(0xFF121317)
    val SurfaceHigh = Color(0xFF1B1D22)
    val Stroke = Color(0xFF2A2D34)

    // Text
    val TextPrimary = Color(0xFFECEDEF)
    val TextMuted = Color(0xFF8D9198)
    val TextFaint = Color(0xFF5C6068)

    // Status
    val Positive = Color(0xFF63C63F)
    val Negative = Color(0xFFFF4D5E)

    // Shimmer sweep
    val ShimmerBase = Color(0xFF14161A)
    val ShimmerHighlight = Color(0xFF22262C)

    // Category accents
    val CategoryLagoon = Color(0xFF4FB6D6)
    val CategoryLime = Color(0xFF7FC241)
    val CategoryRose = Color(0xFFC05CE8)
    val CategoryAmber = Color(0xFFFFB020)

    // Translucent layers stacked over artwork
    val Glass = Color(0x14FFFFFF)
    val GlassStroke = Color(0x1AFFFFFF)
    val Scrim = Color(0xCC05060A)

    /** The signature lava sweep used on primary actions. */
    val AccentGradient: Brush
        get() = Brush.linearGradient(listOf(Accent, Ember))

    /** Hotter variant for badges that must out-shout the artwork behind them. */
    val EmberGradient: Brush
        get() = Brush.linearGradient(listOf(Ember, Gold))
}
