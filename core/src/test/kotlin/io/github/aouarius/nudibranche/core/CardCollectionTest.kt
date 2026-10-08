package io.github.aouarius.nudibranche.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CardCollectionTest {
    private fun sighting(species: String, hash: String) = Sighting(
        speciesId = species,
        photoPath = "photos/$hash.jpg",
        photoHash = hash,
        takenAt = "2026-08-12T10:30",
        unlockedAt = "2026-10-08T12:00",
        cameraModel = "TG-7",
    )

    @Test
    fun firstSightingUnlocksAndStaysTheCardPhoto() {
        val collection = CardCollection()
            .add(sighting("flabellina-affinis", "h1"))
            .add(sighting("flabellina-affinis", "h2"))
            .add(sighting("felimare-picta", "h3"))

        assertTrue(collection.isUnlocked("flabellina-affinis"))
        assertFalse(collection.isUnlocked("glaucus-atlanticus"))
        assertEquals("h1", collection.firstSighting("flabellina-affinis")?.photoHash)
        assertEquals(2, collection.sightingCount("flabellina-affinis"))
        assertEquals(2, collection.unlockedCount())
    }

    @Test
    fun samePhotoCannotBeAddedTwice() {
        val collection = CardCollection().add(sighting("a", "h1"))
        assertThrows(IllegalArgumentException::class.java) { collection.add(sighting("b", "h1")) }
    }

    @Test
    fun collectionSurvivesSaveAndLoad() {
        val collection = CardCollection().add(sighting("a", "h1"))
        assertEquals(collection, CardCollection.decode(collection.encode()))
    }

    @Test
    fun diveDetailsAreSavedAndOldFilesStillLoad() {
        val dive = DiveDetails(site = "Grotta Azzurra", location = "Elba", depthM = 18, waterTempC = 21, notes = "Auf Schwamm")
        val collection = CardCollection().add(sighting("a", "h1").copy(dive = dive))
        assertEquals(dive, CardCollection.decode(collection.encode()).sightings.single().dive)

        val oldFile = """{"sightings":[{"speciesId":"a","photoPath":"p","photoHash":"h","takenAt":"t","unlockedAt":"u"}]}"""
        assertTrue(CardCollection.decode(oldFile).sightings.single().dive.isEmpty)
    }

    @Test
    fun diveNotesCanBeEditedLater() {
        val collection = CardCollection().add(sighting("a", "h1")).add(sighting("a", "h2"))
        val updated = collection.updateDive("h2", DiveDetails(site = "Punta Fetovaia"))
        assertEquals("Punta Fetovaia", updated.sightingsOf("a")[1].dive.site)
        assertTrue(updated.sightingsOf("a")[0].dive.isEmpty)
    }

    @Test
    fun chosenPhotoBecomesTheCardFront() {
        val collection = CardCollection()
            .add(sighting("a", "h1"))
            .add(sighting("a", "h2"))
        assertEquals("h1", collection.coverOf("a")?.photoHash)
        val picked = collection.setCover("h2")
        assertEquals("h2", picked.coverOf("a")?.photoHash)
        assertEquals(picked, CardCollection.decode(picked.encode()))
    }

    @Test
    fun removingPhotosLocksTheCardAgain() {
        val collection = CardCollection()
            .add(sighting("a", "h1"))
            .add(sighting("a", "h2"))
            .setCover("h2")

        val one = collection.remove("h2")
        assertEquals("h1", one.coverOf("a")?.photoHash)
        assertTrue(one.isUnlocked("a"))

        val none = one.remove("h1")
        assertFalse(none.isUnlocked("a"))
        assertEquals(null, none.coverOf("a"))
        assertTrue(none.covers.isEmpty())
        // The photo can be added again after it was removed.
        assertTrue(none.add(sighting("a", "h1")).isUnlocked("a"))
    }

    @Test
    fun oldCollectionsWithoutCoversStillLoad() {
        val old = CardCollection.decode("""{"sightings":[]}""")
        assertTrue(old.covers.isEmpty())
    }
}
