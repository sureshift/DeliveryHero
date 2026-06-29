package com.deliveryninja.ui.earnings

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deliveryninja.data.models.Delivery
import com.deliveryninja.data.models.Expense
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.common.AddDeliveryDialog
import com.deliveryninja.ui.common.AddExpenseDialog
import com.deliveryninja.ui.common.PlatformLogo
import com.deliveryninja.ui.common.parseColor
import com.deliveryninja.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val allDeliveries    by viewModel.allDeliveries.collectAsState()
    val allExpenses      by viewModel.allExpenses.collectAsState()
    val monthEarnings    by viewModel.monthEarnings.collectAsState()
    val platformEarnings by viewModel.platformEarnings.collectAsState()
    val platforms        by viewModel.platforms.collectAsState()
    val restaurants      by viewModel.allRestaurants.collectAsState()
    val customers        by viewModel.allCustomers.collectAsState()
    var selectedTab      by remember { mutableIntStateOf(0) }
    var showAddDelivery  by remember { mutableStateOf(false) }
    var showAddExpense   by remember { mutableStateOf(false) }

    if (showAddDelivery) AddDeliveryDialog(
        platforms = platforms, restaurants = restaurants, customers = customers,
        onDismiss = { showAddDelivery = false },
        onAdd = { pName, e, tip, dist, rName, pAddr, pLat, pLon, cName, dAddr, dLat, dLon, notes ->
            viewModel.addDelivery(pName, e, tip, dist, rName, pAddr, pLat, pLon, cName, dAddr, dLat, dLon, notes)
            showAddDelivery = false
        }
    )
    if (showAddExpense) AddExpenseDialog(
        onDismiss = { showAddExpense = false },
        onAdd = { type, amount, desc -> viewModel.addExpense(type, amount, desc); showAddExpense = false }
    )

    Column(modifier = Modifier.fillMaxSize().background(NinjaDark).padding(paddingValues)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Earnings", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = NinjaWhite)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showAddExpense = true },
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(NinjaCard)) {
                    Icon(Icons.Default.Receipt, null, tint = NinjaGray, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showAddDelivery = true },
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(NinjaOrange)) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = NinjaCard,
            contentColor     = NinjaOrange,
            modifier         = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp))
        ) {
            listOf("Deliveries", "Expenses", "Summary").forEachIndexed { i, title ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i },
                    text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor   = NinjaOrange,
                    unselectedContentColor = NinjaGray
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        when (selectedTab) {
            0 -> DeliveriesList(allDeliveries, platforms.associate { it.name to it }, onDelete = { viewModel.deleteDelivery(it) })
            1 -> ExpensesList(allExpenses, onDelete = { viewModel.deleteExpense(it) })
            2 -> SummaryTab(monthEarnings, platformEarnings.map { it.platformName to it.total }, allExpenses)
        }
    }
}

@Composable
fun DeliveriesList(deliveries: List<Delivery>, platformMap: Map<String, com.deliveryninja.data.models.Platform>, onDelete: (Delivery) -> Unit) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    if (deliveries.isEmpty()) {
        EmptyEarnings()
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(deliveries) { d ->
                val plat  = platformMap[d.platformName]
                val color = plat?.let { parseColor(it.colorHex) } ?: NinjaOrange
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NinjaCard).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (plat != null) PlatformLogo(plat, 44)
                    else Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.15f)),
                        contentAlignment = Alignment.Center) {
                        Text(d.platformName.first().toString(), color = color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(d.platformName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NinjaWhite)
                        if (d.restaurantName.isNotEmpty()) Text("🏪 ${d.restaurantName}", fontSize = 11.sp, color = NinjaGray)
                        if (d.dropAddress.isNotEmpty()) Text("📍 ${d.dropAddress.take(28)}…", fontSize = 11.sp, color = NinjaGray)
                        if (d.distanceKm > 0) Text("${String.format("%.1f", d.distanceKm)} km", fontSize = 10.sp, color = NinjaGray.copy(0.6f))
                        Text(sdf.format(Date(d.createdAt)), fontSize = 10.sp, color = NinjaGray.copy(0.5f))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${String.format("%.0f", d.earnings + d.tipAmount)}",
                            fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = NinjaOrange)
                        if (d.tipAmount > 0) Text("+₹${String.format("%.0f", d.tipAmount)} tip", fontSize = 10.sp, color = NinjaGreen)
                        IconButton(onClick = { onDelete(d) }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Delete, null, tint = NinjaGray.copy(0.4f), modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensesList(expenses: List<Expense>, onDelete: (Expense) -> Unit) {
    val sdf   = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val icons = mapOf("FUEL" to "⛽", "MAINTENANCE" to "🔧", "FOOD" to "🍱", "OTHER" to "💰")
    if (expenses.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No expenses yet", color = NinjaGray)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses) { e ->
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NinjaCard).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(NinjaRed.copy(0.12f)),
                        contentAlignment = Alignment.Center) { Text(icons[e.type] ?: "💰", fontSize = 22.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(e.type, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NinjaWhite)
                        if (e.description.isNotEmpty()) Text(e.description, fontSize = 12.sp, color = NinjaGray)
                        Text(sdf.format(Date(e.date)), fontSize = 10.sp, color = NinjaGray.copy(0.5f))
                    }
                    Text("-₹${String.format("%.0f", e.amount)}",
                        fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = NinjaRed)
                    IconButton(onClick = { onDelete(e) }, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, null, tint = NinjaGray.copy(0.4f), modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryTab(monthEarnings: Double, platformData: List<Pair<String, Double>>, expenses: List<Expense>) {
    val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0) }
    val monthExpenses = expenses.filter { it.date >= cal.timeInMillis }.sumOf { it.amount }
    val net = monthEarnings - monthExpenses

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(NinjaOrange, NinjaOrangeDark))).padding(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("THIS MONTH", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color.White.copy(0.7f), letterSpacing = 1.5.sp)
                    Text("₹${String.format("%.0f", monthEarnings)}",
                        fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Divider(color = Color.White.copy(0.25f), modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("EXPENSES", fontSize = 10.sp, color = Color.White.copy(0.6f), letterSpacing = 1.sp)
                            Text("-₹${String.format("%.0f", monthExpenses)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("NET INCOME", fontSize = 10.sp, color = Color.White.copy(0.6f), letterSpacing = 1.sp)
                            Text("₹${String.format("%.0f", net)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }
        }
        if (platformData.isNotEmpty()) {
            item { Text("PLATFORM BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = NinjaGray, letterSpacing = 1.sp) }
            items(platformData) { (name, total) ->
                val pct = if (monthEarnings > 0) (total / monthEarnings * 100).toInt() else 0
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(NinjaCard).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, fontWeight = FontWeight.SemiBold, color = NinjaWhite)
                        Text("₹${String.format("%.0f", total)}  ($pct%)", color = NinjaGray, fontSize = 13.sp)
                    }
                    LinearProgressIndicator(
                        progress = (pct / 100f).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color            = NinjaOrange,
                        trackColor       = NinjaCardLight
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyEarnings() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("💰", fontSize = 48.sp)
            Text("No deliveries yet", color = NinjaGray, fontWeight = FontWeight.Medium)
            Text("Tap + to log a delivery", fontSize = 12.sp, color = NinjaGray.copy(0.5f))
        }
    }
}
