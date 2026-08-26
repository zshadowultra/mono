package com.zshadowultra.mono.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh
import androidx.core.content.ContextCompat
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import android.Manifest
import android.os.Build
import com.composables.icons.lucide.Archive
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.CircleDot
import com.composables.icons.lucide.Ellipsis
import com.composables.icons.lucide.Eraser
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Trash2
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import com.zshadowultra.mono.ui.glass.InteractiveHighlight
import com.zshadowultra.mono.ui.theme.BgDark
import com.zshadowultra.mono.ui.theme.BgLight
import com.zshadowultra.mono.ui.theme.CardDark
import com.zshadowultra.mono.ui.theme.CardLight
import com.zshadowultra.mono.ui.theme.DoneLight
import com.zshadowultra.mono.ui.theme.NotepadDark
import com.zshadowultra.mono.ui.theme.NotepadLight
import com.zshadowultra.mono.ui.theme.noteFontFamily
import com.zshadowultra.mono.ui.theme.noteFontSize

@Composable
fun EditorScreen(
    backdrop: LayerBackdrop,
    vm: EditorViewModel,
    onOpenArchived: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val dark = when (state.appearance) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val bg = if (dark) BgDark else BgLight
    val card = if (dark) CardDark else CardLight
    val fg = if (dark) Color.White else Color.Black
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    var menuOpen by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    BackHandler(enabled = menuOpen) { menuOpen = false }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        vm.toggleLive(context)
    }
    val toggleLiveAction = {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted && Build.VERSION.SDK_INT >= 33) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vm.toggleLive(context)
        }
    }
    var noteFocused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(bg)
            .pointerInput(Unit) { detectTapGestures(onTap = { menuOpen = false }) }
    ) {
        Box(Modifier.fillMaxSize().background(bg).layerBackdrop(backdrop))
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
        ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(44.dp)
                ) {
                    if (state.activeId != null) {
                        Box(Modifier.align(Alignment.CenterStart)) {
                            CircularIconButton(
                                icon = Lucide.Archive,
                                cd = "Archive",
                                fg = fg,
                                backdrop = backdrop,
                                dark = dark
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.archive(context)
                            }
                        }
                    }
                    Text(
                        text = "Mononote",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = fg,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val cardWidth by animateFloatAsState(
                        targetValue = if (noteFocused) 0.96f else 0.78f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                        label = "cardW"
                    )
                    AnimatedContent(
                        targetState = state.activeId,
                        modifier = Modifier.fillMaxWidth(cardWidth),
                        transitionSpec = {
                            fadeIn(tween(150, easing = FastOutSlowInEasing)) togetherWith
                                    fadeOut(tween(150, easing = FastOutSlowInEasing))
                        },
                        label = "note"
                    ) { _ ->
                        val focusRequester = remember { FocusRequester() }
                        Box(
                            Modifier
                                .shadow(10.dp, RoundedCornerShape(22.dp), clip = false)
                                .drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { RoundedCornerShape(22.dp) },
                                    shadow = null,
                                    effects = {
                                        vibrancy()
                                        blur(4f.dp.toPx())
                                        lens(16f.dp.toPx(), 32f.dp.toPx())
                                    },
                                    onDrawSurface = {
                                        drawRect(if (dark) NotepadDark else NotepadLight)
                                    }
                                )
                                .aspectRatio(1f)
                                .clickable(
                                    interactionSource = null,
                                    indication = null
                                ) { focusRequester.requestFocus() }
                        ) {
                            val noteScroll = rememberScrollState()
                            Box(Modifier.fillMaxSize()) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(noteScroll)
                                ) {
                                    BasicTextField(
                                        value = state.text,
                                        onValueChange = vm::onTextChange,
                                        textStyle = TextStyle(
                                            fontFamily = noteFontFamily(state.font),
                                            fontSize = noteFontSize(state.smallerText),
                                            letterSpacing = if (state.smallerText) (-0.24).sp else (-0.41).sp,
                                            color = fg,
                                            lineHeight = noteFontSize(state.smallerText) * 1.3f,
                                        ),
                                        cursorBrush = SolidColor(fg),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                            .onFocusChanged { noteFocused = it.isFocused }
                                            .padding(16.dp)
                                            .padding(bottom = 40.dp),
                                        decorationBox = { inner ->
                                            Box {
                                                if (state.text.isEmpty()) {
                                                    Text(
                                                        text = "Start typing...",
                                                        style = TextStyle(
                                                            fontFamily = noteFontFamily(state.font),
                                                            fontSize = noteFontSize(state.smallerText),
                                                            letterSpacing = if (state.smallerText) (-0.24).sp else (-0.41).sp,
                                                            lineHeight = noteFontSize(state.smallerText) * 1.3f,
                                                            color = fg.copy(alpha = 0.3f),
                                                        ),
                                                    )
                                                }
                                                inner()
                                            }
                                        },
                                    )
                                }
                                Box(
                                    Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                        .size(28.dp)
                                        .background(Color(0xFFD9F99A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${state.text.length}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = noteFocused,
                        transitionSpec = {
                            (fadeIn(tween(180, easing = FastOutSlowInEasing)) + slideInVertically(tween(220, easing = FastOutSlowInEasing)) { it / 3 }) togetherWith
                            (fadeOut(tween(160)) + slideOutVertically(tween(200)) { it / 3 })
                        },
                        label = "bottomBar"
                    ) { focused ->
                        if (focused) {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    vm.done()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (dark) Color.White else DoneLight,
                                    contentColor = if (dark) Color.Black else Color.White
                                )
                            ) {
                                Text(text = "Done", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularIconButton(icon = Lucide.Trash2, cd = "Delete", fg = fg, backdrop = backdrop, dark = dark) {
                                    showDelete = true
                                }
                                Spacer(Modifier.weight(1f))
                                val liveEnabled = state.text.isNotBlank() || state.live
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .alpha(if (liveEnabled) 1f else 0.35f)
                                        .drawBackdrop(
                                            backdrop = backdrop,
                                            shape = { Capsule() },
                                            effects = {
                                                vibrancy()
                                                blur(4f.dp.toPx())
                                                lens(10f.dp.toPx(), 20f.dp.toPx())
                                            },
                                            onDrawSurface = {
                                                drawRect(if (dark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.35f))
                                            }
                                        )
                                        .clickable(
                                            interactionSource = null,
                                            indication = null,
                                            enabled = liveEnabled
                                        ) { toggleLiveAction() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (state.live) Lucide.CircleDot else Lucide.Circle,
                                            contentDescription = null,
                                            tint = fg,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (state.live) "Stop Live" else "Go Live",
                                            fontSize = 16.sp,
                                            color = fg
                                        )
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                CircularIconButton(icon = Lucide.Eraser, cd = "Clear", fg = fg, backdrop = backdrop, dark = dark) {
                                    vm.clearText()
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            val menuScope = rememberCoroutineScope()
            val interactiveHighlight = remember(menuScope) {
                InteractiveHighlight(animationScope = menuScope)
            }
            val posP by animateFloatAsState(
                targetValue = if (menuOpen) 1f else 0f,
                animationSpec = if (menuOpen) spring(dampingRatio = 0.58f, stiffness = 144f) else spring(dampingRatio = 0.82f, stiffness = 130f),
                label = "menuPos"
            )
            val radiusP by animateFloatAsState(
                targetValue = if (menuOpen) 1f else 0f,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                label = "menuRadius"
            )
            val contentP by animateFloatAsState(
                targetValue = if (menuOpen) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 137f),
                label = "menuContent"
            )
            val dotsAlpha by animateFloatAsState(
                targetValue = if (menuOpen) 0f else 1f,
                animationSpec = if (menuOpen) tween(10, easing = FastOutSlowInEasing) else tween(150, easing = FastOutSlowInEasing),
                label = "menuDots"
            )
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .offset(x = lerp(206.dp, 0.dp, posP), y = lerp(-112.dp, 0.dp, posP))
                    .size(width = 190.dp, height = 96.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(lerp(44.dp, 20.dp, radiusP)) },
                        effects = {
                            vibrancy()
                            blur(4f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        onDrawSurface = {
                            drawRect(
                                if (dark) Color(0xFF2C2C2E).copy(alpha = lerp(0.9f, 0.4f, posP.coerceIn(0f, 1f)))
                                else Color(0xFFFAFAFA).copy(alpha = lerp(0.95f, 0.55f, posP.coerceIn(0f, 1f)))
                            )
                        }
                    )
                    .clip(RoundedCornerShape(lerp(44.dp, 20.dp, radiusP))),
                contentAlignment = Alignment.TopEnd
            ) {
                if (contentP > 0.01f) {
                    Column(
                        Modifier
                            .graphicsLayer {
                                scaleX = lerp(0.96f, 1f, contentP)
                                scaleY = lerp(0.96f, 1f, contentP)
                            }
                            .blur(lerp(4.dp, 0.dp, contentP))
                            .alpha(contentP)
                    ) {
                        BlobMenuRow(icon = Lucide.Archive, label = "Archived Notes", fg = fg, alpha = stagger(contentP, 0)) {
                            menuOpen = false
                            onOpenArchived()
                        }
                        BlobMenuRow(icon = Lucide.Settings, label = "Settings", fg = fg, alpha = stagger(contentP, 1)) {
                            menuOpen = false
                            onOpenSettings()
                        }
                    }
                }
            }

            if (!menuOpen) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .size(44.dp)
                    .alpha(dotsAlpha)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { Capsule() },
                        effects = {
                            vibrancy()
                            blur(2f.dp.toPx())
                            lens(12f.dp.toPx(), 24f.dp.toPx())
                        },
                        layerBlock = {
                            val progress = interactiveHighlight.pressProgress
                            val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)
                            scaleX = scale
                            scaleY = scale
                        },
                        onDrawSurface = {
                            drawRect(if (dark) Color(0xFF2C2C2E) else Color(0xFFFAFAFA))
                        }
                    )
                    .clickable(interactionSource = null, indication = null) {
                        menuOpen = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    .then(interactiveHighlight.modifier)
                    .then(interactiveHighlight.gestureModifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.Ellipsis,
                    contentDescription = null,
                    tint = fg
                )
            }
            }
        }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = card,
            titleContentColor = fg,
            textContentColor = fg.copy(alpha = 0.7f),
            title = { Text("Delete note?", fontWeight = FontWeight.SemiBold) },
            text = { Text("This note will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    vm.delete(context)
                }) { Text("Delete", color = fg) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel", color = fg.copy(alpha = 0.6f)) }
            }
        )
    }
}

