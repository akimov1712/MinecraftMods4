package dev.mod.store.minecraft.feature.ignition

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.mod.store.minecraft.core.ads.ScreenAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface IgnitionComponent {
    val state: StateFlow<IgnitionStore.State>
    fun onIntent(intent: IgnitionStore.Intent)
}

class DefaultIgnitionComponent(
    componentContext: ComponentContext,
    /** [showPromo] is true when another native ad is loaded and should be shown on the way out. */
    private val onProceed: (showPromo: Boolean) -> Unit,
) : IgnitionComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()
    private val screenAds: ScreenAds by inject()

    private val store = instanceKeeper.getStore {
        IgnitionStoreFactory(storeFactory, screenAds).create()
    }
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override val state: StateFlow<IgnitionStore.State> = store.stateFlow(lifecycle)

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        scope.launch {
            store.labels.collect { label ->
                if (label is IgnitionStore.Label.Proceed) onProceed(label.showPromo)
            }
        }
    }

    override fun onIntent(intent: IgnitionStore.Intent) {
        store.accept(intent)
    }
}
