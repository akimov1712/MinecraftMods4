package dev.mod.store.minecraft.core.ads.internal

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
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
import com.cleveradssolutions.sdk.nativead.CASChoicesView
import com.cleveradssolutions.sdk.nativead.CASMediaView
import com.cleveradssolutions.sdk.nativead.CASNativeView
import com.cleveradssolutions.sdk.nativead.NativeAdContent
import dev.mod.store.minecraft.core.ui.R as AtlasR

private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

/**
 * Ads get their own palette, deliberately opposed to the app's.
 *
 * The catalog is blue-ink with a hot red action colour and soft corners everywhere. An ad is also
 * dark — glare has no place here — but on plain neutral grey, marked with yellow and acting in
 * blue, two colours the app never uses. It is square, and it puts the artwork
 * underneath the words instead of above them. Not one of shade, shape or ordering is shared with a
 * mod card, so the two cannot be confused.
 */
private object AdSkin {

    /**
     * The card: a plain dark grey with no tint either way. The app's own surfaces carry a blue
     * cast, so a neutral grey beside them reads as a different material rather than as the same
     * one at a different brightness.
     */
    val Surface = Color.parseColor("#FF1E1E20")

    /** The well behind the artwork, so a slow image is a shadow rather than a hole. */
    val Well = Color.parseColor("#FF2A2A2D")

    val Ink = Color.parseColor("#FFF2F2F3")
    val Muted = Color.parseColor("#FF9A9A9E")
    val Hairline = Color.parseColor("#FF3A3A3E")

    /** The marker plate. Yellow, because a label nobody notices is not a label. */
    val Marker = Color.parseColor("#FFFFC53D")

    /** Text on the yellow plate. */
    val OnMarker = Color.parseColor("#FF17150F")

    /** The button. Blue — not the app's red, and not a grey that disappears into the card. */
    val Action = Color.parseColor("#FF2F6BFF")

    /** Text on the blue button. */
    val OnAction = Color.parseColor("#FFFFFFFF")
}

/**
 * The view tree plus the typed handles the SDK binds an ad into.
 *
 * [adLabel] is registered as `adLabelView`: it is a required asset, and while it was missing the
 * network refused to fill the slot with "Missing required Ad label asset".
 */
internal class NativeAdViews(
    val root: CASNativeView,
    private val title: TextView,
    private val body: TextView,
    private val advertiser: TextView,
    private val cta: Button,
    private val media: CASMediaView,
    private val icon: ImageView,
    private val adChoices: CASChoicesView,
    private val adLabel: TextView,
) {
    var bound: Boolean = false

    fun wire() {
        root.headlineView = title
        root.bodyView = body
        root.advertiserView = advertiser
        root.callToActionView = cta
        root.mediaView = media
        root.iconView = icon
        root.adChoicesView = adChoices
        root.adLabelView = adLabel
    }

    fun bind(ad: NativeAdContent) {
        root.bindAdContent(ad)
    }
}

/**
 * Builds the ad view tree (CAS needs real Views, not Compose). The two forms are written out
 * separately rather than one being a scaled copy of the other.
 */
internal fun buildNativeAdViews(context: Context, fullscreen: Boolean): NativeAdViews =
    if (fullscreen) buildFillingAd(context) else buildInlineAd(context)

// region inline

/**
 * The form used between list items. Words first, button second, picture last — the reverse of every
 * mod card in the app, which leads with its artwork.
 */
private fun buildInlineAd(context: Context): NativeAdViews {
    val root = CASNativeView(context).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH, WRAP)
        background = squareStroked(AdSkin.Surface, AdSkin.Hairline, 1.dp())
    }

    val column = LinearLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH, WRAP)
        orientation = LinearLayout.VERTICAL
        setPadding(14.dp(), 12.dp(), 14.dp(), 0)
    }

    val adLabel = marker(context, sizeSp = 10f)
    val adChoices = CASChoicesView(context).apply {
        layoutParams = LinearLayout.LayoutParams(22.dp(), 22.dp())
    }
    val strip = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        addView(adLabel)
        addView(filler(context))
        addView(adChoices)
    }

    val icon = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(38.dp(), 38.dp())
        setBackgroundColor(AdSkin.Well)
    }
    val advertiser = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        maxLines = 1
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTextColor(AdSkin.Muted)
    }
    val title = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        maxLines = 2
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setTextColor(AdSkin.Ink)
        setTypeface(typeface, Typeface.BOLD)
    }
    val titles = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, WRAP).apply {
            weight = 1f
            marginStart = 10.dp()
        }
        addView(advertiser); addView(title)
    }
    val identity = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = 10.dp() }
        addView(icon); addView(titles)
    }

    val body = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = 8.dp() }
        maxLines = 2
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setTextColor(AdSkin.Muted)
    }

    // Sized to its own text and pinned left — nothing like the app's full-width pills.
    val cta = compactCta(context, heightDp = 38, sizeSp = 13f).apply {
        (layoutParams as LinearLayout.LayoutParams).topMargin = 12.dp()
    }

    val media = CASMediaView(context).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH, MATCH)
    }
    val mediaWrap = FrameLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, 132.dp()).apply { topMargin = 12.dp() }
        setBackgroundColor(AdSkin.Well)
        addView(media)
    }

    column.addView(strip)
    column.addView(identity)
    column.addView(body)
    column.addView(cta)
    column.addView(mediaWrap)
    root.addView(column)

    return NativeAdViews(root, title, body, advertiser, cta, media, icon, adChoices, adLabel)
        .also { it.wire() }
}

