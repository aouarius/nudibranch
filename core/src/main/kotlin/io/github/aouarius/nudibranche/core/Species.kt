package io.github.aouarius.nudibranche.core

import kotlinx.serialization.Serializable

enum class Language { DE, EN }

@Serializable
enum class Region(private val german: String, private val english: String) {
    MITTELMEER("Mittelmeer", "Mediterranean"),
    NORDOSTATLANTIK("Nordostatlantik", "Northeast Atlantic"),
    ROTES_MEER("Rotes Meer", "Red Sea"),
    INDOPAZIFIK("Indopazifik", "Indo-Pacific"),
    KARIBIK("Karibik", "Caribbean"),
    WELTWEIT("Weltweit", "Worldwide"),
    ;

    fun label(language: Language): String = if (language == Language.DE) german else english
}

@Serializable
enum class Rarity(private val german: String, private val english: String) {
    HAEUFIG("Häufig", "Common"),
    SELTEN("Selten", "Rare"),
    LEGENDAER("Legendär", "Legendary"),
    ;

    fun label(language: Language): String = if (language == Language.DE) german else english
}

@Serializable
data class Species(
    val id: String,
    val number: Int,
    val latinName: String,
    val germanName: String? = null,
    val englishName: String? = null,
    val family: String,
    val region: Region,
    val rarity: Rarity,
    val maxSizeCm: Int,
    val depthMinM: Int,
    val depthMaxM: Int,
    val food: String,
    val foodEn: String,
    val habitat: String,
    val habitatEn: String,
    val photo: SpeciesPhoto? = null,
) {
    /** Name shown on the card: the common name when one is established, else the Latin name. */
    fun name(language: Language): String =
        (if (language == Language.DE) germanName else englishName) ?: latinName

    fun food(language: Language): String = if (language == Language.DE) food else foodEn

    fun habitat(language: Language): String = if (language == Language.DE) habitat else habitatEn
}

/** Reference photo bundled in assets/species_photos, with the credit its license asks for. */
@Serializable
data class SpeciesPhoto(
    val file: String,
    val author: String? = null,
    val license: String,
    val source: String,
) {
    /** For example "© Yves Bas · CC BY · iNaturalist". */
    val credit: String
        get() = listOfNotNull(author?.let { "© $it" }, license, "iNaturalist").joinToString(" · ")
}
