package com.deliveryninja.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deliveryninja.data.models.Goal
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.theme.*
import com.deliveryninja.ui.common.ninjaTextFieldColors

@Composable
fun GoalsScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val goals        by viewModel.activeGoals.collectAsState()
    val todayEarnings by viewModel.todayEarnings.collectAsState()
    val weekEarnings  by viewModel.weekEarnings.collectAsState()
    val monthEarnings by viewModel.monthEarnings.collectAsState()
    var showAddGoal  by remember { mutableStateOf(false) }

    if (showAddGoal) AddGoalDialog(
        onDismiss = { showAddGoal = false },
        onAdd = { title, target, period -> viewModel.addGoal(title, target, period); showAddGoal = false }
    )

    Column(modifier = Modifier.fillMaxSize().background(NinjaDark).padding(paddingValues)) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Goals", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = NinjaWhite)
            FloatingActionButton(onClick = { showAddGoal = true }, containerColor = NinjaOrange,
                modifier = Modifier.size(46.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }

        // Quick progress bars
        Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickGoalCard("Daily",   todayEarnings, 1000.0,  Modifier.weight(1f))
            QuickGoalCard("Weekly",  weekEarnings,  7000.0,  Modifier.weight(1f))
            QuickGoalCard("Monthly", monthEarnings, 30000.0, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        if (goals.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎯", fontSize = 52.sp)
                    Text("No goals yet", color = NinjaGray, fontWeight = FontWeight.Medium)
                    Text("Set targets and track your progress", fontSize = 12.sp, color = NinjaGray.copy(0.5f))
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(goals) { goal ->
                    val current = when (goal.periodType) {
                        "DAILY" -> todayEarnings; "WEEKLY" -> weekEarnings
                        "MONTHLY" -> monthEarnings; else -> goal.currentAmount
                    }
                    GoalCard(goal.copy(currentAmount = current), onDelete = { viewModel.deleteGoal(goal) })
                }
            }
        }
    }
}

@Composable
fun QuickGoalCard(label: String, current: Double, target: Double, modifier: Modifier = Modifier) {
    val pct     = (current / target).coerceIn(0.0, 1.0).toFloat()
    val achieved = current >= target
    val color   = if (achieved) NinjaGreen else NinjaOrange
    Column(modifier = modifier.clip(RoundedCornerShape(14.dp)).background(NinjaCard).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 10.sp, color = NinjaGray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text("₹${String.format("%.0f", current)}", fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold, color = NinjaWhite)
        LinearProgressIndicator(progress = pct, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color, trackColor = NinjaCardLight)
        Text("${(pct * 100).toInt()}%", fontSize = 10.sp, color = color)
    }
}

@Composable
fun GoalCard(goal: Goal, onDelete: () -> Unit) {
    val pct      = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0) else 0.0
    val achieved  = goal.currentAmount >= goal.targetAmount
    val color     = if (achieved) NinjaGreen else NinjaOrange
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
        .background(if (achieved) NinjaGreen.copy(0.08f) else NinjaCard).padding(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.15f)),
                        contentAlignment = Alignment.Center) {
                        Text(if (achieved) "✅" else "🎯", fontSize = 20.sp)
                    }
                    Column {
                        Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NinjaWhite)
                        Text(goal.periodType, fontSize = 11.sp, color = NinjaGray)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Delete, null, tint = NinjaGray.copy(0.4f), modifier = Modifier.size(14.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom) {
                Text("₹${String.format("%.0f", goal.currentAmount)}",
                    fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color)
                Text("/ ₹${String.format("%.0f", goal.targetAmount)}",
                    fontSize = 14.sp, color = NinjaGray, modifier = Modifier.padding(bottom = 4.dp))
            }
            LinearProgressIndicator(progress = pct.toFloat(),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color, trackColor = NinjaCardLight)
            if (achieved)
                Text("🎉 Goal achieved! Great work!", color = NinjaGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            else
                Text("₹${String.format("%.0f", remaining)} more to reach your goal",
                    color = NinjaGray, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onAdd: (String, Double, String) -> Unit) {
    var title    by remember { mutableStateOf("") }
    var target   by remember { mutableStateOf("") }
    var period   by remember { mutableStateOf("DAILY") }
    var expanded by remember { mutableStateOf(false) }
    val periods  = listOf("DAILY", "WEEKLY", "MONTHLY")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NinjaCard,
        title = { Text("New Goal", fontWeight = FontWeight.Bold, color = NinjaWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Goal Name") }, modifier = Modifier.fillMaxWidth(),
                    colors = ninjaTextFieldColors())
                OutlinedTextField(value = target, onValueChange = { target = it },
                    label = { Text("Target (₹)") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = ninjaTextFieldColors())
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(value = period, onValueChange = {}, readOnly = true,
                        label = { Text("Period") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(), colors = ninjaTextFieldColors())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                        containerColor = NinjaCardLight) {
                        periods.forEach { p ->
                            DropdownMenuItem(text = { Text(p, color = NinjaWhite) },
                                onClick = { period = p; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { target.toDoubleOrNull()?.let { onAdd(title, it, period) } },
                enabled = title.isNotBlank() && target.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NinjaOrange)) {
                Text("Create Goal")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = NinjaGray) } }
    )
}

    focusedBorderColor   = NinjaOrange,
    unfocusedBorderColor = NinjaGray.copy(0.3f),
    focusedLabelColor    = NinjaOrange,
    unfocusedLabelColor  = NinjaGray,
    cursorColor          = NinjaOrange,
    focusedTextColor     = NinjaWhite,
    unfocusedTextColor   = NinjaWhite
)
