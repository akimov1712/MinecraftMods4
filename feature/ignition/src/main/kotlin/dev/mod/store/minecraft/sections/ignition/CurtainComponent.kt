package dev.mod.store.minecraft.feature.ignition

import com.arkivanov.decompose.ComponentContext
import dev.mod.store.minecraft.core.ads.ScreenAds
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The one full-screen promo between the splash and the catalog. It exists only when an ad is
 * actually loaded and waiting; [hasNativeAd] lets the screen close itself rather than show an
 * empty page if the pool emptied between the tap and the render.
 */
interface CurtainComponent {
    val hasNativeAd: Boolean
    fun close()
}

class DefaultCurtainComponent(
    componentContext: ComponentContext,
    private val onClose: () -> Unit,
) : CurtainComponent, ComponentContext by componentContext, KoinComponent {

    private val screenAds: ScreenAds by inject()

    override val hasNativeAd: Boolean get() = screenAds.hasNativeAd

    override fun close() = onClose()
}