internal fun stagger(progress: Float, index: Int): Float =
    ((progress - 0.35f - index * 0.2f) / 0.4f).coerceIn(0f, 1f)

@Composable
private fun BlobMenuRow(
    icon: ImageVector,
    label: String,
    fg: Color,
    alpha: Float,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = null,
                indication = null,
                enabled = alpha > 0.9f,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text = label, fontSize = 15.sp, color = fg, modifier = Modifier.alpha(alpha))
    }
}

@Composable
private fun CircularIconButton(
    icon: ImageVector,
    cd: String,
    fg: Color,
    backdrop: LayerBackdrop,
    dark: Boolean,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val highlight = remember(scope) { InteractiveHighlight(animationScope = scope) }
    Box(
        Modifier
            .size(48.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(10f.dp.toPx(), 20f.dp.toPx())
                },
                layerBlock = {
                    val s = lerp(1f, 1.06f, highlight.pressProgress)
                    scaleX = s
                    scaleY = s
                },
                onDrawSurface = {
                    drawRect(if (dark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.35f))
                }
            )
            .clickable(interactionSource = null, indication = null, onClick = onClick)
            .then(highlight.modifier)
            .then(highlight.gestureModifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = cd, tint = fg, modifier = Modifier.size(22.dp))
    }
}
