package io.github.aouarius.nudibranche.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.aouarius.nudibranche.core.CardCollection
import io.github.aouarius.nudibranche.core.PhotoCheckResult
import io.github.aouarius.nudibranche.core.PhotoChecker
import io.github.aouarius.nudibranche.core.PhotoProblem
import io.github.aouarius.nudibranche.core.Sighting
import io.github.aouarius.nudibranche.core.Species
import io.github.aouarius.nudibranche.core.SpeciesCatalog
import io.github.aouarius.nudibranche.data.CollectionStore
import io.github.aouarius.nudibranche.data.ImportedPhoto
import io.github.aouarius.nudibranche.data.PhotoImporter
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime

sealed interface ImportState {
    data object Idle : ImportState
    data object Checking : ImportState
    class ChooseSpecies(val photo: ImportedPhoto) : ImportState
    data class Rejected(val problems: List<PhotoProblem>) : ImportState
    data class Failed(val message: String) : ImportState
    data class Unlocked(val species: Species, val firstFind: Boolean) : ImportState
}

class CatalogViewModel(app: Application) : AndroidViewModel(app) {

    val species: List<Species> =
        SpeciesCatalog.parse(app.assets.open("species.json").bufferedReader().use { it.readText() })

    private val store = CollectionStore(File(app.filesDir, "collection.json"))
    private val importer = PhotoImporter(app)

    var collection: CardCollection by mutableStateOf(store.load())
        private set

    var importState: ImportState by mutableStateOf(ImportState.Idle)
        private set

    fun onPhotoPicked(uri: Uri) {
        importState = ImportState.Checking
        viewModelScope.launch {
            importState = try {
                val photo = importer.read(uri)
                when (val result = PhotoChecker.check(photo.metadata, LocalDateTime.now(), collection.usedHashes)) {
                    PhotoCheckResult.Accepted -> ImportState.ChooseSpecies(photo)
                    is PhotoCheckResult.Rejected -> ImportState.Rejected(result.problems)
                }
            } catch (e: Exception) {
                ImportState.Failed(e.message ?: "Das Foto konnte nicht gelesen werden.")
            }
        }
    }

    /** Until the recognition model exists, the diver picks the species themselves. */
    fun onSpeciesChosen(species: Species) {
        val state = importState as? ImportState.ChooseSpecies ?: return
        viewModelScope.launch {
            val firstFind = !collection.isUnlocked(species.id)
            val metadata = state.photo.metadata
            val path = importer.save(state.photo)
            collection = collection.add(
                Sighting(
                    speciesId = species.id,
                    photoPath = path,
                    photoHash = metadata.contentHash,
                    takenAt = metadata.takenAt.toString(),
                    unlockedAt = LocalDateTime.now().withNano(0).toString(),
                    cameraModel = metadata.cameraModel,
                ),
            )
            store.save(collection)
            importState = ImportState.Unlocked(species, firstFind)
        }
    }

    fun dismissImport() {
        importState = ImportState.Idle
    }
}
