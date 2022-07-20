package com.domil.tankhahp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)*/

private val LightColorPalette = lightColors(
    primary = Jeanswest,
    primaryVariant = JeanswestStatusBar,
    background = JeanswestBackground,
    surface = JeanswestBackground,
    onPrimary = Color.White,
    secondaryVariant = Jeanswest,
    onBackground = Color.Black,
    onSurface = Color.Black,
    secondary = BorderLight,
    error = errorColor,
)

@Composable
fun TankhahPTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = LightColorPalette


    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}