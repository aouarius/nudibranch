package io.github.aouarius.nudibranche.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One accepted photo of a species. Dates are ISO-8601 local date-times. */
@Serializable
data class Sighting(
    val speciesId: String,
    val photoPath: String,
    val photoHash: String,
    val takenAt: String,
    val unlockedAt: String,
    val cameraModel: String? = null,
)

@Serializable
data class CardCollection(val sightings: List<Sighting> = emptyList()) {

    val usedHashes: Set<String> get() = sightings.mapTo(mutableSetOf()) { it.photoHash }

    fun isUnlocked(speciesId: String): Boolean = sightings.any { it.speciesId == speciesId }

    /** The first sighting unlocks the card and stays its photo. */
    fun firstSighting(speciesId: String): Sighting? = sightings.firstOrNull { it.speciesId == speciesId }

    fun sightingCount(speciesId: String): Int = sightings.count { it.speciesId == speciesId }

    fun unlockedCount(): Int = sightings.mapTo(mutableSetOf()) { it.speciesId }.size

    fun add(sighting: Sighting): CardCollection {
        require(sighting.photoHash !in usedHashes) { "Foto bereits verwendet" }
        return copy(sightings = sightings + sighting)
    }

    fun encode(): String = json.encodeToString(serializer(), this)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun decode(text: String): CardCollection = json.decodeFromString(serializer(), text)
    }
}
