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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.mod.store.minecraft.core.ui.R
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
private val KeyShape = RoundedCornerShape(22.dp)

/** Slot order inside the bar; the null slot is search, which is not a destination. */
private val BarSlots = listOf(HubTab.Showcase, HubTab.Stash, null, HubTab.Compendium, HubTab.Settings)

// The bar is built from fixed measurements rather than fractions, because the glow behind the keys
// is painted by hand and has to land on exactly the same centre line the layout puts the icons on.

private val BAR_HEIGHT = 82.dp

/** Keeps the outer keys off the rounded rim instead of letting them run into it. */
private val BAR_INSET = 8.dp

/** Every key reserves the same band for its glyph, whatever size that glyph is drawn at. */
private val ICON_BAND = 42.dp
private val KEY_GAP = 3.dp
private val LABEL_HEIGHT = 15.dp

private val KEY_COLUMN = ICON_BAND + KEY_GAP + LABEL_HEIGHT

/**
 * The bar floats clear of the screen edges and carries no dividers or pills. Five slots of equal
 * width, each one an icon band with its name under it — search included, so the solid key in the
 * middle sits on the same centre line as everything else instead of pushing its neighbours around.
 * What marks the open destination is light: a red aura that glides under the icons and a cap of
 * colour on the rim above it, and every press throws a ring outward from the key you touched.
 *
 * The glow, the cap and the rings are painted in one [drawBehind] that reads its animations straight
 * from [Animatable]s — the draw phase repeats each frame, the composition does not.
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(BAR_HEIGHT)
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
                val inset = BAR_INSET.toPx()
                val slot = (size.width - inset * 2f) / BarSlots.size

                /** Centre of the slot a key occupies — the same maths the Row lays keys out with. */
                fun slotCentre(index: Float) = inset + slot * (index + 0.5f)

                // The centre line of the icon band, so the glow sits on the glyphs and not below.
                val row = (size.height - KEY_COLUMN.toPx()) / 2f + ICON_BAND.toPx() / 2f

                // The aura under the open destination.
                val auraX = slotCentre(aura.value)
                val auraR = slot * 1.05f
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

                // The spread thrown by the last press.
                val p = spread.value
                if (p < 1f) {
                    val burstX = slotCentre(spreadSlot.toFloat())
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
            }
            .padding(horizontal = BAR_INSET),
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

/**
 * The shape every slot shares: one band of fixed height for the glyph, its name directly under it,
 * the pair centred in the bar. Because the band is the same height for a 25dp icon and a 42dp disc,
 * all five glyphs land on one line and all five names on another.
 */
@Composable
private fun RowScope.BarKey(
    label: String,
    tint: Color,
    bold: Boolean,
    pressedScale: Float,
    onClick: () -> Unit,
    glyph: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(KeyShape)
            .tappable(pressedScale = pressedScale, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ICON_BAND),
            contentAlignment = Alignment.Center,
            content = { glyph() },
        )
        Spacer(Modifier.height(KEY_GAP))
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Search: the one key painted solid, so "do" never looks like "go". */
@Composable
private fun RowScope.SearchKey(onClick: () -> Unit) {
    BarKey(
        label = stringResource(R.string.hub_search),
        tint = Palette.AccentSoft,
        bold = true,
        pressedScale = 0.88f,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Palette.Accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.hub_search),
                tint = Palette.OnAccentDark,
                modifier = Modifier.size(23.dp),
            )
        }
    }
}

/** A destination key: solid icon that swells inside the aura, name underneath. */
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
    val icon by animateDpAsState(
        targetValue = if (active) 28.dp else 25.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "key-icon",
    )

    BarKey(
        label = stringResource(tab.labelRes),
        tint = tint,
        bold = active,
        pressedScale = 0.9f,
        onClick = onClick,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(icon),
        )
    }
}
