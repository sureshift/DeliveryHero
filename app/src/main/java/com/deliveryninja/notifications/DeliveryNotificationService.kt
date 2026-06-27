package com.deliveryninja.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.deliveryninja.DeliveryNinjaApp
import com.deliveryninja.data.models.Delivery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeliveryNotificationService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Map of package names to platform string names
    private val platformPackages = mapOf(
        "in.swiggy.deliveryapp"          to "Swiggy",
        "com.shadowfax.supplyapp"         to "Shadowfax",
        "com.rapido.captain"              to "Rapido",
        "com.loadshare.partner"           to "Loadshare",
        "com.magicfleet.driver"           to "MagicFleet",
        "com.olacabs.driver"              to "Ola",
        "com.shiprocket.deliverypartner"  to "Shiprocket"
    )

    private val earningsPatterns = listOf(
        Regex("₹\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("Rs\\.?\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("earned\\s*₹\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("payment.*?₹\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("(\\d+(?:\\.\\d{1,2})?)\\s*credited")
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val platformName = platformPackages[sbn.packageName] ?: return

        val extras = sbn.notification.extras
        val title   = extras.getString("android.title") ?: ""
        val text    = extras.getString("android.text") ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""
        val fullText = "$title $text $bigText"

        if (isEarningsNotification(fullText)) {
            val earnings = extractEarnings(fullText)
            if (earnings > 0) {
                scope.launch {
                    val delivery = Delivery(
                        platformName = platformName,
                        earnings = earnings,
                        notes = "Auto-captured: ${fullText.take(150)}"
                    )
                    DeliveryNinjaApp.getInstance().repository.addDelivery(delivery)
                }
            }
        }
    }

    private fun isEarningsNotification(text: String): Boolean {
        val keywords = listOf("delivered","completed","earned","payment","credited",
            "order complete","delivery done","trip ended","₹","Rs.")
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    private fun extractEarnings(text: String): Double {
        for (pattern in earningsPatterns) {
            val match = pattern.find(text)
            if (match != null) return match.groupValues[1].toDoubleOrNull() ?: 0.0
        }
        return 0.0
    }
}
