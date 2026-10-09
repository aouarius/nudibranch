package io.github.aouarius.nudibranche.data

import android.content.Context
import android.net.Uri
import io.github.aouarius.nudibranche.core.CardCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Saves the whole collection, photos included, as one zip file the diver keeps
 * somewhere safe (Drive, a computer), and reads such a file back on a new phone.
 */
class Backup(private val context: Context) {

    suspend fun export(collection: CardCollection, target: Uri) = withContext(Dispatchers.IO) {
        val out = context.contentResolver.openOutputStream(target) ?: error("Backup file could not be opened")
        ZipOutputStream(out.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(COLLECTION_ENTRY))
            zip.write(collection.encode().toByteArray())
            zip.closeEntry()
            for (sighting in collection.sightings.distinctBy { it.photoPath }) {
                val photo = File(context.filesDir, sighting.photoPath)
                if (!photo.isFile) continue
                zip.putNextEntry(ZipEntry(sighting.photoPath))
                photo.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    /**
     * Copies the photos into app storage and returns the backed-up collection,
     * keeping only photos that actually arrived.
     */
    suspend fun read(source: Uri): CardCollection = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(source) ?: error("Backup file could not be opened")
        var collection: CardCollection? = null
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name
                when {
                    name == COLLECTION_ENTRY -> collection = CardCollection.decode(zip.readBytes().decodeToString())
                    // Only plain photo names: a crafted zip must not write anywhere else.
                    PHOTO_ENTRY.matches(name) -> {
                        val file = File(context.filesDir, name)
                        if (!file.exists()) {
                            file.parentFile?.mkdirs()
                            val tmp = File(file.parentFile, "${file.name}.tmp")
                            tmp.outputStream().use { zip.copyTo(it) }
                            tmp.renameTo(file)
                        }
                    }
                }
                zip.closeEntry()
            }
        }
        val backup = collection ?: error("Not a Nudidex backup")
        backup.copy(sightings = backup.sightings.filter { File(context.filesDir, it.photoPath).isFile })
    }

    private companion object {
        const val COLLECTION_ENTRY = "collection.json"
        val PHOTO_ENTRY = Regex("photos/[0-9a-f]{16,128}\\.jpg")
    }
}
