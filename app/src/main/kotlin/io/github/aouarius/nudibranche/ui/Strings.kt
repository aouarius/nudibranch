package io.github.aouarius.nudibranche.ui

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.aouarius.nudibranche.core.Language
import io.github.aouarius.nudibranche.core.PhotoProblem
import java.util.Locale

/** Every text the app shows, in German and English. */
interface Strings {
    val language: Language
    val locale: Locale
    val datePattern: String

    fun cardsFound(found: Int, total: Int): String
    val findSpotsButton: String
    val addPhoto: String
    val allRegions: String
    val showAll: String
    val showFound: String
    val noCardsFoundYet: String

    val size: String
    fun upTo(cm: Int): String
    val depth: String
    val surface: String
    val food: String
    val habitat: String
    val diveToFind: String

    val checkingPhoto: String
    val photoNotAccepted: String
    fun problem(problem: PhotoProblem): String
    val error: String
    val photoUnreadable: String
    val ok: String
    val newCardUnlocked: String
    val sightingSaved: String
    val great: String
    val whichSpecies: String
    val recognitionLater: String
    val search: String
    val cancel: String
    val skip: String
    val save: String

    val yourCard: String
    val writeOnBack: String
    val tapToTurnBack: String
    val tapForLogbook: String
    val logbook: String
    val nothingNoted: String
    val diveSite: String
    val place: String
    val findSpot: String
    val setOnGlobe: String
    val depthMeters: String
    val waterCelsius: String
    val notes: String

    val findSpotsTitle: String
    val noFindSpots: String
    fun findsHint(count: Int): String
    val close: String
    val setFindSpot: String
    val turnAndTap: String
    val noSpotChosen: String
    val remove: String
    val useSpot: String
    val north: String
    val south: String
    val east: String
    val west: String

    val collectionTab: String
    val libraryTab: String
    val libraryTitle: String
    fun libraryCount(total: Int, found: Int): String
    val found: String
    val notFoundYet: String
    val family: String
    val region: String
    val rarity: String
    fun firstFound(date: String): String

    fun yourPhotos(count: Int): String
    val addPhotoHere: String
    val tapToEnlarge: String
    val cardPhoto: String
    val useAsCardPhoto: String
    val delete: String
    val deletePhotoTitle: String
    fun deletePhotoText(lastPhoto: Boolean): String
}

object GermanStrings : Strings {
    override val language = Language.DE
    override val locale: Locale = Locale.GERMAN
    override val datePattern = "dd.MM.yyyy"

    override fun cardsFound(found: Int, total: Int) = "$found von $total Karten gefunden"
    override val findSpotsButton = "🌍 Fundorte"
    override val addPhoto = "Foto hinzufügen"
    override val allRegions = "Alle"
    override val showAll = "Alle"
    override val showFound = "Gefundene"
    override val noCardsFoundYet = "Hier erscheinen deine gefundenen Karten. Tippe auf „Foto hinzufügen“, um die erste freizuschalten."

    override val size = "Größe"
    override fun upTo(cm: Int) = "bis $cm cm"
    override val depth = "Tiefe"
    override val surface = "Oberfläche"
    override val food = "Nahrung"
    override val habitat = "Lebensraum"
    override val diveToFind = "Tauche, um sie zu finden"

