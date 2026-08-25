package com.zshadowultra.mono.ui

import android.content.ComponentName
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Contrast
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Puzzle
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.X
import com.composables.icons.lucide.Type
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.zshadowultra.mono.data.NoteFont
import com.zshadowultra.mono.ui.theme.BgDark
import com.zshadowultra.mono.ui.theme.BgLight
import com.zshadowultra.mono.ui.theme.CardDark
import com.zshadowultra.mono.ui.theme.CardLight
import com.zshadowultra.mono.widget.NoteWidgetReceiver

@Composable
fun SettingsScreen(
    backdrop: LayerBackdrop,
    vm: EditorViewModel,
    onBack: () -> Unit,
    onOpenNoteText: () -> Unit,
    onOpenLiveActivity: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val dark = isSystemInDarkTheme()
    val bg = if (dark) BgDark else BgLight
    val card = if (dark) CardDark else CardLight
    val fg = if (dark) Color.White else Color.Black
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var appearanceOpen by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(bg)
            .pointerInput(Unit) { detectTapGestures(onTap = { appearanceOpen = false }) }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .layerBackdrop(backdrop)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Lucide.X, contentDescription = "Close", tint = fg)
                }
                Text(
                    text = "Settings",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = fg,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(12.dp))
            Box(Modifier.padding(horizontal = 16.dp)) {
                Column {
                    SettingsCard {
                        SettingsRow(
                            icon = Lucide.Contrast,
                            label = "Appearance",
                            fg = fg,
                            onClick = { appearanceOpen = true }
                        ) {
                            Text(
                                text = appearanceLabel(state.appearance),
                                fontSize = 15.sp,
                                color = fg.copy(alpha = 0.4f),
                                modifier = Modifier.alpha(if (appearanceOpen) 0f else 1f)
                            )
                        }
                        SettingsRow(
                            icon = Lucide.Type,
                            label = "Note Text",
                            fg = fg,
                            trailing = { Icon(Lucide.ChevronRight, null, Modifier.size(16.dp), tint = fg.copy(alpha = 0.3f)) },
                            onClick = onOpenNoteText
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 46.dp),
                            thickness = 0.5.dp,
                            color = fg.copy(alpha = 0.06f)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    SettingsCard {
                        SettingsRow(
                            icon = Lucide.Puzzle,
                            label = "Widget",
                            fg = fg,
                            onClick = {
                                runCatching {
                                    AppWidgetManager.getInstance(context).requestPinAppWidget(
                                        ComponentName(context, NoteWidgetReceiver::class.java),
                                        null,
                                        null
                                    )
                                }
                            },
                            trailing = { Icon(Lucide.ChevronRight, null, Modifier.size(16.dp), tint = fg.copy(alpha = 0.3f)) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 46.dp),
                            thickness = 0.5.dp,
                            color = fg.copy(alpha = 0.06f)
                        )
                        SettingsRow(
                            icon = Lucide.Smartphone,
                            label = "Live Activity",
                            fg = fg,
                            trailing = { Icon(Lucide.ChevronRight, null, Modifier.size(16.dp), tint = fg.copy(alpha = 0.3f)) },
                            onClick = onOpenLiveActivity
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    SettingsCard {
                        SettingsRow(
                            icon = Lucide.MessageSquare,
                            label = "Give Feedback",
                            fg = fg,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:hello@digitalminimalist.com"))
                                    )
                                }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 46.dp),
                            thickness = 0.5.dp,
                            color = fg.copy(alpha = 0.06f)
                        )
                        SettingsRow(
                            icon = Lucide.Star,
                            label = "Rate Mononote",
                            fg = fg,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.zshadowultra.mono"))
                                    )
                                }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 46.dp),
                            thickness = 0.5.dp,
                            color = fg.copy(alpha = 0.06f)
                        )
                        SettingsRow(
                            icon = Lucide.Info,
                            label = "About Mononote",
                            fg = fg,
                            trailing = { Icon(Lucide.ChevronRight, null, Modifier.size(16.dp), tint = fg.copy(alpha = 0.3f)) },
                            onClick = { showAbout = true }
                        )
                    }
                    Spacer(Modifier.height(48.dp))
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(shape = RoundedCornerShape(14.dp), color = fg.copy(alpha = 0.06f)) {
                            Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier
                                        .size(22.dp)
                                        .background(fg, RoundedCornerShape(6.dp))
                                ) {
                                    Box(
                                        Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(9.dp)
                                            .background(if (dark) BgDark else BgLight)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Mononote", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = fg)
                        Text(
                            "Version 0.1 (1)",
                            fontSize = 11.sp,
                            color = fg.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Focus on one note at a time",
                            fontSize = 12.sp,
                            color = fg.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                }

            }
        }

        AppearancePopover(
            visible = appearanceOpen,
            selected = state.appearance,
            fg = fg,
            dark = dark,
            onSelect = { option ->
                vm.setAppearance(option)
                appearanceOpen = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 104.dp, end = 24.dp),
            backdrop = backdrop
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            containerColor = card,
            titleContentColor = fg,
            textContentColor = fg.copy(alpha = 0.7f),
            title = { Text("Mononote", fontWeight = FontWeight.SemiBold) },
            text = { Text("One note at a time. Write down anything you want your future self to remember, and keep it visible on your Home Screen and Lock Screen.") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Close", color = fg) }
            }
        )
    }
}

