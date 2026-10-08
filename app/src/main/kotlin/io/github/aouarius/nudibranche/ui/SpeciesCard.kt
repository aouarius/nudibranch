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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.aouarius.nudibranche.core.Rarity
import io.github.aouarius.nudibranche.core.Sighting
import io.github.aouarius.nudibranche.core.Species
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** A collectible card. Without a sighting it shows the locked side with only a habitat hint. */
@Composable
fun SpeciesCard(species: Species, sighting: Sighting?, modifier: Modifier = Modifier) {
    val unlocked = sighting != null
    val innerShape = RoundedCornerShape(8.dp)
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (unlocked) CardColors.frame(species.rarity) else SolidColor(CardColors.Locked))
            .padding(6.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(innerShape)
                .background(if (unlocked) CardColors.CardInner else CardColors.LockedInner)
                .border(1.dp, CardColors.CardBorder, innerShape)
                .padding(8.dp),
        ) {
            Text(
                text = if (unlocked) species.displayName else "???",
                color = if (unlocked) CardColors.Text else CardColors.TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (unlocked) species.latinName else "Noch nicht entdeckt",
                color = CardColors.TextMuted,
                fontStyle = FontStyle.Italic,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            CardArt(species, sighting)
            Spacer(Modifier.height(6.dp))
            RarityTag(species, unlocked)
            Spacer(Modifier.height(4.dp))
            if (sighting != null) {
                StatRow("Größe", "bis ${species.maxSizeCm} cm")
                StatRow("Tiefe", depthText(species))
                StatRow("Nahrung", species.food)
                StatRow("Lebensraum", species.habitat)
            } else {
                StatRow("Lebensraum", species.habitat)
            }
            HorizontalDivider(Modifier.padding(top = 6.dp, bottom = 4.dp), color = CardColors.CardBorder)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (sighting != null) "📷 ${formatDate(sighting.takenAt)}" else "Tauche, um sie zu finden",
                    color = CardColors.Label,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text("#%03d".format(species.number), color = CardColors.Label, fontSize = 9.sp)
            }
        }
        if (unlocked && species.rarity == Rarity.LEGENDAER) {
            HoloShimmer(Modifier.matchParentSize())
        }
    }
}

@Composable
private fun CardArt(species: Species, sighting: Sighting?) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .background(Color(0xFF0D0F13))
            .border(1.dp, Color(0xFF555555)),
        contentAlignment = Alignment.Center,
    ) {
        if (sighting != null) {
            AsyncImage(
                model = File(LocalContext.current.filesDir, sighting.photoPath),
                contentDescription = species.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text("?", color = Color(0xFF3A404B), fontSize = 40.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RarityTag(species: Species, unlocked: Boolean) {
    val label = if (unlocked) {
        listOfNotNull(
            species.rarity.displayName.uppercase(),
            if (species.rarity == Rarity.LEGENDAER) "HOLO" else null,
            species.region.displayName,
        ).joinToString(" · ")
    } else {
        "? · ${species.region.displayName}"
    }
    Text(
        text = label,
        color = if (unlocked) Color(0xFF111111) else CardColors.TextMuted,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (unlocked) CardColors.tag(species.rarity) else SolidColor(Color(0xFF3A404B)))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 1.dp)) {
        Text(label, color = CardColors.Label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(66.dp))
        Text(value, color = Color(0xFFC8CED8), fontSize = 10.sp)
    }
}

@Composable
private fun HoloShimmer(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "holo")
    val progress by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 4000, easing = LinearEasing)),
        label = "holoProgress",
    )
    Canvas(modifier) {
        val x = progress * size.width
        drawRect(
            Brush.linearGradient(
                colors = listOf(Color.Transparent, Color(0x38C8AAFF), Color.Transparent),
                start = Offset(x - size.width * 0.3f, 0f),
                end = Offset(x + size.width * 0.3f, size.height),
            ),
        )
    }
}

private fun depthText(species: Species): String =
    if (species.depthMaxM == 0) "Oberfläche" else "${species.depthMinM}–${species.depthMaxM} m"

private val CARD_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun formatDate(isoDateTime: String): String =
    runCatching { LocalDateTime.parse(isoDateTime).format(CARD_DATE) }.getOrDefault("")
