package io.github.aouarius.nudibranche.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** What the diver noted about the dive. Every field is optional. */
@Serializable
data class DiveDetails(
    val site: String? = null,
    val location: String? = null,
    val depthM: Int? = null,
    val waterTempC: Int? = null,
    val notes: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    val isEmpty: Boolean
        get() = site == null && location == null && depthM == null && waterTempC == null && notes == null &&
            spot == null

    /** Where the find was made, when the diver set it. */
    val spot: LatLon?
        get() = if (latitude != null && longitude != null) LatLon(latitude, longitude) else null
}

/** One accepted photo of a species. Dates are ISO-8601 local date-times. */
@Serializable
data class Sighting(
    val speciesId: String,
    val photoPath: String,
    val photoHash: String,
    val takenAt: String,
    val unlockedAt: String,
    val cameraModel: String? = null,
    val dive: DiveDetails = DiveDetails(),
)

@Serializable
data class CardCollection(val sightings: List<Sighting> = emptyList()) {

    val usedHashes: Set<String> get() = sightings.mapTo(mutableSetOf()) { it.photoHash }

    fun isUnlocked(speciesId: String): Boolean = sightings.any { it.speciesId == speciesId }

    /** The first sighting unlocks the card and stays its photo. */
    fun firstSighting(speciesId: String): Sighting? = sightings.firstOrNull { it.speciesId == speciesId }

    fun sightingsOf(speciesId: String): List<Sighting> = sightings.filter { it.speciesId == speciesId }

    fun sightingCount(speciesId: String): Int = sightings.count { it.speciesId == speciesId }

    fun unlockedCount(): Int = sightings.mapTo(mutableSetOf()) { it.speciesId }.size

    fun add(sighting: Sighting): CardCollection {
        require(sighting.photoHash !in usedHashes) { "Foto bereits verwendet" }
        return copy(sightings = sightings + sighting)
    }

    /** Replaces the dive notes of the sighting with this photo. */
    fun updateDive(photoHash: String, dive: DiveDetails): CardCollection =
        copy(sightings = sightings.map { if (it.photoHash == photoHash) it.copy(dive = dive) else it })

    fun encode(): String = json.encodeToString(serializer(), this)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun decode(text: String): CardCollection = json.decodeFromString(serializer(), text)
    }
}
