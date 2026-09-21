package dev.mod.store.minecraft.feature.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.mod.store.minecraft.core.ads.ScreenAds
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.mod.store.minecraft.domain.bookmark.ClearBookmarksUseCase
import dev.mod.store.minecraft.domain.bookmark.ObserveBookmarkCountUseCase
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
    private val observeBookmarkCount: ObserveBookmarkCountUseCase by inject()
    private val clearBookmarks: ClearBookmarksUseCase by inject()

    private val store = instanceKeeper.getStore {
        SettingsStoreFactory(storeFactory, observeBookmarkCount, clearBookmarks).create()
    }

    override val hasNativeAd: Boolean get() = screenAds.hasNativeAd

    override val state: StateFlow<SettingsStore.State> = store.stateFlow(lifecycle)
    override val labels: Flow<SettingsStore.Label> = store.labels

    override fun onIntent(intent: SettingsStore.Intent) {
        store.accept(intent)
    }

    override fun openWalkthrough() = onOpenWalkthrough()
    override fun openOutreach() = onOpenOutreach()
}
