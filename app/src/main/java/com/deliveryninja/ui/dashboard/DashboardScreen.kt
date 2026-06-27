package com.deliveryninja.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.common.AddDeliveryDialog
import com.deliveryninja.ui.common.parseColor
import com.deliveryninja.ui.theme.OrangeSwiggy
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val todayEarnings by viewModel.todayEarnings.collectAsState()
    val weekEarnings by viewModel.weekEarnings.collectAsState()
    val monthEarnings by viewModel.monthEarnings.collectAsState()
    val todayCount by viewModel.todayCount.collectAsState()
    val netEarnings by viewModel.todayNetEarnings.collectAsState()
    val recentDeliveries by viewModel.recentDeliveries.collectAsState()
    val platformEarnings by viewModel.platformEarnings.collectAsState()
    val platforms by viewModel.platforms.collectAsState()
    val topRestaurants by viewModel.topRestaurants.collectAsState()
    var showAddDelivery by remember { mutableStateOf(false) }
    val restaurants by viewModel.allRestaurants.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()

    if (showAddDelivery) {
        AddDeliveryDialog(
            platforms = platforms,
            restaurants = restaurants,
            customers = customers,
            onDismiss = { showAddDelivery = false },
            onAdd = { pName, e, tip, dist, rName, pAddr, pLat, pLon, cName, dAddr, dLat, dLon, notes ->
                viewModel.addDelivery(pName, e, tip, dist, rName, pAddr, pLat, pLon, cName, dAddr, dLat, dLon, notes)
                showAddDelivery = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("🥷 Delivery Ninja", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OrangeSwiggy)
                    Text(SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(Date()), fontSize = 13.sp, color = Color.Gray)
                }
                FloatingActionButton(onClick = { showAddDelivery = true }, containerColor = OrangeSwiggy, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = OrangeSwiggy), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Today's Earnings", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text("₹${String.format("%.0f", todayEarnings)}", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TwoWheeler, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("$todayCount deliveries", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Net ₹${String.format("%.0f", netEarnings)}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EarningCard("This Week", weekEarnings, Modifier.weight(1f))
                EarningCard("This Month", monthEarnings, Modifier.weight(1f))
            }
        }

        if (platformEarnings.isNotEmpty()) {
            item {
                Text("This Month by Platform", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(platformEarnings) { pe ->
                        val platform = platforms.find { it.name == pe.platformName }
                        val color = platform?.let { parseColor(it.colorHex) } ?: OrangeSwiggy
                        Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.1f)) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(pe.platformName, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
                                Text("₹${String.format("%.0f", pe.total)}", fontWeight = FontWeight.Bold, color = color)
                            }
                        }
                    }
                }
            }
        }

        if (topRestaurants.isNotEmpty()) {
            item {
                Text("Top Restaurants", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                topRestaurants.take(3).forEach { rs ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🏪", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(rs.restaurantName, modifier = Modifier.weight(1f), fontSize = 13.sp)
                        Text("${rs.count} orders", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("₹${String.format("%.0f", rs.total)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OrangeSwiggy)
                    }
                }
            }
        }

        item { Text("Recent Deliveries", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }

        if (recentDeliveries.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛵", fontSize = 40.sp)
                            Text("No deliveries yet", color = Color.Gray)
                            Text("Tap + to add your first delivery", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        } else {
            items(recentDeliveries) { delivery ->
                val platform = viewModel.platforms.value.find { it.name == delivery.platformName }
                val color = platform?.let { parseColor(it.colorHex) } ?: OrangeSwiggy
                DeliveryCard(
                    platformName = delivery.platformName,
                    platformColor = color,
                    restaurantName = delivery.restaurantName,
                    dropAddress = delivery.dropAddress,
                    earnings = delivery.earnings + delivery.tipAmount,
                    time = delivery.createdAt,
                    onDelete = { viewModel.deleteDelivery(delivery) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun EarningCard(label: String, amount: Double, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text("₹${String.format("%.0f", amount)}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
fun DeliveryCard(
    platformName: String, platformColor: Color,
    restaurantName: String, dropAddress: String,
    earnings: Double, time: Long, onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(platformColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center) {
                Text(platformName.first().toString(), color = platformColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(platformName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                if (restaurantName.isNotEmpty()) Text("🏪 $restaurantName", fontSize = 11.sp, color = Color.Gray)
                if (dropAddress.isNotEmpty()) Text("📍 $dropAddress", fontSize = 11.sp, color = Color.Gray)
                Text(sdf.format(Date(time)), fontSize = 11.sp, color = Color.LightGray)
            }
            Text("₹${String.format("%.0f", earnings)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OrangeSwiggy)
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }
    }
}
