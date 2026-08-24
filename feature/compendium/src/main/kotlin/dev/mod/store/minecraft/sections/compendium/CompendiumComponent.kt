package dev.mod.store.minecraft.feature.compendium

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface CompendiumComponent {
    val state: StateFlow<CompendiumStore.State>
    fun onIntent(intent: CompendiumStore.Intent)
}

class DefaultCompendiumComponent(
    componentContext: ComponentContext,
) : CompendiumComponent, ComponentContext by componentContext, KoinComponent {

    private val storeFactory: StoreFactory by inject()

    private val store = instanceKeeper.getStore {
        CompendiumStoreFactory(storeFactory).create()
    }

    override val state: StateFlow<CompendiumStore.State> = store.stateFlow(lifecycle)

    override fun onIntent(intent: CompendiumStore.Intent) {
        store.accept(intent)
    }
}
