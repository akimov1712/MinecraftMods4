package dev.mod.store.minecraft.core.ads.internal

import com.cleveradssolutions.sdk.AdContentInfo
import com.cleversolutions.ads.AdError
import io.github.aakira.napier.Napier

/** Thin logging facade for ad lifecycle events. */
internal object AdLog {

    private const val TAG = "billboard"

    fun loaded(type: String, ad: AdContentInfo) =
        Napier.d("$type loaded · src=${ad.sourceName}", tag = TAG)

    fun impression(type: String, ad: AdContentInfo) =
        Napier.d("$type impression · src=${ad.sourceName} · revenue=${ad.revenue}", tag = TAG)

    fun clicked(type: String, ad: AdContentInfo) =
        Napier.d("$type clicked · src=${ad.sourceName}", tag = TAG)

    fun dismissed(type: String) = Napier.d("$type dismissed", tag = TAG)

    fun failed(stage: String, error: AdError) =
        Napier.d("$stage failed · code=${error.code} · ${error.message}", tag = TAG)
}
