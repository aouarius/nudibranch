package io.github.aouarius.nudibranche.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SpeciesCatalogTest {

    @Test
    fun appSpeciesListIsValid() {
        val text = File("../app/src/main/assets/species.json").readText()
        val species = SpeciesCatalog.parse(text)
        assertTrue(species.isNotEmpty())
        assertEquals(species.sortedBy { it.number }, species)
    }

    @Test
    fun everySpeciesHasAFreelyLicensedPhoto() {
        val text = File("../app/src/main/assets/species.json").readText()
        for (species in SpeciesCatalog.parse(text)) {
            val photo = species.photo
            assertTrue("${species.id} has no photo", photo != null)
            assertTrue("${species.id} photo file missing", File("../app/src/main/assets/species_photos/${photo!!.file}").isFile)
            assertTrue("${species.id} license ${photo.license}", photo.license in setOf("CC0", "CC BY", "CC BY-SA"))
            assertTrue(photo.source.startsWith("https://www.inaturalist.org/photos/"))
        }
    }

    @Test
    fun comicArtFilesAreNamedAfterSpecies() {
        val ids = SpeciesCatalog.parse(File("../app/src/main/assets/species.json").readText()).map { it.id }.toSet()
        val art = File("../app/src/main/assets/species_art").listFiles().orEmpty().map { it.nameWithoutExtension }
        assertTrue(art.isNotEmpty())
        assertTrue("unknown art files: ${art - ids}", ids.containsAll(art))
    }

    @Test
    fun photoCreditNamesAuthorLicenseAndSource() {
        assertEquals("© Yves Bas · CC BY · iNaturalist", SpeciesPhoto("a.jpg", "Yves Bas", "CC BY", "https://x").credit)
        assertEquals("CC0 · iNaturalist", SpeciesPhoto("a.jpg", null, "CC0", "https://x").credit)
    }

    @Test
    fun nameFallsBackToLatinNamePerLanguage() {
        val species = SpeciesCatalog.parse(
            """[{"id":"a","number":1,"latinName":"Hexabranchus sanguineus","englishName":"Spanish dancer",
            "family":"F","region":"INDOPAZIFIK","rarity":"LEGENDAER","maxSizeCm":40,"depthMinM":1,"depthMaxM":50,
            "food":"Schwämme","foodEn":"Sponges","habitat":"Riffe","habitatEn":"Reefs"}]""",
        ).single()
        assertEquals("Hexabranchus sanguineus", species.name(Language.DE))
        assertEquals("Spanish dancer", species.name(Language.EN))
        assertEquals("Sponges", species.food(Language.EN))
        assertEquals("Riffe", species.habitat(Language.DE))
    }

    @Test
    fun duplicateIdsAreRejected() {
        val entry = """{"id":"a","number":%d,"latinName":"L","family":"F","region":"MITTELMEER",
            "rarity":"HAEUFIG","maxSizeCm":4,"depthMinM":1,"depthMaxM":40,"food":"x","foodEn":"x","habitat":"y","habitatEn":"y"}"""
        assertThrows(IllegalArgumentException::class.java) {
            SpeciesCatalog.parse("[${entry.format(1)},${entry.format(2)}]")
        }
    }
}
