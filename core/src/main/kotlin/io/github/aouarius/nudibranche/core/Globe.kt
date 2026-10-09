package io.github.aouarius.nudibranche.core

import kotlinx.serialization.json.Json
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLon(val latitude: Double, val longitude: Double)

/** A point on the globe's disc: x to the right, y upwards, both within -1..1. */
data class DiscPoint(val x: Double, val y: Double, val visible: Boolean)

/** Orthographic projection: the globe as seen from far away, centred on [center]. */
object Orthographic {

    fun project(point: LatLon, center: LatLon): DiscPoint {
        val phi = Math.toRadians(point.latitude)
        val phi0 = Math.toRadians(center.latitude)
        val dLambda = Math.toRadians(point.longitude - center.longitude)
        val x = cos(phi) * sin(dLambda)
        val y = cos(phi0) * sin(phi) - sin(phi0) * cos(phi) * cos(dLambda)
        val cosC = sin(phi0) * sin(phi) + cos(phi0) * cos(phi) * cos(dLambda)
        return DiscPoint(x, y, visible = cosC >= 0)
    }

    /** The point on the globe under a disc position, or null outside the globe. */
    fun unproject(x: Double, y: Double, center: LatLon): LatLon? {
        val rho = sqrt(x * x + y * y)
        if (rho > 1.0) return null
        if (rho == 0.0) return center
        val c = asin(rho)
        val phi0 = Math.toRadians(center.latitude)
        val phi = asin(cos(c) * sin(phi0) + y * sin(c) * cos(phi0) / rho)
        val lambda = atan2(x * sin(c), rho * cos(c) * cos(phi0) - y * sin(c) * sin(phi0))
        return LatLon(Math.toDegrees(phi), normalizeLongitude(center.longitude + Math.toDegrees(lambda)))
    }

    fun normalizeLongitude(longitude: Double): Double = ((longitude + 540.0) % 360.0) - 180.0
}

/** Land outlines as flat [lon, lat, lon, lat, ...] rings. */
object LandShapes {
    fun parse(text: String): List<DoubleArray> =
        Json.decodeFromString<List<List<Double>>>(text).map { it.toDoubleArray() }
}
