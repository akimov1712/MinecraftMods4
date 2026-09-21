package dev.mod.store.minecraft.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.mod.store.minecraft.core.ui.effect.StoneBackdrop
import dev.mod.store.minecraft.core.ui.theme.AtlasTheme
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.hub.HubPane
import dev.mod.store.minecraft.feature.ignition.CurtainPane
import dev.mod.store.minecraft.feature.ignition.IgnitionPane
import dev.mod.store.minecraft.feature.loadout.LoadoutPane
import dev.mod.store.minecraft.feature.outreach.OutreachPane
import dev.mod.store.minecraft.feature.search.SearchPane
import dev.mod.store.minecraft.feature.spotlight.SpotlightPane
import dev.mod.store.minecraft.feature.walkthrough.WalkthroughPane

/**
 * Renders the root navigation stack inside the :atlas theme, over the shared stone backdrop.
 */
@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    AtlasTheme {
        Surface(modifier = modifier.fillMaxSize(), color = Palette.Canvas) {
            // One ambience for the whole app; screens paint their own opaque surfaces on top.
            StoneBackdrop()

            Children(
                stack = component.childStack,
                animation = stackAnimation(fade() + scale()),
            ) { created ->
                when (val child = created.instance) {
                    is RootComponent.Child.Ignition -> IgnitionPane(child.component, Modifier.fillMaxSize())
                    is RootComponent.Child.Curtain -> CurtainPane(child.component, Modifier.fillMaxSize())
                    is RootComponent.Child.Hub -> HubPane(child.component, Modifier.fillMaxSize())
                    is RootComponent.Child.Search -> SearchPane(child.component, Modifier.fillMaxSize())
                    is RootComponent.Child.Spotlight -> SpotlightPane(child.component, Modifier.fillMaxSize())
                    is RootComponent.Child.Loadout -> LoadoutPane(child.component, Modifier.fillMaxSize())
                    is RootComponent.Child.Outreach -> OutreachPane(child.component, Modifier.fillMaxSize())
                    is RootComponent.Child.Walkthrough -> WalkthroughPane(child.component, Modifier.fillMaxSize())
                }
            }
        }
    }
}
