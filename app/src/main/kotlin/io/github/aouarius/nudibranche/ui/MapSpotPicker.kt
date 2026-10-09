package io.github.aouarius.nudibranche.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.aouarius.nudibranche.R
import io.github.aouarius.nudibranche.core.LatLon
import io.github.aouarius.nudibranche.core.Orthographic
import io.github.aouarius.nudibranche.data.Place
import io.github.aouarius.nudibranche.data.PlaceSearch
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView

/** Free OpenStreetMap vector map, no account or key needed. */
private const val MapStyleUrl = "https://tiles.openfreemap.org/styles/liberty"
private const val SpotZoom = 11.0
private const val WorldZoom = 1.5

/**
 * Picks a find spot on a real map: the diver moves the map under a fixed nudibranch pin,
 * like iNaturalist does with its crosshair. Place search finds dive sites and towns. Without
 * internet the map stays empty, so the old globe is one tap away.
 */
@Composable
fun SpotPickerDialog(
    land: List<DoubleArray>,
    initial: LatLon?,
    onPick: (LatLon?) -> Unit,
    onDismiss: () -> Unit,
    searchSuggestion: String = "",
) {
    var useGlobe by remember { mutableStateOf(false) }
    if (useGlobe) {
        GlobeSpotPickerDialog(land, initial, onPick, onDismiss)
        return
    }
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var center by remember { mutableStateOf(initial ?: DefaultCenter) }
    var moveTo by remember { mutableStateOf<LatLon?>(null) }
    var query by remember { mutableStateOf(searchSuggestion) }
    var searching by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Place>?>(null) }
    var searchError by remember { mutableStateOf(false) }
    var mapFailed by remember { mutableStateOf(false) }

    fun search() {
        if (query.isBlank() || searching) return
        searching = true
        searchError = false
        scope.launch {
            results = try {
                PlaceSearch.search(query.trim(), strings.language.name.lowercase())
            } catch (e: Exception) {
                searchError = true
                null
            }
            searching = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .safeDrawingPadding(),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = Space.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.cancel, tint = AppColors.Text)
                }
                Column(Modifier.weight(1f)) {
                    Text(strings.setFindSpot, color = AppColors.Text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(formatSpot(center, strings), color = AppColors.TextMuted, fontSize = 13.sp)
                }
                IconButton(onClick = { onPick(center) }) {
                    Icon(Icons.Filled.Check, contentDescription = strings.useSpot, tint = AppColors.Accent)
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    results = null
                },
                placeholder = { Text(strings.searchPlace) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searching) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { search() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space.l, vertical = Space.s),
            )

            Box(Modifier.weight(1f).fillMaxWidth()) {
                SpotMap(
                    start = initial,
                    moveTo = moveTo,
                    onCenterChange = { center = it },
                    onFailed = { mapFailed = true },
                    modifier = Modifier.fillMaxSize(),
                )
                // The pin's tip sits exactly on the map centre, which is the chosen spot.
                Image(
                    painterResource(R.drawable.ic_nudi_pin),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-30).dp)
                        .size(48.dp, 64.dp),
                )
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .size(6.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.Black, CircleShape),
                )
                SearchResults(
                    results = results,
                    error = searchError,
                    onChoose = { place ->
                        moveTo = place.position
                        results = null
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = Space.l),
                )
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = Space.l, vertical = Space.m)) {
                Text(
                    strings.mapHint,
                    color = if (mapFailed) AppColors.Accent else AppColors.TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = Space.s),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { useGlobe = true }) { Text(strings.useGlobe) }
                    Spacer(Modifier.weight(1f))
                    if (initial != null) TextButton(onClick = { onPick(null) }) { Text(strings.remove) }
                    Spacer(Modifier.width(Space.xs))
                    Button(onClick = { onPick(center) }) { Text(strings.useSpot) }
                }
            }
        }
    }
}

@Composable
private fun SearchResults(results: List<Place>?, error: Boolean, onChoose: (Place) -> Unit, modifier: Modifier) {
    val strings = LocalStrings.current
    if (results == null && !error) return
    Surface(shape = RoundedCornerShape(14.dp), color = AppColors.Surface, shadowElevation = 6.dp, modifier = modifier.fillMaxWidth()) {
        Column {
            when {
                error -> Text(strings.searchFailed, color = AppColors.TextMuted, fontSize = 14.sp, modifier = Modifier.padding(Space.m))
                results.isNullOrEmpty() -> Text(strings.noPlaceFound, color = AppColors.TextMuted, fontSize = 14.sp, modifier = Modifier.padding(Space.m))
                else -> results.forEachIndexed { index, place ->
                    if (index > 0) HorizontalDivider(color = AppColors.Border)
                    Text(
                        place.name,
                        color = AppColors.Text,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChoose(place) }
                            .padding(horizontal = Space.m, vertical = Space.s),
                    )
                }
            }
            Text(
                "© OpenStreetMap",
                color = AppColors.TextMuted,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End).padding(end = Space.m, bottom = Space.xs),
            )
        }
    }
}

/** MapLibre map view wrapped for Compose, reporting where its centre is. */
@Composable
private fun SpotMap(
    start: LatLon?,
    moveTo: LatLon?,
    onCenterChange: (LatLon) -> Unit,
    onFailed: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnCenterChange by rememberUpdatedState(onCenterChange)
    val currentOnFailed by rememberUpdatedState(onFailed)
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    val mapView = remember {
        MapLibre.getInstance(context)
        // Texture mode so the map draws inside the dialog window like any other view.
        MapView(context, MapLibreMapOptions.createFromAttributes(context).textureMode(true)).apply { onCreate(null) }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.addOnDidFailLoadingMapListener { currentOnFailed() }
        mapView.getMapAsync { m ->
            m.setStyle(MapStyleUrl)
            m.uiSettings.isRotateGesturesEnabled = false
            m.uiSettings.isTiltGesturesEnabled = false
            val target = start ?: DefaultCenter
            m.cameraPosition = CameraPosition.Builder()
                .target(LatLng(target.latitude, target.longitude))
                .zoom(if (start != null) SpotZoom else WorldZoom)
                .build()
            val report = {
                m.cameraPosition.target?.let { t ->
                    currentOnCenterChange(LatLon(t.latitude, Orthographic.normalizeLongitude(t.longitude)))
                }
            }
            m.addOnCameraMoveListener { report() }
            m.addOnCameraIdleListener { report() }
            map = m
        }
    }

    LaunchedEffect(moveTo, map) {
        val m = map ?: return@LaunchedEffect
        val target = moveTo ?: return@LaunchedEffect
        m.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(target.latitude, target.longitude), SpotZoom))
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
