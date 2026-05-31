package com.love.interaction.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Pink40,
    onPrimary = WarmWhite,
    primaryContainer = Pink80,
    onPrimaryContainer = Pink20,
    secondary = Lavender40,
    onSecondary = WarmWhite,
    secondaryContainer = Lavender80,
    onSecondaryContainer = Lavender20,
    tertiary = Coral40,
    onTertiary = WarmWhite,
    tertiaryContainer = Coral80,
    onTertiaryContainer = Coral20,
    background = WarmWhite,
    onBackground = DarkGray,
    surface = SurfaceLight,
    onSurface = DarkGray,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = MediumGray,
    error = Error,
    onError = WarmWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = Pink60,
    onPrimary = Pink20,
    primaryContainer = Pink20,
    onPrimaryContainer = Pink80,
    secondary = Lavender60,
    onSecondary = Lavender20,
    secondaryContainer = Lavender20,
    onSecondaryContainer = Lavender80,
    tertiary = Coral60,
    onTertiary = Coral20,
    tertiaryContainer = Coral20,
    onTertiaryContainer = Coral80,
    background = DeepDark,
    onBackground = LightGray,
    surface = SurfaceDark,
    onSurface = LightGray,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = MediumGray,
    error = Error,
    onError = WarmWhite
)

@Composable
fun LoveInteractionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Removed SideEffect that manipulated statusBarColor —
    // enableEdgeToEdge() in MainActivity handles system bars correctly.

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
