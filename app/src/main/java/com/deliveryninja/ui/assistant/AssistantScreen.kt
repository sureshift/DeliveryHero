package com.deliveryninja.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.theme.OrangeSwiggy
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(val text: String, val isUser: Boolean, val time: Long = System.currentTimeMillis())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val todayEarnings by viewModel.todayEarnings.collectAsState()
    val weekEarnings by viewModel.weekEarnings.collectAsState()
    val monthEarnings by viewModel.monthEarnings.collectAsState()
    val todayCount by viewModel.todayDeliveryCount.collectAsState()
    val netEarnings by viewModel.todayNetEarnings.collectAsState()
    val platformEarnings by viewModel.platformEarnings.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                "👋 Namaste! I'm your Delivery Ninja AI assistant.\n\nAsk me things like:\n• How much did I earn today?\n• Which platform pays me most?\n• What are my goals?\n• How can I earn more?",
                isUser = false
            )
        )
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun processQuery(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("today") && (q.contains("earn") || q.contains("made") || q.contains("kitna")) ->
                "💰 Today's earnings: ₹${String.format("%.0f", todayEarnings)}\n📦 Deliveries: $todayCount\n🧾 Net (after expenses): ₹${String.format("%.0f", netEarnings)}"

            q.contains("week") && q.contains("earn") ->
                "📅 This week you've earned ₹${String.format("%.0f", weekEarnings)}"

            q.contains("month") && q.contains("earn") ->
                "🗓️ This month's earnings: ₹${String.format("%.0f", monthEarnings)}"

            q.contains("platform") || q.contains("best") || q.contains("swiggy") || q.contains("rapido") -> {
                if (platformEarnings.isEmpty()) {
                    "No platform data yet. Add some deliveries first!"
                } else {
                    val top = platformEarnings.maxByOrNull { it.total }
                    val breakdown = platformEarnings.joinToString("\n") { "• ${it.platform}: ₹${String.format("%.0f", it.total)}" }
                    "🏆 Best platform this month: ${top?.platform}\n\nBreakdown:\n$breakdown"
                }
            }

            q.contains("goal") ->
                "🎯 Check the Goals tab to see your targets!\n\nYou can set daily, weekly, and monthly earning goals there."

            q.contains("tip") || q.contains("earn more") || q.contains("improve") ->
                "💡 Tips to earn more:\n\n1. 🕐 Work during peak hours (12-2pm, 7-9pm)\n2. 🗺️ Stay near high-density areas\n3. 📱 Keep all apps active simultaneously\n4. ⭐ Maintain high ratings for priority orders\n5. 🌧️ Work during rain/festivals for surge pricing"

            q.contains("expense") || q.contains("fuel") || q.contains("cost") ->
                "⛽ Go to Earnings → Expenses tab to track your fuel and other costs.\n\nNet earnings = Gross - Expenses"

            q.contains("how many") && q.contains("deliver") ->
                "📦 Today you've completed $todayCount deliveries"

            q.contains("average") || q.contains("per delivery") ->
                if (todayCount > 0) "📊 Today's avg per delivery: ₹${String.format("%.0f", todayEarnings / todayCount)}"
                else "No deliveries today yet!"

            q.contains("heatmap") || q.contains("map") || q.contains("zone") ->
                "🗺️ Check the Map tab! It shows where your deliveries are concentrated.\n\nHint: Add pickup/drop coordinates when logging deliveries to see your heatmap."

            q.contains("notification") || q.contains("auto") ->
                "🔔 To enable auto-capture from delivery apps:\n1. Go to phone Settings\n2. Search 'Notification access'\n3. Enable Delivery Ninja\n\nI'll then automatically capture your earnings!"

            q.contains("hello") || q.contains("hi") || q.contains("namaste") ->
                "👋 Namaste! Ready to help you track your deliveries. What would you like to know?"

            else ->
                "🤔 I can help you with:\n• Earnings (today/week/month)\n• Platform comparisons\n• Delivery counts\n• Tips to earn more\n• Expense tracking\n• Goals progress\n\nWhat would you like to know?"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(OrangeSwiggy, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🤖", fontSize = 20.sp) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Mitra AI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Your delivery assistant", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Divider()

        // Suggestions
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Today's earnings", "Best platform", "Earn more tips").forEach { suggestion ->
                SuggestionChip(
                    onClick = {
                        messages.add(ChatMessage(suggestion, isUser = true))
                        val reply = processQuery(suggestion)
                        messages.add(ChatMessage(reply, isUser = false))
                        scope.launch { listState.animateScrollToItem(messages.size - 1) }
                    },
                    label = { Text(suggestion, fontSize = 11.sp) }
                )
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask me anything...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 2
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val q = inputText
                        inputText = ""
                        messages.add(ChatMessage(q, isUser = true))
                        val reply = processQuery(q)
                        messages.add(ChatMessage(reply, isUser = false))
                        scope.launch { listState.animateScrollToItem(messages.size - 1) }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(OrangeSwiggy, RoundedCornerShape(24.dp))
            ) {
                Icon(Icons.Default.Send, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                color = if (message.isUser) OrangeSwiggy else Color(0xFFF0F0F0)
            ) {
                Text(
                    message.text,
                    modifier = Modifier.padding(12.dp),
                    color = if (message.isUser) Color.White else Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            Text(
                sdf.format(Date(message.time)),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
