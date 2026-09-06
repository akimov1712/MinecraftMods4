package dev.mod.store.minecraft.core.ui.util

import android.text.format.DateUtils
import java.util.Locale

/** `13173` → `13 173` in whatever grouping the current locale uses. */
fun formatCount(value: Int): String = String.format(Locale.getDefault(), "%,d", value)

/** `13173` → `13.2K`, `1_400_000` → `1.4M`. Used where a full number would not fit. */
fun formatCompact(value: Int): String = when {
    value < 1_000 -> value.toString()
    value < 1_000_000 -> trimZero(value / 1_000.0) + "K"
    else -> trimZero(value / 1_000_000.0) + "M"
}

/** `3.88` → `3.9`; ratings are always shown with a single decimal. */
fun formatRating(value: Double): String = String.format(Locale.getDefault(), "%.1f", value)

/** How old something has to be before a plain date reads better than "N months ago". */
private const val RELATIVE_LIMIT_MS = 60L * DateUtils.DAY_IN_MILLIS

/** Localized "2 days ago" for recent timestamps, a short date for anything older. */
fun formatRelativeTime(epochMillis: Long): String {
    val age = System.currentTimeMillis() - epochMillis
    if (age > RELATIVE_LIMIT_MS) return formatShortDate(epochMillis)
    return DateUtils.getRelativeTimeSpanString(
        epochMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}

/** Localized short date ("18 Aug") for badges that show *when* rather than *how long ago*. */
fun formatShortDate(epochMillis: Long): String =
    DateUtils.formatDateTime(
        null,
        epochMillis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR,
    )

private fun trimZero(value: Double): String =
    String.format(Locale.getDefault(), "%.1f", value).removeSuffix(decimalZeroSuffix())

private fun decimalZeroSuffix(): String =
    String.format(Locale.getDefault(), "%.1f", 1.0).drop(1)
