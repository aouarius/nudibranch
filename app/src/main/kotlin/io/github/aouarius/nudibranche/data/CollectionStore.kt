package io.github.aouarius.nudibranche.data

import io.github.aouarius.nudibranche.core.CardCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Keeps the player's collection as a JSON file in app storage. */
class CollectionStore(private val file: File) {

    fun load(): CardCollection =
        if (file.exists()) CardCollection.decode(file.readText()) else CardCollection()

    suspend fun save(collection: CardCollection) = withContext(Dispatchers.IO) {
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(collection.encode())
        tmp.renameTo(file)
    }
}
