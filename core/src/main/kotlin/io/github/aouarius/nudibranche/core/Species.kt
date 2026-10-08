package io.github.aouarius.nudibranche.core

import kotlinx.serialization.Serializable

@Serializable
enum class Region(val displayName: String) {
    MITTELMEER("Mittelmeer"),
    NORDOSTATLANTIK("Nordostatlantik"),
    INDOPAZIFIK("Indopazifik"),
    WELTWEIT("Weltweit"),
}

@Serializable
enum class Rarity(val displayName: String) {
    HAEUFIG("Häufig"),
    SELTEN("Selten"),
    LEGENDAER("Legendär"),
}

@Serializable
data class Species(
    val id: String,
    val number: Int,
    val latinName: String,
    val germanName: String? = null,
    val family: String,
    val region: Region,
    val rarity: Rarity,
    val maxSizeCm: Int,
    val depthMinM: Int,
    val depthMaxM: Int,
    val food: String,
    val habitat: String,
) {
    /** Name shown on the card: the German name when one is established, else the Latin name. */
    val displayName: String get() = germanName ?: latinName
}