    override val checkingPhoto = "Foto wird geprüft …"
    override val photoNotAccepted = "Foto nicht akzeptiert"
    override fun problem(problem: PhotoProblem) = when (problem) {
        PhotoProblem.NO_DATE -> "Das Foto hat kein Aufnahmedatum. Bitte das Originalfoto aus der Kamera verwenden."
        PhotoProblem.FUTURE_DATE -> "Das Aufnahmedatum liegt in der Zukunft."
        PhotoProblem.NO_CAMERA -> "Im Foto ist keine Kamera hinterlegt. Screenshots und bearbeitete Bilder zählen nicht."
        PhotoProblem.SCREENSHOT -> "Das Bild ist ein Screenshot."
        PhotoProblem.ALREADY_USED -> "Dieses Foto wurde schon für eine Karte verwendet."
    }
    override val error = "Fehler"
    override val photoUnreadable = "Das Foto konnte nicht gelesen werden."
    override val ok = "OK"
    override val newCardUnlocked = "Neue Karte freigeschaltet!"
    override val sightingSaved = "Weitere Sichtung gespeichert"
    override val great = "Super"
    override val whichSpecies = "Welche Art ist auf dem Foto?"
    override val recognitionLater =
        "Die automatische Erkennung kommt in einer späteren Version. Bis dahin wählst du die Art selbst."
    override val search = "Suchen"
    override val cancel = "Abbrechen"
    override val skip = "Überspringen"
    override val save = "Speichern"

    override val yourCard = "Deine Karte"
    override val writeOnBack = "Schreib deinen Tauchgang auf die Rückseite"
    override val tapToTurnBack = "Tippen, um die Karte umzudrehen"
    override val tapForLogbook = "Tippen für das Logbuch auf der Rückseite"
    override val logbook = "LOGBUCH"
    override val nothingNoted = "Noch nichts notiert. Tippe auf ✎."
    override val diveSite = "Tauchplatz"
    override val place = "Ort / Land"
    override val findSpot = "Fundort"
    override val setOnGlobe = "Auf dem Globus setzen"
    override val depthMeters = "Tiefe (m)"
    override val waterCelsius = "Wasser (°C)"
    override val notes = "Notizen"

    override val findSpotsTitle = "Fundorte"
    override val noFindSpots = "Noch keine Fundorte. Setze den Fundort auf der Rückseite einer Karte."
    override fun findsHint(count: Int) = "$count ${if (count == 1) "Fund" else "Funde"} · ziehen zum Drehen, zwei Finger zum Zoomen"
    override val close = "Schließen"
    override val setFindSpot = "Fundort setzen"
    override val turnAndTap = "Globus drehen, zoomen und auf die Fundstelle tippen"
    override val noSpotChosen = "Noch kein Fundort gewählt"
    override val remove = "Entfernen"
    override val useSpot = "Übernehmen"
    override val north = "N"
    override val south = "S"
    override val east = "O"
    override val west = "W"

    override val collectionTab = "Sammlung"
    override val libraryTab = "Bibliothek"
    override val libraryTitle = "Bibliothek"
    override fun libraryCount(total: Int, found: Int) = "$total Arten · $found gefunden"
    override val found = "Gefunden"
    override val notFoundYet = "Noch nicht gefunden"
    override val family = "Familie"
    override val region = "Region"
    override val rarity = "Seltenheit"
    override fun firstFound(date: String) = "Zum ersten Mal gefunden am $date"

    override fun yourPhotos(count: Int) = if (count == 1) "Dein Foto" else "Deine Fotos ($count)"
    override val addPhotoHere = "Foto hinzufügen"
    override val tapToEnlarge = "Antippen zum Vergrößern"
    override val cardPhoto = "Kartenfoto"
    override val useAsCardPhoto = "Als Kartenfoto"
    override val delete = "Löschen"
    override val deletePhotoTitle = "Foto löschen?"
    override fun deletePhotoText(lastPhoto: Boolean) =
        "Der Logbuch-Eintrag zu diesem Foto wird mitgelöscht." +
            if (lastPhoto) " Es ist dein letztes Foto dieser Art, die Karte wird danach wieder gesperrt." else ""
}

object EnglishStrings : Strings {
    override val language = Language.EN
    override val locale: Locale = Locale.ENGLISH
    override val datePattern = "d MMM yyyy"

    override fun cardsFound(found: Int, total: Int) = "$found of $total cards found"
    override val findSpotsButton = "🌍 Find spots"
    override val addPhoto = "Add photo"
    override val allRegions = "All"
    override val showAll = "All"
    override val showFound = "Found"
    override val noCardsFoundYet = "Your found cards appear here. Tap “Add photo” to unlock the first one."

