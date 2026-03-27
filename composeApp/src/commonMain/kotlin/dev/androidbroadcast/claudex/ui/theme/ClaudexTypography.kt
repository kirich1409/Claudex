package dev.androidbroadcast.claudex.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import claudex.composeapp.generated.resources.Inter_Medium
import claudex.composeapp.generated.resources.Inter_Regular
import claudex.composeapp.generated.resources.Inter_SemiBold
import claudex.composeapp.generated.resources.Res
import claudex.composeapp.generated.resources.RobotoMono_Regular
import org.jetbrains.compose.resources.Font

@Composable
internal fun robotoMonoFontFamily(): FontFamily {
    val regular = Font(Res.font.RobotoMono_Regular, weight = FontWeight.Normal)
    return remember(regular) { FontFamily(regular) }
}

public val LocalCodeFont: ProvidableCompositionLocal<FontFamily> =
    staticCompositionLocalOf { error("LocalCodeFont not provided — wrap with ClaudexTheme") }

@Composable
internal fun claudexTypography(): Typography {
    val interRegular = Font(Res.font.Inter_Regular, weight = FontWeight.Normal)
    val interMedium = Font(Res.font.Inter_Medium, weight = FontWeight.Medium)
    val interSemiBold = Font(Res.font.Inter_SemiBold, weight = FontWeight.SemiBold)
    val inter =
        remember(interRegular, interMedium, interSemiBold) {
            FontFamily(interRegular, interMedium, interSemiBold)
        }
    return remember(inter) {
        Typography().run {
            copy(
                displayLarge = displayLarge.copy(fontFamily = inter),
                displayMedium = displayMedium.copy(fontFamily = inter),
                displaySmall = displaySmall.copy(fontFamily = inter),
                headlineLarge = headlineLarge.copy(fontFamily = inter),
                headlineMedium = headlineMedium.copy(fontFamily = inter),
                headlineSmall = headlineSmall.copy(fontFamily = inter),
                titleLarge = titleLarge.copy(fontFamily = inter),
                titleMedium = titleMedium.copy(fontFamily = inter),
                titleSmall = titleSmall.copy(fontFamily = inter),
                bodyLarge = bodyLarge.copy(fontFamily = inter),
                bodyMedium = bodyMedium.copy(fontFamily = inter),
                bodySmall = bodySmall.copy(fontFamily = inter),
                labelLarge = labelLarge.copy(fontFamily = inter),
                labelMedium = labelMedium.copy(fontFamily = inter),
                labelSmall = labelSmall.copy(fontFamily = inter),
            )
        }
    }
}
