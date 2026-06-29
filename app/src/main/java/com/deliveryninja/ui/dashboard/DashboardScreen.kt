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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deliveryninja.data.models.Platform
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.common.AddDeliveryDialog
import com.deliveryninja.ui.common.PlatformLogo
import com.deliveryninja.ui.common.parseColor
import com.deliveryninja.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val todayEarnings   by viewModel.todayEarnings.collectAsState()
    val weekEarnings    by viewModel.weekEarnings.collectAsState()
    val monthEarnings   by viewModel.monthEarnings.collectAsState()
    val todayCount      by viewModel.todayCount.collectAsState()
    val netEarnings     by viewModel.todayNetEarnings.collectAsState()
    val recentDeliveries by viewModel.recentDeliveries.collectAsState()
    val platformEarnings by viewModel.platformEarnings.collectAsState()
    val platforms       by viewModel.platforms.collectAsState()
    val topRestaurants  by viewModel.topRestaurants.collectAsState()
    val restaurants     by viewModel.allRestaurants.collectAsState()
    val customers       by viewModel.allCustomers.collectAsState()
    var showAddDelivery by remember { mutableStateOf(false) }

    if (showAddDelivery) {
        AddDeliveryDialog(
            platforms = platforms, restaurants = restaurants, customers = customers,
            onDismiss = { showAddDelivery = false },
            onAdd = { pName, e, tip, dist, rName, pAddr, pLat, pLon, cName, dAddr, dLat, dLon, notes ->
                viewModel.addDelivery(pName, e, tip, dist, rName, pAddr, pLat, pLon, cName, dAddr, dLat, dLon, notes)
                showAddDelivery = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).background(NinjaDark),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Header ──────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    Brush.verticalGradient(listOf(NinjaCard, NinjaDark))
                ).padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🥷 Delivery Ninja", fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold, color = NinjaOrange)
                        Text(
                            SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date()),
                            fontSize = 12.sp, color = NinjaGray
                        )
                    }
                    FloatingActionButton(
                        onClick = { showAddDelivery = true },
                        containerColor = NinjaOrange,
                        contentColor   = Color.White,
                        modifier       = Modifier.size(46.dp),
                        shape          = RoundedCornerShape(14.dp)
                    ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(22.dp)) }
                }
            }
        }

        // ── Hero earnings card ───────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(listOf(NinjaOrange, NinjaOrangeDark, Color(0xFFCC4400)))
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TODAY'S EARNINGS", fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("₹${String.format("%.0f", todayEarnings)}",
                        fontSize = 44.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        HeroStat("Deliveries", todayCount.toString(), Icons.Default.TwoWheeler)
                        HeroStat("Net Earned", "₹${String.format("%.0f", netEarnings)}", Icons.Default.Payments)
                    }
                }
            }
        }

        // ── Week / Month mini cards ──────────────────────────────
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStatCard("This Week",  "₹${String.format("%.0f", weekEarnings)}",  Modifier.weight(1f))
                MiniStatCard("This Month", "₹${String.format("%.0f", monthEarnings)}", Modifier.weight(1f))
            }
        }

        // ── Platform earnings ────────────────────────────────────
        if (platformEarnings.isNotEmpty()) {
            item {
                SectionHeader("Platforms This Month")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(platformEarnings) { pe ->
                        val plat = platforms.find { it.name == pe.platformName }
                        val color = plat?.let { parseColor(it.colorHex) } ?: NinjaOrange
                        PlatformEarningsChip(plat, pe.platformName, pe.total, color)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Top restaurants ──────────────────────────────────────
        if (topRestaurants.isNotEmpty()) {
            item {
                SectionHeader("Top Restaurants")
                Column(modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    topRestaurants.take(3).forEach { rs ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NinjaCard)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                .background(NinjaOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center) {
                                Text("🏪", fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(rs.restaurantName, modifier = Modifier.weight(1f),
                                fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NinjaWhite)
                            Text("${rs.count} orders", fontSize = 11.sp, color = NinjaGray)
                            Spacer(Modifier.width(8.dp))
                            Text("₹${String.format("%.0f", rs.total)}",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NinjaOrange)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Recent deliveries ────────────────────────────────────
        item { SectionHeader("Recent Deliveries") }

        if (recentDeliveries.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🛵", fontSize = 48.sp)
                        Text("No deliveries yet", color = NinjaGray,
                            fontWeight = FontWeight.Medium)
                        Text("Tap + to log your first delivery",
                            fontSize = 12.sp, color = NinjaGray.copy(alpha = 0.6f))
                    }
                }
            }
        } else {
            items(recentDeliveries) { delivery ->
                val plat = platforms.find { it.name == delivery.platformName }
                val color = plat?.let { parseColor(it.colorHex) } ?: NinjaOrange
                ModernDeliveryCard(
                    platform = plat,
                    platformName  = delivery.platformName,
                    platformColor = color,
                    restaurantName= delivery.restaurantName,
                    dropAddress   = delivery.dropAddress,
                    earnings      = delivery.earnings + delivery.tipAmount,
                    tip           = delivery.tipAmount,
                    distanceKm    = delivery.distanceKm,
                    time          = delivery.createdAt,
                    onDelete      = { viewModel.deleteDelivery(delivery) }
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
fun HeroStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Column {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun MiniStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(14.dp)).background(NinjaCard).padding(14.dp)) {
        Column {
            Text(label, fontSize = 11.sp, color = NinjaGray)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NinjaWhite)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        fontSize = 13.sp, fontWeight = FontWeight.Bold,
        color = NinjaGray, letterSpacing = 0.8.sp)
}

@Composable
fun PlatformEarningsChip(platform: Platform?, name: String, total: Double, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(14.dp))
        .background(color.copy(alpha = 0.12f))
        .padding(horizontal = 14.dp, vertical = 10.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (platform != null) PlatformLogo(platform, size = 32)
            else Text(name.first().toString(), color = color,
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(name, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
            Text("₹${String.format("%.0f", total)}",
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NinjaWhite)
        }
    }
}

@Composable
fun ModernDeliveryCard(
    platform: Platform?, platformName: String, platformColor: Color,
    restaurantName: String, dropAddress: String,
    earnings: Double, tip: Double, distanceKm: Double,
    time: Long, onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NinjaCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (platform != null) PlatformLogo(platform, size = 44)
        else Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
            .background(platformColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center) {
            Text(platformName.first().toString(),
                color = platformColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(platformName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NinjaWhite)
            if (restaurantName.isNotEmpty())
                Text("🏪 $restaurantName", fontSize = 11.sp, color = NinjaGray)
            if (dropAddress.isNotEmpty())
                Text("📍 ${dropAddress.take(32)}${if (dropAddress.length > 32) "…" else ""}",
                    fontSize = 11.sp, color = NinjaGray)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(sdf.format(Date(time)), fontSize = 10.sp, color = NinjaGray.copy(alpha = 0.6f))
                if (distanceKm > 0)
                    Text("• ${String.format("%.1f", distanceKm)} km",
                        fontSize = 10.sp, color = NinjaGray.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("₹${String.format("%.0f", earnings)}",
                fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = NinjaOrange)
            if (tip > 0)
                Text("+₹${String.format("%.0f", tip)} tip",
                    fontSize = 10.sp, color = NinjaGreen)
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, null, tint = NinjaGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp))
            }
        }
    }
}
