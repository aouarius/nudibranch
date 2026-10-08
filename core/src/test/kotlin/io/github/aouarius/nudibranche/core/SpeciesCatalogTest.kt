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
