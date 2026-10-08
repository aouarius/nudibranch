package io.github.aouarius.nudibranche.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.aouarius.nudibranche.core.LatLon
import io.github.aouarius.nudibranche.core.Rarity
import io.github.aouarius.nudibranche.core.Species
import kotlin.math.abs

private val DefaultCenter = LatLon(40.0, 15.0)

fun markerColor(rarity: Rarity): Color = when (rarity) {
    Rarity.HAEUFIG -> Color(0xFFE6E8EC)
    Rarity.SELTEN -> Color(0xFF7FB2FF)
    Rarity.LEGENDAER -> Color(0xFFF0A6C8)
}

fun formatSpot(spot: LatLon, strings: Strings): String {
    val lat = "%.2f° %s".format(strings.locale, abs(spot.latitude), if (spot.latitude >= 0) strings.north else strings.south)
    val lon = "%.2f° %s".format(strings.locale, abs(spot.longitude), if (spot.longitude >= 0) strings.east else strings.west)
    return "$lat, $lon"
}

/** All finds with a spot on one globe. Tapping a marker shows its card. */
@Composable
fun FindsGlobeDialog(viewModel: CatalogViewModel, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    val finds = viewModel.collection.sightings.mapNotNull { sighting ->
        val spot = sighting.dive.spot ?: return@mapNotNull null
        val species = viewModel.species.firstOrNull { it.id == sighting.speciesId } ?: return@mapNotNull null
        Triple(sighting, species, spot)
    }
    var center by remember { mutableStateOf(finds.lastOrNull()?.third ?: DefaultCenter) }
    var selected by remember { mutableStateOf<String?>(null) }
    val selectedFind = finds.firstOrNull { it.first.photoHash == selected }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = AppColors.Surface) {
            Column(
                Modifier.padding(Space.l).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(strings.findSpotsTitle, color = AppColors.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Space.xs))
                Text(
                    if (finds.isEmpty()) strings.noFindSpots else strings.findsHint(finds.size),
                    color = AppColors.TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
                Spacer(Modifier.height(Space.m))
                Globe(
                    land = viewModel.land,
                    center = center,
                    onCenterChange = { center = it },
                    markers = finds.map { (sighting, species, spot) ->
                        GlobeMarker(sighting.photoHash, spot, markerColor(species.rarity))
                    },
                    onMarkerTap = { selected = it },
                    onSpotTap = { selected = null },
                    modifier = Modifier.fillMaxWidth(),
                )
                selectedFind?.let { (sighting, species, spot) ->
                    Spacer(Modifier.height(Space.m))
                    SpeciesCard(species, sighting, Modifier.width(200.dp), compact = true)
                    Spacer(Modifier.height(Space.s))
                    Text(
                        listOfNotNull(sighting.dive.site, formatSpot(spot, strings)).joinToString(" · "),
                        color = AppColors.TextMuted,
                        fontSize = 13.sp,
                    )
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text(strings.close) }
            }
        }
    }
}

/** Lets the diver tap the find spot on the globe. */
@Composable
fun SpotPickerDialog(
    land: List<DoubleArray>,
    initial: LatLon?,
    onPick: (LatLon?) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    var center by remember { mutableStateOf(initial ?: DefaultCenter) }
    var spot by remember { mutableStateOf(initial) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = AppColors.Surface) {
            Column(Modifier.padding(Space.l), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(strings.setFindSpot, color = AppColors.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(Space.xs))
                Text(strings.turnAndTap, color = AppColors.TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(Space.m))
                Globe(
                    land = land,
                    center = center,
                    onCenterChange = { center = it },
                    markers = listOfNotNull(spot?.let { GlobeMarker("spot", it, Color(0xFFFFD36E)) }),
                    onSpotTap = { spot = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Space.s))
                Text(spot?.let { formatSpot(it, strings) } ?: strings.noSpotChosen, color = AppColors.Text, fontSize = 15.sp)
                Row(Modifier.fillMaxWidth().padding(top = Space.m), horizontalArrangement = Arrangement.End) {
                    if (initial != null) TextButton(onClick = { onPick(null) }) { Text(strings.remove) }
                    TextButton(onClick = onDismiss) { Text(strings.cancel) }
                    Button(onClick = { onPick(spot) }, enabled = spot != null) { Text(strings.useSpot) }
                }
            }
        }
    }
}
