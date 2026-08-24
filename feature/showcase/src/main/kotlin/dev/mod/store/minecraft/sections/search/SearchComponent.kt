package dev.mod.store.minecraft.feature.search

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.mod.store.minecraft.core.ads.ScreenAds
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.domain.creation.FetchHomeDigestUseCase
import dev.mod.store.minecraft.domain.creation.FetchShowcaseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SearchComponent {
    val state: StateFlow<SearchStore.State>
    val labels: Flow<SearchStore.Label>
    val nativeAdInterval: Int
    fun onIntent(intent: SearchStore.Intent)
    fun openCreation(creationId: Int)
    fun back()
}

class DefaultSearchComponent(
    componentContext: ComponentContext,
    private val onOpenCreation: (Int) -> Unit,
    private val onBack: () -> Unit,
) : SearchComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()
    private val fetchShowcase: FetchShowcaseUseCase by inject()
    private val fetchHomeDigest: FetchHomeDigestUseCase by inject()
    private val faults: FaultMessages by inject()
    private val screenAds: ScreenAds by inject()

    private val store = instanceKeeper.getStore {
        SearchStoreFactory(storeFactory, fetchShowcase, fetchHomeDigest, faults).create()
    }

    override val state: StateFlow<SearchStore.State> = store.stateFlow(lifecycle)
    override val labels: Flow<SearchStore.Label> = store.labels
    override val nativeAdInterval: Int get() = screenAds.nativeInterval

    override fun onIntent(intent: SearchStore.Intent) {
        store.accept(intent)
    }

    override fun openCreation(creationId: Int) {
        onOpenCreation(creationId)
    }

    override fun back() {
        onBack()
    }
}
