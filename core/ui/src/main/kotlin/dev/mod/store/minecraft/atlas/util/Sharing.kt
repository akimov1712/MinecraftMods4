package dev.mod.store.minecraft.core.ui.util

import android.content.Context
import android.content.Intent

/** Where the app lives on the store — the only thing a share has to carry. */
fun storeLink(context: Context): String =
    "https://play.google.com/store/apps/details?id=${context.packageName}"

/**
 * Hands [text] to whatever the reader picks from the system sheet. Failures are swallowed on
 * purpose: a device with nothing that accepts plain text should not crash the screen behind it.
 */
fun sharePlainText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching {
        context.startActivity(
            Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
