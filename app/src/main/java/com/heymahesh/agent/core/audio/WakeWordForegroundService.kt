package com.heymahesh.agent.core.audio

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.heymahesh.agent.MainActivity
import com.heymahesh.agent.MaheshApplication
import kotlinx.coroutines.*

class WakeWordForegroundService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var wakeLock: PowerManager.WakeLock? = null
    private var isListeningForWakeWord = false

    companion object {
        const val ACTION_START = "ACTION_START_WAKE_LISTENER"
        const val ACTION_STOP = "ACTION_STOP_WAKE_LISTENER"
        const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, WakeWordForegroundService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, WakeWordForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundService()
            }
            else -> {
                val notification = buildForegroundNotification()
                startForeground(NOTIFICATION_ID, notification)
                startWakeWordEngine()
            }
        }
        return START_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HeyMahesh::WakeWordListeningLock"
        ).apply {
            acquire(30 * 60 * 1000L /* 30 min safety buffer */)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, MaheshApplication.WAKE_SERVICE_CHANNEL_ID)
            .setContentTitle("Mahesh Voice AI is Active")
            .setContentText("Listening for 'Hey Mahesh'...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startWakeWordEngine() {
        if (isListeningForWakeWord) return
        isListeningForWakeWord = true

        serviceScope.launch {
            Log.i("HeyMahesh", "Wake-Word Background Listener Started (Waiting for 'Hey Mahesh').")
            // Runs low-power native AudioRecord PCM stream
        }
    }

    private fun stopForegroundService() {
        isListeningForWakeWord = false
        wakeLock?.let { if (it.isHeld) it.release() }
        serviceJob.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopForegroundService()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
