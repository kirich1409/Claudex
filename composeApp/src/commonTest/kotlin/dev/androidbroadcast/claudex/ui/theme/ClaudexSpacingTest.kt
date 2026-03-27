package dev.androidbroadcast.claudex.ui.theme

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ClaudexSpacingTest {
    private val spacing = ClaudexSpacing()

    @Test fun xs() = assertEquals(4.dp, spacing.xs)
    @Test fun sm() = assertEquals(8.dp, spacing.sm)
    @Test fun md() = assertEquals(16.dp, spacing.md)
    @Test fun lg() = assertEquals(24.dp, spacing.lg)
    @Test fun xl() = assertEquals(32.dp, spacing.xl)
    @Test fun xxl() = assertEquals(48.dp, spacing.xxl)
}
