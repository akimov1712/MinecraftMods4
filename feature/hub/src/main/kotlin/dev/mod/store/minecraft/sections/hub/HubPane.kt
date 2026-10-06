package dev.mod.store.minecraft.feature.hub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.compendium.CompendiumPane
import dev.mod.store.minecraft.feature.settings.SettingsPane
import dev.mod.store.minecraft.feature.showcase.ShowcasePane
import dev.mod.store.minecraft.feature.stash.StashPane

/** Renders the active tab child above the ledge. */
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
            HubLedge(
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
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) { created ->
            when (val child = created.instance) {
                is HubComponent.Child.Showcase -> ShowcasePane(child.component, Modifier.fillMaxSize())
                is HubComponent.Child.Stash -> StashPane(child.component, Modifier.fillMaxSize())
                is HubComponent.Child.Compendium -> CompendiumPane(child.component, Modifier.fillMaxSize())
                is HubComponent.Child.Settings -> SettingsPane(child.component, Modifier.fillMaxSize())
            }
        }
    }
}

private val LEDGE_HEIGHT = 62.dp

/** How far the torch beam reaches down from the rim. */
private val BEAM_DEPTH = 46.dp

/** The lantern's shape: a block set into the rock, not a button stuck on it. */
private val LanternShape = RoundedCornerShape(16.dp)

/** Slot order along the ledge; the null slot is search, which is not a destination. */
private val Slots = listOf(
    HubTab.Showcase,
    HubTab.Stash,
    null,
    HubTab.Compendium,
    HubTab.Settings,
)

/**
 * A ledge rather than a floating bar: it sits flush on the bottom edge like a shelf of rock, with a
 * hairline rim along its top.
 *
 * Four of the five slots are destinations and one is lit at a time: a torch beam falls from the rim
 * onto it and only that one says its name, the rest staying glyphs in the dark. The middle slot is
 * not a destination at all — it is a lantern set into the rock, raised above the rim and burning on
 * its own, because searching is something you *do* rather than somewhere you *are*.
 *
 * The beam and the lantern's halo are painted in one [drawBehind] reading an [Animatable], so the
 * light travels every frame without recomposing the keys.
 */
@Composable
private fun HubLedge(
    activeTab: HubTab,
    onSelect: (HubTab) -> Unit,
    onSearch: () -> Unit,
) {
    val activeSlot = Slots.indexOf(activeTab).coerceAtLeast(0)
    val beam = remember { Animatable(activeSlot.toFloat()) }

    LaunchedEffect(activeSlot) {
        beam.animateTo(
            targetValue = activeSlot.toFloat(),
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.Surface)
            .drawBehind {
                val slot = size.width / Slots.size
                val beamCentre = slot * (beam.value + 0.5f)
                val lanternCentre = slot * 2.5f

                // The rim: a dark hairline the whole way across, bright only under the torch.
                drawRect(color = Palette.Stroke, size = Size(size.width, 1.dp.toPx()))
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Palette.Accent, Color.Transparent),
                        startX = beamCentre - slot * 0.5f,
                        endX = beamCentre + slot * 0.5f,
                    ),
                    topLeft = Offset(beamCentre - slot * 0.5f, 0f),
                    size = Size(slot, 2.dp.toPx()),
                )

                // The beam, falling from the rim onto the lit key.
                val depth = BEAM_DEPTH.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Palette.Accent.copy(alpha = 0.22f),
                            Palette.Accent.copy(alpha = 0.07f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = depth,
                    ),
                    topLeft = Offset(beamCentre - slot * 0.42f, 0f),
                    size = Size(slot * 0.84f, depth),
                )

                // The lantern's own halo, which never moves.
                val halo = slot * 0.62f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Palette.Accent.copy(alpha = 0.30f),
                            Palette.Accent.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(lanternCentre, 6.dp.toPx()),
                        radius = halo,
                    ),
                    radius = halo,
                    center = Offset(lanternCentre, 6.dp.toPx()),
                )
            }
            .navigationBarsPadding()
            .height(LEDGE_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Slots.forEach { tab ->
            if (tab == null) {
                Lantern(onClick = onSearch)
            } else {
                LedgeKey(tab = tab, lit = tab == activeTab, onClick = onSelect.let { { it(tab) } })
            }
        }
    }
}

/**
 * Search: the one key that is filled rather than drawn, lifted clear of the rim so it reads as an
 * object sitting on the ledge instead of another glyph cut into it.
 */
@Composable
private fun RowScope.Lantern(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-13).dp)
                .size(48.dp)
                .clip(LanternShape)
                .tappable(pressedScale = 0.88f, onClick = onClick)
                .background(Palette.Accent)
                .border(2.dp, Palette.Canvas.copy(alpha = 0.55f), LanternShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.hub_search),
                tint = Palette.OnAccentDark,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(R.string.hub_search),
            color = Palette.Accent,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(y = (-9).dp),
        )
    }
}

/** A glyph in the dark; when the torch reaches it, its name unfolds underneath. */
@Composable
private fun RowScope.LedgeKey(
    tab: HubTab,
    lit: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clip(SmallShape)
            .tappable(pressedScale = 0.92f, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = stringResource(tab.labelRes),
            tint = if (lit) Palette.Accent else Palette.TextFaint,
            modifier = Modifier.size(if (lit) 25.dp else 23.dp),
        )
        AnimatedVisibility(
            visible = lit,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = stringResource(tab.labelRes),
                color = Palette.AccentSoft,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}
