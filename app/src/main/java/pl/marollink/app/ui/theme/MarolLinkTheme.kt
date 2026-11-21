package pl.marollink.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object CarPalette {
    val Background = Color(0xFF090E16)
    val Surface = Color(0xFF121A26)
    val SurfaceElevated = Color(0xFF1B2638)
    val Border = Color(0xFF2B3B55)
    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF94A3B8)
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Danger = Color(0xFFEF4444)
}

private val AutomotiveTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
)

@Composable
fun MarolLinkTheme(
    accentHue: Float = 200f,
    content: @Composable () -> Unit
) {
    val safeHue = accentHue.coerceIn(0f, 360f)
    val primary = Color.hsv(safeHue, 0.72f, 0.96f)
    val secondary = Color.hsv((safeHue + 24f) % 360f, 0.60f, 0.90f)
    val primaryContainer = Color.hsv(safeHue, 0.65f, 0.28f)

    val colors = darkColorScheme(
        primary = primary,
        onPrimary = Color(0xFF04101A),
        primaryContainer = primaryContainer,
        onPrimaryContainer = Color(0xFFE2F4FF),
        secondary = secondary,
        onSecondary = Color(0xFF04101A),
        background = CarPalette.Background,
        onBackground = CarPalette.TextPrimary,
        surface = CarPalette.Surface,
        onSurface = CarPalette.TextPrimary,
        surfaceVariant = CarPalette.SurfaceElevated,
        onSurfaceVariant = CarPalette.TextSecondary,
        outline = CarPalette.Border,
        error = CarPalette.Danger
    )

    MaterialTheme(
        colorScheme = colors,
        typography = AutomotiveTypography,
        content = content
    )
}
