package com.zshadowultra.mono.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgLight = Color(0xFFF2F2F7)
val CardLight = Color(0xFFFBFBFD)
val BgDark = Color(0xFF000000)
val CardDark = Color(0xFF1C1C1E)

@Composable
fun MonoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColorScheme(background = BgDark, surface = CardDark, onBackground = Color.White, onSurface = Color.White)
    } else {
        lightColorScheme(background = BgLight, surface = CardLight, onBackground = Color.Black, onSurface = Color.Black)
    }
    MaterialTheme(colorScheme = colors, content = content)
}
