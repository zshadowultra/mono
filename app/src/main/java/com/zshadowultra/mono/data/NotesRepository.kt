package com.zshadowultra.mono.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "mono")

@Serializable
data class Note(
    val id: Long,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long? = null,
)

@Serializable
data class NotesData(
    val active: Note? = null,
    val archived: List<Note> = emptyList(),
    val nextId: Long = 1L,
)

enum class NoteFont { DEFAULT, SERIF, MONO }

enum class NoteSize { SMALL, MEDIUM, LARGE }

data class Settings(
    val font: NoteFont = NoteFont.DEFAULT,
    val size: NoteSize = NoteSize.MEDIUM,
    val live: Boolean = false,
)

class NotesRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    private val json = Json { ignoreUnknownKeys = true }

    val data: Flow<NotesData> = dataStore.data.map { prefs ->
        prefs[stringPreferencesKey("notes_data")]?.let { encoded ->
            try {
                json.decodeFromString<NotesData>(encoded)
            } catch (e: IOException) {
                NotesData()
            } catch (e: SerializationException) {
                NotesData()
            }
        } ?: NotesData()
    }

    val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            font = prefs[stringPreferencesKey("font")]?.let { stored ->
                runCatching { NoteFont.valueOf(stored) }.getOrNull()
            } ?: NoteFont.DEFAULT,
            size = prefs[stringPreferencesKey("size")]?.let { stored ->
                runCatching { NoteSize.valueOf(stored) }.getOrNull()
            } ?: NoteSize.MEDIUM,
            live = prefs[booleanPreferencesKey("live")] ?: false,
        )
    }

    suspend fun saveActive(content: String) {
        val now = System.currentTimeMillis()
        dataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey("notes_data")])
            if (content.isBlank() && current.active == null) return@edit
            val updated = if (content.isBlank()) {
                current.copy(active = current.active?.copy(content = "", updatedAt = now))
            } else {
                val active = current.active
                if (active == null) {
                    current.copy(
                        active = Note(id = current.nextId, content = content, createdAt = now, updatedAt = now),
                        nextId = current.nextId + 1,
                    )
                } else {
                    current.copy(active = active.copy(content = content, updatedAt = now))
                }
            }
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(updated)
        }
    }

    suspend fun archiveActive(): Note? {
        val now = System.currentTimeMillis()
        var archivedNote: Note? = null
        dataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey("notes_data")])
            val active = current.active ?: return@edit
            val moved = active.copy(archivedAt = now)
            archivedNote = moved
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(
                current.copy(active = null, archived = listOf(moved) + current.archived),
            )
        }
        return archivedNote
    }

    suspend fun deleteActive() {
        dataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey("notes_data")])
            prefs[stringPreferencesKey("notes_data")] =
                json.encodeToString(current.copy(active = null))
        }
    }

    suspend fun restore(noteId: Long) {
        val now = System.currentTimeMillis()
        dataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey("notes_data")])
            val note = current.archived.firstOrNull { it.id == noteId } ?: return@edit
            val remaining = current.archived.filterNot { it.id == noteId }
            val restored = note.copy(archivedAt = null, updatedAt = now)
            val updated = when (val active = current.active) {
                null -> current.copy(active = restored, archived = remaining)
                else -> {
                    val rearchived = active.copy(archivedAt = now)
                    current.copy(active = restored, archived = listOf(rearchived) + remaining)
                }
            }
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(updated)
        }
    }

    suspend fun deleteArchived(noteId: Long) {
        dataStore.edit { prefs ->
            val current = decode(prefs[stringPreferencesKey("notes_data")])
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(
                current.copy(archived = current.archived.filterNot { it.id == noteId }),
            )
        }
    }

    suspend fun setFont(font: NoteFont) {
        dataStore.edit { it[stringPreferencesKey("font")] = font.name }
    }

    suspend fun setSize(size: NoteSize) {
        dataStore.edit { it[stringPreferencesKey("size")] = size.name }
    }

    suspend fun setLive(live: Boolean) {
        dataStore.edit { it[booleanPreferencesKey("live")] = live }
    }

    private fun decode(encoded: String?): NotesData {
        if (encoded == null) return NotesData()
        return try {
            json.decodeFromString<NotesData>(encoded)
        } catch (e: IOException) {
            NotesData()
        } catch (e: SerializationException) {
            NotesData()
        }
    }
}
