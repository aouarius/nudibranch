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

fun formatSpot(spot: LatLon): String {
    val lat = "%.2f° %s".format(abs(spot.latitude), if (spot.latitude >= 0) "N" else "S")
    val lon = "%.2f° %s".format(abs(spot.longitude), if (spot.longitude >= 0) "O" else "W")
    return "$lat, $lon"
}

/** All finds with a spot on one globe. Tapping a marker shows its card. */
@Composable
fun FindsGlobeDialog(viewModel: CatalogViewModel, onDismiss: () -> Unit) {
    val finds = viewModel.collection.sightings.mapNotNull { sighting ->
        val spot = sighting.dive.spot ?: return@mapNotNull null
        val species = viewModel.species.firstOrNull { it.id == sighting.speciesId } ?: return@mapNotNull null
        Triple(sighting, species, spot)
    }
    var center by remember { mutableStateOf(finds.lastOrNull()?.third ?: DefaultCenter) }
    var selected by remember { mutableStateOf<String?>(null) }
    val selectedFind = finds.firstOrNull { it.first.photoHash == selected }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = CardColors.Background) {
            Column(
                Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Fundorte", color = CardColors.Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (finds.isEmpty()) {
                        "Noch keine Fundorte. Setze den Fundort auf der Rückseite einer Karte."
                    } else {
                        "${finds.size} ${if (finds.size == 1) "Fund" else "Funde"} · ziehen zum Drehen"
                    },
                    color = CardColors.TextMuted,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(12.dp))
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
                    Spacer(Modifier.height(12.dp))
                    SpeciesCard(species, viewModel.collection.firstSighting(species.id), Modifier.width(220.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        listOfNotNull(sighting.dive.site, formatSpot(spot)).joinToString(" · "),
                        color = CardColors.TextMuted,
                        fontSize = 12.sp,
                    )
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Schließen") }
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
    var center by remember { mutableStateOf(initial ?: DefaultCenter) }
    var spot by remember { mutableStateOf(initial) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = CardColors.Background) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Fundort setzen", color = CardColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Drehen und auf die Fundstelle tippen", color = CardColors.TextMuted, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                Globe(
                    land = land,
                    center = center,
                    onCenterChange = { center = it },
                    markers = listOfNotNull(spot?.let { GlobeMarker("spot", it, Color(0xFFE6DCC3)) }),
                    onSpotTap = { spot = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(spot?.let(::formatSpot) ?: "Noch kein Fundort gewählt", color = CardColors.Text, fontSize = 13.sp)
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    if (initial != null) TextButton(onClick = { onPick(null) }) { Text("Entfernen") }
                    TextButton(onClick = onDismiss) { Text("Abbrechen") }
                    Button(onClick = { onPick(spot) }, enabled = spot != null) { Text("Übernehmen") }
                }
            }
        }
    }
}
