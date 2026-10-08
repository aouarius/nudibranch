package io.github.aouarius.nudibranche.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import io.github.aouarius.nudibranche.core.Region
import io.github.aouarius.nudibranche.core.Species
import io.github.aouarius.nudibranche.core.SpeciesPhoto

/** Reference page with every species, found or not. The collection stays the main page. */
@Composable
fun LibraryPage(viewModel: CatalogViewModel) {
    val strings = LocalStrings.current
    val language = strings.language
    val collection = viewModel.collection
    var region by rememberSaveable { mutableStateOf<Region?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    val shown = viewModel.species.filter { species ->
        (region == null || species.region == region) &&
            (
                query.isBlank() ||
                    species.latinName.contains(query, ignoreCase = true) ||
                    species.name(language).contains(query, ignoreCase = true) ||
                    species.family.contains(query, ignoreCase = true)
                )
    }

    Column {
        PageHeader(
            title = strings.libraryTitle,
            subtitle = strings.libraryCount(viewModel.species.size, collection.unlockedCount()),
            viewModel = viewModel,
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(strings.search) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.l),
        )
        Spacer(Modifier.height(Space.s))
        RegionFilter(selected = region, onSelect = { region = it })
        LazyColumn(
            contentPadding = PaddingValues(start = Space.l, end = Space.l, top = Space.m, bottom = Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            items(shown, key = { it.id }) { species ->
                LibraryRow(species, found = collection.isUnlocked(species.id), onClick = { detailId = species.id })
            }
        }
    }

    viewModel.species.firstOrNull { it.id == detailId }?.let { species ->
        LibraryDetailDialog(species, viewModel, onDismiss = { detailId = null })
    }
}

@Composable
private fun NumberBadge(species: Species) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(CardColors.tag(species.rarity)),
        contentAlignment = Alignment.Center,
    ) {
        Text("#%03d".format(species.number), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

private fun SpeciesPhoto.assetUri() = "file:///android_asset/species_photos/$file"

/** Reference photo with the species number on a small rarity-colored tag. */
@Composable
private fun SpeciesThumbnail(species: Species) {
    val photo = species.photo ?: return NumberBadge(species)
    Box(
        Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Border),
    ) {
        AsyncImage(
            model = photo.assetUri(),
            contentDescription = species.latinName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Text(
            "#%03d".format(species.number),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(3.dp)
                .clip(RoundedCornerShape(50))
                .background(CardColors.tag(species.rarity))
                .padding(horizontal = 5.dp, vertical = 1.dp),
        )
    }
}

@Composable
private fun LibraryRow(species: Species, found: Boolean, onClick: () -> Unit) {
    val strings = LocalStrings.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.Surface)
            .clickable(onClick = onClick)
            .padding(Space.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpeciesThumbnail(species)
        Spacer(Modifier.width(Space.m))
        Column(Modifier.weight(1f)) {
            Text(
                species.name(strings.language),
                color = AppColors.Text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                species.latinName,
                color = AppColors.TextMuted,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${species.family} · ${species.region.label(strings.language)}",
                color = AppColors.TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(Space.s))
        Icon(
            if (found) Icons.Filled.Check else Icons.Filled.Lock,
            contentDescription = if (found) strings.found else strings.notFoundYet,
            tint = if (found) AppColors.Accent else AppColors.Border,
        )
    }
}

@Composable
private fun LibraryDetailDialog(species: Species, viewModel: CatalogViewModel, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    val language = strings.language
    val first = viewModel.collection.firstSighting(species.id)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = AppColors.Surface) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Space.xl),
            ) {
                species.photo?.let { photo ->
                    LibraryPhoto(species, photo)
                    Spacer(Modifier.height(Space.l))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberBadge(species)
                    Spacer(Modifier.width(Space.m))
                    Column {
                        Text(
                            species.name(language),
                            color = AppColors.Text,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp,
                        )
                        Text(species.latinName, color = AppColors.TextMuted, fontSize = 14.sp, fontStyle = FontStyle.Italic)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = Space.l), color = AppColors.Border)
                Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
                    Fact(strings.family, species.family)
                    Fact(strings.region, species.region.label(language))
                    Fact(strings.rarity, species.rarity.label(language))
                    Fact(strings.size, strings.upTo(species.maxSizeCm))
                    Fact(strings.depth, depthText(species, strings))
                    Fact(strings.food, species.food(language))
                    Fact(strings.habitat, species.habitat(language))
                }
                Spacer(Modifier.height(Space.l))
                Text(
                    first?.let { strings.firstFound(formatCardDate(it.takenAt, strings).orEmpty()) } ?: strings.notFoundYet,
                    color = if (first != null) AppColors.Accent else AppColors.TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text(strings.close) }
            }
        }
    }
}

@Composable
private fun LibraryPhoto(species: Species, photo: SpeciesPhoto) {
    val uriHandler = LocalUriHandler.current
    Column {
        AsyncImage(
            model = photo.assetUri(),
            contentDescription = species.latinName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.Border),
        )
        Text(
            photo.credit,
            color = AppColors.TextMuted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { uriHandler.openUri(photo.source) }
                .padding(top = Space.xs),
        )
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row {
        Text(label, color = AppColors.TextMuted, fontSize = 14.sp, modifier = Modifier.width(104.dp))
        Text(value, color = AppColors.Text, fontSize = 15.sp, lineHeight = 20.sp)
    }
}
