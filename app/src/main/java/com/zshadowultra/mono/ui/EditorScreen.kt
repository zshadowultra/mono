package com.zshadowultra.mono.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.composables.icons.lucide.Archive
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Ellipsis
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Radio
import com.composables.icons.lucide.Share
import com.composables.icons.lucide.Trash2
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import com.zshadowultra.mono.ui.glass.InteractiveHighlight
import androidx.compose.ui.util.lerp
import androidx.compose.runtime.rememberCoroutineScope
import com.zshadowultra.mono.data.NoteFont
import com.zshadowultra.mono.data.NoteSize
import com.zshadowultra.mono.ui.theme.BgDark
import com.zshadowultra.mono.ui.theme.BgLight
import com.zshadowultra.mono.ui.theme.CardDark
import com.zshadowultra.mono.ui.theme.CardLight
import com.zshadowultra.mono.ui.theme.noteFontFamily
import com.zshadowultra.mono.ui.theme.noteFontSize

@Composable
fun EditorScreen(backdrop: LayerBackdrop, vm: EditorViewModel, onOpenArchived: () -> Unit) {
    val state by vm.state.collectAsState()
    val dark = isSystemInDarkTheme()
    val bg = if (dark) BgDark else BgLight
    val card = if (dark) CardDark else CardLight
    val fg = if (dark) Color.White else Color.Black
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current
    var menuOpen by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        vm.toggleLive(context)
    }
    val toggleLiveAction = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!granted && Build.VERSION.SDK_INT >= 33) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            vm.toggleLive(context)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(bg)
            .layerBackdrop(backdrop)
            .pointerInput(Unit) { detectTapGestures(onTap = { menuOpen = false }) }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(44.dp)
            ) {
                Text(
                    text = "Mononote",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = fg,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(Modifier.height(12.dp))
            AnimatedContent(
                targetState = state.activeId,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                transitionSpec = {
                    (
                        slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) { it } +
                            fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        ) togetherWith (
                        slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) { -it } +
                            fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        )
                },
                label = "note"
            ) { _ ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(card, RoundedCornerShape(24.dp))
                ) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = "Start typing...",
                            fontSize = noteFontSize(state.size),
                            fontFamily = noteFontFamily(state.font),
                            color = fg.copy(alpha = 0.3f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    BasicTextField(
                        value = state.text,
                        onValueChange = vm::onTextChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        textStyle = TextStyle(
                            fontFamily = noteFontFamily(state.font),
                            fontSize = noteFontSize(state.size),
                            color = fg
                        ),
                        cursorBrush = SolidColor(fg)
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        val scale by animateFloatAsState(
                            targetValue = if (state.live) 1.15f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "live"
                        )
                        Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
                            Canvas(
                                Modifier
                                    .size(28.dp)
                                    .clickable(interactionSource = null, indication = null) {
                                        toggleLiveAction()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                            ) {
                                drawCircle(
                                    color = fg.copy(alpha = 0.25f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                if (state.live) {
                                    drawCircle(color = fg, radius = 4.dp.toPx())
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    vm.done()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (dark) Color.White else Color.Black,
                    contentColor = if (dark) Color.Black else Color.White
                )
            ) {
                Text(text = "Done", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                val menuScope = rememberCoroutineScope()
                val interactiveHighlight = remember(menuScope) {
                    InteractiveHighlight(animationScope = menuScope)
                }
                Box(
                    Modifier
                        .size(44.dp)
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
                                drawRect(if (dark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
                            }
                        )
                        .clickable(interactionSource = null, indication = null) {
                            menuOpen = !menuOpen
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .then(interactiveHighlight.modifier)
                        .then(interactiveHighlight.gestureModifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.Ellipsis,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(20.dp)
                    )
                }
                AnimatedVisibility(
                    visible = menuOpen,
                    enter = scaleIn(
                        initialScale = 0.92f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                        transformOrigin = TransformOrigin(1f, 0f)
                    ) + fadeIn(),
                    exit = scaleOut(
                        targetScale = 0.92f,
                        transformOrigin = TransformOrigin(1f, 0f)
                    ) + fadeOut()
                ) {
                    Column(
                        Modifier
                            .width(240.dp)
                            .padding(top = 8.dp)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedRectangle(24f.dp) },
                                effects = {
                                    vibrancy()
                                    blur(4f.dp.toPx())
                                    lens(16f.dp.toPx(), 32f.dp.toPx())
                                },
                                onDrawSurface = {
                                    drawRect(if (dark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
                                }
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .verticalScroll(rememberScrollState())
                    ) {
                        MenuRow(icon = Lucide.Radio, label = if (state.live) "Stop Live" else "Go Live", fg = fg) {
                            menuOpen = false
                            toggleLiveAction()
                        }
                        MenuDivider(fg = fg)
                        MenuRow(icon = Lucide.Share, label = "Share", fg = fg) {
                            menuOpen = false
                            runCatching {
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, state.text)
                                }
                                context.startActivity(Intent.createChooser(send, null))
                            }
                        }
                        MenuDivider(fg = fg)
                        MenuRow(icon = Lucide.Copy, label = "Copy", fg = fg) {
                            menuOpen = false
                            clipboard.setText(AnnotatedString(state.text))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                        MenuDivider(fg = fg)
                        MenuRow(icon = Lucide.Archive, label = "Archived", fg = fg) {
                            menuOpen = false
                            onOpenArchived()
                        }
                        MenuDivider(fg = fg)
                        MenuRow(icon = Lucide.Trash2, label = "Delete", fg = fg) {
                            menuOpen = false
                            showDelete = true
                        }
                        MenuDivider(fg = fg)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FontChip(label = "Default", selected = state.font == NoteFont.DEFAULT, fg = fg) { vm.setFont(NoteFont.DEFAULT) }
                            FontChip(label = "Serif", selected = state.font == NoteFont.SERIF, fg = fg) { vm.setFont(NoteFont.SERIF) }
                            FontChip(label = "Mono", selected = state.font == NoteFont.MONO, fg = fg) { vm.setFont(NoteFont.MONO) }
                        }
                        MenuDivider(fg = fg)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FontChip(label = "S", selected = state.size == NoteSize.SMALL, fg = fg) { vm.setSize(NoteSize.SMALL) }
                            FontChip(label = "M", selected = state.size == NoteSize.MEDIUM, fg = fg) { vm.setSize(NoteSize.MEDIUM) }
                            FontChip(label = "L", selected = state.size == NoteSize.LARGE, fg = fg) { vm.setSize(NoteSize.LARGE) }
                        }
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuRow(icon: ImageVector, label: String, fg: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(interactionSource = null, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = label, fontSize = 15.sp, color = fg)
    }
}

@Composable
private fun MenuDivider(fg: Color) {
    HorizontalDivider(
        thickness = 1.dp,
        color = fg.copy(alpha = 0.08f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontChip(label: String, selected: Boolean, fg: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) fg.copy(alpha = 0.12f) else Color.Transparent
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = fg,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
