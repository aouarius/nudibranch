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
}
