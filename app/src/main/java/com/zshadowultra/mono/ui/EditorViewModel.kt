package com.zshadowultra.mono.ui

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zshadowultra.mono.MonoApp
import com.zshadowultra.mono.data.Note
import com.zshadowultra.mono.data.NoteFont
import com.zshadowultra.mono.data.Settings
import com.zshadowultra.mono.live.GoLiveManager
import com.zshadowultra.mono.widget.updateNoteWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditorViewModel(app: android.app.Application) : AndroidViewModel(app) {

    data class UiState(
        val loaded: Boolean = false,
        val text: String = "",
        val activeId: Long? = null,
        val archived: List<Note> = emptyList(),
        val font: NoteFont = NoteFont.DEFAULT,
        val smallerText: Boolean = false,
        val live: Boolean = false,
        val appearance: String = "system",
        val laClear: Boolean = false,
    )

    private val repo = (app as MonoApp).repository
    private val external = MutableStateFlow(UiState())
    private val localText = MutableStateFlow<String?>(null)
    private var saveJob: Job? = null

    val state: StateFlow<UiState> =
        combine(repo.data, repo.settings, external, localText) { data, settings, ext, text ->
            val active = data.active
            val adopted = if (active?.id != ext.activeId) (active?.content ?: "") else ext.text
            UiState(
                loaded = true,
                text = text ?: adopted,
                activeId = active?.id,
                archived = data.archived,
                font = settings.font,
                smallerText = settings.smallerText,
                live = settings.live,
                appearance = settings.appearance,
                laClear = settings.laClear,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    fun onTextChange(text: String) {
        localText.value = text
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(600)
            persist()
        }
    }

    fun done() {
        saveJob?.cancel()
        viewModelScope.launch { persist() }
    }

    fun clearText() {
        localText.value = ""
        saveJob?.cancel()
        viewModelScope.launch { persist() }
    }

    private suspend fun persist() {
        val text = localText.value ?: state.value.text
        repo.saveActive(text)
        val ctx = getApplication<MonoApp>()
        if (state.value.live) GoLiveManager.update(ctx, text, state.value.laClear)
        runCatching { updateNoteWidget(ctx) }
    }

    fun archive(context: Context) {
        viewModelScope.launch {
            if (state.value.live) {
                GoLiveManager.stop(context)
                repo.setLive(false)
            }
            repo.archiveActive()
            localText.value = ""
            runCatching { updateNoteWidget(context) }
        }
    }

    fun delete(context: Context) {
        viewModelScope.launch {
            if (state.value.live) {
                GoLiveManager.stop(context)
                repo.setLive(false)
            }
            repo.deleteActive()
            localText.value = ""
            runCatching { updateNoteWidget(context) }
        }
    }

    fun restore(id: Long, context: Context) {
        viewModelScope.launch {
            repo.restore(id)
            localText.value = null
            runCatching { updateNoteWidget(context) }
        }
    }

    fun deleteArchived(id: Long, context: Context) {
        viewModelScope.launch {
            repo.deleteArchived(id)
            runCatching { updateNoteWidget(context) }
        }
    }

    fun deleteArchived(ids: Set<Long>, context: Context) {
        viewModelScope.launch {
            repo.deleteArchived(ids)
            runCatching { updateNoteWidget(context) }
        }
    }

    fun setFont(font: NoteFont) = viewModelScope.launch { repo.setFont(font) }
    fun setSmallerText(smaller: Boolean) = viewModelScope.launch { repo.setSmallerText(smaller) }
    fun setAppearance(appearance: String) = viewModelScope.launch { repo.setAppearance(appearance) }
    fun setLaClear(clear: Boolean) = viewModelScope.launch { repo.setLaClear(clear) }

    fun toggleLive(context: Context) {
        viewModelScope.launch {
            val live = state.value.live
            if (live) {
                GoLiveManager.stop(context)
                repo.setLive(false)
            } else {
                GoLiveManager.start(context, state.value.text, state.value.laClear)
                repo.setLive(true)
            }
        }
    }
}
