package io.github.aouarius.nudibranche.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionTest {
    private fun species(id: String) = Species(
        id = id, number = 1, latinName = id, family = "F", region = Region.MITTELMEER, rarity = Rarity.HAEUFIG,
        maxSizeCm = 3, depthMinM = 1, depthMaxM = 30, food = "", foodEn = "", habitat = "", habitatEn = "",
    )

    private val catalog = listOf(species("a"), species("b"), species("c"), species("d"))

    @Test
    fun bestThreeComeFirst() {
        val result = Recognition.suggestions(floatArrayOf(0.1f, 0.6f, 0.05f, 0.25f), listOf("a", "b", "c", "d"), catalog)
        assertEquals(listOf("b", "d", "a"), result.map { it.species.id })
        assertEquals(0.6f, result.first().confidence)
    }

    @Test
    fun unlikelyAndUnknownSpeciesAreLeftOut() {
        val result = Recognition.suggestions(floatArrayOf(0.5f, 0.45f, 0.05f), listOf("gone", "a", "b"), catalog)
        assertEquals(listOf("a"), result.map { it.species.id })
    }

    @Test
    fun aFlatGuessGivesNoSuggestions() {
        val flat = FloatArray(20) { 0.05f }
        assertTrue(Recognition.suggestions(flat, List(20) { "a" }, catalog).isEmpty())
    }

    @Test
    fun labelsMustMatchTheModel() {
        assertThrows(IllegalArgumentException::class.java) {
            Recognition.suggestions(floatArrayOf(1f), listOf("a", "b"), catalog)
        }
    }
}
