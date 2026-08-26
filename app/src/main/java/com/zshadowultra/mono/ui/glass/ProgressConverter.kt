/*
 * Verbatim copy from Kyant0/AndroidLiquidGlass
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/main/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/utils/ProgressConverter.kt
 * Apache License 2.0 - package renamed to com.zshadowultra.mono.ui.glass
 */
package com.zshadowultra.mono.ui.glass

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign

fun interface ProgressConverter {

    fun convert(progress: Float): Float

    companion object {

        val Default: ProgressConverter =
            ProgressConverter { progress ->
                (1f - exp(-abs(progress))) * progress.sign
            }
    }
}
