package io.github.aouarius.nudibranche.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.aouarius.nudibranche.core.Badge
import io.github.aouarius.nudibranche.core.BadgeKind
import io.github.aouarius.nudibranche.core.Progress
import java.time.LocalDate

/** Dive statistics, badges and the backup, all on one page. */
@Composable
fun LogbookPage(viewModel: CatalogViewModel) {
    val strings = LocalStrings.current
    val stats = viewModel.stats
    val badges = viewModel.badges

    Column {
        PageHeader(
            title = strings.logbookTitle,
            subtitle = strings.logbookSubtitle(stats.diveDays, stats.photos),
            viewModel = viewModel,
        )
        LazyColumn(
            contentPadding = PaddingValues(start = Space.l, end = Space.l, top = Space.s, bottom = Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.s),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                    StatTile(strings.statSpecies, "${stats.species.found}/${stats.species.total}", Modifier.weight(1f))
                    StatTile(strings.statDiveDays, "${stats.diveDays}", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                    StatTile(strings.statPhotos, "${stats.photos}", Modifier.weight(1f))
                    StatTile(strings.statSites, "${stats.sites}", Modifier.weight(1f))
                }
            }
            stats.deepest?.let { deepest ->
                item {
                    StatTile(
                        strings.statDeepest,
                        "${deepest.depthM} m",
                        Modifier.fillMaxWidth(),
                        detail = deepest.species.name(strings.language),
                    )
                }
            }
            stats.favoriteSite?.let { site ->
                item { StatTile(strings.statFavoriteSite, site.name, Modifier.fillMaxWidth(), detail = strings.finds(site.finds)) }
            }
            val cold = stats.coldestWaterC
            val warm = stats.warmestWaterC
            if (cold != null && warm != null) {
                item {
                    StatTile(strings.statWater, if (cold == warm) "$cold °C" else "$cold – $warm °C", Modifier.fillMaxWidth())
                }
            }

            item { SectionTitle(strings.byRegion) }
            items(stats.byRegion.entries.toList(), key = { "region-${it.key}" }) { (region, progress) ->
                ProgressRow(region.label(strings.language), progress)
            }
            item { SectionTitle(strings.byRarity) }
            items(stats.byRarity.entries.filter { it.value.total > 0 }, key = { "rarity-${it.key}" }) { (rarity, progress) ->
                ProgressRow(rarity.label(strings.language), progress)
            }
            if (stats.findsPerYear.isNotEmpty()) {
                item { SectionTitle(strings.perYear) }
                val most = stats.findsPerYear.maxOf { it.second }
                items(stats.findsPerYear, key = { "year-${it.first}" }) { (year, count) ->
                    YearBar(year, count, most)
                }
            }

            item { SectionTitle("${strings.badgesTitle} · ${strings.badgesEarned(badges.count { it.earned }, badges.size)}") }
            items(badges.chunked(2), key = { row -> row.joinToString { it.key } }) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                    row.forEach { BadgeTile(it, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            item { BackupSection(viewModel) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = AppColors.Text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = Space.l, bottom = Space.xs),
    )
}

private val TileShape = RoundedCornerShape(14.dp)

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier, detail: String? = null) {
    Column(
        modifier
            .clip(TileShape)
            .background(AppColors.Surface)
            .padding(horizontal = Space.l, vertical = Space.m),
    ) {
        Text(label.uppercase(), color = AppColors.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(Space.xs))
        Text(
            value,
            color = AppColors.Text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (detail != null) {
            Text(detail, color = AppColors.TextMuted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ProgressRow(label: String, progress: Progress) {
    Column(Modifier.fillMaxWidth().padding(vertical = Space.xs)) {
        Row {
            Text(label, color = AppColors.Text, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text("${progress.found}/${progress.total}", color = AppColors.TextMuted, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(Space.xs))
        LinearProgressIndicator(
            progress = { if (progress.total == 0) 0f else progress.found / progress.total.toFloat() },
            color = AppColors.Accent,
            trackColor = AppColors.Border,
            drawStopIndicator = {},
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun YearBar(year: Int, count: Int, most: Int) {
    Row(Modifier.fillMaxWidth().height(28.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$year",
            color = if (year == LocalDate.now().year) AppColors.Accent else AppColors.TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp),
        )
        Box(Modifier.weight(1f).fillMaxHeight().padding(vertical = 5.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(count / most.toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.Accent),
            )
        }
        Text("$count", color = AppColors.Text, fontSize = 14.sp, modifier = Modifier.padding(start = Space.s))
    }
}

/** Picture for each badge; region badges share one. */
fun badgeEmoji(kind: BadgeKind): String = when (kind) {
    BadgeKind.FIRST_FIND -> "🐚"
    BadgeKind.FIVE_SPECIES -> "🖐️"
    BadgeKind.TEN_SPECIES -> "🔟"
    BadgeKind.ALL_SPECIES -> "🏆"
    BadgeKind.REGION_COMPLETE -> "🗺️"
    BadgeKind.FIRST_LEGENDARY -> "🌟"
    BadgeKind.ALL_LEGENDARY -> "👑"
    BadgeKind.FIVE_FAMILIES -> "🧬"
    BadgeKind.THREE_REGIONS -> "🌍"
    BadgeKind.TEN_DIVE_DAYS -> "🤿"
    BadgeKind.DEEP_FIND -> "⚓"
    BadgeKind.TWENTY_FIVE_PHOTOS -> "📸"
}

@Composable
fun BadgeTile(badge: Badge, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    Column(
        modifier
            .clip(TileShape)
            .background(if (badge.earned) AppColors.Accent.copy(alpha = 0.18f) else AppColors.Surface)
            .heightIn(min = 132.dp)
            .padding(Space.m),
    ) {
        Text(badgeEmoji(badge.kind), fontSize = 28.sp, modifier = Modifier.alpha(if (badge.earned) 1f else 0.35f))
        Spacer(Modifier.height(Space.xs))
        Text(
            strings.badgeTitle(badge),
            color = if (badge.earned) AppColors.Text else AppColors.TextMuted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 19.sp,
        )
        Text(strings.badgeGoal(badge), color = AppColors.TextMuted, fontSize = 12.sp, lineHeight = 16.sp)
        if (!badge.earned) {
            Spacer(Modifier.height(Space.s))
            LinearProgressIndicator(
                progress = { badge.current / badge.target.toFloat() },
                color = AppColors.Accent,
                trackColor = AppColors.Border,
                drawStopIndicator = {},
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)),
            )
        }
    }
}

@Composable
private fun BackupSection(viewModel: CatalogViewModel) {
    val strings = LocalStrings.current
    val save = rememberLauncherForActivityResult(CreateDocument("application/zip")) { uri ->
        if (uri != null) viewModel.exportBackup(uri)
    }
    val load = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) viewModel.restoreBackup(uri)
    }
    Column {
        SectionTitle(strings.backupTitle)
        Text(strings.backupText, color = AppColors.TextMuted, fontSize = 14.sp, lineHeight = 20.sp)
        Spacer(Modifier.height(Space.m))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.s), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { save.launch("nudidex-${LocalDate.now()}.zip") }, enabled = !viewModel.backupRunning) {
                Text(strings.backupSave)
            }
            OutlinedButton(
                onClick = { load.launch(arrayOf("application/zip", "application/octet-stream")) },
                enabled = !viewModel.backupRunning,
            ) {
                Text(strings.backupLoad)
            }
            if (viewModel.backupRunning) CircularProgressIndicator(Modifier.height(24.dp).width(24.dp), strokeWidth = 3.dp)
        }
    }

    viewModel.backupResult?.let { result ->
        AlertDialog(
            onDismissRequest = viewModel::dismissBackupResult,
            title = { Text(strings.backupTitle) },
            text = {
                Text(
                    when (result) {
                        BackupResult.Saved -> strings.backupSaved
                        is BackupResult.Restored -> strings.backupRestored(result.photos, result.cards)
                        BackupResult.Failed -> strings.backupFailed
                    },
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                )
            },
            confirmButton = { TextButton(onClick = viewModel::dismissBackupResult) { Text(strings.ok) } },
        )
    }
}
