package com.heymahesh.agent.core.actions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.heymahesh.agent.core.models.ResolvedAction

class AndroidActionBridge(private val context: Context) {

    val contactsHelper = ContactsProviderHelper(context)

    fun executeAction(action: ResolvedAction): Boolean {
        return when (action) {
            is ResolvedAction.PhoneCall -> makePhoneCall(action.phoneNumber, action.useSpeakerphone)
            is ResolvedAction.FlashlightToggle -> toggleFlashlight(action.turnOn)
            is ResolvedAction.AppLaunch -> launchApp(action.appName, action.packageName)
            is ResolvedAction.AlarmSet -> setAlarm(action.timeExpression, action.label)
            is ResolvedAction.WhatsAppMessage -> openWhatsAppChat(action.phoneNumber, action.messageBody)
            is ResolvedAction.InstagramDM -> openInstagramDM()
            is ResolvedAction.CameraSnap -> openCameraApp()
            is ResolvedAction.SpeakOnly -> true
        }
    }

    private fun makePhoneCall(phoneNumber: String, useSpeaker: Boolean): Boolean {
        val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$cleanNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent)

            if (useSpeaker) {
                // Enable speakerphone after call initiates
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.isSpeakerphoneOn = true
            }
            return true
        } else {
            // Fallback to dialer screen if direct CALL permission isn't granted yet
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
            return true
        }
    }

    private fun toggleFlashlight(turnOn: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return false
            cameraManager.setTorchMode(cameraId, turnOn)
            true
        } catch (e: Exception) {
            Log.e("HeyMahesh", "Flashlight error: ${e.message}")
            false
        }
    }

    private fun launchApp(appName: String, explicitPackage: String?): Boolean {
        val pm = context.packageManager
        if (explicitPackage != null) {
            val intent = pm.getLaunchIntentForPackage(explicitPackage)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return true
            }
        }

        // Fuzzy match installed applications
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(appName.lowercase())) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }
        return false
    }

    private fun setAlarm(timeExpr: String, label: String): Boolean {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_HOUR, 7)
            putExtra(AlarmClock.EXTRA_MINUTES, 0)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun openWhatsAppChat(phoneNumber: String?, message: String): Boolean {
        val cleanNumber = phoneNumber?.replace("[^0-9]".toRegex(), "") ?: ""
        val url = if (cleanNumber.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}"
        } else {
            "https://api.whatsapp.com/send?text=${Uri.encode(message)}"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            `package` = "com.whatsapp"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to browser or generic share intent
            val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(genericIntent)
            true
        }
    }

    private fun openInstagramDM(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("instagram://direct_inbox")
            `package` = "com.instagram.android"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            launchApp("Instagram", "com.instagram.android")
        }
    }

    private fun openCameraApp(): Boolean {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
