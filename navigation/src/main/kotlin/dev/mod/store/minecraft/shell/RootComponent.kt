package dev.mod.store.minecraft.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.mod.store.minecraft.feature.hub.HubComponent
import dev.mod.store.minecraft.feature.ignition.IgnitionComponent
import dev.mod.store.minecraft.feature.loadout.LoadoutComponent
import dev.mod.store.minecraft.feature.outreach.OutreachComponent
import dev.mod.store.minecraft.feature.search.SearchComponent
import dev.mod.store.minecraft.feature.spotlight.SpotlightComponent
import dev.mod.store.minecraft.feature.walkthrough.WalkthroughComponent

/**
 * Top of the component tree. Drives the root stack: Ignition (splash) → Hub (tabs), with
 * Search, Spotlight, Loadout and Walkthrough pushed on top.
 */
interface RootComponent {

    val childStack: Value<ChildStack<*, Child>>

    sealed interface Child {
        data class Ignition(val component: IgnitionComponent) : Child
        data class Hub(val component: HubComponent) : Child
        data class Search(val component: SearchComponent) : Child
        data class Outreach(val component: OutreachComponent) : Child
        data class Spotlight(val component: SpotlightComponent) : Child
        data class Loadout(val component: LoadoutComponent) : Child
        data class Walkthrough(val component: WalkthroughComponent) : Child
    }
}
