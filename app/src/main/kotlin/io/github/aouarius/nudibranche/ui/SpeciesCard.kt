package io.github.aouarius.nudibranche.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.aouarius.nudibranche.core.Rarity
import io.github.aouarius.nudibranche.core.Sighting
import io.github.aouarius.nudibranche.core.Species
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Type sizes and gaps for the small grid tile and the large card. */
private data class CardMetrics(
    val frame: Dp,
    val inner: Dp,
    val gap: Dp,
    val name: TextUnit,
    val latin: TextUnit,
    val tag: TextUnit,
    val stat: TextUnit,
    val statLabel: TextUnit,
    /** Share of a stat row taken by its label, so long labels never run into the value. */
    val labelWeight: Float,
    val statGap: Dp,
    val footer: TextUnit,
)

private val Compact = CardMetrics(
    frame = 5.dp, inner = 8.dp, gap = 6.dp,
    name = 13.sp, latin = 10.sp, tag = 9.sp, stat = 10.sp, statLabel = 9.sp, labelWeight = 0.4f,
    statGap = 1.5.dp, footer = 9.sp,
)

private val Large = CardMetrics(
    frame = 8.dp, inner = 14.dp, gap = 12.dp,
    name = 21.sp, latin = 14.sp, tag = 11.sp, stat = 15.sp, statLabel = 12.sp, labelWeight = 0.36f,
    statGap = 4.dp, footer = 12.sp,
)

/**
 * A collectible card. Without a sighting it shows the locked side: only the picture and
 * the region where the species lives.
 * [compact] is the grid tile: name, picture, rarity, size and depth; the large card shows everything.
 */
@Composable
fun SpeciesCard(
    species: Species,
    sighting: Sighting?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val strings = LocalStrings.current
    val language = strings.language
    val m = if (compact) Compact else Large
    val unlocked = sighting != null
    val innerShape = RoundedCornerShape(if (compact) 7.dp else 9.dp)
    val ink = if (unlocked) CardColors.Ink else CardColors.LockedText

    Box(
        modifier
            .clip(RoundedCornerShape(if (compact) 11.dp else 14.dp))
            .background(if (unlocked) CardColors.frame(species.rarity) else SolidColor(CardColors.LockedFrame))
            .padding(m.frame),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(innerShape)
                .background(if (unlocked) CardColors.Paper else CardColors.LockedPaper)
                .padding(m.inner),
        ) {
            if (unlocked) {
                Text(
                    text = species.name(language),
                    color = ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = m.name,
                    lineHeight = m.name * 1.15f,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = species.latinName,
                    color = CardColors.InkMuted,
                    fontStyle = FontStyle.Italic,
                    fontSize = m.latin,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(m.gap))
            }
            CardArt(species, sighting)
            Spacer(Modifier.height(m.gap))
            RarityTag(species, unlocked, m.tag)
            Spacer(Modifier.height(m.gap * 0.6f))
            when {
                sighting == null -> Unit
                compact -> {
                    StatRow(strings.size, strings.upTo(species.maxSizeCm), m, ink)
                    StatRow(strings.depth, depthText(species, strings), m, ink)
                }
                else -> {
                    StatRow(strings.size, strings.upTo(species.maxSizeCm), m, ink)
                    StatRow(strings.depth, depthText(species, strings), m, ink)
                    StatRow(strings.food, species.food(language), m, ink)
                    StatRow(strings.habitat, species.habitat(language), m, ink)
                }
            }
            HorizontalDivider(
                Modifier.padding(top = m.gap * 0.7f, bottom = m.gap * 0.5f),
                color = if (unlocked) CardColors.Divider else CardColors.LockedFrame,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (sighting != null) "📷 ${formatCardDate(sighting.takenAt, strings).orEmpty()}" else strings.diveToFind,
                    color = if (unlocked) CardColors.Label else CardColors.LockedText,
                    fontSize = m.footer,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "#%03d".format(species.number),
                    color = if (unlocked) CardColors.Label else CardColors.LockedText,
                    fontSize = m.footer,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (unlocked && species.rarity == Rarity.LEGENDAER) {
            HoloShimmer(Modifier.matchParentSize())
        }
    }
}

@Composable
private fun CardArt(species: Species, sighting: Sighting?) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(shape)
            .background(if (sighting != null) CardColors.ArtBackground else Color(0xFF15181D))
            .border(2.dp, if (sighting != null) CardColors.ArtBorder else CardColors.LockedFrame, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (sighting != null) {
            AsyncImage(
                model = File(LocalContext.current.filesDir, sighting.photoPath),
                contentDescription = species.latinName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val art = comicArtUri(species)
            if (art != null) {
                AsyncImage(
                    model = art,
                    contentDescription = species.latinName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text("?", color = Color(0xFF4A505B), fontSize = 40.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

/** Comic art files in assets/species_art, named after the species id (e.g. glaucus-atlanticus.jpg). */
private var comicArtFiles: Map<String, String>? = null

/** Comic illustration shown on a card until the diver finds the species, if there is one. */
@Composable
private fun comicArtUri(species: Species): String? {
    val context = LocalContext.current
    val files = comicArtFiles ?: (context.assets.list("species_art").orEmpty()
        .associateBy { it.substringBeforeLast('.') }
        .also { comicArtFiles = it })
    return files[species.id]?.let { "file:///android_asset/species_art/$it" }
}

@Composable
private fun RarityTag(species: Species, unlocked: Boolean, size: TextUnit) {
    val language = LocalStrings.current.language
    val label = if (unlocked) {
        listOfNotNull(
            species.rarity.label(language).uppercase(),
            if (species.rarity == Rarity.LEGENDAER) "HOLO" else null,
            species.region.label(language),
        ).joinToString(" · ")
    } else {
        "📍 ${species.region.label(language)}"
    }
    Text(
        text = label,
        color = if (unlocked) Color.White else CardColors.LockedText,
        fontSize = size,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.3.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (unlocked) CardColors.tag(species.rarity) else SolidColor(CardColors.LockedFrame))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun StatRow(label: String, value: String, m: CardMetrics, ink: Color) {
    Row(Modifier.padding(vertical = m.statGap)) {
        Text(
            label.uppercase(),
            color = if (ink == CardColors.Ink) CardColors.Label else ink,
            fontSize = m.statLabel,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(m.labelWeight)
                .alignByBaseline(),
        )
        Text(
            value,
            color = ink,
            fontSize = m.stat,
            fontWeight = FontWeight.Medium,
            lineHeight = m.stat * 1.3f,
            modifier = Modifier
                .weight(1f - m.labelWeight)
                .alignByBaseline(),
        )
    }
}

@Composable
private fun HoloShimmer(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "holo")
    val progress by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 3200, easing = LinearEasing)),
        label = "holoProgress",
    )
    Canvas(modifier) {
        val x = progress * size.width
        drawRect(
            Brush.linearGradient(
                colors = listOf(Color.Transparent, Color(0x55FFFFFF), Color.Transparent),
                start = Offset(x - size.width * 0.3f, 0f),
                end = Offset(x + size.width * 0.3f, size.height),
            ),
        )
    }
}

internal fun depthText(species: Species, strings: Strings): String =
    if (species.depthMaxM == 0) strings.surface else "${species.depthMinM}–${species.depthMaxM} m"

fun formatCardDate(isoDateTime: String, strings: Strings): String? =
    runCatching {
        LocalDateTime.parse(isoDateTime).format(DateTimeFormatter.ofPattern(strings.datePattern, strings.locale))
    }.getOrNull()
