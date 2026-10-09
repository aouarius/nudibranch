package io.github.aouarius.nudibranche.data

import io.github.aouarius.nudibranche.core.LatLon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Place(val name: String, val position: LatLon)

/**
 * Place search on OpenStreetMap data (Nominatim). Its usage policy asks for an identifying
 * user agent and at most one request per second, so the app only searches on submit.
 */
object PlaceSearch {
    private const val ENDPOINT = "https://nominatim.openstreetmap.org/search"
    suspend fun search(query: String, language: String): List<Place> = withContext(Dispatchers.IO) {
        val url = "$ENDPOINT?format=json&limit=6&q=${URLEncoder.encode(query, "UTF-8")}"
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.setRequestProperty("User-Agent", "Nudidex/0.1 (github.com/aouarius/nudibranch)")
            connection.setRequestProperty("Accept-Language", language)
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val results = JSONArray(text)
            (0 until results.length()).mapNotNull { i ->
                val result = results.getJSONObject(i)
                val lat = result.optString("lat").toDoubleOrNull() ?: return@mapNotNull null
                val lon = result.optString("lon").toDoubleOrNull() ?: return@mapNotNull null
                Place(result.optString("display_name"), LatLon(lat, lon))
            }
        } finally {
            connection.disconnect()
        }
    }
}
