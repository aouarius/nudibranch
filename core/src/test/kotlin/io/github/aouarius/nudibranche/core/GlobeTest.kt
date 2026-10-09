package io.github.aouarius.nudibranche.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GlobeTest {
    private val elba = LatLon(42.78, 10.25)

    @Test
    fun centerIsInTheMiddleAndAntipodeIsHidden() {
        val center = Orthographic.project(elba, elba)
        assertEquals(0.0, center.x, 1e-9)
        assertEquals(0.0, center.y, 1e-9)
        assertTrue(center.visible)
        assertFalse(Orthographic.project(LatLon(-42.78, -169.75), elba).visible)
    }

    @Test
    fun northIsUpAndEastIsRight() {
        val north = Orthographic.project(LatLon(50.0, 10.25), elba)
        val east = Orthographic.project(LatLon(42.78, 20.0), elba)
        assertTrue(north.y > 0)
        assertTrue(east.x > 0)
    }

    @Test
    fun unprojectReversesProject() {
        val bali = LatLon(-8.4, 115.2)
        val center = LatLon(0.0, 100.0)
        val disc = Orthographic.project(bali, center)
        val back = Orthographic.unproject(disc.x, disc.y, center)!!
        assertEquals(bali.latitude, back.latitude, 1e-6)
        assertEquals(bali.longitude, back.longitude, 1e-6)
        assertNull(Orthographic.unproject(0.9, 0.9, center))
    }

    @Test
    fun appLandShapesLoad() {
        val rings = LandShapes.parse(File("../app/src/main/assets/land.json").readText())
        assertTrue(rings.size > 100)
        assertTrue(rings.all { it.size % 2 == 0 && it.size >= 6 })
    }

    @Test
    fun spotNeedsBothCoordinates() {
        assertEquals(elba, DiveDetails(latitude = 42.78, longitude = 10.25).spot)
        assertNull(DiveDetails(latitude = 42.78).spot)
        assertFalse(DiveDetails(latitude = 42.78, longitude = 10.25).isEmpty)
    }
}
