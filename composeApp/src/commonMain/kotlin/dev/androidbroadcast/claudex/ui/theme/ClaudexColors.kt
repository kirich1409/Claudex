package dev.androidbroadcast.claudex.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal object ClaudexColors {
    private val Orange10 = Color(0xFF3D1700)
    private val Orange30 = Color(0xFF8A3515)
    private val Orange40 = Color(0xFFB8512F)
    private val Orange80 = Color(0xFFDA7756)
    private val Orange90 = Color(0xFFF4B8A0)
    private val NeutralDark = Color(0xFF0C0C0D)
    private val NeutralLight = Color(0xFFF8F7F6)
    private val NeutralVariant20 = Color(0xFF2B2B2E)
    private val NeutralVariant90 = Color(0xFFE3E2E6)

    val darkScheme =
        darkColorScheme(
            primary = Orange80,
            onPrimary = Orange10,
            primaryContainer = Orange30,
            onPrimaryContainer = Orange90,
            background = NeutralDark,
            onBackground = Color.White,
            surface = NeutralVariant20,
            onSurface = Color.White,
            surfaceVariant = NeutralVariant20,
            onSurfaceVariant = NeutralVariant90,
        )

    val lightScheme =
        lightColorScheme(
            primary = Orange40,
            onPrimary = Color.White,
            primaryContainer = Orange90,
            onPrimaryContainer = Orange10,
            background = NeutralLight,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            surfaceVariant = NeutralVariant90,
            onSurfaceVariant = NeutralVariant20,
        )
}
