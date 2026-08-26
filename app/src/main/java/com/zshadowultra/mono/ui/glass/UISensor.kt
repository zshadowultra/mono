/*
 * Verbatim copy from Kyant0/AndroidLiquidGlass
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/main/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/utils/UISensor.kt
 * Apache License 2.0 - package renamed to com.zshadowultra.mono.ui.glass
 */
package com.zshadowultra.mono.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset

@Composable
expect fun rememberUISensor(): UISensor

interface UISensor {

    val gravityAngle: Float

    val gravity: Offset

    fun start()

    fun stop()
}
