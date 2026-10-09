package io.github.aouarius.nudibranche.data

import io.github.aouarius.nudibranche.core.CardCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Keeps the player's collection as a JSON file in app storage. */
class CollectionStore(private val file: File) {
    private val writeLock = Mutex()

    @Volatile
    private var latest: CardCollection? = null

    fun load(): CardCollection =
        if (file.exists()) CardCollection.decode(file.readText()) else CardCollection()

    /** Saves are serialized and always write the newest collection, even if they finish out of order. */
    suspend fun save(collection: CardCollection) {
        latest = collection
        withContext(Dispatchers.IO) {
            writeLock.withLock { latest?.let(::write) }
        }
    }

    private fun write(collection: CardCollection) {
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(collection.encode())
        tmp.renameTo(file)
    }
}
