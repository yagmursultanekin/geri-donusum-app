package com.geridonusum.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.geridonusum.app.model.RecyclingBin
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

@Composable
fun MapScreen(
    bins: List<RecyclingBin>,
    errorMessage: String?,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedBinId by remember { mutableStateOf<String?>(null) }
    val selectedBin = bins.firstOrNull { it.id == selectedBinId }

    val mapView = remember {
        // Harita sunucusuna uygulamayı tanıtır, önbelleği uygulamanın kendi klasöründe tutar
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(6.0)
            controller.setCenter(GeoPoint(39.0, 35.0))
        }
    }

    // İlk kutu geldiğinde haritayı oraya götür
    LaunchedEffect(bins.firstOrNull()?.id) {
        val first = bins.firstOrNull()
        if (first != null) {
            mapView.controller.setZoom(13.0)
            mapView.controller.setCenter(GeoPoint(first.latitude, first.longitude))
        }
    }

    // Harita, uygulama durdurulunca durup açılınca devam etsin
    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            mapView.onDetach()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Geri Dönüşüm Kutuları",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onLogout) {
                Text("Çıkış")
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            update = { view ->
                view.overlays.clear()
                bins.forEach { bin ->
                    val marker = Marker(view)
                    marker.position = GeoPoint(bin.latitude, bin.longitude)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = bin.name
                    marker.setOnMarkerClickListener { _, _ ->
                        selectedBinId = bin.id
                        true
                    }
                    view.overlays.add(marker)
                }
                view.invalidate()
            }
        )

        if (selectedBin != null) {
            BinDetailCard(
                bin = selectedBin,
                onClose = { selectedBinId = null }
            )
        }
    }
}