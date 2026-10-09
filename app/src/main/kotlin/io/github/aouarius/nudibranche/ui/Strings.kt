package io.github.aouarius.nudibranche.ui

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.aouarius.nudibranche.core.Badge
import io.github.aouarius.nudibranche.core.BadgeKind
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
    val suggestionsTitle: String
    val suggestionsUnsure: String
    val noSuggestions: String
    val allSpecies: String
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
    val mapHint: String
    val searchPlace: String
    val noPlaceFound: String
    val searchFailed: String
    val useGlobe: String
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

    val logbookTab: String
    val logbookTitle: String
    fun logbookSubtitle(diveDays: Int, photos: Int): String
    val statSpecies: String
    val statDiveDays: String
    val statPhotos: String
    val statSites: String
    val statDeepest: String
    val statFavoriteSite: String
    val statWater: String
    fun finds(count: Int): String
    val byRegion: String
    val byRarity: String
    val perYear: String
    val badgesTitle: String
    fun badgesEarned(earned: Int, total: Int): String
    fun badgeTitle(badge: Badge): String
    fun badgeGoal(badge: Badge): String
    fun newBadges(count: Int): String
    val backupTitle: String
    val backupText: String
    val backupSave: String
    val backupLoad: String
    val backupSaved: String
    fun backupRestored(photos: Int, cards: Int): String
    val backupFailed: String
    val shareCard: String
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
    override val suggestionsTitle = "Das könnte sie sein"
    override val suggestionsUnsure = "Ich bin mir nicht ganz sicher. Vergleich die Vorschläge oder such die Art in der Liste."
    override val noSuggestions = "Ich erkenne die Art nicht sicher. Wähl sie bitte aus der Liste."
    override val allSpecies = "Alle Arten"
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
    override val mapHint = "Karte verschieben, bis die Pin-Spitze auf die Fundstelle zeigt"
    override val searchPlace = "Ort oder Tauchplatz suchen"
    override val noPlaceFound = "Nichts gefunden"
    override val searchFailed = "Suche geht gerade nicht (kein Internet?)"
    override val useGlobe = "Ohne Internet: Globus"
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
    override val logbookTab = "Logbuch"
    override val logbookTitle = "Logbuch"
    override fun logbookSubtitle(diveDays: Int, photos: Int) =
        "${if (diveDays == 1) "1 Tauchtag" else "$diveDays Tauchtage"} · ${if (photos == 1) "1 Foto" else "$photos Fotos"}"
    override val statSpecies = "Arten"
    override val statDiveDays = "Tauchtage"
    override val statPhotos = "Fotos"
    override val statSites = "Tauchplätze"
    override val statDeepest = "Tiefster Fund"
    override val statFavoriteSite = "Lieblingsplatz"
    override val statWater = "Wasser"
    override fun finds(count: Int) = if (count == 1) "1 Fund" else "$count Funde"
    override val byRegion = "Nach Region"
    override val byRarity = "Nach Seltenheit"
    override val perYear = "Fotos pro Jahr"
    override val badgesTitle = "Abzeichen"
    override fun badgesEarned(earned: Int, total: Int) = "$earned von $total"
    override fun badgeTitle(badge: Badge) = when (badge.kind) {
        BadgeKind.FIRST_FIND -> "Erster Fund"
        BadgeKind.FIVE_SPECIES -> "Sammler"
        BadgeKind.TEN_SPECIES -> "Kenner"
        BadgeKind.ALL_SPECIES -> "Nudidex komplett"
        BadgeKind.REGION_COMPLETE -> "${badge.region?.label(language)} komplett"
        BadgeKind.FIRST_LEGENDARY -> "Legende"
        BadgeKind.ALL_LEGENDARY -> "Alle Legenden"
        BadgeKind.FIVE_FAMILIES -> "Familienbande"
        BadgeKind.THREE_REGIONS -> "Weltenbummler"
        BadgeKind.TEN_DIVE_DAYS -> "Stammgast"
        BadgeKind.DEEP_FIND -> "Tiefenrausch"
        BadgeKind.TWENTY_FIVE_PHOTOS -> "Fotograf"
    }
    override fun badgeGoal(badge: Badge) = when (badge.kind) {
        BadgeKind.FIRST_FIND -> "Schalte deine erste Karte frei"
        BadgeKind.FIVE_SPECIES -> "Finde 5 Arten"
        BadgeKind.TEN_SPECIES -> "Finde 10 Arten"
        BadgeKind.ALL_SPECIES -> "Finde alle Arten"
        BadgeKind.REGION_COMPLETE -> "Finde alle Arten aus der Region ${badge.region?.label(language)}"
        BadgeKind.FIRST_LEGENDARY -> "Finde eine legendäre Art"
        BadgeKind.ALL_LEGENDARY -> "Finde alle legendären Arten"
        BadgeKind.FIVE_FAMILIES -> "Finde Arten aus 5 Familien"
        BadgeKind.THREE_REGIONS -> "Finde Arten in 3 Regionen"
        BadgeKind.TEN_DIVE_DAYS -> "Mach Funde an 10 Tauchtagen"
        BadgeKind.DEEP_FIND -> "Ein Fund auf ${badge.target} m oder tiefer"
        BadgeKind.TWENTY_FIVE_PHOTOS -> "Sammle 25 Fotos"
    }
    override fun newBadges(count: Int) = if (count == 1) "Neues Abzeichen!" else "$count neue Abzeichen!"
    override val backupTitle = "Sicherung"
    override val backupText =
        "Speichere deine Sammlung mit allen Fotos als Datei, zum Beispiel in Google Drive. " +
            "Auf einem neuen Handy lädst du sie wieder, und nichts geht verloren."
    override val backupSave = "Sicherung speichern"
    override val backupLoad = "Sicherung laden"
    override val backupSaved = "Sicherung gespeichert."
    override fun backupRestored(photos: Int, cards: Int) =
        if (photos == 0) "Sicherung geladen. Alles darin war schon auf diesem Handy."
        else "Sicherung geladen: ${if (photos == 1) "1 Foto" else "$photos Fotos"}, " +
            "${if (cards == 1) "1 neue Karte" else "$cards neue Karten"}."
    override val backupFailed = "Das hat nicht geklappt. Ist das eine Nudidex-Sicherung?"
    override val shareCard = "Karte teilen"
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
    override val suggestionsTitle = "This could be it"
    override val suggestionsUnsure = "I'm not quite sure. Compare the suggestions or search the list."
    override val noSuggestions = "I can't tell the species for sure. Please pick it from the list."
    override val allSpecies = "All species"
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
    override val mapHint = "Move the map until the pin's tip points at the spot"
    override val searchPlace = "Search place or dive site"
    override val noPlaceFound = "Nothing found"
    override val searchFailed = "Search isn't working right now (no internet?)"
    override val useGlobe = "Offline: use globe"
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
    override val logbookTab = "Logbook"
    override val logbookTitle = "Logbook"
    override fun logbookSubtitle(diveDays: Int, photos: Int) =
        "${if (diveDays == 1) "1 dive day" else "$diveDays dive days"} · ${if (photos == 1) "1 photo" else "$photos photos"}"
    override val statSpecies = "Species"
    override val statDiveDays = "Dive days"
    override val statPhotos = "Photos"
    override val statSites = "Dive sites"
    override val statDeepest = "Deepest find"
    override val statFavoriteSite = "Favourite site"
    override val statWater = "Water"
    override fun finds(count: Int) = if (count == 1) "1 find" else "$count finds"
    override val byRegion = "By region"
    override val byRarity = "By rarity"
    override val perYear = "Photos per year"
    override val badgesTitle = "Badges"
    override fun badgesEarned(earned: Int, total: Int) = "$earned of $total"
    override fun badgeTitle(badge: Badge) = when (badge.kind) {
        BadgeKind.FIRST_FIND -> "First find"
        BadgeKind.FIVE_SPECIES -> "Collector"
        BadgeKind.TEN_SPECIES -> "Expert"
        BadgeKind.ALL_SPECIES -> "Nudidex complete"
        BadgeKind.REGION_COMPLETE -> "${badge.region?.label(language)} complete"
        BadgeKind.FIRST_LEGENDARY -> "Legend"
        BadgeKind.ALL_LEGENDARY -> "All legends"
        BadgeKind.FIVE_FAMILIES -> "Family ties"
        BadgeKind.THREE_REGIONS -> "Globetrotter"
        BadgeKind.TEN_DIVE_DAYS -> "Regular"
        BadgeKind.DEEP_FIND -> "Deep diver"
        BadgeKind.TWENTY_FIVE_PHOTOS -> "Photographer"
    }
    override fun badgeGoal(badge: Badge) = when (badge.kind) {
        BadgeKind.FIRST_FIND -> "Unlock your first card"
        BadgeKind.FIVE_SPECIES -> "Find 5 species"
        BadgeKind.TEN_SPECIES -> "Find 10 species"
        BadgeKind.ALL_SPECIES -> "Find every species"
        BadgeKind.REGION_COMPLETE -> "Find every species of the ${badge.region?.label(language)} region"
        BadgeKind.FIRST_LEGENDARY -> "Find a legendary species"
        BadgeKind.ALL_LEGENDARY -> "Find every legendary species"
        BadgeKind.FIVE_FAMILIES -> "Find species from 5 families"
        BadgeKind.THREE_REGIONS -> "Find species in 3 regions"
        BadgeKind.TEN_DIVE_DAYS -> "Make finds on 10 dive days"
        BadgeKind.DEEP_FIND -> "A find at ${badge.target} m or deeper"
        BadgeKind.TWENTY_FIVE_PHOTOS -> "Collect 25 photos"
    }
    override fun newBadges(count: Int) = if (count == 1) "New badge!" else "$count new badges!"
    override val backupTitle = "Backup"
    override val backupText =
        "Save your collection with all photos as a file, for example on Google Drive. " +
            "Load it on a new phone and nothing is lost."
    override val backupSave = "Save backup"
    override val backupLoad = "Load backup"
    override val backupSaved = "Backup saved."
    override fun backupRestored(photos: Int, cards: Int) =
        if (photos == 0) "Backup loaded. Everything in it was already on this phone."
        else "Backup loaded: ${if (photos == 1) "1 photo" else "$photos photos"}, " +
            "${if (cards == 1) "1 new card" else "$cards new cards"}."
    override val backupFailed = "That didn't work. Is this a Nudidex backup?"
    override val shareCard = "Share card"
}

fun stringsFor(language: Language): Strings = if (language == Language.DE) GermanStrings else EnglishStrings

val LocalStrings = staticCompositionLocalOf<Strings> { GermanStrings }
