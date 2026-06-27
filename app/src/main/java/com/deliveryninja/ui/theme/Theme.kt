package com.deliveryninja.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OrangeSwiggy = Color(0xFFFC8019)
val DeepBlue = Color(0xFF1E3A5F)
val LightOrange = Color(0xFFFFF3E0)
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFF44336)
val NeutralGray = Color(0xFF9E9E9E)

private val LightColorScheme = lightColorScheme(
    primary = OrangeSwiggy,
    onPrimary = Color.White,
    primaryContainer = LightOrange,
    secondary = DeepBlue,
    onSecondary = Color.White,
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = ErrorRed
)

@Composable
fun DeliveryNinjaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
