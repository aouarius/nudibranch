package io.github.aouarius.nudibranche.core

import java.time.LocalDateTime

/** What the app could read from a photo's EXIF data, plus a hash of the file. */
data class PhotoMetadata(
    val takenAt: LocalDateTime?,
    val cameraMake: String?,
    val cameraModel: String?,
    val software: String?,
    val contentHash: String,
)

enum class PhotoProblem(val message: String) {
    NO_DATE("Das Foto hat kein Aufnahmedatum. Bitte das Originalfoto aus der Kamera verwenden."),
    FUTURE_DATE("Das Aufnahmedatum liegt in der Zukunft."),
    NO_CAMERA("Im Foto ist keine Kamera hinterlegt. Screenshots und bearbeitete Bilder zählen nicht."),
    SCREENSHOT("Das Bild ist ein Screenshot."),
    ALREADY_USED("Dieses Foto wurde schon für eine Karte verwendet."),
}

sealed interface PhotoCheckResult {
    data object Accepted : PhotoCheckResult
    data class Rejected(val problems: List<PhotoProblem>) : PhotoCheckResult
}

object PhotoChecker {
    /** Allows for time zone differences between the camera clock and the phone. */
    private const val FUTURE_TOLERANCE_HOURS = 24L

    fun check(
        photo: PhotoMetadata,
        now: LocalDateTime,
        usedHashes: Set<String>,
    ): PhotoCheckResult {
        val problems = buildList {
            when {
                photo.takenAt == null -> add(PhotoProblem.NO_DATE)
                photo.takenAt.isAfter(now.plusHours(FUTURE_TOLERANCE_HOURS)) -> add(PhotoProblem.FUTURE_DATE)
            }
            if (photo.cameraMake.isNullOrBlank() && photo.cameraModel.isNullOrBlank()) {
                add(PhotoProblem.NO_CAMERA)
            }
            if (photo.software?.contains("screenshot", ignoreCase = true) == true) {
                add(PhotoProblem.SCREENSHOT)
            }
            if (photo.contentHash in usedHashes) {
                add(PhotoProblem.ALREADY_USED)
            }
        }
        return if (problems.isEmpty()) PhotoCheckResult.Accepted else PhotoCheckResult.Rejected(problems)
    }
}
