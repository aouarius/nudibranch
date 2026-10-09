package io.github.aouarius.nudibranche.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.aouarius.nudibranche.core.Badge
import io.github.aouarius.nudibranche.core.Badges
import io.github.aouarius.nudibranche.core.CardCollection
import io.github.aouarius.nudibranche.core.DiveDetails
import io.github.aouarius.nudibranche.core.LandShapes
import io.github.aouarius.nudibranche.core.Logbook
import io.github.aouarius.nudibranche.core.LogbookStats
import io.github.aouarius.nudibranche.core.Language
import io.github.aouarius.nudibranche.core.PhotoCheckResult
import io.github.aouarius.nudibranche.core.PhotoChecker
import io.github.aouarius.nudibranche.core.PhotoProblem
import io.github.aouarius.nudibranche.core.Sighting
import io.github.aouarius.nudibranche.core.Species
import io.github.aouarius.nudibranche.core.SpeciesCatalog
import io.github.aouarius.nudibranche.core.Suggestion
import io.github.aouarius.nudibranche.data.Backup
import io.github.aouarius.nudibranche.data.CollectionStore
import io.github.aouarius.nudibranche.data.ImportedPhoto
import io.github.aouarius.nudibranche.data.PhotoImporter
import io.github.aouarius.nudibranche.data.SpeciesRecognizer
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.util.Locale

sealed interface ImportState {
    data object Idle : ImportState
    data object Checking : ImportState
    /** [suggestions] are the model's best guesses, most likely first; empty when it has none. */
    class ChooseSpecies(val photo: ImportedPhoto, val suggestions: List<Suggestion> = emptyList()) : ImportState
    /** The photo is stored; the card turns over so the diver can write on its back. */
    data class EnterDiveDetails(val sighting: Sighting, val species: Species, val firstFind: Boolean) : ImportState
    data class Rejected(val problems: List<PhotoProblem>) : ImportState
    data object Failed : ImportState
    data class Unlocked(
        val species: Species,
        val firstFind: Boolean,
        val sighting: Sighting,
        val newBadges: List<Badge> = emptyList(),
    ) : ImportState
}

/** Outcome of saving or loading a backup, shown once. */
sealed interface BackupResult {
    data object Saved : BackupResult
    data class Restored(val photos: Int, val cards: Int) : BackupResult
    data object Failed : BackupResult
}

class CatalogViewModel(app: Application) : AndroidViewModel(app) {

    val species: List<Species> =
        SpeciesCatalog.parse(app.assets.open("species.json").bufferedReader().use { it.readText() })

    /** Land outlines for the globe, read once when first needed. */
    val land: List<DoubleArray> by lazy {
        LandShapes.parse(app.assets.open("land.json").bufferedReader().use { it.readText() })
    }

    private val settings = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** Chosen in the app; until then German phones get German, all others English. */
    var language: Language by mutableStateOf(
        settings.getString("language", null)?.let { saved -> Language.entries.firstOrNull { it.name == saved } }
            ?: if (Locale.getDefault().language == "de") Language.DE else Language.EN,
    )
        private set

    /** Collection page shows only found cards instead of all of them. */
    var onlyFound: Boolean by mutableStateOf(settings.getBoolean("onlyFound", false))
        private set

    fun changeOnlyFound(value: Boolean) {
        onlyFound = value
        settings.edit().putBoolean("onlyFound", value).apply()
    }

    fun changeLanguage(newLanguage: Language) {
        language = newLanguage
        settings.edit().putString("language", newLanguage.name).apply()
    }

    private companion object {
        /**
         * Date, camera and screenshot checks are switched off for now (Alex, 2026-10-08):
         * cheating only spoils it for the cheater. Set to true to bring them back.
         */
        const val PHOTO_ANTI_CHEAT = false
    }

    private val store = CollectionStore(File(app.filesDir, "collection.json"))
    private val importer = PhotoImporter(app)
    private val backup = Backup(app)
    private val recognizer = SpeciesRecognizer(app)

    var collection: CardCollection by mutableStateOf(store.load())
        private set

    var importState: ImportState by mutableStateOf(ImportState.Idle)
        private set

    val stats: LogbookStats get() = Logbook.stats(collection, species)

