package dev.androidbroadcast.claudex.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ClaudexColorsTest {
    @Test fun darkPrimary() = assertEquals(Color(0xFFDA7756), ClaudexColors.darkScheme.primary)

    @Test fun lightPrimary() = assertEquals(Color(0xFFB8512F), ClaudexColors.lightScheme.primary)

    @Test fun darkBackground() = assertEquals(Color(0xFF0C0C0D), ClaudexColors.darkScheme.background)

    @Test fun lightBackground() = assertEquals(Color(0xFFF8F7F6), ClaudexColors.lightScheme.background)

    @Test fun darkOnPrimary() = assertEquals(Color(0xFF3D1700), ClaudexColors.darkScheme.onPrimary)
}
