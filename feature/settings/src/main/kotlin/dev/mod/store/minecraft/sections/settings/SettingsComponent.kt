package dev.mod.store.minecraft.feature.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.mod.store.minecraft.core.ads.ScreenAds
import dev.mod.store.minecraft.domain.config.AdPlacement
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.essenty.lifecycle.doOnResume
import dev.mod.store.minecraft.domain.loadout.ClearDownloadedFilesUseCase
import dev.mod.store.minecraft.domain.loadout.CountDownloadedFilesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SettingsComponent {
    val state: StateFlow<SettingsStore.State>
    val labels: Flow<SettingsStore.Label>
    fun onIntent(intent: SettingsStore.Intent)

    /** True when a native ad is buffered and a slot on this screen can fill immediately. */
    val hasNativeAd: Boolean
    fun openWalkthrough()
    fun openOutreach()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val onOpenWalkthrough: () -> Unit,
    private val onOpenOutreach: () -> Unit,
) : SettingsComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()
    private val screenAds: ScreenAds by inject()
    private val countDownloadedFiles: CountDownloadedFilesUseCase by inject()
    private val clearDownloadedFiles: ClearDownloadedFilesUseCase by inject()

    private val store = instanceKeeper.getStore {
        SettingsStoreFactory(
            storeFactory = storeFactory,
            countDownloadedFiles = countDownloadedFiles,
            clearDownloadedFiles = clearDownloadedFiles,
        ).create()
    }

    init {
        // Files arrive while this screen sits in the background, so the figure is re-read every
        // time the tab comes back rather than once when the component is built.
        lifecycle.doOnResume { store.accept(SettingsStore.Intent.Refresh) }
    }

    override val hasNativeAd: Boolean
        get() = screenAds.hasNativeAd(AdPlacement.SETTINGS_NATIVE)

    override val state: StateFlow<SettingsStore.State> = store.stateFlow(lifecycle)
    override val labels: Flow<SettingsStore.Label> = store.labels

    override fun onIntent(intent: SettingsStore.Intent) {
        store.accept(intent)
    }

    override fun openWalkthrough() = onOpenWalkthrough()
    override fun openOutreach() = onOpenOutreach()
}
