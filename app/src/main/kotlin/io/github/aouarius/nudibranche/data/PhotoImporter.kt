package io.github.aouarius.nudibranche.data

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import io.github.aouarius.nudibranche.core.LatLon
import io.github.aouarius.nudibranche.core.PhotoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** [gps] is only set when the photo still carries a location (cameras with GPS, files not stripped by Android). */
class ImportedPhoto(val bytes: ByteArray, val metadata: PhotoMetadata, val gps: LatLon?)

/** Reads a picked photo, extracts its EXIF data and stores accepted photos in app storage. */
class PhotoImporter(private val context: Context) {

    suspend fun read(uri: Uri): ImportedPhoto = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Photo could not be opened")
        val exif = ExifInterface(ByteArrayInputStream(bytes))
        val date = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
        ImportedPhoto(
            bytes = bytes,
            metadata = PhotoMetadata(
                takenAt = date?.let(::parseExifDate),
                cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE),
                cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL),
                software = exif.getAttribute(ExifInterface.TAG_SOFTWARE),
                contentHash = sha256(bytes),
            ),
            gps = exif.latLong?.let { LatLon(it[0], it[1]) },
        )
    }

    /** Copies the photo into app storage and returns its path relative to filesDir. */
    suspend fun save(photo: ImportedPhoto): String = withContext(Dispatchers.IO) {
        val relativePath = "photos/${photo.metadata.contentHash}.jpg"
        val file = File(context.filesDir, relativePath)
        file.parentFile?.mkdirs()
        file.writeBytes(photo.bytes)
        relativePath
    }

    suspend fun delete(relativePath: String) = withContext(Dispatchers.IO) {
        File(context.filesDir, relativePath).delete()
    }

    private fun parseExifDate(value: String): LocalDateTime? =
        runCatching { LocalDateTime.parse(value.trim(), EXIF_DATE) }.getOrNull()

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private companion object {
        val EXIF_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
    }
}
