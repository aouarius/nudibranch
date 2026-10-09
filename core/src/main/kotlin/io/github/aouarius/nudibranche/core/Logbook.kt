package io.github.aouarius.nudibranche.core

import java.time.LocalDateTime

/** How many of a group of species the diver has found. */
data class Progress(val found: Int, val total: Int)

/** A find worth pointing out, for example the deepest one. */
data class NotableFind(val species: Species, val sighting: Sighting, val depthM: Int)

/** A dive site and how many photos were taken there. */
data class SiteCount(val name: String, val finds: Int)

/** Figures for the logbook page, all derived from the collection. */
data class LogbookStats(
    val species: Progress,
    val photos: Int,
    /** Days with at least one find; several dives on one day count once. */
    val diveDays: Int,
    val sites: Int,
    val favoriteSite: SiteCount?,
    val deepest: NotableFind?,
    val coldestWaterC: Int?,
    val warmestWaterC: Int?,
    val byRegion: Map<Region, Progress>,
    val byRarity: Map<Rarity, Progress>,
    /** Photos per year, oldest year first. */
    val findsPerYear: List<Pair<Int, Int>>,
)

object Logbook {

    fun stats(collection: CardCollection, catalog: List<Species>): LogbookStats {
        val byId = catalog.associateBy { it.id }
        val sightings = collection.sightings.filter { it.speciesId in byId }
        val found = sightings.mapTo(mutableSetOf()) { it.speciesId }

        fun progress(group: List<Species>) = Progress(group.count { it.id in found }, group.size)

        // "Cap de Creus" and "cap de creus" are one site; it keeps the spelling used most.
        val siteGroups = sightings.mapNotNull { siteName(it.dive) }.groupBy { it.lowercase() }
        val favorite = siteGroups.values
            .map { names -> SiteCount(names.groupingBy { it }.eachCount().maxBy { it.value }.key, names.size) }
            .sortedWith(compareByDescending<SiteCount> { it.finds }.thenBy { it.name })
            .firstOrNull()

        val deepest = sightings
            .filter { it.dive.depthM != null }
            .maxByOrNull { it.dive.depthM!! }
            ?.let { NotableFind(byId.getValue(it.speciesId), it, it.dive.depthM!!) }
        val temperatures = sightings.mapNotNull { it.dive.waterTempC }

        return LogbookStats(
            species = progress(catalog),
            photos = sightings.size,
            diveDays = sightings.mapNotNull { dayOf(it) }.toSet().size,
            sites = siteGroups.size,
            favoriteSite = favorite,
            deepest = deepest,
            coldestWaterC = temperatures.minOrNull(),
            warmestWaterC = temperatures.maxOrNull(),
            byRegion = catalog.groupBy { it.region }.mapValues { progress(it.value) },
            byRarity = Rarity.entries.associateWith { rarity -> progress(catalog.filter { it.rarity == rarity }) },
            findsPerYear = sightings
                .mapNotNull { dayOf(it)?.take(4)?.toIntOrNull() }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedBy { it.first },
        )
    }

    /** The site name the diver wrote, else the place. */
    private fun siteName(dive: DiveDetails): String? =
        (dive.site ?: dive.location)?.trim()?.takeIf { it.isNotEmpty() }

    private fun dayOf(sighting: Sighting): String? =
        runCatching { LocalDateTime.parse(sighting.takenAt).toLocalDate().toString() }.getOrNull()
}

enum class BadgeKind {
    FIRST_FIND,
    FIVE_SPECIES,
    TEN_SPECIES,
    ALL_SPECIES,
    REGION_COMPLETE,
    FIRST_LEGENDARY,
    ALL_LEGENDARY,
    FIVE_FAMILIES,
    THREE_REGIONS,
    TEN_DIVE_DAYS,
    DEEP_FIND,
    TWENTY_FIVE_PHOTOS,
}

/** An achievement; [region] is set for [BadgeKind.REGION_COMPLETE]. */
data class Badge(val kind: BadgeKind, val current: Int, val target: Int, val region: Region? = null) {
    val earned: Boolean get() = current >= target

    /** Stable key, also for telling which badges are new. */
    val key: String get() = if (region == null) kind.name else "${kind.name}:${region.name}"
}

object Badges {
    /** Depth in metres a find needs for [BadgeKind.DEEP_FIND]. */
    const val DEEP_FIND_M = 30

    fun all(collection: CardCollection, catalog: List<Species>): List<Badge> {
        val stats = Logbook.stats(collection, catalog)
        val found = catalog.filter { collection.isUnlocked(it.id) }
        val legendary = stats.byRarity.getValue(Rarity.LEGENDAER)
        val regionsWithFinds = found.mapTo(mutableSetOf()) { it.region }.size
        val families = found.mapTo(mutableSetOf()) { it.family }.size
        val total = stats.species.total

        // Worldwide species (the drifting blue dragon) live everywhere, so they get no region badge.
        val regionBadges = stats.byRegion
            .filterKeys { it != Region.WELTWEIT }
            .map { (region, progress) -> Badge(BadgeKind.REGION_COMPLETE, progress.found, progress.total, region) }

        return listOf(
            Badge(BadgeKind.FIRST_FIND, found.size, 1),
            Badge(BadgeKind.FIVE_SPECIES, found.size, 5),
            Badge(BadgeKind.TEN_SPECIES, found.size, 10),
        ) + regionBadges + listOf(
            Badge(BadgeKind.FIRST_LEGENDARY, legendary.found, 1),
            Badge(BadgeKind.ALL_LEGENDARY, legendary.found, legendary.total),
            Badge(BadgeKind.FIVE_FAMILIES, families, 5),
            Badge(BadgeKind.THREE_REGIONS, regionsWithFinds, 3),
            Badge(BadgeKind.TEN_DIVE_DAYS, stats.diveDays, 10),
            Badge(BadgeKind.DEEP_FIND, stats.deepest?.depthM ?: 0, DEEP_FIND_M),
            Badge(BadgeKind.TWENTY_FIVE_PHOTOS, stats.photos, 25),
            Badge(BadgeKind.ALL_SPECIES, found.size, total),
        ).filter { it.target > 0 }.map { it.copy(current = it.current.coerceAtMost(it.target)) }
    }

    /** Badges earned in [after] that were not earned in [before]. */
    fun newlyEarned(before: CardCollection, after: CardCollection, catalog: List<Species>): List<Badge> {
        val had = all(before, catalog).filter { it.earned }.mapTo(mutableSetOf()) { it.key }
        return all(after, catalog).filter { it.earned && it.key !in had }
    }
}
