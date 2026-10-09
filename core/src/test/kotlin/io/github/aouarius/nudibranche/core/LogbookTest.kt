package io.github.aouarius.nudibranche.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LogbookTest {
    private fun species(id: String, region: Region, rarity: Rarity = Rarity.HAEUFIG, family: String = "F-$id") = Species(
        id = id, number = 1, latinName = id, family = family, region = region, rarity = rarity,
        maxSizeCm = 3, depthMinM = 1, depthMaxM = 30, food = "", foodEn = "", habitat = "", habitatEn = "",
    )

    private val catalog = listOf(
        species("a", Region.MITTELMEER),
        species("b", Region.MITTELMEER, Rarity.SELTEN),
        species("c", Region.INDOPAZIFIK, Rarity.LEGENDAER),
        species("d", Region.WELTWEIT, Rarity.LEGENDAER),
    )

    private fun sighting(species: String, hash: String, takenAt: String, dive: DiveDetails = DiveDetails()) =
        Sighting(species, "photos/$hash.jpg", hash, takenAt, takenAt, dive = dive)

    private val collection = CardCollection()
        .add(sighting("a", "h1", "2025-07-01T10:00", DiveDetails(site = "Cap de Creus", depthM = 18, waterTempC = 21)))
        .add(sighting("a", "h2", "2025-07-01T15:00", DiveDetails(site = "cap de creus", depthM = 34, waterTempC = 19)))
        .add(sighting("b", "h3", "2026-05-03T09:00", DiveDetails(site = "Medes")))
        .add(sighting("c", "h4", "2026-09-20T11:00"))

    @Test
    fun statsSumUpTheCollection() {
        val stats = Logbook.stats(collection, catalog)

        assertEquals(Progress(3, 4), stats.species)
        assertEquals(4, stats.photos)
        assertEquals(3, stats.diveDays)
        assertEquals(2, stats.sites)
        assertEquals(SiteCount("Cap de Creus", 2), stats.favoriteSite)
        assertEquals(34, stats.deepest?.depthM)
        assertEquals("a", stats.deepest?.species?.id)
        assertEquals(19, stats.coldestWaterC)
        assertEquals(21, stats.warmestWaterC)
        assertEquals(Progress(2, 2), stats.byRegion[Region.MITTELMEER])
        assertEquals(Progress(1, 2), stats.byRarity[Rarity.LEGENDAER])
        assertEquals(listOf(2025 to 2, 2026 to 2), stats.findsPerYear)
    }

    @Test
    fun emptyCollectionHasNoHighlights() {
        val stats = Logbook.stats(CardCollection(), catalog)
        assertEquals(Progress(0, 4), stats.species)
        assertNull(stats.favoriteSite)
        assertNull(stats.deepest)
        assertNull(stats.coldestWaterC)
        assertTrue(Badges.all(CardCollection(), catalog).none { it.earned })
    }

    @Test
    fun badgesFollowTheFinds() {
        val badges = Badges.all(collection, catalog).associateBy { it.key }

        assertTrue(badges.getValue("FIRST_FIND").earned)
        assertTrue(badges.getValue("REGION_COMPLETE:MITTELMEER").earned)
        assertTrue(badges.getValue("REGION_COMPLETE:INDOPAZIFIK").earned)
        assertFalse("worldwide species get no region badge", "REGION_COMPLETE:WELTWEIT" in badges)
        assertTrue(badges.getValue("FIRST_LEGENDARY").earned)
        assertFalse(badges.getValue("ALL_LEGENDARY").earned)
        assertTrue(badges.getValue("DEEP_FIND").earned)
        assertEquals(3, badges.getValue("FIVE_SPECIES").current)
        assertEquals(2, badges.getValue("THREE_REGIONS").current)
    }

    @Test
    fun newlyEarnedNamesOnlyTheNewBadges() {
        val before = CardCollection().add(sighting("a", "h1", "2025-07-01T10:00"))
        val after = before.add(sighting("c", "h4", "2026-09-20T11:00"))

        val fresh = Badges.newlyEarned(before, after, catalog).map { it.key }

        assertEquals(listOf("REGION_COMPLETE:INDOPAZIFIK", "FIRST_LEGENDARY"), fresh)
    }
}
