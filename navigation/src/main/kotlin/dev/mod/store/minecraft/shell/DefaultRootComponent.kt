package dev.mod.store.minecraft.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import dev.mod.store.minecraft.feature.hub.DefaultHubComponent
import dev.mod.store.minecraft.feature.ignition.DefaultIgnitionComponent
import dev.mod.store.minecraft.feature.loadout.DefaultLoadoutComponent
import dev.mod.store.minecraft.feature.outreach.DefaultOutreachComponent
import dev.mod.store.minecraft.feature.search.DefaultSearchComponent
import dev.mod.store.minecraft.feature.spotlight.DefaultSpotlightComponent
import dev.mod.store.minecraft.feature.walkthrough.DefaultWalkthroughComponent
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Ignition,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(config: Config, context: ComponentContext): RootComponent.Child =
        when (config) {
            Config.Ignition -> RootComponent.Child.Ignition(
                DefaultIgnitionComponent(
                    componentContext = context,
                    onProceed = { navigation.replaceAll(Config.Hub) },
                ),
            )

            Config.Hub -> RootComponent.Child.Hub(
                DefaultHubComponent(
                    componentContext = context,
                    onOpenCreation = { creationId -> navigation.pushNew(Config.Spotlight(creationId)) },
                    onOpenSearch = { navigation.pushNew(Config.Search) },
                ),
            )

            Config.Search -> RootComponent.Child.Search(
                DefaultSearchComponent(
                    componentContext = context,
                    onOpenCreation = { creationId -> navigation.pushNew(Config.Spotlight(creationId)) },
                    onBack = { navigation.pop() },
                ),
            )

            is Config.Spotlight -> RootComponent.Child.Spotlight(
                DefaultSpotlightComponent(
                    componentContext = context,
                    creationId = config.creationId,
                    onBack = { navigation.pop() },
                    onOpenLoadout = { creationId -> navigation.pushNew(Config.Loadout(creationId)) },
                    onOpenWalkthrough = { navigation.pushNew(Config.Walkthrough) },
                    onOpenOutreach = { navigation.pushNew(Config.Outreach) },
                ),
            )

            is Config.Loadout -> RootComponent.Child.Loadout(
                DefaultLoadoutComponent(
                    componentContext = context,
                    creationId = config.creationId,
                    onBack = { navigation.pop() },
                ),
            )

            Config.Outreach -> RootComponent.Child.Outreach(
                DefaultOutreachComponent(
                    componentContext = context,
                    onBack = { navigation.pop() },
                ),
            )

            Config.Walkthrough -> RootComponent.Child.Walkthrough(
                DefaultWalkthroughComponent(
                    componentContext = context,
                    onBack = { navigation.pop() },
                ),
            )
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Ignition : Config

        @Serializable
        data object Hub : Config

        @Serializable
        data object Search : Config

        @Serializable
        data class Spotlight(val creationId: Int) : Config

        @Serializable
        data class Loadout(val creationId: Int) : Config

        @Serializable
        data object Outreach : Config

        @Serializable
        data object Walkthrough : Config
    }
}
