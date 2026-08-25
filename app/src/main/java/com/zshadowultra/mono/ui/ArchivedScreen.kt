package com.zshadowultra.mono.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Inbox
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Undo2
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.RoundedRectangle
import com.zshadowultra.mono.data.Note
import com.zshadowultra.mono.ui.theme.BgDark
import com.zshadowultra.mono.ui.theme.BgLight
import com.zshadowultra.mono.ui.theme.CardDark
import com.zshadowultra.mono.ui.theme.CardLight

@Composable
fun ArchivedScreen(backdrop: LayerBackdrop, vm: EditorViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val dark = isSystemInDarkTheme()
    val bg = if (dark) BgDark else BgLight
    val card = if (dark) CardDark else CardLight
    val fg = if (dark) Color.White else Color.Black
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxSize()
            .background(bg)
            .layerBackdrop(backdrop)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(56.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(28f.dp) },
                        effects = {
                            vibrancy()
                            blur(4f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        onDrawSurface = {
                            drawRect(if (dark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
                        }
                    )
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(imageVector = Lucide.ChevronLeft, contentDescription = null, tint = fg)
                }
                Text(
                    text = "Archived",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = fg,
                    modifier = Modifier.align(Alignment.Center)
                )
                Spacer(Modifier.width(44.dp).align(Alignment.CenterEnd))
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
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.archived, key = { it.id }) { note ->
                        ArchivedNoteRow(note = note, cardColor = card, fg = fg,
                            onRestore = { vm.restore(note.id, context) },
                            onDelete = { vm.deleteArchived(note.id, context) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedNoteRow(
    note: Note,
    cardColor: Color,
    fg: Color,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = note.content,
                fontSize = 15.sp,
                color = fg,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = DateUtils.getRelativeTimeSpanString(note.updatedAt).toString(),
                fontSize = 12.sp,
                color = fg.copy(alpha = 0.4f)
            )
        }
        IconButton(onClick = onRestore) {
            Icon(imageVector = Lucide.Undo2, contentDescription = null, tint = fg)
        }
        IconButton(onClick = onDelete) {
            Icon(imageVector = Lucide.Trash2, contentDescription = null, tint = fg)
        }
    }
}
