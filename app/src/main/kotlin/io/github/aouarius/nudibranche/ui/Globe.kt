package io.github.aouarius.nudibranche.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import io.github.aouarius.nudibranche.core.LatLon
import io.github.aouarius.nudibranche.core.Orthographic
import kotlin.math.hypot
import kotlin.math.min

data class GlobeMarker(val id: String, val position: LatLon, val color: Color)

private val OceanLight = Color(0xFF2C6B8F)
private val OceanDark = Color(0xFF0A2233)
private val LandFill = Color(0xFF3A4250)
private val LandEdge = Color(0xFF0C0E12)
private val Rim = Color(0xFF5B8FD6)

/**
 * A globe that turns when dragged. Tapping a marker reports its id; tapping elsewhere
 * on the globe reports that spot.
 */
@Composable
fun Globe(
    land: List<DoubleArray>,
    center: LatLon,
    onCenterChange: (LatLon) -> Unit,
    modifier: Modifier = Modifier,
    markers: List<GlobeMarker> = emptyList(),
    onMarkerTap: (String) -> Unit = {},
    onSpotTap: (LatLon) -> Unit = {},
) {
    val currentCenter = rememberUpdatedState(center)
    val currentMarkers = rememberUpdatedState(markers)
    Canvas(
        modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    val radius = min(size.width, size.height) / 2f
                    val degreesPerPixel = Math.toDegrees(1.0 / radius)
                    val c = currentCenter.value
                    onCenterChange(
                        LatLon(
                            latitude = (c.latitude + drag.y * degreesPerPixel).coerceIn(-85.0, 85.0),
                            longitude = Orthographic.normalizeLongitude(c.longitude - drag.x * degreesPerPixel),
                        ),
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    val radius = min(size.width, size.height) / 2f
                    val mid = Offset(size.width / 2f, size.height / 2f)
                    val c = currentCenter.value
                    val hit = currentMarkers.value.firstOrNull { marker ->
                        val p = Orthographic.project(marker.position, c)
                        p.visible && hypot(mid.x + p.x * radius - tap.x, mid.y - p.y * radius - tap.y) < 28f
                    }
                    if (hit != null) {
                        onMarkerTap(hit.id)
                    } else {
                        Orthographic.unproject(
                            ((tap.x - mid.x) / radius).toDouble(),
                            ((mid.y - tap.y) / radius).toDouble(),
                            c,
                        )?.let(onSpotTap)
                    }
                }
            },
    ) {
        val radius = min(size.width, size.height) / 2f
        val mid = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            Brush.radialGradient(listOf(OceanLight, OceanDark), center = mid - Offset(radius * 0.3f, radius * 0.3f), radius = radius * 1.4f),
            radius = radius,
            center = mid,
        )

        // Points on the far side are pulled onto the rim, so shapes that cross it stay closed.
        land.forEach { ring ->
            val path = Path()
            var anyVisible = false
            var i = 0
            while (i < ring.size) {
                val p = Orthographic.project(LatLon(ring[i + 1], ring[i]), center)
                var x = p.x
                var y = p.y
                if (!p.visible) {
                    val length = hypot(x, y).coerceAtLeast(1e-9)
                    x /= length
                    y /= length
                } else {
                    anyVisible = true
                }
                val point = Offset(mid.x + (x * radius).toFloat(), mid.y - (y * radius).toFloat())
                if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                i += 2
            }
            if (anyVisible) {
                path.close()
                drawPath(path, LandFill)
                drawPath(path, LandEdge, style = Stroke(width = 1.2f))
            }
        }

        drawCircle(Rim, radius = radius, center = mid, style = Stroke(width = 2f))

        markers.forEach { marker ->
            val p = Orthographic.project(marker.position, center)
            if (p.visible) {
                val point = Offset(mid.x + (p.x * radius).toFloat(), mid.y - (p.y * radius).toFloat())
                drawCircle(Color(0xFF0C0E12), radius = 9f, center = point)
                drawCircle(marker.color, radius = 6.5f, center = point)
            }
        }
    }
}
