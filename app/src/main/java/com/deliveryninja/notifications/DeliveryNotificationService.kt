package com.deliveryninja.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.deliveryninja.DeliveryNinjaApp
import com.deliveryninja.data.models.Delivery
import com.deliveryninja.data.models.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeliveryNotificationService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Map of package names to platforms
    private val platformPackages = mapOf(
        "in.swiggy.deliveryapp" to Platform.SWIGGY,
        "com.shadowfax.supplyapp" to Platform.SHADOWFAX,
        "com.rapido.captain" to Platform.RAPIDO,
        "com.loadshare.partner" to Platform.LOADSHARE,
        "com.magicfleet.driver" to Platform.MAGICFLEET,
        "com.olacabs.driver" to Platform.OLA,
        "com.shiprocket.deliverypartner" to Platform.SHIPROCKET
    )

    // Earnings patterns per platform
    private val earningsPatterns = listOf(
        Regex("₹\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("Rs\\.?\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("earned\\s*₹\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("payment.*?₹\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("(\\d+(?:\\.\\d{1,2})?)\\s*credited")
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val platform = platformPackages[packageName] ?: return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        val fullText = "$title $text $bigText"

        // Check if it's an earnings/order completion notification
        if (isEarningsNotification(fullText)) {
            val earnings = extractEarnings(fullText)
            if (earnings > 0) {
                saveDeliveryFromNotification(platform, earnings, fullText)
            }
        }
    }

    private fun isEarningsNotification(text: String): Boolean {
        val keywords = listOf(
            "delivered", "completed", "earned", "payment", "credited",
            "order complete", "delivery done", "trip ended", "₹", "Rs."
        )
        return keywords.any { text.lowercase().contains(it.lowercase()) }
    }

    private fun extractEarnings(text: String): Double {
        for (pattern in earningsPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull() ?: 0.0
            }
        }
        return 0.0
    }

    private fun saveDeliveryFromNotification(platform: Platform, earnings: Double, rawText: String) {
        scope.launch {
            val delivery = Delivery(
                platform = platform.name,
                earnings = earnings,
                notes = "Auto-captured: $rawText".take(200)
            )
            DeliveryNinjaApp.getInstance().repository.addDelivery(delivery)
        }
    }
}
