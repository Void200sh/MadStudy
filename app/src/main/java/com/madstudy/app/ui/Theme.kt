package com.madstudy.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Palette(
    val id: String,
    val name: String,
    val note: String,
    val scheme: androidx.compose.material3.ColorScheme,
    val isDark: Boolean = false,
    val pixel: Boolean = false,
    val cardRadius: Int = 22,
    val bigRadius: Int = 26,
    val pill: Boolean = false,
    val line: Color,
    val gradA: Color,
    val gradB: Color,
    val glow: Color,
    val swatch: List<Color>
)

/* Values mirrored 1:1 from the approved HTML preview (CSS custom props). */

private val sage = Palette(
    id = "sage", name = "Cozy Sage", note = "Hangat & natural",
    scheme = lightColorScheme(
        primary = Color(0xFF7D9B80), onPrimary = Color.White,
        primaryContainer = Color(0xFFE7EEE6), onPrimaryContainer = Color(0xFF223026),
        secondary = Color(0xFFC9A87C), onSecondary = Color.White,
        secondaryContainer = Color(0xFFF3E7D3), onSecondaryContainer = Color(0xFF3A2E1C),
        tertiary = Color(0xFF8FB39A), background = Color(0xFFF7F3E9),
        onBackground = Color(0xFF28352D), surface = Color(0xFFFFFEFA),
        onSurface = Color(0xFF28352D), surfaceVariant = Color(0xFFEFEADF),
        onSurfaceVariant = Color(0xFF6C796E), outline = Color(0xFFECE6D8)
    ),
    line = Color(0xFFECE6D8),
    gradA = Color(0xFF8AA98C), gradB = Color(0xFF6E8E72), glow = Color(0x598AA98C),
    swatch = listOf(Color(0xFFF7F3E9), Color(0xFF7D9B80), Color(0xFFC9A87C))
)

private val midnight = Palette(
    id = "midnight", name = "Midnight", note = "Gelap, fokus malam", isDark = true,
    scheme = darkColorScheme(
        primary = Color(0xFFA7A1E8), onPrimary = Color(0xFF171B22),
        primaryContainer = Color(0xFF2E3440), onPrimaryContainer = Color(0xFFE7E4FF),
        secondary = Color(0xFF8FC0A9), onSecondary = Color(0xFF12201A),
        secondaryContainer = Color(0xFF2A3A33), onSecondaryContainer = Color(0xFFD3F2E3),
        tertiary = Color(0xFFE0B98A), background = Color(0xFF171B22),
        onBackground = Color(0xFFF2F1F7), surface = Color(0xFF242A33),
        onSurface = Color(0xFFF2F1F7), surfaceVariant = Color(0xFF2E3440),
        onSurfaceVariant = Color(0xFFB6B9C6), outline = Color(0xFF2E3440)
    ),
    line = Color(0xFF2E3440),
    gradA = Color(0xFFB3AEF0), gradB = Color(0xFF8A83DA), glow = Color(0x4D8A83DA),
    swatch = listOf(Color(0xFF171B22), Color(0xFFA7A1E8), Color(0xFF8FC0A9))
)

private val lavender = Palette(
    id = "lavender", name = "Lavender", note = "Lembut & tenang",
    scheme = lightColorScheme(
        primary = Color(0xFF9B86C9), onPrimary = Color.White,
        primaryContainer = Color(0xFFEDE6F7), onPrimaryContainer = Color(0xFF2C2340),
        secondary = Color(0xFFC9A87C), onSecondary = Color.White,
        secondaryContainer = Color(0xFFF5E8D6), onSecondaryContainer = Color(0xFF3A2E1C),
        tertiary = Color(0xFFB49BD8), background = Color(0xFFF6F1F8),
        onBackground = Color(0xFF2E2838), surface = Color(0xFFFFFDFF),
        onSurface = Color(0xFF2E2838), surfaceVariant = Color(0xFFEFE8F4),
        onSurfaceVariant = Color(0xFF6F6779), outline = Color(0xFFEAE2F0)
    ),
    line = Color(0xFFEAE2F0),
    gradA = Color(0xFFAE9BD6), gradB = Color(0xFF8E7BC4), glow = Color(0x598E7BC4),
    swatch = listOf(Color(0xFFF6F1F8), Color(0xFF9B86C9), Color(0xFFC9A87C))
)