// endregion

// region filling

/**
 * The form for a bounded box — the splash promo and the full-screen curtain. Same order as the
 * inline card, but the artwork is weighted, so it takes every pixel the copy does not need and is
 * the one thing guaranteed to be big. Nothing here can outgrow its container, so nothing is ever
 * clipped off the bottom.
 */
private fun buildFillingAd(context: Context): NativeAdViews {
    val root = CASNativeView(context).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH, MATCH)
        setBackgroundColor(AdSkin.Surface)
    }

    val column = LinearLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH, MATCH)
        orientation = LinearLayout.VERTICAL
    }

    val adLabel = marker(context, sizeSp = 11f)
    val adChoices = CASChoicesView(context).apply {
        layoutParams = LinearLayout.LayoutParams(24.dp(), 24.dp())
    }
    val strip = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        // Room on the right for the close key the curtain draws over this view.
        setPadding(16.dp(), 12.dp(), 60.dp(), 12.dp())
        addView(adLabel)
        addView(filler(context))
        addView(adChoices)
    }

    val icon = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(42.dp(), 42.dp())
        setBackgroundColor(AdSkin.Well)
    }
    val advertiser = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        maxLines = 1
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTextColor(AdSkin.Muted)
    }
    val title = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        maxLines = 2
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setTextColor(AdSkin.Ink)
        setTypeface(typeface, Typeface.BOLD)
    }
    val titles = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, WRAP).apply {
            weight = 1f
            marginStart = 11.dp()
        }
        addView(advertiser); addView(title)
    }
    val identity = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        addView(icon); addView(titles)
    }

    val body = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP).apply { topMargin = 8.dp() }
        maxLines = 2
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setTextColor(AdSkin.Muted)
    }
    val cta = compactCta(context, heightDp = 44, sizeSp = 14f).apply {
        (layoutParams as LinearLayout.LayoutParams).topMargin = 12.dp()
    }

    val copy = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP)
        setPadding(16.dp(), 0, 16.dp(), 14.dp())
        addView(identity); addView(body); addView(cta)
    }

    val media = CASMediaView(context).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH, MATCH)
    }
    val mediaWrap = FrameLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(MATCH, 0).apply { weight = 1f }
        minimumHeight = 120.dp()
        setBackgroundColor(AdSkin.Well)
        addView(media)
    }

    column.addView(strip)
    column.addView(copy)
    column.addView(mediaWrap)
    root.addView(column)

    return NativeAdViews(root, title, body, advertiser, cta, media, icon, adChoices, adLabel)
        .also { it.wire() }
}

// endregion

/** The required marker: yellow, square, spaced capitals — unlike any chip the app draws. */
private fun marker(context: Context, sizeSp: Float) = TextView(context).apply {
    text = context.getString(AtlasR.string.atlas_ad).uppercase()
    setTextColor(AdSkin.OnMarker)
    setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
    setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
    letterSpacing = 0.2f
    setPadding(8.dp(), 3.dp(), 8.dp(), 3.dp())
    setBackgroundColor(AdSkin.Marker)
    layoutParams = LinearLayout.LayoutParams(WRAP, WRAP)
}

/**
 * The call to action: blue, square, and as wide as its words and no wider. The app's own buttons
 * are full-width red pills, so this reads as belonging to something else entirely.
 */
private fun compactCta(context: Context, heightDp: Int, sizeSp: Float) = Button(context).apply {
    layoutParams = LinearLayout.LayoutParams(WRAP, heightDp.dp()).apply {
        gravity = Gravity.START
    }
    minWidth = 0
    minimumWidth = 0
    isAllCaps = false
    letterSpacing = 0.03f
    setPadding(22.dp(), 0, 22.dp(), 0)
    setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
    setTextColor(AdSkin.OnAction)
    setTypeface(typeface, Typeface.BOLD)
    background = squareFill(AdSkin.Action)
    backgroundTintList = ColorStateList.valueOf(AdSkin.Action)
    stateListAnimator = null
}

private fun filler(context: Context) = View(context).apply {
    layoutParams = LinearLayout.LayoutParams(0, 0).apply { weight = 1f }
}

private fun squareFill(color: Int) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    setColor(color)
}

private fun squareStroked(fill: Int, stroke: Int, width: Int) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    setColor(fill)
    setStroke(width, stroke)
}

private fun Int.dp(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
