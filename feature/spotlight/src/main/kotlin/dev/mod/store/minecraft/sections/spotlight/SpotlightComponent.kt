package dev.mod.store.minecraft.feature.spotlight

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.core.ads.ScreenAds
import dev.mod.store.minecraft.domain.bookmark.ToggleBookmarkUseCase
import dev.mod.store.minecraft.domain.creation.FetchCreationUseCase
import dev.mod.store.minecraft.domain.outreach.SubmitReportUseCase
import dev.mod.store.minecraft.domain.reaction.FetchReactionsUseCase
import dev.mod.store.minecraft.domain.reaction.SetReactionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SpotlightComponent {
    val state: StateFlow<SpotlightStore.State>
    val labels: Flow<SpotlightStore.Label>
    fun onIntent(intent: SpotlightStore.Intent)

    /** True when a native ad is buffered and a slot on this screen can fill immediately. */
    val hasNativeAd: Boolean
    fun back()
    fun openLoadout()
    fun openWalkthrough()
    fun openOutreach()

    /** Opens another mod: a similar one, picked from this page. */
    fun openCreation(creationId: Int)
}

class DefaultSpotlightComponent(
    componentContext: ComponentContext,
    private val creationId: Int,
    private val onBack: () -> Unit,
    private val onOpenLoadout: (Int) -> Unit,
    private val onOpenWalkthrough: () -> Unit,
    private val onOpenOutreach: () -> Unit,
    private val onOpenCreation: (Int) -> Unit,
) : SpotlightComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()
    private val fetchCreation: FetchCreationUseCase by inject()
    private val toggleBookmark: ToggleBookmarkUseCase by inject()
    private val submitReport: SubmitReportUseCase by inject()
    private val fetchReactions: FetchReactionsUseCase by inject()
    private val setReaction: SetReactionUseCase by inject()
    private val faults: FaultMessages by inject()
    private val screenAds: ScreenAds by inject()

    init {
        // Interstitial on screen enter — bound to the Decompose lifecycle, not a Compose effect.
        lifecycle.doOnCreate { screenAds.onSpotlightEntered() }
    }

    private val store = instanceKeeper.getStore {
        SpotlightStoreFactory(
            storeFactory = storeFactory,
            creationId = creationId,
            fetchCreation = fetchCreation,
            toggleBookmark = toggleBookmark,
            submitReport = submitReport,
            fetchReactions = fetchReactions,
            setReaction = setReaction,
            faults = faults,
        ).create()
    }

    override val hasNativeAd: Boolean get() = screenAds.hasNativeAd

    override val state: StateFlow<SpotlightStore.State> = store.stateFlow(lifecycle)
    override val labels: Flow<SpotlightStore.Label> = store.labels

    override fun onIntent(intent: SpotlightStore.Intent) {
        store.accept(intent)
    }

    override fun back() = onBack()
    override fun openLoadout() = onOpenLoadout(creationId)
    override fun openWalkthrough() = onOpenWalkthrough()
    override fun openOutreach() = onOpenOutreach()
    override fun openCreation(creationId: Int) = onOpenCreation(creationId)
}
