package com.deliveryninja.ui.earnings

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
import com.deliveryninja.data.models.Delivery
import com.deliveryninja.data.models.Expense
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.common.AddDeliveryDialog
import com.deliveryninja.ui.common.AddExpenseDialog
import com.deliveryninja.ui.common.parseColor
import com.deliveryninja.ui.theme.OrangeSwiggy
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val allDeliveries by viewModel.allDeliveries.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val monthEarnings by viewModel.monthEarnings.collectAsState()
    val platformEarnings by viewModel.platformEarnings.collectAsState()
    val platforms by viewModel.platforms.collectAsState()
    val restaurants by viewModel.allRestaurants.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDelivery by remember { mutableStateOf(false) }
    var showAddExpense by remember { mutableStateOf(false) }

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

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Earnings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = { showAddDelivery = true }) { Icon(Icons.Default.Add, null, tint = OrangeSwiggy) }
                IconButton(onClick = { showAddExpense = true }) { Icon(Icons.Default.Receipt, null, tint = Color.Gray) }
            }
        }
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Deliveries") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Expenses") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Summary") })
        }
        when (selectedTab) {
            0 -> DeliveriesList(allDeliveries, platforms.associate { it.name to it.colorHex }, onDelete = { viewModel.deleteDelivery(it) })
            1 -> ExpensesList(allExpenses, onDelete = { viewModel.deleteExpense(it) })
            2 -> SummaryTab(monthEarnings, platformEarnings.map { it.platformName to it.total }, allExpenses)
        }
    }
}

@Composable
fun DeliveriesList(deliveries: List<Delivery>, platformColors: Map<String, String>, onDelete: (Delivery) -> Unit) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    if (deliveries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No deliveries yet", color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(deliveries) { d ->
                val color = platformColors[d.platformName]?.let { parseColor(it) } ?: OrangeSwiggy
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
                            Text(d.platformName.first().toString(), color = color, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(d.platformName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            if (d.restaurantName.isNotEmpty()) Text("🏪 ${d.restaurantName}", fontSize = 12.sp, color = Color.Gray)
                            if (d.pickupAddress.isNotEmpty()) Text("From: ${d.pickupAddress}", fontSize = 11.sp, color = Color.Gray)
                            if (d.dropAddress.isNotEmpty()) Text("To: ${d.dropAddress}", fontSize = 11.sp, color = Color.Gray)
                            if (d.customerName.isNotEmpty()) Text("👤 ${d.customerName}", fontSize = 11.sp, color = Color.Gray)
                            if (d.distanceKm > 0) Text("📏 ${d.distanceKm} km", fontSize = 11.sp, color = Color.Gray)
                            Text(sdf.format(Date(d.createdAt)), fontSize = 11.sp, color = Color.LightGray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${String.format("%.0f", d.earnings + d.tipAmount)}", fontWeight = FontWeight.Bold, color = OrangeSwiggy)
                            if (d.tipAmount > 0) Text("+₹${String.format("%.0f", d.tipAmount)} tip", fontSize = 10.sp, color = Color.Gray)
                            IconButton(onClick = { onDelete(d) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensesList(expenses: List<Expense>, onDelete: (Expense) -> Unit) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val icons = mapOf("FUEL" to "⛽", "MAINTENANCE" to "🔧", "FOOD" to "🍱", "OTHER" to "💰")
    if (expenses.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No expenses yet", color = Color.Gray) }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses) { e ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(icons[e.type] ?: "💰", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(e.type, fontWeight = FontWeight.Medium)
                            if (e.description.isNotEmpty()) Text(e.description, fontSize = 12.sp, color = Color.Gray)
                            Text(sdf.format(Date(e.date)), fontSize = 11.sp, color = Color.Gray)
                        }
                        Text("-₹${String.format("%.0f", e.amount)}", fontWeight = FontWeight.Bold, color = Color.Red)
                        IconButton(onClick = { onDelete(e) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
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
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = OrangeSwiggy)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("This Month", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text("₹${String.format("%.0f", monthEarnings)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column { Text("Expenses", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp); Text("-₹${String.format("%.0f", monthExpenses)}", color = Color.White) }
                        Column { Text("Net Income", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp); Text("₹${String.format("%.0f", monthEarnings - monthExpenses)}", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
        if (platformData.isNotEmpty()) {
            item { Text("Platform Breakdown", fontWeight = FontWeight.SemiBold) }
            items(platformData) { (name, total) ->
                val pct = if (monthEarnings > 0) (total / monthEarnings * 100).toInt() else 0
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(name, fontWeight = FontWeight.Medium)
                            Text("₹${String.format("%.0f", total)} ($pct%)")
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(progress = (pct / 100f).coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth(), color = OrangeSwiggy)
                    }
                }
            }
        }
    }
}
