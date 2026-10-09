@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.aouarius.nudibranche.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.aouarius.nudibranche.data.CardShare
import io.github.aouarius.nudibranche.core.Recognition
import io.github.aouarius.nudibranche.core.Suggestion
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.aouarius.nudibranche.core.DiveDetails
import io.github.aouarius.nudibranche.core.Language
import io.github.aouarius.nudibranche.core.Region
import io.github.aouarius.nudibranche.core.Sighting
import io.github.aouarius.nudibranche.core.Species
import kotlinx.coroutines.delay

private val DialogCardWidth = 320.dp

@Composable
fun CatalogScreen(viewModel: CatalogViewModel = viewModel()) {
    CompositionLocalProvider(LocalStrings provides stringsFor(viewModel.language)) {
        Catalog(viewModel)
    }
}

private enum class Page { COLLECTION, LIBRARY, LOGBOOK }

@Composable
private fun Catalog(viewModel: CatalogViewModel) {
    val strings = LocalStrings.current
    var page by rememberSaveable { mutableStateOf(Page.COLLECTION) }
    var showGlobe by rememberSaveable { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoPicked(uri)
    }
    // Adding a photo from a card skips the species question.
    var photoForSpeciesId by rememberSaveable { mutableStateOf<String?>(null) }
    val cardPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        val species = viewModel.species.firstOrNull { it.id == photoForSpeciesId }
        if (uri != null && species != null) viewModel.onPhotoPicked(uri, forSpecies = species)
        photoForSpeciesId = null
    }

    val sideways = isSideways()
    val addPhoto: @Composable () -> Unit = {
        if (page == Page.COLLECTION) {
            ExtendedFloatingActionButton(
                onClick = { picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(strings.addPhoto, fontSize = 15.sp) },
            )
        }
    }
    val tabs = listOf(
        Triple(Page.COLLECTION, Icons.Filled.Home, strings.collectionTab),
        Triple(Page.LIBRARY, Icons.AutoMirrored.Filled.List, strings.libraryTab),
        Triple(Page.LOGBOOK, Icons.Filled.Star, strings.logbookTab),
    )

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            if (!sideways) {
                NavigationBar(containerColor = AppColors.Surface) {
                    tabs.forEach { (tab, icon, label) ->
                        NavigationBarItem(
                            selected = page == tab,
                            onClick = { page = tab },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
        // Held sideways, the button sits inside the content so it stays clear of the system buttons.
        floatingActionButton = { if (!sideways) addPhoto() },
    ) { padding ->
        Row(Modifier.padding(padding)) {
            if (sideways) {
                NavigationRail(containerColor = AppColors.Surface, windowInsets = WindowInsets(0)) {
                    Spacer(Modifier.weight(1f))
                    tabs.forEach { (tab, icon, label) ->
                        NavigationRailItem(
                            selected = page == tab,
                            onClick = { page = tab },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(label) },
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
            Box(Modifier.weight(1f)) {
                Column {
                    when (page) {
                        Page.COLLECTION -> CollectionPage(
                            viewModel,
                            onShowGlobe = { showGlobe = true },
                            onAddPhoto = { species ->
                                photoForSpeciesId = species.id
                                cardPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                            },
                        )
                        Page.LIBRARY -> LibraryPage(viewModel)
                        Page.LOGBOOK -> LogbookPage(viewModel)
                    }
                }
                if (sideways) {
                    Box(Modifier.align(Alignment.BottomEnd).padding(Space.l)) { addPhoto() }
                }
            }
        }
    }

    if (showGlobe) {
        FindsGlobeDialog(viewModel, onDismiss = { showGlobe = false })
    }
    ImportDialogs(viewModel)
}

/** Title row with the language switch, shared by both pages. */
@Composable
fun PageHeader(
    title: String,
    subtitle: String,
    viewModel: CatalogViewModel,
    action: @Composable () -> Unit = {},
) {
    if (isSideways()) {
        // Held sideways there is little height, so everything shares one line.
        Row(
            Modifier.padding(start = Space.l, end = Space.l, top = Space.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.m),
        ) {
            Text(title, color = AppColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(
                subtitle,
                color = AppColors.TextMuted,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            action()
            LanguageToggle(viewModel.language, onChange = viewModel::changeLanguage)
        }
        return
    }
    Column(Modifier.padding(start = Space.l, end = Space.l, top = Space.l, bottom = Space.s)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                color = AppColors.Text,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f),
            )
            LanguageToggle(viewModel.language, onChange = viewModel::changeLanguage)
        }
        Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(subtitle, color = AppColors.TextMuted, fontSize = 14.sp, modifier = Modifier.weight(1f))
            action()
        }
    }
}

/** The main page: the player's cards. */
@Composable
private fun CollectionPage(viewModel: CatalogViewModel, onShowGlobe: () -> Unit, onAddPhoto: (Species) -> Unit) {
    val strings = LocalStrings.current
    val collection = viewModel.collection
    var region by rememberSaveable { mutableStateOf<Region?>(null) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    val shown = viewModel.species.filter {
        (region == null || it.region == region) && (!viewModel.onlyFound || collection.isUnlocked(it.id))
    }

    Column {
        PageHeader(
            title = "Nudidex",
            subtitle = strings.cardsFound(collection.unlockedCount(), viewModel.species.size),
            viewModel = viewModel,
            action = { TextButton(onClick = onShowGlobe) { Text(strings.findSpotsButton, fontSize = 14.sp) } },
        )
        val foundFilter = @Composable {
            SegmentedPill(
                options = listOf(false, true),
                selected = viewModel.onlyFound,
                label = { if (it) strings.showFound else strings.showAll },
                onSelect = viewModel::changeOnlyFound,
            )
        }
        if (isSideways()) {
            Row(Modifier.padding(start = Space.l, top = Space.s), verticalAlignment = Alignment.CenterVertically) {
                foundFilter()
                Box(Modifier.weight(1f)) { RegionFilter(selected = region, onSelect = { region = it }) }
            }
        } else {
            Row(Modifier.padding(start = Space.l, end = Space.l, bottom = Space.s)) { foundFilter() }
            RegionFilter(selected = region, onSelect = { region = it })
        }
        if (shown.isEmpty() && viewModel.onlyFound) {
            Text(
                strings.noCardsFoundYet,
                color = AppColors.TextMuted,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.xl),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            contentPadding = PaddingValues(start = Space.l, end = Space.l, top = Space.m, bottom = 104.dp),
            horizontalArrangement = Arrangement.spacedBy(Space.m),
            verticalArrangement = Arrangement.spacedBy(Space.m),
        ) {
            items(shown, key = { it.id }) { species ->
                SpeciesCard(
                    species = species,
                    sighting = collection.coverOf(species.id),
                    compact = true,
                    modifier = Modifier.clickable { detailId = species.id },
                )
            }
        }
    }

    viewModel.species.firstOrNull { it.id == detailId }?.let { species ->
        CardDetailDialog(
            species = species,
            viewModel = viewModel,
            onDismiss = { detailId = null },
            onAddPhoto = {
                detailId = null
                onAddPhoto(species)
            },
        )
    }
}

/** Two-part pill: DE | EN. */
@Composable
private fun LanguageToggle(current: Language, onChange: (Language) -> Unit) =
    SegmentedPill(Language.entries, current, label = { it.name }, onSelect = onChange)

/** Pill with two or more options, the chosen one filled. */
@Composable
private fun <T> SegmentedPill(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .clip(shape)
            .border(1.dp, AppColors.Border, shape),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Text(
                label(option),
                color = if (isSelected) AppColors.Background else AppColors.TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(if (isSelected) AppColors.Accent else AppColors.Background)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
fun RegionFilter(selected: Region?, onSelect: (Region?) -> Unit) {
    val strings = LocalStrings.current
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Space.l),
        horizontalArrangement = Arrangement.spacedBy(Space.s),
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text(strings.allRegions) })
        Region.entries.forEach { region ->
            FilterChip(
                selected = selected == region,
                onClick = { onSelect(region) },
                label = { Text(region.label(strings.language)) },
            )
        }
    }
}

private val Scrim = Color(0xF20C0E12)

/**
 * Full-screen dialog for a big card: a dark backdrop hides the grid behind it, tapping
 * the backdrop closes it, and tall content scrolls.
 */
@Composable
private fun CardDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Scrim)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.l, vertical = Space.xl)
                    // Taps on the content itself must not close the dialog.
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

