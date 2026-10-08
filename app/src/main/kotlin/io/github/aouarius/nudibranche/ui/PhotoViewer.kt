package io.github.aouarius.nudibranche.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import io.github.aouarius.nudibranche.core.Sighting
import io.github.aouarius.nudibranche.core.Species
import java.io.File

private val DeleteRed = Color(0xFFFF7A7A)
private const val MaxZoom = 5f

/**
 * Full-screen view of the diver's photos of one species, for showing them off:
 * swipe between photos, pinch or double-tap to zoom.
 */
@Composable
fun PhotoViewer(
    species: Species,
    sightings: List<Sighting>,
    startIndex: Int,
    cover: Sighting?,
    onSetCover: (Sighting) -> Unit,
    onDelete: (Sighting) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    val pager = rememberPagerState(initialPage = startIndex.coerceIn(0, (sightings.size - 1).coerceAtLeast(0))) { sightings.size }
    var zoomed by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Sighting?>(null) }

    LaunchedEffect(sightings.isEmpty()) {
        if (sightings.isEmpty()) onDismiss()
    }
    val current = sightings.getOrNull(pager.currentPage) ?: return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pager,
                userScrollEnabled = !zoomed,
                key = { sightings[it].photoHash },
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                ZoomablePhoto(
                    sighting = sightings[page],
                    description = species.name(strings.language),
                    onZoomChange = { if (page == pager.currentPage) zoomed = it },
                )
            }

            Column(Modifier.fillMaxWidth().safeDrawingPadding()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.s),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (sightings.size > 1) {
                        Text(
                            "${pager.currentPage + 1} / ${sightings.size}",
                            color = Color.White,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = Space.m),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = strings.close, tint = Color.White)
                    }
                }
            }

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .safeDrawingPadding()
                    .padding(horizontal = Space.l, vertical = Space.m),
            ) {
                Text(
                    species.name(strings.language),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val details = listOfNotNull(
                    formatCardDate(current.takenAt, strings),
                    current.dive.site,
                    current.dive.location,
                    current.dive.depthM?.let { "$it m" },
                ).joinToString(" · ")
                if (details.isNotEmpty()) {
                    Text(details, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                Spacer(Modifier.height(Space.s))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.s), verticalAlignment = Alignment.CenterVertically) {
                    if (current.photoHash == cover?.photoHash) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = AppColors.Accent, modifier = Modifier.size(18.dp))
                        Text(strings.cardPhoto, color = AppColors.Accent, fontSize = 14.sp)
                    } else {
                        TextButton(onClick = { onSetCover(current) }) {
                            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(Space.xs))
                            Text(strings.useAsCardPhoto, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { confirmDelete = current }) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = DeleteRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(Space.xs))
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
                        onDelete(sighting)
                    }) { Text(strings.delete, color = DeleteRed) }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text(strings.cancel) } },
            )
        }
    }
}

@Composable
private fun ZoomablePhoto(sighting: Sighting, description: String, onZoomChange: (Boolean) -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    fun reset() {
        scale = 1f
        offset = Offset.Zero
        onZoomChange(false)
    }
    val transform = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, MaxZoom)
        offset = if (scale > 1f) offset + panChange * scale else Offset.Zero
        onZoomChange(scale > 1f)
    }
    AsyncImage(
        model = File(LocalContext.current.filesDir, sighting.photoPath),
        contentDescription = description,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (scale > 1f) {
                        reset()
                    } else {
                        scale = 2.5f
                        onZoomChange(true)
                    }
                })
            }
            // Panning only while zoomed in, so a plain swipe still moves to the next photo.
            .transformable(transform, canPan = { scale > 1f })
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
    )
}
