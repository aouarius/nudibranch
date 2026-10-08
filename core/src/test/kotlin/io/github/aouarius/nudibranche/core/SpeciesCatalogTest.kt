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
    fun displayNameFallsBackToLatinName() {
        val species = SpeciesCatalog.parse(
            """[{"id":"a","number":1,"latinName":"Cratena peregrina","family":"F","region":"MITTELMEER",
            "rarity":"HAEUFIG","maxSizeCm":4,"depthMinM":1,"depthMaxM":40,"food":"x","habitat":"y"}]""",
        )
        assertEquals("Cratena peregrina", species.single().displayName)
    }

    @Test
    fun duplicateIdsAreRejected() {
        val entry = """{"id":"a","number":%d,"latinName":"L","family":"F","region":"MITTELMEER",
            "rarity":"HAEUFIG","maxSizeCm":4,"depthMinM":1,"depthMaxM":40,"food":"x","habitat":"y"}"""
        assertThrows(IllegalArgumentException::class.java) {
            SpeciesCatalog.parse("[${entry.format(1)},${entry.format(2)}]")
        }
    }
}
