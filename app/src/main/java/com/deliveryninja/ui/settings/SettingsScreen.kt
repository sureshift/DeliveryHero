package com.deliveryninja.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deliveryninja.data.models.Customer
import com.deliveryninja.data.models.Platform
import com.deliveryninja.data.models.Restaurant
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.common.*
import com.deliveryninja.ui.theme.*

@Composable
fun SettingsScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val platforms   by viewModel.platforms.collectAsState()
    val restaurants by viewModel.allRestaurants.collectAsState()
    val customers   by viewModel.allCustomers.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPlatform  by remember { mutableStateOf(false) }
    var showAddRestaurant by remember { mutableStateOf(false) }
    var showAddCustomer  by remember { mutableStateOf(false) }

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

    Column(modifier = Modifier.fillMaxSize().background(NinjaDark).padding(paddingValues)) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = NinjaWhite)
            FloatingActionButton(
                onClick = { when (selectedTab) { 0 -> showAddPlatform = true; 1 -> showAddRestaurant = true; 2 -> showAddCustomer = true } },
                containerColor = NinjaOrange, modifier = Modifier.size(46.dp), shape = RoundedCornerShape(14.dp)
            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
        }

        TabRow(selectedTabIndex = selectedTab, containerColor = NinjaCard, contentColor = NinjaOrange,
            modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp))) {
            listOf("Platforms (${platforms.size})", "Restaurants", "Customers").forEachIndexed { i, title ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = NinjaOrange, unselectedContentColor = NinjaGray)
            }
        }
        Spacer(Modifier.height(8.dp))

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
        NinjaEmptyState("No platforms", "Tap + to add a delivery platform")
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(platforms) { p ->
                val color = parseColor(p.colorHex)
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NinjaCard).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    PlatformLogo(p, size = 48)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NinjaWhite)
                        Text(p.colorHex, fontSize = 11.sp, color = color)
                        if (p.logoUri.isNotEmpty()) Text("Custom logo ✓", fontSize = 10.sp, color = NinjaGreen)
                    }
                    Box(Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(color))
                    Spacer(Modifier.width(10.dp))
                    IconButton(onClick = { onDelete(p) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = NinjaGray.copy(0.4f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantsList(restaurants: List<Restaurant>, onDelete: (Restaurant) -> Unit) {
    if (restaurants.isEmpty()) {
        NinjaEmptyState("No restaurants", "Tap + to add pickup locations")
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(restaurants) { r ->
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NinjaCard).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(NinjaOrange.copy(0.12f)),
                        contentAlignment = Alignment.Center) { Text("🏪", fontSize = 22.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.name, fontWeight = FontWeight.Bold, color = NinjaWhite)
                        if (r.address.isNotEmpty()) Text(r.address, fontSize = 12.sp, color = NinjaGray)
                        if (r.lat != 0.0) Text("📍 ${String.format("%.4f", r.lat)}, ${String.format("%.4f", r.lon)}", fontSize = 10.sp, color = NinjaGray.copy(0.5f))
                    }
                    IconButton(onClick = { onDelete(r) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = NinjaGray.copy(0.4f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomersList(customers: List<Customer>, onDelete: (Customer) -> Unit) {
    if (customers.isEmpty()) {
        NinjaEmptyState("No customer areas", "Tap + to add drop locations")
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(customers) { c ->
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NinjaCard).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(NinjaGreen.copy(0.12f)),
                        contentAlignment = Alignment.Center) { Text("🏠", fontSize = 22.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        if (c.area.isNotEmpty()) Text(c.area, fontWeight = FontWeight.Bold, color = NinjaWhite)
                        if (c.address.isNotEmpty()) Text(c.address, fontSize = 12.sp, color = NinjaGray)
                        if (c.name.isNotEmpty()) Text("👤 ${c.name}", fontSize = 11.sp, color = NinjaGray)
                        if (c.lat != 0.0) Text("📍 ${String.format("%.4f", c.lat)}, ${String.format("%.4f", c.lon)}", fontSize = 10.sp, color = NinjaGray.copy(0.5f))
                    }
                    IconButton(onClick = { onDelete(c) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = NinjaGray.copy(0.4f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NinjaEmptyState(title: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📋", fontSize = 48.sp)
            Text(title, fontWeight = FontWeight.Medium, color = NinjaGray)
            Text(subtitle, fontSize = 12.sp, color = NinjaGray.copy(0.5f))
        }
    }
}
