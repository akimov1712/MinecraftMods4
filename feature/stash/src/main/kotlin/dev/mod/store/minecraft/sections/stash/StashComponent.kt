package dev.mod.store.minecraft.feature.stash

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.mod.store.minecraft.core.ads.ScreenAds
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.domain.bookmark.FetchBookmarkedCreationsUseCase
import dev.mod.store.minecraft.domain.bookmark.ObserveBookmarkCountUseCase
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface StashComponent {
    val state: StateFlow<StashStore.State>

    /** How many saved mods to place between two inline native ads (0 = none). */
    val nativeAdInterval: Int

    fun onIntent(intent: StashStore.Intent)
    fun openCreation(creationId: Int)
}

class DefaultStashComponent(
    componentContext: ComponentContext,
    private val onOpenCreation: (Int) -> Unit,
) : StashComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()
    private val screenAds: ScreenAds by inject()
    private val observeCount: ObserveBookmarkCountUseCase by inject()
    private val fetchBookmarked: FetchBookmarkedCreationsUseCase by inject()
    private val faults: FaultMessages by inject()

    private val store = instanceKeeper.getStore {
        StashStoreFactory(storeFactory, observeCount, fetchBookmarked, faults).create()
    }

    override val nativeAdInterval: Int get() = screenAds.nativeInterval

    override val state: StateFlow<StashStore.State> = store.stateFlow(lifecycle)

    override fun onIntent(intent: StashStore.Intent) {
        store.accept(intent)
    }

    override fun openCreation(creationId: Int) {
        onOpenCreation(creationId)
    }
}
