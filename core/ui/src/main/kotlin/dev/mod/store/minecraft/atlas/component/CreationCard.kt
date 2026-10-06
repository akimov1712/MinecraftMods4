package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.effect.popIn
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.domain.creation.CreationEntity

private val CardShape = RoundedCornerShape(18.dp)
private val TagShape = RoundedCornerShape(9.dp)

/**
 * One creation, the width of the screen: the artwork first and the words under it.
 *
 * A thumbnail beside a paragraph is what a catalogue of anything looks like; here the picture is
 * the whole point — it is what tells a reader whether a mod is worth opening — so it gets the full
 * width in 16:9 and the text sits underneath on stone. The bottom of the artwork is darkened into
 * the card so the two read as one block rather than a picture with a caption stuck below it.
 */
@Composable
fun CreationCard(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .tappable(onClick = onClick)
            .background(Palette.Surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Palette.SurfaceHigh)
                // The artwork fades into the card instead of ending on a hard line.
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Palette.Surface),
                            startY = size.height * 0.68f,
                            endY = size.height,
                        ),
                    )
                },
        ) {
            RemoteImage(
                url = creation.imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(TagShape)
                    .background(Palette.Scrim)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = creationCategoryIcon(creation.category),
                    contentDescription = null,
                    tint = creationCategoryAccent(creation.category),
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = creationCategoryLabel(creation.category),
                    color = creationCategoryAccent(creation.category),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }

            if (creation.isBookmarked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .popIn()
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Palette.Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = null,
                        tint = Palette.OnAccentDark,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = creation.title,
                color = Palette.TextPrimary,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                creation.supportedVersions.firstOrNull()?.let { version ->
                    MetaChip(
                        text = stringResource(R.string.creation_version_short, version),
                        tint = Palette.TextFaint,
                    )
                }
                if (creation.rating > 0.0) {
                    MetaChip(
                        text = formatRating(creation.rating),
                        icon = Icons.Rounded.Star,
                        tint = Palette.Gold,
                    )
                }
            }
        }
    }
}
