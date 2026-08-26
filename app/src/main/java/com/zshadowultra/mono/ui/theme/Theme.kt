package com.zshadowultra.mono.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.zshadowultra.mono.R
import com.zshadowultra.mono.data.NoteFont

val BgLight = Color(0xFFF2F2F7)
val CardLight = Color(0xFFFDFDFD)
val NotepadLight = Color(0xFFF6F6F6)
val BgDark = Color(0xFF000000)
val CardDark = Color(0xFF1C1C1E)
val NotepadDark = Color(0xFF1C1C1E)
val DoneLight = Color(0xFF131313)
val DotsLight = Color(0xFFFAFAFA)

val InterFamily = FontFamily(
    Font(R.font.inter_var, FontWeight.Normal, FontStyle.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter_var, FontWeight.Medium, FontStyle.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.inter_var, FontWeight.SemiBold, FontStyle.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.inter_var, FontWeight.Bold, FontStyle.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)
val SerifVarFamily = FontFamily(Font(R.font.source_serif4_var))
val MonoVarFamily = FontFamily(Font(R.font.jetbrains_mono_var))

fun noteFontFamily(font: NoteFont): FontFamily = when (font) {
    NoteFont.DEFAULT -> InterFamily
    NoteFont.SERIF -> SerifVarFamily
    NoteFont.MONO -> MonoVarFamily
}

fun noteFontSize(smaller: Boolean): TextUnit = if (smaller) 15.sp else 17.sp

private val BaseTypography = Typography()

val AppTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = InterFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = InterFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = InterFamily),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = InterFamily),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = InterFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = InterFamily),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = InterFamily),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = InterFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = InterFamily),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = InterFamily),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = InterFamily),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = InterFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = InterFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = InterFamily),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = InterFamily),
)

@Composable
fun MonoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) {
        darkColorScheme(background = BgDark, surface = CardDark, onBackground = Color.White, onSurface = Color.White)
    } else {
        lightColorScheme(background = BgLight, surface = CardLight, onBackground = Color.Black, onSurface = Color.Black)
    }
    MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
}