@Composable
private fun CardDetailDialog(
    species: Species,
    viewModel: CatalogViewModel,
    onDismiss: () -> Unit,
    onAddPhoto: () -> Unit,
) {
    val strings = LocalStrings.current
    val sightings = viewModel.collection.sightingsOf(species.id)
    val cover = viewModel.collection.coverOf(species.id)
    var flipped by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Sighting?>(null) }
    var viewing by remember { mutableStateOf<Int?>(null) }
    // The front is recorded while it is drawn, so sharing sends exactly what is on screen.
    val cardPicture = rememberGraphicsLayer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    CardDialog(onDismiss = onDismiss) {
        FlippableCard(
            flipped = flipped,
            modifier = Modifier
                .width(DialogCardWidth)
                .clickable(enabled = sightings.isNotEmpty() && editing == null) { flipped = !flipped },
            front = {
                SpeciesCard(
                    species,
                    cover,
                    Modifier.drawWithContent {
                        cardPicture.record { this@drawWithContent.drawContent() }
                        drawLayer(cardPicture)
                    },
                )
            },
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
            Spacer(Modifier.height(Space.m))
            Text(
                when {
                    editing != null -> strings.writeOnBack
                    flipped -> strings.tapToTurnBack
                    else -> strings.tapForLogbook
                },
                color = AppColors.TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(DialogCardWidth),
            )
        }
        if (sightings.isNotEmpty() && !flipped && editing == null) {
            Spacer(Modifier.height(Space.m))
            OutlinedButton(onClick = {
                scope.launch {
                    val picture = cardPicture.toImageBitmap().asAndroidBitmap()
                    CardShare.share(context, picture, fileName = species.id, chooserTitle = strings.shareCard)
                }
            }) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.padding(end = Space.s))
                Text(strings.shareCard, fontSize = 15.sp)
            }
        }
        if (editing == null) {
            Spacer(Modifier.height(Space.l))
            CardPhotos(
                sightings = sightings,
                cover = cover,
                onAdd = onAddPhoto,
                onOpen = { viewing = it },
                modifier = Modifier.width(DialogCardWidth),
            )
        }
    }

    viewing?.let { index ->
        PhotoViewer(
            species = species,
            sightings = sightings,
            startIndex = index,
            cover = cover,
            onSetCover = viewModel::setCover,
            onDelete = { sighting ->
                viewModel.deleteSighting(sighting)
                if (sightings.size == 1) {
                    flipped = false
                    viewing = null
                }
            },
            onDismiss = { viewing = null },
        )
    }
}

