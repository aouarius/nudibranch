package io.github.aouarius.nudibranche.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.aouarius.nudibranche.core.DiveDetails
import io.github.aouarius.nudibranche.core.LatLon
import io.github.aouarius.nudibranche.core.Sighting
import io.github.aouarius.nudibranche.core.Species

private val Handwriting = TextStyle(
    fontFamily = FontFamily.Cursive,
    fontSize = 18.sp,
    lineHeight = 22.sp,
    color = CardColors.Handwriting,
)

/**
 * Turns a card over around its vertical axis. The front keeps defining the size,
 * so the back is exactly as big as the card.
 */
@Composable
fun FlippableCard(
    flipped: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(durationMillis = 550), label = "flip")
    val density = LocalDensity.current.density
    Box(
        modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        },
    ) {
        Box(Modifier.graphicsLayer { alpha = if (rotation <= 90f) 1f else 0f }) { front() }
        if (rotation > 90f) {
            Box(Modifier.matchParentSize().graphicsLayer { rotationY = 180f }) { back() }
        }
    }
}

/** The back of a card: same frame as the front, with ruled paper like a logbook page. */
@Composable
private fun CardBackFrame(species: Species, content: @Composable ColumnScope.() -> Unit) {
    val strings = LocalStrings.current
    val innerShape = RoundedCornerShape(9.dp)
    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(CardColors.frame(species.rarity))
            .padding(8.dp),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(innerShape)
                .background(CardColors.Paper)
                .drawBehind {
                    val step = 26.dp.toPx()
                    var y = 44.dp.toPx()
                    while (y < size.height) {
                        drawLine(CardColors.Rule, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
                        y += step
                    }
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.logbook, color = CardColors.Label, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("#%03d".format(species.number), color = CardColors.Label, fontSize = 12.sp)
            }
            Spacer(Modifier.height(Space.s))
            content()
        }
    }
}

/** Read-only back side listing every sighting of the species. */
@Composable
fun CardBack(species: Species, sightings: List<Sighting>, onEdit: (Sighting) -> Unit) {
    CardBackFrame(species) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            sightings.forEach { sighting ->
                SightingEntry(sighting, onEdit = { onEdit(sighting) })
                Spacer(Modifier.height(Space.m))
            }
        }
    }
}

@Composable
private fun SightingEntry(sighting: Sighting, onEdit: () -> Unit) {
    val strings = LocalStrings.current
    val dive = sighting.dive
    val facts = listOfNotNull(dive.depthM?.let { "$it m" }, dive.waterTempC?.let { "$it °C" })
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                listOfNotNull(formatCardDate(sighting.takenAt, strings), dive.site).joinToString(" · "),
                style = Handwriting,
                modifier = Modifier.weight(1f),
            )
            Text(
                "✎",
                color = CardColors.Label,
                fontSize = 18.sp,
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onEdit).padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        dive.location?.let { Text(it, style = Handwriting) }
        dive.spot?.let { Text("🌍 ${formatSpot(it, strings)}", style = Handwriting.copy(fontSize = 15.sp)) }
        if (facts.isNotEmpty()) Text(facts.joinToString(" · "), style = Handwriting)
        dive.notes?.let { Text(it, style = Handwriting.copy(fontSize = 16.sp)) }
        if (dive.isEmpty) {
            Text(strings.nothingNoted, color = CardColors.Label, fontSize = 12.sp)
        }
    }
}

/** Back side with blank lines to write the dive details on. */
@Composable
fun CardBackEditor(
    species: Species,
    land: List<DoubleArray>,
    initial: DiveDetails,
    onSave: (DiveDetails) -> Unit,
    onCancel: () -> Unit,
    cancelLabel: String? = null,
) {
    val strings = LocalStrings.current
    var site by rememberSaveable { mutableStateOf(initial.site.orEmpty()) }
    var location by rememberSaveable { mutableStateOf(initial.location.orEmpty()) }
    var depth by rememberSaveable { mutableStateOf(initial.depthM?.toString().orEmpty()) }
    var temperature by rememberSaveable { mutableStateOf(initial.waterTempC?.toString().orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initial.notes.orEmpty()) }
    var latitude by rememberSaveable { mutableStateOf(initial.latitude) }
    var longitude by rememberSaveable { mutableStateOf(initial.longitude) }
    var pickingSpot by rememberSaveable { mutableStateOf(false) }
    val spot = if (latitude != null && longitude != null) LatLon(latitude!!, longitude!!) else null

    if (pickingSpot) {
        SpotPickerDialog(
            land = land,
            initial = spot,
            onPick = {
                latitude = it?.latitude
                longitude = it?.longitude
                pickingSpot = false
            },
            onDismiss = { pickingSpot = false },
        )
    }

    CardBackFrame(species) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            WrittenField(strings.diveSite, site) { site = it }
            WrittenField(strings.place, location) { location = it }
            Column(Modifier.fillMaxWidth().clickable { pickingSpot = true }) {
                Text(strings.findSpot, color = CardColors.Label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    spot?.let { "🌍 ${formatSpot(it, strings)}" } ?: "🌍 ${strings.setOnGlobe}",
                    style = if (spot != null) Handwriting else Handwriting.copy(color = CardColors.Label, fontSize = 15.sp),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Space.m)) {
                WrittenField(strings.depthMeters, depth, number = true, modifier = Modifier.weight(1f)) {
                    depth = it.filter(Char::isDigit).take(3)
                }
                WrittenField(strings.waterCelsius, temperature, number = true, modifier = Modifier.weight(1f)) {
                    temperature = it.filter { c -> c.isDigit() || c == '-' }.take(3)
                }
            }
            WrittenField(strings.notes, notes, singleLine = false) { notes = it }
        }
        Row(Modifier.fillMaxWidth().padding(top = Space.s), horizontalArrangement = Arrangement.End) {
            CardButton(cancelLabel ?: strings.cancel, emphasized = false, onClick = onCancel)
            CardButton(strings.save, emphasized = true) {
                onSave(
                    DiveDetails(
                        site = site.trim().ifEmpty { null },
                        location = location.trim().ifEmpty { null },
                        depthM = depth.toIntOrNull(),
                        waterTempC = temperature.toIntOrNull(),
                        notes = notes.trim().ifEmpty { null },
                        latitude = spot?.latitude,
                        longitude = spot?.longitude,
                    ),
                )
            }
        }
    }
}

/** A text field that looks like handwriting on a ruled line. */
@Composable
private fun WrittenField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    number: Boolean = false,
    singleLine: Boolean = true,
    onChange: (String) -> Unit,
) {
    Column(modifier) {
        Text(label, color = CardColors.Label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 2,
            textStyle = Handwriting,
            cursorBrush = SolidColor(CardColors.Handwriting),
            keyboardOptions = if (number) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(CardColors.Label, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1f)
                }
                .padding(top = 2.dp, bottom = 4.dp),
        )
    }
}

@Composable
private fun CardButton(label: String, emphasized: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (emphasized) Color.White else CardColors.InkMuted,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(start = 8.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (emphasized) CardColors.Handwriting else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
