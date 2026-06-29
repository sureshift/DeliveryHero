package com.deliveryninja.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.deliveryninja.R

// ── Fonts ──────────────────────────────────────────────────────────
val InterFont = FontFamily(
    Font(R.font.inter_regular,   FontWeight.Normal),
    Font(R.font.inter_medium,    FontWeight.Medium),
    Font(R.font.inter_semibold,  FontWeight.SemiBold),
    Font(R.font.inter_bold,      FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

val PoppinsFont = FontFamily(
    Font(R.font.poppins_regular,   FontWeight.Normal),
    Font(R.font.poppins_medium,    FontWeight.Medium),
    Font(R.font.poppins_semibold,  FontWeight.SemiBold),
    Font(R.font.poppins_bold,      FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

// ── Brand colors ───────────────────────────────────────────────────
val NinjaBrand      = Color(0xFFDB2648)   // primary brand red
val NinjaBrandDark  = Color(0xFFB01D38)   // darker variant
val NinjaBrandLight = Color(0xFFFF4D6D)   // lighter variant
val NinjaDark       = Color(0xFF0F0F14)
val NinjaCard       = Color(0xFF1C1C26)
val NinjaCardLight  = Color(0xFF252533)
val NinjaSurface    = Color(0xFF18181F)
val NinjaGreen      = Color(0xFF00C896)
val NinjaRed        = Color(0xFFFF4757)
val NinjaBlue       = Color(0xFF4A90FF)
val NinjaGray       = Color(0xFF8888A0)
val NinjaWhite      = Color(0xFFF0F0FF)

// Keep alias so existing files don't break
val NinjaOrange      = NinjaBrand
val NinjaOrangeDark  = NinjaBrandDark
val NinjaOrangeLight = NinjaBrandLight
val OrangeSwiggy     = NinjaBrand

// ── Color scheme ──────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = NinjaBrand,
    onPrimary        = Color.White,
    primaryContainer = NinjaBrandDark,
    secondary        = NinjaBlue,
    background       = NinjaDark,
    surface          = NinjaCard,
    surfaceVariant   = NinjaCardLight,
    onBackground     = NinjaWhite,
    onSurface        = NinjaWhite,
    onSurfaceVariant = NinjaGray,
    error            = NinjaRed,
    outline          = Color(0xFF2E2E3E)
)

// ── Typography ────────────────────────────────────────────────────
// Poppins → headings & titles
// Inter   → body, labels, numbers
private val NinjaTypography = Typography(
    headlineLarge  = TextStyle(fontFamily = PoppinsFont, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = NinjaWhite),
    headlineMedium = TextStyle(fontFamily = PoppinsFont, fontWeight = FontWeight.Bold,      fontSize = 22.sp, color = NinjaWhite),
    headlineSmall  = TextStyle(fontFamily = PoppinsFont, fontWeight = FontWeight.Bold,      fontSize = 18.sp, color = NinjaWhite),
    titleLarge     = TextStyle(fontFamily = PoppinsFont, fontWeight = FontWeight.Bold,      fontSize = 18.sp, color = NinjaWhite),
    titleMedium    = TextStyle(fontFamily = PoppinsFont, fontWeight = FontWeight.SemiBold,  fontSize = 15.sp, color = NinjaWhite),
    titleSmall     = TextStyle(fontFamily = PoppinsFont, fontWeight = FontWeight.SemiBold,  fontSize = 13.sp, color = NinjaWhite),
    bodyLarge      = TextStyle(fontFamily = InterFont,   fontWeight = FontWeight.Normal,    fontSize = 15.sp, color = NinjaWhite),
    bodyMedium     = TextStyle(fontFamily = InterFont,   fontWeight = FontWeight.Normal,    fontSize = 13.sp, color = NinjaGray),
    bodySmall      = TextStyle(fontFamily = InterFont,   fontWeight = FontWeight.Normal,    fontSize = 11.sp, color = NinjaGray),
    labelLarge     = TextStyle(fontFamily = InterFont,   fontWeight = FontWeight.SemiBold,  fontSize = 13.sp, color = NinjaWhite),
    labelMedium    = TextStyle(fontFamily = InterFont,   fontWeight = FontWeight.Medium,    fontSize = 11.sp, color = NinjaGray),
    labelSmall     = TextStyle(fontFamily = InterFont,   fontWeight = FontWeight.Medium,    fontSize = 10.sp, color = NinjaGray),
)

@Composable
fun DeliveryNinjaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = NinjaTypography,
        content     = content
    )
}
