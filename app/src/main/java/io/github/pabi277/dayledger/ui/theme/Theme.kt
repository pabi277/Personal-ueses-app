package io.github.pabi277.dayledger.ui.theme

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
    primary = DayLedgerPrimaryLight,
    onPrimary = DayLedgerOnPrimaryLight,
    primaryContainer = DayLedgerPrimaryContainerLight,
    onPrimaryContainer = DayLedgerOnPrimaryContainerLight,
    secondary = DayLedgerSecondaryLight,
    onSecondary = DayLedgerOnSecondaryLight,
    secondaryContainer = DayLedgerSecondaryContainerLight,
    onSecondaryContainer = DayLedgerOnSecondaryContainerLight,
    tertiary = DayLedgerTertiaryLight,
    onTertiary = DayLedgerOnTertiaryLight,
    tertiaryContainer = DayLedgerTertiaryContainerLight,
    onTertiaryContainer = DayLedgerOnTertiaryContainerLight,
    error = DayLedgerErrorLight,
    onError = DayLedgerOnErrorLight,
    errorContainer = DayLedgerErrorContainerLight,
    onErrorContainer = DayLedgerOnErrorContainerLight,
    background = DayLedgerBackgroundLight,
    onBackground = DayLedgerOnBackgroundLight,
    surface = DayLedgerSurfaceLight,
    onSurface = DayLedgerOnSurfaceLight,
    surfaceVariant = DayLedgerSurfaceVariantLight,
    onSurfaceVariant = DayLedgerOnSurfaceVariantLight,
    outline = DayLedgerOutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = DayLedgerPrimaryDark,
    onPrimary = DayLedgerOnPrimaryDark,
    primaryContainer = DayLedgerPrimaryContainerDark,
    onPrimaryContainer = DayLedgerOnPrimaryContainerDark,
    secondary = DayLedgerSecondaryDark,
    onSecondary = DayLedgerOnSecondaryDark,
    secondaryContainer = DayLedgerSecondaryContainerDark,
    onSecondaryContainer = DayLedgerOnSecondaryContainerDark,
    tertiary = DayLedgerTertiaryDark,
    onTertiary = DayLedgerOnTertiaryDark,
    tertiaryContainer = DayLedgerTertiaryContainerDark,
    onTertiaryContainer = DayLedgerOnTertiaryContainerDark,
    error = DayLedgerErrorDark,
    onError = DayLedgerOnErrorDark,
    errorContainer = DayLedgerErrorContainerDark,
    onErrorContainer = DayLedgerOnErrorContainerDark,
    background = DayLedgerBackgroundDark,
    onBackground = DayLedgerOnBackgroundDark,
    surface = DayLedgerSurfaceDark,
    onSurface = DayLedgerOnSurfaceDark,
    surfaceVariant = DayLedgerSurfaceVariantDark,
    onSurfaceVariant = DayLedgerOnSurfaceVariantDark,
    outline = DayLedgerOutlineDark
)

@Composable
fun DayLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DayLedgerTypography,
        shapes = DayLedgerShapes,
        content = content
    )
}