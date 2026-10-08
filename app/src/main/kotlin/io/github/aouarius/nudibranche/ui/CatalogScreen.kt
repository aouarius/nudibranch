@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.aouarius.nudibranche.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import io.github.aouarius.nudibranche.core.DiveDetails
import io.github.aouarius.nudibranche.core.Region
import io.github.aouarius.nudibranche.core.Sighting
import io.github.aouarius.nudibranche.core.Species

@Composable
fun CatalogScreen(viewModel: CatalogViewModel = viewModel()) {
    val collection = viewModel.collection
    var region by rememberSaveable { mutableStateOf<Region?>(null) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var showGlobe by rememberSaveable { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoPicked(uri)
    }
    val shown = viewModel.species.filter { region == null || it.region == region }

    Scaffold(
        containerColor = CardColors.Background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Foto hinzufügen") },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Nudidex", color = CardColors.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${collection.unlockedCount()} von ${viewModel.species.size} Karten gefunden",
                        color = CardColors.TextMuted,
                        fontSize = 13.sp,
                    )
                }
                OutlinedButton(onClick = { showGlobe = true }) { Text("🌍 Fundorte") }
            }
            RegionFilter(selected = region, onSelect = { region = it })
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(shown, key = { it.id }) { species ->
                    SpeciesCard(
                        species = species,
                        sighting = collection.firstSighting(species.id),
                        modifier = Modifier.clickable { detailId = species.id },
                    )
                }
            }
        }
    }

    viewModel.species.firstOrNull { it.id == detailId }?.let { species ->
        CardDetailDialog(
            species = species,
            viewModel = viewModel,
            onDismiss = { detailId = null },
        )
    }
    if (showGlobe) {
        FindsGlobeDialog(viewModel, onDismiss = { showGlobe = false })
    }
    ImportDialogs(viewModel)
}

@Composable
private fun RegionFilter(selected: Region?, onSelect: (Region?) -> Unit) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("Alle") })
        Region.entries.forEach { region ->
            FilterChip(
                selected = selected == region,
                onClick = { onSelect(region) },
                label = { Text(region.displayName) },
            )
        }
    }
}

@Composable
private fun CardDetailDialog(species: Species, viewModel: CatalogViewModel, onDismiss: () -> Unit) {
    val sightings = viewModel.collection.sightingsOf(species.id)
    var flipped by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Sighting?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FlippableCard(
                flipped = flipped,
                modifier = Modifier
                    .width(300.dp)
                    .clickable(enabled = sightings.isNotEmpty() && editing == null) { flipped = !flipped },
                front = { SpeciesCard(species, sightings.firstOrNull()) },
                back = {
                    val edited = editing
                    if (edited != null) {
                        CardBackEditor(
                            species = species,
                            land = viewModel.land,
                            initial = edited.dive,
                            onSave = {
                                viewModel.updateDive(edited, it)
                                editing = null
                            },
                            onCancel = { editing = null },
                        )
                    } else {
                        CardBack(species, sightings, onEdit = { editing = it })
                    }
                },
            )
            if (sightings.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    when {
                        editing != null -> "Schreib deinen Tauchgang auf die Rückseite"
                        flipped -> "Tippen, um die Karte umzudrehen"
                        else -> "Tippen für das Logbuch auf der Rückseite"
                    },
                    color = CardColors.TextMuted,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun WriteOnBackDialog(
    state: ImportState.EnterDiveDetails,
    land: List<DoubleArray>,
    onSave: (DiveDetails) -> Unit,
) {
    var flipped by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(900)
        flipped = true
    }
    Dialog(onDismissRequest = { onSave(DiveDetails()) }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (flipped) "Schreib deinen Tauchgang auf die Rückseite" else "Deine Karte",
                color = CardColors.Text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            FlippableCard(
                flipped = flipped,
                modifier = Modifier.width(300.dp),
                front = { SpeciesCard(state.species, state.sighting) },
                back = {
                    CardBackEditor(
                        species = state.species,
                        land = land,
                        initial = state.sighting.dive,
                        onSave = onSave,
                        onCancel = { onSave(DiveDetails()) },
                        cancelLabel = "Überspringen",
                    )
                },
            )
        }
    }
}

@Composable
private fun ImportDialogs(viewModel: CatalogViewModel) {
    when (val state = viewModel.importState) {
        ImportState.Idle -> Unit
        ImportState.Checking -> Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(16.dp))
                    Text("Foto wird geprüft …")
                }
            }
        }
        is ImportState.ChooseSpecies -> SpeciesPicker(
            species = viewModel.species,
            onChoose = viewModel::onSpeciesChosen,
            onDismiss = viewModel::dismissImport,
        )
        is ImportState.EnterDiveDetails -> WriteOnBackDialog(state, viewModel.land, onSave = viewModel::onDiveDetailsEntered)
        is ImportState.Rejected -> AlertDialog(
            onDismissRequest = viewModel::dismissImport,
            title = { Text("Foto nicht akzeptiert") },
            text = { Text(state.problems.joinToString("\n\n") { "• ${it.message}" }) },
            confirmButton = { TextButton(onClick = viewModel::dismissImport) { Text("OK") } },
        )
        is ImportState.Failed -> AlertDialog(
            onDismissRequest = viewModel::dismissImport,
            title = { Text("Fehler") },
            text = { Text(state.message) },
            confirmButton = { TextButton(onClick = viewModel::dismissImport) { Text("OK") } },
        )
        is ImportState.Unlocked -> Dialog(onDismissRequest = viewModel::dismissImport) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (state.firstFind) "Neue Karte freigeschaltet!" else "Weitere Sichtung gespeichert",
                    color = CardColors.Text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                SpeciesCard(
                    state.species,
                    viewModel.collection.firstSighting(state.species.id),
                    Modifier.width(300.dp),
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::dismissImport) { Text("Super") }
            }
        }
    }
}

@Composable
private fun SpeciesPicker(species: List<Species>, onChoose: (Species) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val matches = species.filter {
        query.isBlank() ||
            it.latinName.contains(query, ignoreCase = true) ||
            it.germanName?.contains(query, ignoreCase = true) == true
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp).heightIn(max = 560.dp)) {
                Text("Welche Art ist auf dem Foto?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Die automatische Erkennung kommt in einer späteren Version. Bis dahin wählst du die Art selbst.",
                    color = CardColors.TextMuted,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Suchen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(Modifier.weight(1f, fill = false)) {
                    items(matches, key = { it.id }) { item ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onChoose(item) }
                                .padding(vertical = 10.dp),
                        ) {
                            Text(item.displayName, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${item.latinName} · ${item.region.displayName}",
                                color = CardColors.TextMuted,
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                            )
                        }
                        HorizontalDivider(color = CardColors.CardBorder)
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Abbrechen") }
            }
        }
    }
}
