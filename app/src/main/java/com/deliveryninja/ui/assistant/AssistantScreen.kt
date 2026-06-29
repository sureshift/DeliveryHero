package com.deliveryninja.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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
import com.deliveryninja.ui.MainViewModel
import com.deliveryninja.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(val text: String, val isUser: Boolean, val time: Long = System.currentTimeMillis())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val todayEarnings    by viewModel.todayEarnings.collectAsState()
    val weekEarnings     by viewModel.weekEarnings.collectAsState()
    val monthEarnings    by viewModel.monthEarnings.collectAsState()
    val todayCount       by viewModel.todayCount.collectAsState()
    val netEarnings      by viewModel.todayNetEarnings.collectAsState()
    val platformEarnings by viewModel.platformEarnings.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val messages  = remember {
        mutableStateListOf(
            ChatMessage("👋 Namaste! I'm your **Delivery Ninja AI**.\n\nAsk me anything:\n• How much did I earn today?\n• Which platform pays me most?\n• What's my average per delivery?\n• Tips to earn more?", isUser = false)
        )
    }
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()
    val quickReplies = listOf("Today's earnings", "Best platform", "Tips to earn more", "This week")

    fun processQuery(q: String): String {
        val ql = q.lowercase()
        return when {
            (ql.contains("today") || ql.contains("aaj")) && (ql.contains("earn") || ql.contains("kitna")) ->
                "💰 Today's earnings: ₹${String.format("%.0f", todayEarnings)}\n📦 Deliveries: $todayCount\n🧾 Net (after expenses): ₹${String.format("%.0f", netEarnings)}"
            ql.contains("week") ->
                "📅 This week: ₹${String.format("%.0f", weekEarnings)}"
            ql.contains("month") ->
                "🗓️ This month: ₹${String.format("%.0f", monthEarnings)}"
            ql.contains("platform") || ql.contains("best") -> {
                if (platformEarnings.isEmpty()) "No platform data yet. Add deliveries first!"
                else {
                    val top = platformEarnings.maxByOrNull { it.total }
                    val list = platformEarnings.joinToString("\n") { "• ${it.platformName}: ₹${String.format("%.0f", it.total)}" }
                    "🏆 Best: ${top?.platformName}\n\n$list"
                }
            }
            ql.contains("average") || ql.contains("per delivery") ->
                if (todayCount > 0) "📊 Avg per delivery today: ₹${String.format("%.0f", todayEarnings / todayCount.toDouble())}"
                else "No deliveries today yet!"
            ql.contains("tip") || ql.contains("earn more") || ql.contains("improve") ->
                "💡 Tips to earn more:\n\n1. 🕐 Peak hours: 12–2pm & 7–9pm\n2. 🌧️ Rain/festivals = surge pricing\n3. 🗺️ Stay near dense restaurant zones\n4. ⭐ High rating = priority orders\n5. 📱 Keep multiple apps active"
            ql.contains("how many") && ql.contains("deliver") ->
                "📦 Today: $todayCount deliveries"
            ql.contains("hello") || ql.contains("hi") || ql.contains("namaste") ->
                "👋 Namaste! Ready to help. What would you like to know?"
            else ->
                "🤔 I can help with:\n• Earnings (today/week/month)\n• Platform comparison\n• Delivery counts\n• Tips to earn more\n• Goals progress"
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(NinjaDark).padding(paddingValues)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().background(NinjaCard).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(NinjaOrange, NinjaOrangeDark))),
                contentAlignment = Alignment.Center) { Text("🥷", fontSize = 22.sp) }
            Column {
                Text("Ninja AI", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NinjaWhite)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(NinjaGreen))
                    Text("Online", fontSize = 11.sp, color = NinjaGreen)
                }
            }
        }

        // Quick replies
        LazyRow(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickReplies) { reply ->
                SuggestionChip(
                    onClick = {
                        messages.add(ChatMessage(reply, isUser = true))
                        messages.add(ChatMessage(processQuery(reply), isUser = false))
                        scope.launch { listState.animateScrollToItem(messages.size - 1) }
                    },
                    label = { Text(reply, fontSize = 12.sp, color = NinjaOrange) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = NinjaOrange.copy(0.1f)),
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = NinjaOrange.copy(0.3f))
                )
            }
        }

        // Messages
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
            state = listState, verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)) {
            items(messages) { msg -> ChatBubble(msg) }
        }

        // Input bar
        Row(modifier = Modifier.fillMaxWidth().background(NinjaCard).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = inputText, onValueChange = { inputText = it },
                placeholder = { Text("Ask me anything...", color = NinjaGray, fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp), maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = NinjaOrange,
                    unfocusedBorderColor = NinjaGray.copy(0.2f),
                    focusedTextColor     = NinjaWhite,
                    unfocusedTextColor   = NinjaWhite,
                    cursorColor          = NinjaOrange,
                    unfocusedContainerColor = NinjaCardLight,
                    focusedContainerColor   = NinjaCardLight
                )
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val q = inputText.trim(); inputText = ""
                        messages.add(ChatMessage(q, isUser = true))
                        messages.add(ChatMessage(processQuery(q), isUser = false))
                        scope.launch { listState.animateScrollToItem(messages.size - 1) }
                    }
                },
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (inputText.isNotBlank()) NinjaOrange else NinjaCardLight)
            ) {
                Icon(Icons.Default.Send, null,
                    tint = if (inputText.isNotBlank()) Color.White else NinjaGray,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start) {
        Column(horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)) {
            Box(modifier = Modifier.clip(RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd   = if (message.isUser) 4.dp  else 16.dp
            )).background(if (message.isUser) NinjaOrange else NinjaCard).padding(12.dp)) {
                Text(message.text, color = if (message.isUser) Color.White else NinjaWhite,
                    fontSize = 13.sp, lineHeight = 20.sp)
            }
            Text(sdf.format(Date(message.time)), fontSize = 10.sp, color = NinjaGray.copy(0.5f),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }
    }
}
