package dev.mod.store.minecraft.feature.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.compendium.CompendiumPane
import dev.mod.store.minecraft.feature.showcase.ShowcasePane
import dev.mod.store.minecraft.feature.stash.StashPane

/** Renders the active tab child above a thin tab strip. */
@Composable
fun HubPane(
    component: HubComponent,
    modifier: Modifier = Modifier,
) {
    val stack by component.stack.subscribeAsState()
    val activeTab = stack.active.instance.tab

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = Palette.TextPrimary,
        bottomBar = {
            HubBar(
                activeTab = activeTab,
                onSelect = component::selectTab,
                onSearch = component::openSearch,
            )
        },
    ) { innerPadding ->
        Children(
            stack = component.stack,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) { created ->
            when (val child = created.instance) {
                is HubComponent.Child.Showcase -> ShowcasePane(child.component, Modifier.fillMaxSize())
                is HubComponent.Child.Stash -> StashPane(child.component, Modifier.fillMaxSize())
                is HubComponent.Child.Compendium -> CompendiumPane(child.component, Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * A thin strip: a hairline, four evenly spaced keys, small icons and small labels. Search is one
 * of the keys rather than a raised button, so nothing sticks out of the bar.
 */
@Composable
private fun HubBar(
    activeTab: HubTab,
    onSelect: (HubTab) -> Unit,
    onSearch: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Palette.Stroke),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.Canvas)
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HubKey(
                icon = HubTab.Showcase.icon,
                label = stringResource(HubTab.Showcase.labelRes),
                active = activeTab == HubTab.Showcase,
                onClick = { onSelect(HubTab.Showcase) },
            )
            HubKey(
                icon = Icons.Rounded.Search,
                label = stringResource(R.string.hub_search),
                active = false,
                onClick = onSearch,
            )
            HubKey(
                icon = HubTab.Stash.icon,
                label = stringResource(HubTab.Stash.labelRes),
                active = activeTab == HubTab.Stash,
                onClick = { onSelect(HubTab.Stash) },
            )
            HubKey(
                icon = HubTab.Compendium.icon,
                label = stringResource(HubTab.Compendium.labelRes),
                active = activeTab == HubTab.Compendium,
                onClick = { onSelect(HubTab.Compendium) },
            )
        }
    }
}

@Composable
private fun RowScope.HubKey(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (active) Palette.Accent else Palette.TextFaint,
        label = "key-tint",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .tappable(pressedScale = 0.94f, onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(21.dp),
        )
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
