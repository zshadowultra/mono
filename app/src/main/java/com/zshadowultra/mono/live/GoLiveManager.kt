package com.zshadowultra.mono.live

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zshadowultra.mono.MainActivity
import com.zshadowultra.mono.MonoApp
import com.zshadowultra.mono.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object GoLiveManager {
    const val CHANNEL_ID = "live_note"
    const val NOTIFICATION_ID = 1001
    const val ACTION_STOP = "com.zshadowultra.mono.action.STOP_LIVE"

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Live note",
            NotificationManager.IMPORTANCE_LOW,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun start(context: Context, text: String) {
        ensureChannel(context)
        notify(context, text)
    }

    fun update(context: Context, text: String) {
        notify(context, text)
    }

    fun stop(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun notify(context: Context, text: String) {
        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(ACTION_STOP).setPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_note)
            .setContentTitle("Note")
            .setContentText(text.ifBlank { " " })
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openIntent)
            .addAction(0, "Stop", stopIntent)
            .setShowWhen(false)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted; nothing to do.
        }
    }
}

class GoLiveReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != GoLiveManager.ACTION_STOP) return
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                (appContext as MonoApp).repository.setLive(false)
                GoLiveManager.stop(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
