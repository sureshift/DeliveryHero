package com.deliveryninja.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deliveryninja.data.models.Customer
import com.deliveryninja.data.models.Platform
import com.deliveryninja.data.models.Restaurant
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.common.*
import com.deliveryninja.ui.theme.OrangeSwiggy

@Composable
fun SettingsScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val platforms by viewModel.platforms.collectAsState()
    val restaurants by viewModel.allRestaurants.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPlatform by remember { mutableStateOf(false) }
    var showAddRestaurant by remember { mutableStateOf(false) }
    var showAddCustomer by remember { mutableStateOf(false) }

    if (showAddPlatform) AddPlatformDialog(
        onDismiss = { showAddPlatform = false },
        onAdd = { name, color, logo -> viewModel.addPlatform(name, color, logo); showAddPlatform = false }
    )
    if (showAddRestaurant) AddRestaurantDialog(
        onDismiss = { showAddRestaurant = false },
        onAdd = { name, address, lat, lon, notes -> viewModel.addRestaurant(name, address, lat, lon, notes); showAddRestaurant = false }
    )
    if (showAddCustomer) AddCustomerDialog(
        onDismiss = { showAddCustomer = false },
        onAdd = { name, address, area, lat, lon, notes -> viewModel.addCustomer(name, address, area, lat, lon, notes); showAddCustomer = false }
    )

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            FloatingActionButton(
                onClick = { when (selectedTab) { 0 -> showAddPlatform = true; 1 -> showAddRestaurant = true; 2 -> showAddCustomer = true } },
                containerColor = OrangeSwiggy, modifier = Modifier.size(46.dp)
            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                text = { Text("Platforms (${platforms.size})") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                text = { Text("Restaurants") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                text = { Text("Customers") })
        }

        when (selectedTab) {
            0 -> PlatformsList(platforms, onDelete = { viewModel.deletePlatform(it) })
            1 -> RestaurantsList(restaurants, onDelete = { viewModel.deleteRestaurant(it) })
            2 -> CustomersList(customers, onDelete = { viewModel.deleteCustomer(it) })
        }
    }
}

@Composable
fun PlatformsList(platforms: List<Platform>, onDelete: (Platform) -> Unit) {
    if (platforms.isEmpty()) {
        EmptyState("No platforms yet", "Tap + to add your first delivery platform")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(platforms) { p ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = parseColor(p.colorHex).copy(alpha = 0.06f))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        PlatformLogo(p, size = 48)
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(p.colorHex, fontSize = 11.sp, color = parseColor(p.colorHex))
                            if (p.logoUri.isNotEmpty())
                                Text("Custom logo ✓", fontSize = 11.sp, color = Color.Gray)
                        }
                        // Color swatch
                        Box(modifier = Modifier.size(20.dp).then(
                            androidx.compose.ui.Modifier
                        ), contentAlignment = Alignment.Center) {
                            Surface(shape = RoundedCornerShape(4.dp), color = parseColor(p.colorHex), modifier = Modifier.size(20.dp)) {}
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { onDelete(p) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantsList(restaurants: List<Restaurant>, onDelete: (Restaurant) -> Unit) {
    if (restaurants.isEmpty()) {
        EmptyState("No restaurants yet", "Tap + to add pickup locations")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(restaurants) { r ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🏪", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.name, fontWeight = FontWeight.Medium)
                            if (r.address.isNotEmpty()) Text(r.address, fontSize = 12.sp, color = Color.Gray)
                            if (r.lat != 0.0) Text("📍 ${r.lat}, ${r.lon}", fontSize = 10.sp, color = Color.LightGray)
                        }
                        IconButton(onClick = { onDelete(r) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomersList(customers: List<Customer>, onDelete: (Customer) -> Unit) {
    if (customers.isEmpty()) {
        EmptyState("No customer locations yet", "Tap + to add drop locations")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(customers) { c ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🏠", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (c.area.isNotEmpty()) Text(c.area, fontWeight = FontWeight.Medium)
                            if (c.address.isNotEmpty()) Text(c.address, fontSize = 12.sp, color = Color.Gray)
                            if (c.name.isNotEmpty()) Text("👤 ${c.name}", fontSize = 11.sp, color = Color.Gray)
                            if (c.lat != 0.0) Text("📍 ${c.lat}, ${c.lon}", fontSize = 10.sp, color = Color.LightGray)
                        }
                        IconButton(onClick = { onDelete(c) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📋", fontSize = 48.sp)
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 13.sp, color = Color.Gray)
        }
    }
}
