package dev.mod.store.minecraft.data.database.identity

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import com.russhwolf.settings.Settings
import dev.mod.store.minecraft.domain.identity.ClientIdentity
import java.security.MessageDigest
import java.util.UUID

/**
 * Derives this device's anonymous identity from the platform's ANDROID_ID.
 *
 * ANDROID_ID is the right seed for this job because of when it changes. Since Android 8 it is
 * scoped to the app's signing key and survives uninstalling and reinstalling, so wiping the app
 * does not mint a fresh reader who can react to every mod again; it resets only on a factory reset.
 * It is hashed with the package name before leaving the device, so the backend never sees the raw
 * value and can't correlate it with any other app.
 *
 * On the rare device that has no usable ANDROID_ID — some emulators, and an old build bug that
 * handed every device the same constant — a random UUID is generated once and kept in local
 * storage instead. That one does reset on reinstall; nothing better is available there.
 */
internal class ClientIdentityImpl(
    private val context: Context,
    private val settings: Settings,
) : ClientIdentity {

    override val id: String by lazy {
        val seed = androidId() ?: fallbackSeed()
        sha256("$seed:${context.packageName}")
    }

    @SuppressLint("HardwareIds")
    private fun androidId(): String? =
        runCatching { Secure.getString(context.contentResolver, Secure.ANDROID_ID) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() && it != BROKEN_ANDROID_ID }

    private fun fallbackSeed(): String =
        settings.getStringOrNull(KEY_FALLBACK)
            ?: UUID.randomUUID().toString().also { settings.putString(KEY_FALLBACK, it) }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val KEY_FALLBACK = "identity.fallback"

        /** The value a buggy Android 2.2 build returned on every device; worthless as an id. */
        const val BROKEN_ANDROID_ID = "9774d56d682e549c"
    }
}
