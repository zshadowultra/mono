package com.zshadowultra.mono.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zshadowultra.mono.MonoApp
import com.zshadowultra.mono.data.NoteFont
import com.zshadowultra.mono.data.NoteSize
import com.zshadowultra.mono.data.NotesRepository
import com.zshadowultra.mono.live.GoLiveManager
import com.zshadowultra.mono.widget.updateNoteWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditorViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as MonoApp).repository
    private val appScope = (app as MonoApp).appScope

    data class UiState(
        val loaded: Boolean = false,
        val text: String = "",
        val activeId: Long? = null,
        val archived: List<com.zshadowultra.mono.data.Note> = emptyList(),
        val font: NoteFont = NoteFont.DEFAULT,
        val size: NoteSize = NoteSize.MEDIUM,
        val live: Boolean = false,
    )

    private val mutableState = MutableStateFlow(UiState())

    val state: StateFlow<UiState> = mutableState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UiState(),
    )

    private var debounceJob: Job? = null

    init {
        viewModelScope.launch {
            repository.data.collect { data ->
                val current = mutableState.value
                if (data.active?.id != current.activeId) {
                    mutableState.value = current.copy(
                        loaded = true,
                        text = data.active?.content.orEmpty(),
                        activeId = data.active?.id,
                        archived = data.archived,
                    )
                    debounceJob?.cancel()
                } else {
                    mutableState.value = current.copy(
                        loaded = true,
                        archived = data.archived,
                        activeId = data.active?.id,
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.settings.collect { settings ->
                mutableState.value = mutableState.value.copy(
                    font = settings.font,
                    size = settings.size,
                    live = settings.live,
                )
            }
        }
    }

    fun onTextChange(text: String) {
        mutableState.value = mutableState.value.copy(text = text)
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(600)
            persist()
        }
    }

    fun done() {
        debounceJob?.cancel()
        persist()
    }

    fun archive(context: Context) {
        if (mutableState.value.live) GoLiveManager.stop(context)
        appScope.launch {
            repository.archiveActive()
            refreshWidgetSafely(context)
        }
    }

    fun delete(context: Context) {
        if (mutableState.value.live) GoLiveManager.stop(context)
        appScope.launch {
            repository.deleteActive()
            refreshWidgetSafely(context)
        }
    }

    fun restore(id: Long, context: Context) {
        appScope.launch {
            repository.restore(id)
            refreshWidgetSafely(context)
        }
    }

    fun deleteArchived(id: Long, context: Context) {
        appScope.launch {
            repository.deleteArchived(id)
        }
    }

    fun setFont(font: NoteFont) {
        appScope.launch {
            repository.setFont(font)
        }
    }

    fun setSize(size: NoteSize) {
        appScope.launch {
            repository.setSize(size)
        }
    }

    fun toggleLive(context: Context) {
        val live = !mutableState.value.live
        if (live) {
            GoLiveManager.start(context, mutableState.value.text)
        } else {
            GoLiveManager.stop(context)
        }
        appScope.launch {
            repository.setLive(live)
        }
    }

    private fun persist() {
        val snapshot = mutableState.value
        val live = snapshot.live
        appScope.launch {
            repository.saveActive(snapshot.text)
            if (live) {
                try {
                    GoLiveManager.update(getApplication(), snapshot.text)
                } catch (e: Exception) {
                }
            }
            refreshWidgetSafely(getApplication())
        }
    }

    private fun refreshWidgetSafely(context: Context) {
        appScope.launch {
            try {
                updateNoteWidget(context.applicationContext)
            } catch (e: Exception) {
            }
        }
    }
}
