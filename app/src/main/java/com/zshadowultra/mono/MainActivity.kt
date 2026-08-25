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
import com.zshadowultra.mono.ui.LiveActivityScreen
import com.zshadowultra.mono.ui.NoteTextScreen
import com.zshadowultra.mono.ui.SettingsScreen
import com.zshadowultra.mono.ui.theme.BgDark
import com.zshadowultra.mono.ui.theme.BgLight
import com.zshadowultra.mono.ui.theme.MonoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoAppUi()
        }
    }
}

enum class Screen { Editor, Archived, Settings, NoteText, LiveActivity }

@Composable
fun MonoAppUi(vm: EditorViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val dark = when (state.appearance) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    MonoTheme(darkTheme = dark) {
        val bg = if (dark) BgDark else BgLight
        val backdrop = rememberLayerBackdrop { drawRect(bg); drawContent() }
        var screen by remember { mutableStateOf(Screen.Editor) }

        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                val forward = targetState.ordinal >= initialState.ordinal
                if (forward) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 3 }
                } else {
                    slideInHorizontally { -it / 3 } togetherWith slideOutHorizontally { it }
                }
            },
            label = "nav"
        ) { s ->
            when (s) {
                Screen.Editor -> EditorScreen(
                    backdrop = backdrop,
                    vm = vm,
                    onOpenArchived = { screen = Screen.Archived },
                    onOpenSettings = { screen = Screen.Settings },
                )
                Screen.Archived -> ArchivedScreen(backdrop, vm, onBack = { screen = Screen.Editor })
                Screen.Settings -> SettingsScreen(
                    backdrop = backdrop,
                    vm = vm,
                    onBack = { screen = Screen.Editor },
                    onOpenNoteText = { screen = Screen.NoteText },
                    onOpenLiveActivity = { screen = Screen.LiveActivity },
                )
                Screen.NoteText -> NoteTextScreen(backdrop, vm, onBack = { screen = Screen.Settings })
                Screen.LiveActivity -> LiveActivityScreen(backdrop, vm, onBack = { screen = Screen.Settings })
            }
        }
    }
}
