package dev.mod.store.minecraft.feature.showcase

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.core.ads.ScreenAds
import dev.mod.store.minecraft.domain.bookmark.FetchBookmarkedIdsUseCase
import dev.mod.store.minecraft.domain.bookmark.ObserveBookmarkCountUseCase
import dev.mod.store.minecraft.domain.creation.FetchHomeDigestUseCase
import dev.mod.store.minecraft.domain.creation.FetchShowcaseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ShowcaseComponent {
    val state: StateFlow<ShowcaseStore.State>
    val labels: Flow<ShowcaseStore.Label>
    val nativeAdInterval: Int
    fun onIntent(intent: ShowcaseStore.Intent)
    fun openCreation(creationId: Int)
}

/**
 * Pulls its store factory and use cases from Koin (so the navigation tree doesn't have to thread
 * dependencies down). Navigation out is the one thing it cannot know about — that arrives as the
 * [onOpenCreation] callback supplied by the parent.
 */
class DefaultShowcaseComponent(
    componentContext: ComponentContext,
    private val onOpenCreation: (Int) -> Unit,
) : ShowcaseComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()
    private val fetchHomeDigest: FetchHomeDigestUseCase by inject()
    private val fetchShowcase: FetchShowcaseUseCase by inject()
    private val observeBookmarkCount: ObserveBookmarkCountUseCase by inject()
    private val fetchBookmarkedIds: FetchBookmarkedIdsUseCase by inject()
    private val faults: FaultMessages by inject()
    private val screenAds: ScreenAds by inject()

    private val store = instanceKeeper.getStore {
        ShowcaseStoreFactory(
            storeFactory = storeFactory,
            fetchHomeDigest = fetchHomeDigest,
            fetchShowcase = fetchShowcase,
            observeBookmarkCount = observeBookmarkCount,
            fetchBookmarkedIds = fetchBookmarkedIds,
            faults = faults,
        ).create()
    }

    override val state: StateFlow<ShowcaseStore.State> = store.stateFlow(lifecycle)
    override val labels: Flow<ShowcaseStore.Label> = store.labels
    override val nativeAdInterval: Int get() = screenAds.nativeInterval

    override fun onIntent(intent: ShowcaseStore.Intent) {
        store.accept(intent)
    }

    override fun openCreation(creationId: Int) {
        onOpenCreation(creationId)
    }
}
