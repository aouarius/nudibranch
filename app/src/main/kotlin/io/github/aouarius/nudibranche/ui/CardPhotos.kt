package io.github.aouarius.nudibranche.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.aouarius.nudibranche.core.Sighting
import java.io.File

/** The diver's photos of one species under the card: tap one to see it big, or add another. */
@Composable
fun CardPhotos(
    sightings: List<Sighting>,
    cover: Sighting?,
    onAdd: () -> Unit,
    onOpen: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .padding(Space.m),
    ) {
        if (sightings.isNotEmpty()) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(strings.yourPhotos(sightings.size), color = AppColors.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(strings.tapToEnlarge, color = AppColors.TextMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(Space.s))
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            sightings.forEachIndexed { index, sighting ->
                PhotoThumb(sighting, isCover = sighting.photoHash == cover?.photoHash, onClick = { onOpen(index) })
            }
            AddTile(onAdd, showLabel = sightings.isEmpty())
        }
    }
}

@Composable
private fun PhotoThumb(sighting: Sighting, isCover: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.Border)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = File(LocalContext.current.filesDir, sighting.photoPath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        if (isCover) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AppColors.Accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Star, contentDescription = LocalStrings.current.cardPhoto, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun AddTile(onAdd: () -> Unit, showLabel: Boolean) {
    val strings = LocalStrings.current
    Row(
        Modifier
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onAdd)
            .padding(horizontal = if (showLabel) Space.l else 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, contentDescription = strings.addPhotoHere, tint = AppColors.Accent)
        if (showLabel) {
            Spacer(Modifier.size(Space.s))
            Text(strings.addPhotoHere, color = AppColors.Text, fontSize = 15.sp)
        }
    }
}
