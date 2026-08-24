package dev.mod.store.minecraft.feature.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.mod.store.minecraft.core.ui.effect.halo
import dev.mod.store.minecraft.core.ui.effect.pulse
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.compendium.CompendiumPane
import dev.mod.store.minecraft.feature.outreach.OutreachPane
import dev.mod.store.minecraft.feature.showcase.ShowcasePane
import dev.mod.store.minecraft.feature.stash.StashPane

/** Renders the active tab child above a floating glass tab bar. */
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
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
        ) { created ->
            when (val child = created.instance) {
                is HubComponent.Child.Showcase -> ShowcasePane(child.component, Modifier.fillMaxSize())
                is HubComponent.Child.Stash -> StashPane(child.component, Modifier.fillMaxSize())
                is HubComponent.Child.Outreach -> OutreachPane(child.component, Modifier.fillMaxSize())
                is HubComponent.Child.Compendium -> CompendiumPane(child.component, Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * Four tabs with the search key sunk into the middle of the bar — search is a place you go to,
 * not a field that eats the top of the home screen.
 */
@Composable
private fun HubBar(
    activeTab: HubTab,
    onSelect: (HubTab) -> Unit,
    onSearch: () -> Unit,
) {
    val tabs = HubTab.entries
    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Palette.Surface)
            .border(1.dp, Palette.GlassStroke, RoundedCornerShape(28.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.take(2).forEach { tab ->
            HubBarItem(tab = tab, active = tab == activeTab, onClick = { onSelect(tab) })
        }
        SearchKey(onClick = onSearch)
        tabs.drop(2).forEach { tab ->
            HubBarItem(tab = tab, active = tab == activeTab, onClick = { onSelect(tab) })
        }
    }
}

/** The molten round button in the centre of the bar. */
@Composable
private fun SearchKey(onClick: () -> Unit) {
    val glow = pulse(from = 0.28f, to = 0.6f, periodMillis = 2400)
    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .size(56.dp)
            .halo(Palette.Accent, CircleShape, radius = 20.dp, alpha = glow)
            .clip(CircleShape)
            .background(Palette.AccentGradient)
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = stringResource(R.string.hub_search),
            tint = Palette.OnAccentDark,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun RowScope.HubBarItem(
    tab: HubTab,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (active) Palette.Accent else Palette.TextMuted,
        label = "tab-tint",
    )
    val lift by animateFloatAsState(
        targetValue = if (active) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tab-lift",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .tappable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .scale(lift)
                .then(
                    if (active) {
                        Modifier
                            .halo(Palette.Accent, CircleShape, radius = 18.dp, alpha = 0.55f)
                            .clip(CircleShape)
                            .background(Palette.Accent.copy(alpha = 0.16f))
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(21.dp),
            )
        }
        Text(
            text = stringResource(tab.labelRes),
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}
