package com.zshadowultra.mono.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Inbox
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Redo2
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.zshadowultra.mono.data.Note
import com.zshadowultra.mono.ui.theme.BgDark
import com.zshadowultra.mono.ui.theme.noteFontFamily
import com.zshadowultra.mono.ui.theme.BgLight
import com.zshadowultra.mono.ui.theme.CardDark
import com.zshadowultra.mono.ui.theme.CardLight

@Composable
fun ArchivedScreen(backdrop: LayerBackdrop, vm: EditorViewModel, onBack: () -> Unit) {
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
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var query by remember { mutableStateOf("") }

    val filtered = state.archived.filter {
        query.isBlank() || it.content.contains(query, ignoreCase = true)
    }

    Box(Modifier.fillMaxSize().background(bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Lucide.X, contentDescription = "Close", tint = fg)
                }
                Text(
                    text = "Archived Notes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = fg,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = if (selecting) "Done" else "Select",
                    fontSize = 15.sp,
                    color = fg,
                    modifier = Modifier
                        .clickable(
                            interactionSource = null,
                            indication = null
                        ) {
                            selecting = !selecting
                            if (!selecting) selected = emptySet()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            if (state.archived.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Lucide.Inbox,
                        contentDescription = null,
                        tint = fg.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(text = "No archived notes", fontSize = 15.sp, color = fg.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(filtered, key = { it.id }) { note ->
                        ArchivedRow(
                            note = note,
                            fg = fg,
                            fontFamily = noteFontFamily(state.font),
                            selecting = selecting,
                            checked = note.id in selected,
                            onToggle = {
                                selected = if (note.id in selected) {
                                    selected - note.id
                                } else {
                                    selected + note.id
                                }
                            },
                            onRestore = { vm.restore(note.id, context) }
                        )
                    }
                }
            }
        }

        if (state.archived.isNotEmpty()) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .background(card, RoundedCornerShape(22.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.Search,
                    contentDescription = null,
                    tint = fg.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = fg),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) {
                                Text("Search", fontSize = 15.sp, color = fg.copy(alpha = 0.4f))
                            }
                            inner()
                        }
                    }
                )
            }
        }

        if (selecting && selected.isNotEmpty()) {
            Button(
                onClick = {
                    vm.deleteArchived(selected, context)
                    selected = emptySet()
                    selecting = false
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 72.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC0392B),
                    contentColor = Color.White
                )
            ) {
                Text("Delete ${selected.size}", fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun ArchivedRow(
    note: Note,
    fg: Color,
    fontFamily: FontFamily,
    selecting: Boolean,
    checked: Boolean,
    onToggle: () -> Unit,
    onRestore: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = null,
                enabled = selecting,
                onClick = onToggle
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selecting) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = fg,
                    checkmarkColor = if (fg == Color.White) Color.Black else Color.White
                )
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = note.content.ifBlank { " " },
                fontSize = 15.sp,
                fontFamily = fontFamily,
                color = fg,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = DateUtils.getRelativeTimeSpanString(
                    note.updatedAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString(),
                fontSize = 12.sp,
                color = fg.copy(alpha = 0.4f)
            )
        }
        if (!selecting) {
            IconButton(onClick = onRestore) {
                Icon(imageVector = Lucide.Redo2, contentDescription = "Restore", tint = fg)
            }
        }
    }
}
