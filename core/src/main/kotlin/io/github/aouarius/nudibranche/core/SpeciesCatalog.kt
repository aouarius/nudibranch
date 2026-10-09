package io.github.aouarius.nudibranche.core

import kotlinx.serialization.json.Json

object SpeciesCatalog {
    private val json = Json { ignoreUnknownKeys = true }

    /** Parses the species list and checks that ids and card numbers are unique. */
    fun parse(text: String): List<Species> {
        val species = json.decodeFromString<List<Species>>(text)
        val duplicateIds = species.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicateIds.isEmpty()) { "Doppelte Art-IDs: $duplicateIds" }
        val duplicateNumbers = species.groupBy { it.number }.filterValues { it.size > 1 }.keys
        require(duplicateNumbers.isEmpty()) { "Doppelte Kartennummern: $duplicateNumbers" }
        species.forEach {
            require(it.depthMinM <= it.depthMaxM) { "Tiefe von ${it.id} ist ungültig" }
        }
        return species.sortedBy { it.number }
    }
}
