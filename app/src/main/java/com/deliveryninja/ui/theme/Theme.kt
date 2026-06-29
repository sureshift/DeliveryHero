package com.deliveryninja.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Brand colors
val NinjaOrange     = Color(0xFFFF6B00)
val NinjaOrangeDark = Color(0xFFE05A00)
val NinjaOrangeLight= Color(0xFFFF8C38)
val NinjaDark       = Color(0xFF0F0F14)
val NinjaCard       = Color(0xFF1C1C26)
val NinjaCardLight  = Color(0xFF252533)
val NinjaSurface    = Color(0xFF18181F)
val NinjaGreen      = Color(0xFF00C896)
val NinjaRed        = Color(0xFFFF4757)
val NinjaBlue       = Color(0xFF4A90FF)
val NinjaGray       = Color(0xFF8888A0)
val NinjaWhite      = Color(0xFFF0F0FF)

// Alias for compat
val OrangeSwiggy = NinjaOrange

private val DarkColorScheme = darkColorScheme(
    primary         = NinjaOrange,
    onPrimary       = Color.White,
    primaryContainer= NinjaOrangeDark,
    secondary       = NinjaBlue,
    background      = NinjaDark,
    surface         = NinjaCard,
    surfaceVariant  = NinjaCardLight,
    onBackground    = NinjaWhite,
    onSurface       = NinjaWhite,
    onSurfaceVariant= NinjaGray,
    error           = NinjaRed,
    outline         = Color(0xFF2E2E3E)
)

@Composable
fun DeliveryNinjaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(
            headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = NinjaWhite),
            headlineMedium= TextStyle(fontWeight = FontWeight.Bold,      fontSize = 22.sp, color = NinjaWhite),
            titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 18.sp, color = NinjaWhite),
            titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 15.sp, color = NinjaWhite),
            bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 15.sp, color = NinjaWhite),
            bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 13.sp, color = NinjaGray),
            labelSmall    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 11.sp, color = NinjaGray),
        ),
        content = content
    )
}
