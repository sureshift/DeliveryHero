package com.deliveryninja.ui.heatmap

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.deliveryninja.ui.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun HeatmapScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val deliveries by viewModel.deliveriesWithLocation.collectAsState()
    val restaurants by viewModel.allRestaurants.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = "DeliveryNinja/1.0"
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Delivery Map", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${deliveries.size} deliveries • ${restaurants.size} restaurants • ${customers.size} areas", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // Legend
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem("🔴", "Pickup")
            LegendItem("🔵", "Drop")
            LegendItem("🟠", "Restaurant")
            LegendItem("🟢", "Customer Area")
        }

        if (deliveries.isEmpty() && restaurants.isEmpty() && customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🗺️", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No location data yet", fontWeight = FontWeight.Medium)
                    Text("Add restaurants and customer locations\nin Settings to see your map", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
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
                        // Center on first available location
                        val firstDelivery = deliveries.firstOrNull { it.pickupLat != 0.0 }
                        val firstRestaurant = restaurants.firstOrNull { it.lat != 0.0 }
                        when {
                            firstDelivery != null -> controller.setCenter(GeoPoint(firstDelivery.pickupLat, firstDelivery.pickupLon))
                            firstRestaurant != null -> controller.setCenter(GeoPoint(firstRestaurant.lat, firstRestaurant.lon))
                            else -> controller.setCenter(GeoPoint(28.6139, 77.2090)) // Delhi default
                        }
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()
                    // Delivery pickup markers
                    deliveries.forEach { d ->
                        if (d.pickupLat != 0.0) {
                            Marker(mapView).also { m ->
                                m.position = GeoPoint(d.pickupLat, d.pickupLon)
                                m.title = "Pickup: ${d.restaurantName.ifEmpty { d.platformName }}"
                                m.snippet = "₹${d.earnings}"
                                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(m)
                            }
                        }
                        if (d.dropLat != 0.0) {
                            Marker(mapView).also { m ->
                                m.position = GeoPoint(d.dropLat, d.dropLon)
                                m.title = "Drop: ${d.dropAddress.ifEmpty { d.customerName }}"
                                m.snippet = d.platformName
                                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(m)
                            }
                        }
                    }
                    // Restaurant markers
                    restaurants.filter { it.lat != 0.0 }.forEach { r ->
                        Marker(mapView).also { m ->
                            m.position = GeoPoint(r.lat, r.lon)
                            m.title = "🏪 ${r.name}"
                            m.snippet = r.address
                            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(m)
                        }
                    }
                    // Customer area markers
                    customers.filter { it.lat != 0.0 }.forEach { c ->
                        Marker(mapView).also { m ->
                            m.position = GeoPoint(c.lat, c.lon)
                            m.title = "🏠 ${c.area.ifEmpty { c.address }}"
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
fun LegendItem(emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}
