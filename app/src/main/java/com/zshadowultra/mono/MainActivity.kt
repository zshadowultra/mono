package com.zshadowultra.mono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.zshadowultra.mono.ui.ArchivedScreen
import com.zshadowultra.mono.ui.EditorScreen
import com.zshadowultra.mono.ui.EditorViewModel
import com.zshadowultra.mono.ui.theme.BgDark
import com.zshadowultra.mono.ui.theme.BgLight
import com.zshadowultra.mono.ui.theme.MonoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoTheme {
                MonoAppUi()
            }
        }
    }
}

@Composable
fun MonoAppUi(vm: EditorViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val dark = isSystemInDarkTheme()
    val bg = if (dark) BgDark else BgLight
    val backdrop = rememberLayerBackdrop { drawRect(bg); drawContent() }
    var showArchived by remember { mutableStateOf(false) }
    AnimatedContent(
        targetState = showArchived,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 }
            } else {
                slideInHorizontally { -it / 3 } togetherWith slideOutHorizontally { it }
            }
        },
        label = "nav"
    ) { archived ->
        if (archived) {
            ArchivedScreen(backdrop, vm, onBack = { showArchived = false })
        } else {
            EditorScreen(backdrop, vm, onOpenArchived = { showArchived = true })
        }
    }
}
