package dev.mod.store.minecraft.feature.outreach

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.domain.outreach.SubmitRecommendationUseCase
import dev.mod.store.minecraft.domain.outreach.SubmitReportUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface OutreachComponent {
    val state: StateFlow<OutreachStore.State>
    val labels: Flow<OutreachStore.Label>
    fun onIntent(intent: OutreachStore.Intent)
}

class DefaultOutreachComponent(
    componentContext: ComponentContext,
) : OutreachComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()
    private val submitRecommendation: SubmitRecommendationUseCase by inject()
    private val submitReport: SubmitReportUseCase by inject()
    private val faults: FaultMessages by inject()

    private val store = instanceKeeper.getStore {
        OutreachStoreFactory(storeFactory, submitRecommendation, submitReport, faults).create()
    }

    override val state: StateFlow<OutreachStore.State> = store.stateFlow(lifecycle)
    override val labels: Flow<OutreachStore.Label> = store.labels

    override fun onIntent(intent: OutreachStore.Intent) {
        store.accept(intent)
    }
}
