package dev.dreamteam.a22200322_ito_22200350_huamani.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SpaceViolet, onPrimary = Color(0xFF241747),
    primaryContainer = Color(0xFF30254D), onPrimaryContainer = Color(0xFFE9DFFF),
    secondary = SpaceMint, onSecondary = Color(0xFF063B30),
    secondaryContainer = Color(0xFF173A38), onSecondaryContainer = Color(0xFFB9F1E2),
    tertiary = Color(0xFFF2C28D),
    background = SpaceNavy, onBackground = SpaceText,
    surface = SpaceNavy, onSurface = SpaceText,
    surfaceVariant = Color(0xFF253149), onSurfaceVariant = SpaceMuted,
    surfaceContainer = SpaceSurface, surfaceContainerHigh = Color(0xFF1A263B),
    surfaceContainerHighest = Color(0xFF233048),
    outline = Color(0xFF65728A), outlineVariant = Color(0xFF28364E),
    error = Color(0xFFFFB4AB), errorContainer = Color(0xFF512824),
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF635098), secondary = Color(0xFF286B5B),
    background = Color(0xFFF5F5FB), surface = Color(0xFFF5F5FB),
    onBackground = Color(0xFF172033), onSurface = Color(0xFF172033),
)

@Composable
fun _22200322ITO22200350HUAMANITheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography, content = content)
}