@Composable
private fun WriteOnBackDialog(
    state: ImportState.EnterDiveDetails,
    land: List<DoubleArray>,
    onSave: (DiveDetails) -> Unit,
) {
    val strings = LocalStrings.current
    var flipped by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(900)
        flipped = true
    }
    CardDialog(onDismiss = { onSave(state.sighting.dive) }) {
        Text(
            if (flipped) strings.writeOnBack else strings.yourCard,
            color = AppColors.Text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(Space.m))
        FlippableCard(
            flipped = flipped,
            modifier = Modifier.width(DialogCardWidth),
            front = { SpeciesCard(state.species, state.sighting) },
            back = {
                CardBackEditor(
                    species = state.species,
                    land = land,
                    initial = state.sighting.dive,
                    onSave = onSave,
                    onCancel = { onSave(state.sighting.dive) },
                    cancelLabel = strings.skip,
                )
            },
        )
    }
}

@Composable
private fun ImportDialogs(viewModel: CatalogViewModel) {
    val strings = LocalStrings.current
    when (val state = viewModel.importState) {
        ImportState.Idle -> Unit
        ImportState.Checking -> Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(Space.xl), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(Space.l))
                    Text(strings.checkingPhoto, fontSize = 15.sp)
                }
            }
        }
        is ImportState.ChooseSpecies -> SpeciesPicker(
            species = viewModel.species,
            suggestions = state.suggestions,
            onChoose = viewModel::onSpeciesChosen,
            onDismiss = viewModel::dismissImport,
        )
        is ImportState.EnterDiveDetails -> WriteOnBackDialog(state, viewModel.land, onSave = viewModel::onDiveDetailsEntered)
        is ImportState.Rejected -> AlertDialog(
            onDismissRequest = viewModel::dismissImport,
            title = { Text(strings.photoNotAccepted) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
                    state.problems.forEach { Text("• ${strings.problem(it)}", fontSize = 15.sp, lineHeight = 21.sp) }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::dismissImport) { Text(strings.ok) } },
        )
        ImportState.Failed -> AlertDialog(
            onDismissRequest = viewModel::dismissImport,
            title = { Text(strings.error) },
            text = { Text(strings.photoUnreadable, fontSize = 15.sp) },
            confirmButton = { TextButton(onClick = viewModel::dismissImport) { Text(strings.ok) } },
        )
        is ImportState.Unlocked -> CardDialog(onDismiss = viewModel::dismissImport) {
            Text(
                if (state.firstFind) strings.newCardUnlocked else strings.sightingSaved,
                color = AppColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Space.l))
            SpeciesCard(state.species, state.sighting, Modifier.width(DialogCardWidth))
            if (state.newBadges.isNotEmpty()) {
                Spacer(Modifier.height(Space.l))
                Text(
                    strings.newBadges(state.newBadges.size),
                    color = AppColors.Accent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(Space.s))
                Column(Modifier.width(DialogCardWidth), verticalArrangement = Arrangement.spacedBy(Space.s)) {
                    state.newBadges.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                            row.forEach { BadgeTile(it, Modifier.weight(1f)) }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(Space.l))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.m)) {
                val isCover = viewModel.collection.coverOf(state.species.id)?.photoHash == state.sighting.photoHash
                if (!isCover) {
                    OutlinedButton(onClick = {
                        viewModel.setCover(state.sighting)
                        viewModel.dismissImport()
                    }) { Text(strings.useAsCardPhoto, fontSize = 15.sp) }
                }
                Button(onClick = viewModel::dismissImport) { Text(strings.great, fontSize = 15.sp) }
            }
        }
    }
}

