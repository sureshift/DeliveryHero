package com.deliveryninja.ui.heatmap

import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun HeatmapScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val context     = LocalContext.current
    val deliveries  by viewModel.deliveriesWithLocation.collectAsState()
    val restaurants by viewModel.allRestaurants.collectAsState()
    val customers   by viewModel.allCustomers.collectAsState()

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = "DeliveryNinja/1.0"
    }

    Column(modifier = Modifier.fillMaxSize().background(NinjaDark).padding(paddingValues)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().background(NinjaCard).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Delivery Map", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = NinjaWhite)
                Text("${deliveries.size} deliveries • ${restaurants.size} restaurants • ${customers.size} areas",
                    fontSize = 12.sp, color = NinjaGray)
            }
        }

        // Legend bar
        Row(modifier = Modifier.fillMaxWidth().background(NinjaCard)
            .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendChip("🔴 Pickup")
            LegendChip("🔵 Drop")
            LegendChip("🟠 Restaurant")
            LegendChip("🟢 Customer")
        }

        if (deliveries.isEmpty() && restaurants.isEmpty() && customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🗺️", fontSize = 52.sp)
                    Text("No location data yet", fontWeight = FontWeight.Medium, color = NinjaGray)
                    Text("Add restaurants & customer areas\nin Settings to see your map",
                        fontSize = 12.sp, color = NinjaGray.copy(0.5f), textAlign = TextAlign.Center)
                }
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(12.0)
                        val first = deliveries.firstOrNull { it.pickupLat != 0.0 }
                        val firstR = restaurants.firstOrNull { it.lat != 0.0 }
                        when {
                            first  != null -> controller.setCenter(GeoPoint(first.pickupLat, first.pickupLon))
                            firstR != null -> controller.setCenter(GeoPoint(firstR.lat, firstR.lon))
                            else           -> controller.setCenter(GeoPoint(28.6139, 77.2090))
                        }
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    deliveries.forEach { d ->
                        if (d.pickupLat != 0.0) Marker(mapView).also { m ->
                            m.position = GeoPoint(d.pickupLat, d.pickupLon)
                            m.title   = "Pickup: ${d.restaurantName.ifEmpty { d.platformName }}"
                            m.snippet = "₹${d.earnings}"
                            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(m)
                        }
                        if (d.dropLat != 0.0) Marker(mapView).also { m ->
                            m.position = GeoPoint(d.dropLat, d.dropLon)
                            m.title   = "Drop: ${d.dropAddress.ifEmpty { d.customerName }}"
                            m.snippet = d.platformName
                            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(m)
                        }
                    }
                    restaurants.filter { it.lat != 0.0 }.forEach { r ->
                        Marker(mapView).also { m ->
                            m.position = GeoPoint(r.lat, r.lon)
                            m.title   = "🏪 ${r.name}"
                            m.snippet = r.address
                            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(m)
                        }
                    }
                    customers.filter { it.lat != 0.0 }.forEach { c ->
                        Marker(mapView).also { m ->
                            m.position = GeoPoint(c.lat, c.lon)
                            m.title   = "🏠 ${c.area.ifEmpty { c.address }}"
                            m.snippet = c.address
                            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(m)
                        }
                    }
                    mapView.invalidate()
                }
            )
        }
    }
}

@Composable
fun LegendChip(text: String) {
    Text(text, fontSize = 11.sp, color = NinjaGray,
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(NinjaCardLight).padding(horizontal = 6.dp, vertical = 3.dp))
}
