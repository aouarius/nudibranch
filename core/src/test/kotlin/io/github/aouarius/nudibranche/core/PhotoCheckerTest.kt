package io.github.aouarius.nudibranche.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class PhotoCheckerTest {
    private val now = LocalDateTime.of(2026, 10, 8, 12, 0)
    private val goodPhoto = PhotoMetadata(
        takenAt = LocalDateTime.of(2026, 8, 12, 10, 30),
        cameraMake = "OM Digital Solutions",
        cameraModel = "TG-7",
        software = null,
        contentHash = "abc",
    )

    private fun problems(photo: PhotoMetadata, used: Set<String> = emptySet()) =
        (PhotoChecker.check(photo, now, used) as? PhotoCheckResult.Rejected)?.problems.orEmpty()

    @Test
    fun cameraPhotoIsAccepted() {
        assertEquals(PhotoCheckResult.Accepted, PhotoChecker.check(goodPhoto, now, emptySet()))
    }

    @Test
    fun photoWithoutDateIsRejected() {
        assertEquals(listOf(PhotoProblem.NO_DATE), problems(goodPhoto.copy(takenAt = null)))
    }

    @Test
    fun futureDateIsRejectedButTimeZoneSlackIsAllowed() {
        assertEquals(listOf(PhotoProblem.FUTURE_DATE), problems(goodPhoto.copy(takenAt = now.plusDays(3))))
        assertEquals(emptyList<PhotoProblem>(), problems(goodPhoto.copy(takenAt = now.plusHours(5))))
    }

    @Test
    fun screenshotWithoutCameraIsRejected() {
        val screenshot = goodPhoto.copy(cameraMake = null, cameraModel = "", software = "Android Screenshot")
        assertEquals(listOf(PhotoProblem.NO_CAMERA, PhotoProblem.SCREENSHOT), problems(screenshot))
    }

    @Test
    fun reusedPhotoIsRejected() {
        assertEquals(listOf(PhotoProblem.ALREADY_USED), problems(goodPhoto, used = setOf("abc")))
    }
}
