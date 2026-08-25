package com.zshadowultra.mono.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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

data class Settings(
    val font: NoteFont = NoteFont.DEFAULT,
    val smallerText: Boolean = false,
    val live: Boolean = false,
    val appearance: String = "system",
    val laClear: Boolean = false,
)

class NotesRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    val data: Flow<NotesData> = dataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { prefs ->
            prefs[stringPreferencesKey("notes_data")]?.let { raw ->
                runCatching { json.decodeFromString<NotesData>(raw) }.getOrDefault(NotesData())
            } ?: NotesData()
        }

    val settings: Flow<Settings> = dataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { prefs ->
            Settings(
                font = prefs[stringPreferencesKey("font")]?.let { name ->
                    runCatching { NoteFont.valueOf(name) }.getOrDefault(NoteFont.DEFAULT)
                } ?: NoteFont.DEFAULT,
                smallerText = prefs[booleanPreferencesKey("smaller_text")] ?: false,
                live = prefs[booleanPreferencesKey("live")] ?: false,
                appearance = prefs[stringPreferencesKey("appearance")] ?: "system",
                laClear = prefs[booleanPreferencesKey("la_clear")] ?: false,
            )
        }

    suspend fun saveActive(content: String) {
        dataStore.edit { prefs ->
            val current = decode(prefs)
            val now = System.currentTimeMillis()
            val updated = when {
                content.isBlank() && current.active == null -> current
                content.isBlank() && current.active != null -> current.copy(active = current.active.copy(content = "", updatedAt = now))
                current.active == null -> current.copy(
                    active = Note(id = current.nextId, content = content, createdAt = now, updatedAt = now),
                    nextId = current.nextId + 1,
                )
                else -> current.copy(active = current.active.copy(content = content, updatedAt = now))
            }
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(NotesData.serializer(), updated)
        }
    }

    suspend fun archiveActive(): Note? {
        var archived: Note? = null
        dataStore.edit { prefs ->
            val current = decode(prefs)
            val active = current.active ?: return@edit
            archived = active.copy(archivedAt = System.currentTimeMillis())
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(
                NotesData.serializer(),
                current.copy(active = null, archived = listOf(archived!!) + current.archived),
            )
        }
        return archived
    }

    suspend fun deleteActive() {
        dataStore.edit { prefs ->
            val current = decode(prefs)
            prefs[stringPreferencesKey("notes_data")] =
                json.encodeToString(NotesData.serializer(), current.copy(active = null))
        }
    }

    suspend fun restore(noteId: Long) {
        dataStore.edit { prefs ->
            val current = decode(prefs)
            val note = current.archived.firstOrNull { it.id == noteId } ?: return@edit
            val now = System.currentTimeMillis()
            val restored = note.copy(archivedAt = null, updatedAt = now)
            val newData = if (current.active == null) {
                current.copy(active = restored, archived = current.archived.filterNot { it.id == noteId })
            } else {
                val reArchived = current.active.copy(archivedAt = now)
                current.copy(
                    active = restored,
                    archived = listOf(reArchived) + current.archived.filterNot { it.id == noteId },
                )
            }
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(NotesData.serializer(), newData)
        }
    }

    suspend fun deleteArchived(noteId: Long) {
        dataStore.edit { prefs ->
            val current = decode(prefs)
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(
                NotesData.serializer(),
                current.copy(archived = current.archived.filterNot { it.id == noteId }),
            )
        }
    }

    suspend fun deleteArchived(ids: Set<Long>) {
        dataStore.edit { prefs ->
            val current = decode(prefs)
            prefs[stringPreferencesKey("notes_data")] = json.encodeToString(
                NotesData.serializer(),
                current.copy(archived = current.archived.filterNot { it.id in ids }),
            )
        }
    }

    suspend fun setFont(font: NoteFont) = dataStore.edit { it[stringPreferencesKey("font")] = font.name }
    suspend fun setSmallerText(smaller: Boolean) = dataStore.edit { it[booleanPreferencesKey("smaller_text")] = smaller }
    suspend fun setLive(live: Boolean) = dataStore.edit { it[booleanPreferencesKey("live")] = live }
    suspend fun setAppearance(appearance: String) = dataStore.edit { it[stringPreferencesKey("appearance")] = appearance }
    suspend fun setLaClear(clear: Boolean) = dataStore.edit { it[booleanPreferencesKey("la_clear")] = clear }

    private fun decode(prefs: androidx.datastore.preferences.core.Preferences): NotesData =
        prefs[stringPreferencesKey("notes_data")]?.let { raw ->
            runCatching { json.decodeFromString<NotesData>(raw) }.getOrDefault(NotesData())
        } ?: NotesData()
}