    val badges: List<Badge> get() = Badges.all(collection, species)

    var backupResult: BackupResult? by mutableStateOf(null)
        private set

    var backupRunning: Boolean by mutableStateOf(false)
        private set

    /** With [forSpecies] the photo goes straight to that card, without asking for the species. */
    fun onPhotoPicked(uri: Uri, forSpecies: Species? = null) {
        importState = ImportState.Checking
        viewModelScope.launch {
            importState = try {
                val photo = importer.read(uri)
                val result = PhotoChecker.check(
                    photo.metadata,
                    LocalDateTime.now(),
                    collection.usedHashes,
                    antiCheat = PHOTO_ANTI_CHEAT,
                )
                when (result) {
                    PhotoCheckResult.Accepted -> ImportState.ChooseSpecies(
                        photo,
                        if (forSpecies == null) recognizer.suggest(photo.bytes, species) else emptyList(),
                    )
                    is PhotoCheckResult.Rejected -> ImportState.Rejected(result.problems)
                }
            } catch (e: Exception) {
                ImportState.Failed
            }
            if (forSpecies != null && importState is ImportState.ChooseSpecies) onSpeciesChosen(forSpecies)
        }
    }

    /** The diver confirms a suggestion or picks the species from the list. */
    fun onSpeciesChosen(species: Species) {
        val state = importState as? ImportState.ChooseSpecies ?: return
        viewModelScope.launch {
            val metadata = state.photo.metadata
            val sighting = Sighting(
                speciesId = species.id,
                photoPath = importer.save(state.photo),
                photoHash = metadata.contentHash,
                takenAt = (metadata.takenAt ?: LocalDateTime.now().withNano(0)).toString(),
                unlockedAt = LocalDateTime.now().withNano(0).toString(),
                cameraModel = metadata.cameraModel,
                dive = DiveDetails(latitude = state.photo.gps?.latitude, longitude = state.photo.gps?.longitude),
            )
            importState = ImportState.EnterDiveDetails(sighting, species, !collection.isUnlocked(species.id))
        }
    }

    fun onDiveDetailsEntered(dive: DiveDetails) {
        val state = importState as? ImportState.EnterDiveDetails ?: return
        val sighting = state.sighting.copy(dive = dive)
        val before = collection
        collection = collection.add(sighting)
        val newBadges = Badges.newlyEarned(before, collection, species)
        importState = ImportState.Unlocked(state.species, state.firstFind, sighting, newBadges)
        viewModelScope.launch { store.save(collection) }
    }

    fun updateDive(sighting: Sighting, dive: DiveDetails) {
        collection = collection.updateDive(sighting.photoHash, dive)
        viewModelScope.launch { store.save(collection) }
    }

    fun setCover(sighting: Sighting) {
        collection = collection.setCover(sighting.photoHash)
        viewModelScope.launch { store.save(collection) }
    }

    /** Deletes the photo and its logbook entry. Without photos left the card is locked again. */
    fun deleteSighting(sighting: Sighting) {
        collection = collection.remove(sighting.photoHash)
        viewModelScope.launch {
            store.save(collection)
            importer.delete(sighting.photoPath)
        }
    }

    fun exportBackup(target: Uri) = runBackup {
        backup.export(collection, target)
        BackupResult.Saved
    }

    /** Adds the backup to what is already on the phone; nothing here is overwritten. */
    fun restoreBackup(source: Uri) = runBackup {
        val restored = backup.read(source)
        val before = collection
        // The collection may have changed while the file was read, so merge into the newest one.
        collection = collection.merge(restored)
        store.save(collection)
        BackupResult.Restored(
            photos = collection.sightings.size - before.sightings.size,
            cards = collection.unlockedCount() - before.unlockedCount(),
        )
    }

    private fun runBackup(work: suspend () -> BackupResult) {
        if (backupRunning) return
        backupRunning = true
        viewModelScope.launch {
            backupResult = try {
                work()
            } catch (e: Exception) {
                BackupResult.Failed
            }
            backupRunning = false
        }
    }

    fun dismissBackupResult() {
        backupResult = null
    }

    fun dismissImport() {
        importState = ImportState.Idle
    }
}
