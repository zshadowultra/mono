package com.zshadowultra.mono.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.cornerRadius
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.zshadowultra.mono.data.NotesRepository
import kotlinx.coroutines.flow.first

class NoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = NotesRepository(context).data.first()
        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(
                        ColorProvider(
                            day = Color(android.graphics.Color.parseColor("#F2F2F7")),
                            night = Color(android.graphics.Color.parseColor("#1C1C1E")),
                        ),
                    )
                    .cornerRadius(24.dp)
                    .padding(14.dp),
            ) {
                Text(
                    text = data.active?.content?.ifBlank { "Start typing..." }
                        ?: "Start typing...",
                    style = TextStyle(
                        color = ColorProvider(
                            day = Color(0xFF111111),
                            night = Color(0xFFF2F2F7),
                        ),
                        fontSize = 16.sp,
                    ),
                )
            }
        }
    }
}

class NoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = NoteWidget()
}

suspend fun updateNoteWidget(context: Context) {
    NoteWidget().updateAll(context)
}
