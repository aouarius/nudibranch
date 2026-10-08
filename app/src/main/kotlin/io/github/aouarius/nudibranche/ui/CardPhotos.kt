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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val DeleteRed = Color(0xFFFF7A7A)

/**
 * The diver's photos of one species under the card: tap a photo to put it on the
 * card or delete it, or add another one.
 */
@Composable
fun CardPhotos(
    sightings: List<Sighting>,
    cover: Sighting?,
    onAdd: () -> Unit,
    onSetCover: (Sighting) -> Unit,
    onDelete: (Sighting) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    var selectedHash by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf<Sighting?>(null) }
    val selected = sightings.firstOrNull { it.photoHash == selectedHash }

    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .padding(Space.m),
    ) {
        if (sightings.isNotEmpty()) {
            Text(strings.yourPhotos(sightings.size), color = AppColors.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Space.s))
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            sightings.forEach { sighting ->
                PhotoThumb(
                    sighting = sighting,
                    isCover = sighting.photoHash == cover?.photoHash,
                    isSelected = sighting.photoHash == selectedHash,
                    onClick = { selectedHash = if (selectedHash == sighting.photoHash) null else sighting.photoHash },
                )
            }
            AddTile(onAdd, showLabel = sightings.isEmpty())
        }
        if (selected != null) {
            Spacer(Modifier.height(Space.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selected.photoHash == cover?.photoHash) {
                    Text(strings.cardPhoto, color = AppColors.Accent, fontSize = 14.sp, modifier = Modifier.padding(start = Space.xs))
                } else {
                    TextButton(onClick = { onSetCover(selected) }) { Text(strings.useAsCardPhoto, fontSize = 14.sp) }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { confirmDelete = selected }) {
                    Text(strings.delete, color = DeleteRed, fontSize = 14.sp)
                }
            }
        }
    }

    confirmDelete?.let { sighting ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(strings.deletePhotoTitle) },
            text = { Text(strings.deletePhotoText(lastPhoto = sightings.size == 1), fontSize = 15.sp, lineHeight = 21.sp) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = null
                    selectedHash = null
                    onDelete(sighting)
                }) { Text(strings.delete, color = DeleteRed) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text(strings.cancel) } },
        )
    }
}

@Composable
private fun PhotoThumb(sighting: Sighting, isCover: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .size(72.dp)
            .clip(shape)
            .border(if (isSelected) 3.dp else 0.dp, if (isSelected) AppColors.Accent else Color.Transparent, shape)
            .background(AppColors.Border)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = File(LocalContext.current.filesDir, sighting.photoPath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .padding(if (isSelected) 3.dp else 0.dp)
                .clip(RoundedCornerShape(8.dp)),
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