private val peach = Palette(
    id = "peach", name = "Peach Latte", note = "Hangat & manis",
    scheme = lightColorScheme(
        primary = Color(0xFFC98F6E), onPrimary = Color.White,
        primaryContainer = Color(0xFFF7E4D7), onPrimaryContainer = Color(0xFF3D2519),
        secondary = Color(0xFF8FAE93), onSecondary = Color.White,
        secondaryContainer = Color(0xFFE3EEE2), onSecondaryContainer = Color(0xFF1F2E22),
        tertiary = Color(0xFFD9A383), background = Color(0xFFFBF0E7),
        onBackground = Color(0xFF3A2C24), surface = Color(0xFFFFFCF8),
        onSurface = Color(0xFF3A2C24), surfaceVariant = Color(0xFFF3E5DA),
        onSurfaceVariant = Color(0xFF7C6A5E), outline = Color(0xFFF1E2D6)
    ),
    line = Color(0xFFF1E2D6),
    gradA = Color(0xFFD9A383), gradB = Color(0xFFC0805D), glow = Color(0x54C0805D),
    swatch = listOf(Color(0xFFFBF0E7), Color(0xFFC98F6E), Color(0xFF8FAE93))
)

private val ocean = Palette(
    id = "ocean", name = "Ocean Mist", note = "Sejuk & bersih",
    scheme = lightColorScheme(
        primary = Color(0xFF5E8CA5), onPrimary = Color.White,
        primaryContainer = Color(0xFFDEEBF2), onPrimaryContainer = Color(0xFF152A36),
        secondary = Color(0xFFE0B98A), onSecondary = Color.White,
        secondaryContainer = Color(0xFFF6E8D3), onSecondaryContainer = Color(0xFF3A2E1C),
        tertiary = Color(0xFF7FA9BF), background = Color(0xFFEEF4F7),
        onBackground = Color(0xFF1F2E36), surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1F2E36), surfaceVariant = Color(0xFFE3EDF2),
        onSurfaceVariant = Color(0xFF64777F), outline = Color(0xFFDEE9EE)
    ),
    line = Color(0xFFDEE9EE),
    gradA = Color(0xFF7FA9BF), gradB = Color(0xFF4F7F99), glow = Color(0x544F7F99),
    swatch = listOf(Color(0xFFEEF4F7), Color(0xFF5E8CA5), Color(0xFFE0B98A))
)

/* Pixel — Material You: Google blue, tonal containers, pill shapes. */
private val pixel = Palette(
    id = "pixel", name = "Pixel", note = "Material You ala Google", pixel = true,
    cardRadius = 26, bigRadius = 30, pill = true,
    scheme = lightColorScheme(
        primary = Color(0xFF1A73E8), onPrimary = Color.White,
        primaryContainer = Color(0xFFD3E3FD), onPrimaryContainer = Color(0xFF041E49),
        secondary = Color(0xFF188038), onSecondary = Color.White,
        secondaryContainer = Color(0xFFCEEAD6), onSecondaryContainer = Color(0xFF072711),
        tertiary = Color(0xFFB26A00), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFE0B2), onTertiaryContainer = Color(0xFF3D2A00),
        background = Color(0xFFF8F9FA), onBackground = Color(0xFF1F1F1F),
        surface = Color(0xFFFFFFFF), onSurface = Color(0xFF1F1F1F),
        surfaceVariant = Color(0xFFF1F3F4), onSurfaceVariant = Color(0xFF5F6368),
        outline = Color(0xFFDADCE0), outlineVariant = Color(0xFFE8EAED)
    ),
    line = Color(0xFFDADCE0),
    gradA = Color(0xFF4C8DF6), gradB = Color(0xFF1A73E8), glow = Color(0x571A73E8),
    swatch = listOf(Color(0xFFF8F9FA), Color(0xFF1A73E8), Color(0xFF188038))
)

val Palettes = listOf(sage, midnight, lavender, peach, ocean, pixel)

fun paletteById(id: String): Palette = Palettes.firstOrNull { it.id == id } ?: sage

val LocalPalette = staticCompositionLocalOf { sage }

private fun appTypography(p: Palette): Typography {
    val base = Typography()
    return base.copy(
        headlineLarge = TextStyle(fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
        headlineSmall = TextStyle(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
        titleLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
        titleMedium = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
        bodyMedium = TextStyle(fontSize = 12.5.sp, lineHeight = 19.sp),
        labelSmall = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
    )
}

@Composable
fun MadStudyTheme(p: Palette, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(
            colorScheme = p.scheme,
            typography = appTypography(p),
            shapes = Shapes(
                extraSmall = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(p.cardRadius.dp),
                large = RoundedCornerShape(p.bigRadius.dp),
                extraLarge = RoundedCornerShape((p.bigRadius + 4).dp)
            ),
            content = content
        )
    }
}
