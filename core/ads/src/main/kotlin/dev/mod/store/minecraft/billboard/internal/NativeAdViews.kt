package dev.mod.store.minecraft.core.ads.internal

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import com.cleveradssolutions.sdk.nativead.CASChoicesView
import com.cleveradssolutions.sdk.nativead.CASMediaView
import com.cleveradssolutions.sdk.nativead.CASNativeView
import com.cleveradssolutions.sdk.nativead.NativeAdContent
import dev.mod.store.minecraft.core.ui.R as AtlasR
import dev.mod.store.minecraft.core.ui.theme.Palette

private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

/** The CAS native view tree plus the typed handles the SDK needs bound to render an ad. */
internal class NativeAdViews(
    val root: CASNativeView,
    private val title: TextView,
    private val body: TextView,
    private val advertiser: TextView,
    private val cta: Button,
    private val media: CASMediaView,
    private val icon: ImageView,
    private val adChoices: CASChoicesView,
) {
    var bound: Boolean = false

    /** Tells the SDK which view plays which role, once. */
    fun wire() {
        root.headlineView = title
        root.bodyView = body
        root.advertiserView = advertiser
        root.callToActionView = cta
        root.mediaView = media
        root.iconView = icon
        root.adChoicesView = adChoices
    }

    fun bind(ad: NativeAdContent) {
        root.bindAdContent(ad)
    }
}

/**
 * Builds a native ad card programmatically (CAS needs real Views, not Compose). [fullscreen]
 * scales the layout up for the full-screen slot.
 */
internal fun buildNativeAdViews(context: Context, fullscreen: Boolean): NativeAdViews {
    val pad = if (fullscreen) 16 else 12

    val root = CASNativeView(context).apply {
        layoutParams = if (fullscreen) {
            ViewGroup.LayoutParams(MATCH, MATCH)
        } else {
            ViewGroup.LayoutParams(MATCH, WRAP)
        }
        background = stroked(Palette.Surface.toArgb(), Palette.Stroke.toArgb(), 1.dp(), 14.dpf())
        setPadding(pad.dp(), pad.dp(), pad.dp(), pad.dp())
        if (!fullscreen) minimumHeight = 260.dp()
    }

    val container = LinearLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, if (fullscreen) MATCH else WRAP)
        orientation = LinearLayout.VERTICAL
    }

    val adLabel = TextView(context).apply {
        text = context.getString(AtlasR.string.atlas_ad)
        setTextColor(Palette.Canvas.toArgb())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTypeface(typeface, Typeface.BOLD)
        setPadding(8.dp(), 2.dp(), 8.dp(), 2.dp())
        background = rounded(Palette.CategoryAmber.toArgb(), 4.dpf())
        layoutParams = LinearLayout.LayoutParams(WRAP, WRAP)
    }
    val spacer = View(context).apply {
        layoutParams = LinearLayout.LayoutParams(0, 0).apply { weight = 1f }
    }
    val adChoices = CASChoicesView(context).apply {
        layoutParams = LinearLayout.LayoutParams(28.dp(), 28.dp())
    }
    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        addView(adLabel); addView(spacer); addView(adChoices)
    }

    val media = CASMediaView(context).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH, if (fullscreen) MATCH else WRAP)
        minimumHeight = (if (fullscreen) 220 else 140).dp()
    }
    val icon = ImageView(context).apply {
        layoutParams = FrameLayout.LayoutParams(44.dp(), 44.dp()).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            setMargins(8.dp(), 8.dp(), 8.dp(), 8.dp())
        }
        background = rounded(Palette.Canvas.toArgb(), 6.dpf())
        clipToOutline = true
    }
    val mediaWrap = FrameLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, if (fullscreen) 0 else WRAP).apply {
            if (fullscreen) weight = 1f
            topMargin = 12.dp()
        }
        background = rounded(Palette.Canvas.toArgb(), 8.dpf())
        clipToOutline = true
        addView(media); addView(icon)
    }

    val advertiser = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(WRAP, WRAP).apply { topMargin = 12.dp() }
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(Palette.AccentSoft.toArgb())
    }
    val title = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = 4.dp() }
        maxLines = 2
        setTextSize(TypedValue.COMPLEX_UNIT_SP, if (fullscreen) 22f else 17f)
        setTextColor(Palette.TextPrimary.toArgb())
    }
    val body = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = 6.dp() }
        maxLines = if (fullscreen) 4 else 3
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setTextColor(Palette.TextMuted.toArgb())
    }
    val cta = Button(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, (if (fullscreen) 52 else 44).dp()).apply {
            topMargin = 12.dp()
        }
        isAllCaps = false
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setTextColor(Palette.OnAccent.toArgb())
        background = rounded(Palette.Accent.toArgb(), 10.dpf())
        backgroundTintList = ColorStateList.valueOf(Palette.Accent.toArgb())
    }

    container.addView(header)
    container.addView(mediaWrap)
    container.addView(advertiser)
    container.addView(title)
    container.addView(body)
    container.addView(cta)
    root.addView(container)

    return NativeAdViews(root, title, body, advertiser, cta, media, icon, adChoices).also { it.wire() }
}

private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    cornerRadius = radius
    setColor(color)
}

private fun stroked(fill: Int, stroke: Int, width: Int, radius: Float) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    cornerRadius = radius
    setColor(fill)
    setStroke(width, stroke)
}

private fun Int.dp(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
private fun Int.dpf(): Float = this * Resources.getSystem().displayMetrics.density
