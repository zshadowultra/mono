package com.zshadowultra.mono

import android.app.Application
import android.content.Context
import com.zshadowultra.mono.data.NotesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MonoApp : Application() {
    val repository by lazy { NotesRepository(this) }
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

val Context.monoApp: MonoApp get() = applicationContext as MonoApp