private fun appearanceLabel(appearance: String) = when (appearance) {
    "light" -> "Light"
    "dark" -> "Dark"
    else -> "System"
}

@Composable
private fun AppearancePopover(
    visible: Boolean,
    selected: String,
    fg: Color,
    dark: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: LayerBackdrop,
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 1200f),
        label = "appearanceBlob"
    )
    if (progress <= 0.01f) return
    Box(
        modifier
            .width(lerp(70.dp, 170.dp, progress))
            .height(lerp(40.dp, 132.dp, progress))
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(lerp(14.dp, 20.dp, progress)) },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(if (dark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.55f))
                }
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(vertical = 2.dp)) {
            listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEachIndexed { i, (key, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            enabled = progress > 0.9f
                        ) { onSelect(key) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 15.sp,
                        color = fg,
                        modifier = Modifier
                            .weight(1f)
                            .alpha(stagger(progress, i))
                    )
                    if (selected == key) {
                        Icon(
                            imageVector = Lucide.Check,
                            contentDescription = null,
                            tint = fg,
                            modifier = Modifier
                                .size(16.dp)
                                .alpha(stagger(progress, i))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteTextScreen(backdrop: LayerBackdrop, vm: EditorViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val dark = isSystemInDarkTheme()
    val bg = if (dark) BgDark else BgLight
    val card = if (dark) CardDark else CardLight
    val fg = if (dark) Color.White else Color.Black

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Lucide.ChevronLeft, contentDescription = "Back", tint = fg)
            }
            Text(
                text = "Note Text",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.width(48.dp))
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.padding(horizontal = 16.dp)) {
            Column {
                SettingsCard {
                    listOf(
                        NoteFont.DEFAULT to "Default",
                        NoteFont.SERIF to "Serif",
                        NoteFont.MONO to "Mono",
                    ).forEachIndexed { i, (font, label) ->
                        if (i > 0) CardDivider(fg)
                        SettingsRow(label = label, fg = fg, onClick = { vm.setFont(font) }) {
                            if (state.font == font) {
                                Icon(Lucide.Check, null, Modifier.size(16.dp), tint = fg)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                SettingsCard {
                    SettingsRow(label = "Smaller Text", fg = fg, onClick = { vm.setSmallerText(!state.smallerText) }) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (state.smallerText) fg else fg.copy(alpha = 0.15f),
                            modifier = Modifier.width(44.dp).height(26.dp)
                        ) {
                            Box(contentAlignment = if (state.smallerText) Alignment.CenterEnd else Alignment.CenterStart) {
                                Box(
                                    Modifier
                                        .padding(3.dp)
                                        .size(20.dp)
                                        .background(if (dark) BgDark else BgLight, RoundedCornerShape(50))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveActivityScreen(backdrop: LayerBackdrop, vm: EditorViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val dark = isSystemInDarkTheme()
    val bg = if (dark) BgDark else BgLight
    val card = if (dark) CardDark else CardLight
    val fg = if (dark) Color.White else Color.Black

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Lucide.ChevronLeft, contentDescription = "Back", tint = fg)
            }
            Text(
                text = "Live Activity",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.width(48.dp))
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(card, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.text.isBlank()) "Start typing..." else state.text,
                    fontSize = 14.sp,
                    color = fg.copy(alpha = 0.8f),
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "After you go live in Mononote, your note appears on the Lock Screen and in the Dynamic Island on supported devices. It can remain active for up to 8 hours.",
                fontSize = 13.sp,
                color = fg.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(20.dp))
            Text("Appearance", fontSize = 13.sp, color = fg.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))
            Column {
                SettingsCard {
                    SettingsRow(label = "Default", fg = fg, onClick = { vm.setLaClear(false) }) {
                        if (!state.laClear) Icon(Lucide.Check, null, Modifier.size(16.dp), tint = fg)
                    }
                    CardDivider(fg)
                    SettingsRow(label = "Clear", fg = fg, onClick = { vm.setLaClear(true) }) {
                        if (state.laClear) Icon(Lucide.Check, null, Modifier.size(16.dp), tint = fg)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Choose your preferred look for your note on the Lock Screen.",
                fontSize = 12.sp,
                color = fg.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSystemInDarkTheme()) CardDark else CardLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    fg: Color,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        icon?.let {
            Icon(imageVector = it, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        }
        Text(text = label, fontSize = 15.sp, color = fg, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun CardDivider(fg: Color) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 46.dp),
        thickness = 0.5.dp,
        color = fg.copy(alpha = 0.06f)
    )
}
