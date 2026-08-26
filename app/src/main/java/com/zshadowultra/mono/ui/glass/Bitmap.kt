/*
 * Verbatim copy from Kyant0/AndroidLiquidGlass
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/main/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/utils/Bitmap.kt
 * Apache License 2.0 - package renamed to com.zshadowultra.mono.ui.glass
 */
package com.zshadowultra.mono.ui.glass

import androidx.compose.ui.graphics.ImageBitmap

expect fun ImageBitmap.scale(width: Int, height: Int): ImageBitmap
