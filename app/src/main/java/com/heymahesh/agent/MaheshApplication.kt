package com.heymahesh.agent

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class MaheshApplication : Application() {

    companion object {
        const val TAG = "HeyMahesh"
        const val WAKE_SERVICE_CHANNEL_ID = "hey_mahesh_wake_channel"
        const val ACTION_NOTIFICATION_CHANNEL_ID = "hey_mahesh_action_channel"
        
        lateinit var instance: MaheshApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i(TAG, "🚀 Mahesh- Application Initialized.")
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Low-priority persistent channel for Wake Word Background Listening
            val wakeChannel = NotificationChannel(
                WAKE_SERVICE_CHANNEL_ID,
                "Mahesh Wake Word Listener",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps 'Hey Mahesh' voice detection running in background"
                setShowBadge(false)
            }

            // High-priority channel for Action Prompts and Alerts
            val actionChannel = NotificationChannel(
                ACTION_NOTIFICATION_CHANNEL_ID,
                "Mahesh Action Prompts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows live status when Mahesh is executing an action"
            }

            notificationManager.createNotificationChannel(wakeChannel)
            notificationManager.createNotificationChannel(actionChannel)
        }
    }
}
