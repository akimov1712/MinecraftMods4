package dev.mod.store.minecraft.feature.hub

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.mod.store.minecraft.feature.compendium.CompendiumComponent
import dev.mod.store.minecraft.feature.compendium.DefaultCompendiumComponent
import dev.mod.store.minecraft.feature.settings.DefaultSettingsComponent
import dev.mod.store.minecraft.feature.settings.SettingsComponent
import dev.mod.store.minecraft.feature.showcase.DefaultShowcaseComponent
import dev.mod.store.minecraft.feature.showcase.ShowcaseComponent
import dev.mod.store.minecraft.feature.stash.DefaultStashComponent
import dev.mod.store.minecraft.feature.stash.StashComponent
import kotlinx.serialization.Serializable

/**
 * Bottom-bar container. Each tab is a child in a single stack; tapping a tab brings its child
 * to the front (preserving the others), so switching tabs keeps their state alive.
 */
interface HubComponent {

    val stack: Value<ChildStack<*, Child>>

    fun selectTab(tab: HubTab)

    /** Opens the search screen, which lives above the tabs rather than inside them. */
    fun openSearch()

    sealed interface Child {
        val tab: HubTab

        data class Showcase(val component: ShowcaseComponent) : Child {
            override val tab: HubTab get() = HubTab.Showcase
        }

        data class Stash(val component: StashComponent) : Child {
            override val tab: HubTab get() = HubTab.Stash
        }

        data class Compendium(val component: CompendiumComponent) : Child {
            override val tab: HubTab get() = HubTab.Compendium
        }

        data class Settings(val component: SettingsComponent) : Child {
            override val tab: HubTab get() = HubTab.Settings
        }
    }
}

class DefaultHubComponent(
    componentContext: ComponentContext,
    private val onOpenCreation: (Int) -> Unit,
    private val onOpenSearch: () -> Unit,
    private val onOpenWalkthrough: () -> Unit,
    private val onOpenOutreach: () -> Unit,
) : HubComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, HubComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Showcase,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(config: Config, context: ComponentContext): HubComponent.Child =
        when (config) {
            Config.Showcase -> HubComponent.Child.Showcase(
                DefaultShowcaseComponent(context, onOpenCreation = onOpenCreation),
            )

            Config.Stash -> HubComponent.Child.Stash(
                DefaultStashComponent(context, onOpenCreation = onOpenCreation),
            )

            Config.Compendium -> HubComponent.Child.Compendium(
                DefaultCompendiumComponent(context),
            )

            Config.Settings -> HubComponent.Child.Settings(
                DefaultSettingsComponent(
                    componentContext = context,
                    onOpenWalkthrough = onOpenWalkthrough,
                    onOpenOutreach = onOpenOutreach,
                ),
            )
        }

    override fun selectTab(tab: HubTab) {
        navigation.bringToFront(tab.toConfig())
    }

    override fun openSearch() {
        onOpenSearch()
    }

    @Serializable
    private sealed interface Config {
        val tab: HubTab

        @Serializable
        data object Showcase : Config {
            override val tab: HubTab get() = HubTab.Showcase
        }

        @Serializable
        data object Stash : Config {
            override val tab: HubTab get() = HubTab.Stash
        }

        @Serializable
        data object Compendium : Config {
            override val tab: HubTab get() = HubTab.Compendium
        }

        @Serializable
        data object Settings : Config {
            override val tab: HubTab get() = HubTab.Settings
        }
    }

    private fun HubTab.toConfig(): Config = when (this) {
        HubTab.Showcase -> Config.Showcase
        HubTab.Stash -> Config.Stash
        HubTab.Compendium -> Config.Compendium
        HubTab.Settings -> Config.Settings
    }
}