@Composable
private fun SpeciesPicker(
    species: List<Species>,
    suggestions: List<Suggestion>,
    onChoose: (Species) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    var query by remember { mutableStateOf("") }
    val matches = species.filter {
        query.isBlank() ||
            it.latinName.contains(query, ignoreCase = true) ||
            it.germanName?.contains(query, ignoreCase = true) == true ||
            it.englishName?.contains(query, ignoreCase = true) == true
    }
    val sure = suggestions.firstOrNull()?.let { it.confidence >= Recognition.CONFIDENT } == true
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(Space.l).heightIn(max = 640.dp)) {
                Text(strings.whichSpecies, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (!sure) {
                    Spacer(Modifier.height(Space.xs))
                    Text(
                        if (suggestions.isEmpty()) strings.noSuggestions else strings.suggestionsUnsure,
                        color = AppColors.TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(Space.m))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(strings.search) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Space.s))
                LazyColumn(Modifier.weight(1f, fill = false)) {
                    if (query.isBlank() && suggestions.isNotEmpty()) {
                        item(key = "suggestions") { PickerHeading(strings.suggestionsTitle) }
                        items(suggestions, key = { "suggestion-${it.species.id}" }) { suggestion ->
                            SuggestionRow(suggestion, highlighted = sure && suggestion == suggestions.first()) {
                                onChoose(suggestion.species)
                            }
                        }
                        item(key = "all") { PickerHeading(strings.allSpecies) }
                    }
                    items(matches, key = { it.id }) { item ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onChoose(item) }
                                .padding(vertical = Space.m),
                        ) {
                            Text(item.name(strings.language), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${item.latinName} · ${item.region.label(strings.language)}",
                                color = AppColors.TextMuted,
                                fontStyle = FontStyle.Italic,
                                fontSize = 13.sp,
                            )
                        }
                        HorizontalDivider(color = AppColors.Border)
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text(strings.cancel) }
            }
        }
    }
}

@Composable
private fun PickerHeading(text: String) {
    Text(
        text.uppercase(),
        color = AppColors.TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = Space.m, bottom = Space.xs),
    )
}

/** A guess of the model with its reference photo and how sure the model is. */
@Composable
private fun SuggestionRow(suggestion: Suggestion, highlighted: Boolean, onClick: () -> Unit) {
    val strings = LocalStrings.current
    val item = suggestion.species
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = Space.xs)
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlighted) AppColors.Accent.copy(alpha = 0.18f) else AppColors.Background)
            .clickable(onClick = onClick)
            .padding(Space.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpeciesThumbnail(item)
        Spacer(Modifier.width(Space.m))
        Column(Modifier.weight(1f)) {
            Text(item.name(strings.language), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(item.latinName, color = AppColors.TextMuted, fontStyle = FontStyle.Italic, fontSize = 13.sp)
        }
        Text(
            "${Math.round(suggestion.confidence * 100)} %",
            color = if (highlighted) AppColors.Accent else AppColors.TextMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Phone held sideways: wide but too low for the upright layout. */
@Composable
fun isSideways(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp && configuration.screenHeightDp < 600
}
