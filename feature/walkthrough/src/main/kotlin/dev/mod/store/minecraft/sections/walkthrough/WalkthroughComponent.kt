package dev.mod.store.minecraft.feature.walkthrough

import com.arkivanov.decompose.ComponentContext
import dev.mod.store.minecraft.core.ads.ScreenAds
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A purely static screen — no store, just the back callback. Kept as a component so it lives in
 * the same Decompose tree as every other destination.
 */
interface WalkthroughComponent {
    fun back()

    /** True when a native ad is buffered and a slot on this screen can fill immediately. */
    val hasNativeAd: Boolean
}

class DefaultWalkthroughComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
) : WalkthroughComponent, ComponentContext by componentContext, KoinComponent {

    private val screenAds: ScreenAds by inject()

    override val hasNativeAd: Boolean get() = screenAds.hasNativeAd

    override fun back() = onBack()
}
