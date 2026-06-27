package com.deliveryninja.ui.goals

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
import com.deliveryninja.data.models.Goal
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.theme.OrangeSwiggy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val goals by viewModel.activeGoals.collectAsState()
    val todayEarnings by viewModel.todayEarnings.collectAsState()
    val weekEarnings by viewModel.weekEarnings.collectAsState()
    val monthEarnings by viewModel.monthEarnings.collectAsState()

    var showAddGoal by remember { mutableStateOf(false) }

    if (showAddGoal) {
        AddGoalDialog(
            onDismiss = { showAddGoal = false },
            onAdd = { title, target, period ->
                viewModel.addGoal(title, target, period)
                showAddGoal = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Goals & Streaks", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            FloatingActionButton(
                onClick = { showAddGoal = true },
                containerColor = OrangeSwiggy,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }

        // Quick progress cards
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickGoalCard("Daily", todayEarnings, 1000.0, Modifier.weight(1f))
            QuickGoalCard("Weekly", weekEarnings, 7000.0, Modifier.weight(1f))
            QuickGoalCard("Monthly", monthEarnings, 30000.0, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        if (goals.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No goals set yet", fontWeight = FontWeight.Medium)
                    Text("Tap + to set your earning goals", fontSize = 13.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(goals) { goal ->
                    val current = when (goal.periodType) {
                        "DAILY" -> todayEarnings
                        "WEEKLY" -> weekEarnings
                        "MONTHLY" -> monthEarnings
                        else -> goal.currentAmount
                    }
                    GoalCard(goal.copy(currentAmount = current), onDelete = { viewModel.deleteGoal(goal) })
                }
            }
        }
    }
}

@Composable
fun QuickGoalCard(label: String, current: Double, target: Double, modifier: Modifier = Modifier) {
    val pct = (current / target).coerceIn(0.0, 1.0)
    val achieved = current >= target

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (achieved) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text("₹${String.format("%.0f", current)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = pct.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = if (achieved) Color(0xFF4CAF50) else OrangeSwiggy
            )
            Text("${(pct * 100).toInt()}%", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun GoalCard(goal: Goal, onDelete: () -> Unit) {
    val pct = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0) else 0.0
    val achieved = goal.currentAmount >= goal.targetAmount
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achieved) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (achieved) "✅" else "🎯", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(goal.title, fontWeight = FontWeight.SemiBold)
                        Text(goal.periodType, fontSize = 11.sp, color = Color.Gray)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("₹${String.format("%.0f", goal.currentAmount)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OrangeSwiggy)
                Text("/ ₹${String.format("%.0f", goal.targetAmount)}", color = Color.Gray)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = pct.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = if (achieved) Color(0xFF4CAF50) else OrangeSwiggy
            )
            Spacer(Modifier.height(4.dp))
            if (achieved) {
                Text("🎉 Goal achieved!", color = Color(0xFF4CAF50), fontSize = 12.sp)
            } else {
                Text("₹${String.format("%.0f", remaining)} more to go", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onAdd: (String, Double, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("DAILY") }
    var expanded by remember { mutableStateOf(false) }
    val periods = listOf("DAILY", "WEEKLY", "MONTHLY")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Goal Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = target, onValueChange = { target = it },
                    label = { Text("Target Amount (₹)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(value = period, onValueChange = {}, readOnly = true,
                        label = { Text("Period") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        periods.forEach { p ->
                            DropdownMenuItem(text = { Text(p) }, onClick = { period = p; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = target.toDoubleOrNull() ?: return@Button
                    onAdd(title, t, period)
                },
                enabled = title.isNotBlank() && target.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
