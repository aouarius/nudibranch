package io.github.aouarius.nudibranche.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import io.github.aouarius.nudibranche.core.Recognition
import io.github.aouarius.nudibranche.core.Species
import io.github.aouarius.nudibranche.core.Suggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Guesses the species on a photo with the on-device model in assets
 * (trained by tools/model/train.py). Without a model it simply has no suggestions.
 */
class SpeciesRecognizer(private val context: Context) {

    private class Model(val interpreter: Interpreter, val labels: List<String>)

    private val lock = Mutex()
    private var model: Model? = null
    private var loadFailed = false

    suspend fun suggest(photo: ByteArray, catalog: List<Species>): List<Suggestion> = withContext(Dispatchers.Default) {
        lock.withLock {
            val model = load() ?: return@withLock emptyList()
            runCatching {
                val input = pixels(photo) ?: return@runCatching emptyList()
                val output = Array(1) { FloatArray(model.labels.size) }
                model.interpreter.run(input, output)
                Recognition.suggestions(output[0], model.labels, catalog)
            }.getOrDefault(emptyList())
        }
    }

    private fun load(): Model? {
        model?.let { return it }
        if (loadFailed) return null
        return runCatching {
            val bytes = context.assets.open(MODEL).use { it.readBytes() }
            val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
            buffer.rewind()
            val labels = context.assets.open(LABELS).bufferedReader().readLines().filter { it.isNotBlank() }
            Model(Interpreter(buffer, Interpreter.Options().setNumThreads(4)), labels)
        }.onFailure { loadFailed = true }.getOrNull()?.also { model = it }
    }

    /** Upright photo, short side scaled to [SIZE], centre cut out, as RGB floats 0–255 like in training. */
    private fun pixels(photo: ByteArray): ByteBuffer? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(photo, 0, photo.size, bounds)
        var sample = 1
        while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= SIZE) sample *= 2
        val decoded = BitmapFactory.decodeByteArray(photo, 0, photo.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null
        val upright = rotateUpright(decoded, photo)
        val scale = SIZE.toFloat() / minOf(upright.width, upright.height)
        val scaled = Bitmap.createScaledBitmap(
            upright,
            maxOf(SIZE, Math.round(upright.width * scale)),
            maxOf(SIZE, Math.round(upright.height * scale)),
            true,
        )
        val square = Bitmap.createBitmap(scaled, (scaled.width - SIZE) / 2, (scaled.height - SIZE) / 2, SIZE, SIZE)
        val argb = IntArray(SIZE * SIZE)
        square.getPixels(argb, 0, SIZE, 0, 0, SIZE, SIZE)
        val buffer = ByteBuffer.allocateDirect(4 * 3 * SIZE * SIZE).order(ByteOrder.nativeOrder())
        for (pixel in argb) {
            buffer.putFloat(((pixel shr 16) and 0xFF).toFloat())
            buffer.putFloat(((pixel shr 8) and 0xFF).toFloat())
            buffer.putFloat((pixel and 0xFF).toFloat())
        }
        buffer.rewind()
        return buffer
    }

    private fun rotateUpright(bitmap: Bitmap, photo: ByteArray): Bitmap {
        val degrees = runCatching { ExifInterface(ByteArrayInputStream(photo)).rotationDegrees }.getOrDefault(0)
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private companion object {
        const val SIZE = 224
        const val MODEL = "species_model.tflite"
        const val LABELS = "species_labels.txt"
    }
}