    override val size = "Size"
    override fun upTo(cm: Int) = "up to $cm cm"
    override val depth = "Depth"
    override val surface = "Surface"
    override val food = "Food"
    override val habitat = "Habitat"
    override val diveToFind = "Dive to find it"

    override val checkingPhoto = "Checking photo …"
    override val photoNotAccepted = "Photo not accepted"
    override fun problem(problem: PhotoProblem) = when (problem) {
        PhotoProblem.NO_DATE -> "The photo has no capture date. Please use the original photo from the camera."
        PhotoProblem.FUTURE_DATE -> "The capture date is in the future."
        PhotoProblem.NO_CAMERA -> "The photo has no camera information. Screenshots and edited images don't count."
        PhotoProblem.SCREENSHOT -> "The image is a screenshot."
        PhotoProblem.ALREADY_USED -> "This photo has already been used for a card."
    }
    override val error = "Error"
    override val photoUnreadable = "The photo could not be read."
    override val ok = "OK"
    override val newCardUnlocked = "New card unlocked!"
    override val sightingSaved = "Sighting saved"
    override val great = "Great"
    override val whichSpecies = "Which species is in the photo?"
    override val recognitionLater =
        "Automatic recognition is coming in a later version. Until then, you choose the species yourself."
    override val search = "Search"
    override val cancel = "Cancel"
    override val skip = "Skip"
    override val save = "Save"

    override val yourCard = "Your card"
    override val writeOnBack = "Write your dive on the back"
    override val tapToTurnBack = "Tap to turn the card over"
    override val tapForLogbook = "Tap for the logbook on the back"
    override val logbook = "LOGBOOK"
    override val nothingNoted = "Nothing noted yet. Tap ✎."
    override val diveSite = "Dive site"
    override val place = "Place / country"
    override val findSpot = "Find spot"
    override val setOnGlobe = "Set on the globe"
    override val depthMeters = "Depth (m)"
    override val waterCelsius = "Water (°C)"
    override val notes = "Notes"

    override val findSpotsTitle = "Find spots"
    override val noFindSpots = "No find spots yet. Set one on the back of a card."
    override fun findsHint(count: Int) = "$count ${if (count == 1) "find" else "finds"} · drag to turn, pinch to zoom"
    override val close = "Close"
    override val setFindSpot = "Set find spot"
    override val turnAndTap = "Turn and zoom the globe, then tap the spot"
    override val noSpotChosen = "No spot chosen yet"
    override val remove = "Remove"
    override val useSpot = "Use spot"
    override val north = "N"
    override val south = "S"
    override val east = "E"
    override val west = "W"

    override val collectionTab = "Collection"
    override val libraryTab = "Library"
    override val libraryTitle = "Library"
    override fun libraryCount(total: Int, found: Int) = "$total species · $found found"
    override val found = "Found"
    override val notFoundYet = "Not found yet"
    override val family = "Family"
    override val region = "Region"
    override val rarity = "Rarity"
    override fun firstFound(date: String) = "First found on $date"

    override fun yourPhotos(count: Int) = if (count == 1) "Your photo" else "Your photos ($count)"
    override val addPhotoHere = "Add photo"
    override val tapToEnlarge = "Tap to enlarge"
    override val cardPhoto = "Card photo"
    override val useAsCardPhoto = "Use on card"
    override val delete = "Delete"
    override val deletePhotoTitle = "Delete photo?"
    override fun deletePhotoText(lastPhoto: Boolean) =
        "The logbook entry for this photo is deleted too." +
            if (lastPhoto) " It is your last photo of this species, so the card will be locked again." else ""
}

fun stringsFor(language: Language): Strings = if (language == Language.DE) GermanStrings else EnglishStrings

val LocalStrings = staticCompositionLocalOf<Strings> { GermanStrings }
