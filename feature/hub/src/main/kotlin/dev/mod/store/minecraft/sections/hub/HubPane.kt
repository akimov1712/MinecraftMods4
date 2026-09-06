package dev.mod.store.minecraft.feature.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.compendium.CompendiumPane
import dev.mod.store.minecraft.feature.settings.SettingsPane
import dev.mod.store.minecraft.feature.showcase.ShowcasePane
import dev.mod.store.minecraft.feature.stash.StashPane
import kotlinx.coroutines.launch

/** Renders the active tab child above the navigation bar. */
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
                is HubComponent.Child.Settings -> SettingsPane(child.component, Modifier.fillMaxSize())
            }
        }
    }
}

private val BarShape = RoundedCornerShape(30.dp)

/** Slot order inside the bar; the null slot is search, which is not a destination. */
private val BarSlots = listOf(HubTab.Showcase, HubTab.Stash, null, HubTab.Compendium, HubTab.Settings)

/**
 * The bar floats clear of the screen edges and carries no dividers or pills. What marks the open
 * destination is light: a red aura that glides under the icons and a cap of colour on the rim above
 * it. Every press throws a ring outward from the key you touched, so the bar answers back.
 *
 * The glow, the cap and the rings are all painted in one [drawBehind] that reads its animations
 * straight from [Animatable]s — the draw phase repeats each frame, the composition does not.
 */
@Composable
private fun HubBar(
    activeTab: HubTab,
    onSelect: (HubTab) -> Unit,
    onSearch: () -> Unit,
) {
    val activeSlot = BarSlots.indexOf(activeTab).coerceAtLeast(0)
    val aura = remember { Animatable(activeSlot.toFloat()) }
    val spread = remember { Animatable(1f) }
    var spreadSlot by remember { mutableIntStateOf(activeSlot) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(activeSlot) {
        aura.animateTo(
            targetValue = activeSlot.toFloat(),
            animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessLow),
        )
    }

    fun burst(slot: Int) {
        spreadSlot = slot
        scope.launch {
            spread.snapTo(0f)
            spread.animateTo(1f, tween(durationMillis = 560, easing = FastOutSlowInEasing))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .height(78.dp)
            .shadow(
                elevation = 22.dp,
                shape = BarShape,
                ambientColor = Palette.Accent,
                spotColor = Palette.Accent,
            )
            .clip(BarShape)
            .background(Palette.Surface)
            .border(1.dp, Palette.Stroke, BarShape)
            .drawBehind {
                val slot = size.width / BarSlots.size
                val row = size.height * 0.45f

                // The aura under the open destination.
                val auraX = slot * (aura.value + 0.5f)
                val auraR = slot * 1.15f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Palette.Accent.copy(alpha = 0.46f),
                            Palette.Accent.copy(alpha = 0.13f),
                            Color.Transparent,
                        ),
                        center = Offset(auraX, row),
                        radius = auraR,
                    ),
                    radius = auraR,
                    center = Offset(auraX, row),
                )

                // Its cap, set just inside the rim so the rounded corners never clip it.
                val capHeight = 4.dp.toPx()
                val capWidth = slot * 0.42f
                drawRoundRect(
                    color = Palette.Accent,
                    topLeft = Offset(auraX - capWidth / 2f, 7.dp.toPx()),
                    size = Size(capWidth, capHeight),
                    cornerRadius = CornerRadius(capHeight),
                )

                // A steady halo that keeps search reading as the odd one out.
                val searchX = slot * 2.5f
                val searchR = slot * 0.82f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Palette.Accent.copy(alpha = 0.30f), Color.Transparent),
                        center = Offset(searchX, row),
                        radius = searchR,
                    ),
                    radius = searchR,
                    center = Offset(searchX, row),
                )

                // The spread thrown by the last press.
                val p = spread.value
                if (p < 1f) {
                    val burstX = slot * (spreadSlot + 0.5f)
                    drawCircle(
                        color = Palette.Accent.copy(alpha = 0.38f * (1f - p)),
                        radius = p * slot * 1.7f,
                        center = Offset(burstX, row),
                        style = Stroke(width = (1f + 2.5f * (1f - p)).dp.toPx()),
                    )
                    drawCircle(
                        color = Palette.AccentSoft.copy(alpha = 0.26f * (1f - p)),
                        radius = p * slot * 1.05f,
                        center = Offset(burstX, row),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BarSlots.forEachIndexed { index, tab ->
            if (tab == null) {
                SearchKey(
                    onClick = {
                        burst(index)
                        onSearch()
                    },
                )
            } else {
                HubKey(
                    tab = tab,
                    active = tab == activeTab,
                    onClick = {
                        burst(index)
                        onSelect(tab)
                    },
                )
            }
        }
    }
}

/** Search: the one key painted solid, so "do" never looks like "go". */
@Composable
private fun RowScope.SearchKey(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(26.dp))
            .tappable(pressedScale = 0.88f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-4).dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Palette.Accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.hub_search),
                tint = Palette.OnAccentDark,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

/** A destination key: solid icon that rises into the aura, name underneath. */
@Composable
private fun RowScope.HubKey(
    tab: HubTab,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (active) Palette.OnAccent else Palette.TextFaint,
        label = "key-tint",
    )
    val lift by animateDpAsState(
        targetValue = if (active) (-4).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "key-lift",
    )
    val icon by animateDpAsState(
        targetValue = if (active) 28.dp else 25.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "key-icon",
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(26.dp))
            .tappable(pressedScale = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .offset(y = lift)
                .size(icon),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = stringResource(tab.labelRes),
            color = tint,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 9.dp),
        )
    }
}
