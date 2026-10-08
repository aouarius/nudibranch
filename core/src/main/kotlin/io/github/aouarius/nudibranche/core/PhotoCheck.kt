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

enum class PhotoProblem { NO_DATE, FUTURE_DATE, NO_CAMERA, SCREENSHOT, ALREADY_USED }

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
