package com.heymahesh.agent.core.models

import kotlinx.serialization.Serializable

@Serializable
data class ContactEntity(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val tag: String = "Mobile", // e.g. "College", "Family", "Jio", "Work"
    val photoUri: String? = null
)

@Serializable
enum class ActionType {
    PHONE_CALL,
    WHATSAPP_MESSAGE,
    INSTAGRAM_DM,
    CAMERA_SNAP,
    FLASHLIGHT,
    APP_LAUNCH,
    ALARM,
    GENERAL_CHAT
}

@Serializable
sealed class ResolvedAction {
    @Serializable
    data class PhoneCall(
        val contactName: String,
        val phoneNumber: String,
        val useSpeakerphone: Boolean = true
    ) : ResolvedAction()

    @Serializable
    data class WhatsAppMessage(
        val recipientName: String,
        val phoneNumber: String? = null,
        val messageBody: String
    ) : ResolvedAction()

    @Serializable
    data class InstagramDM(
        val username: String? = null,
        val messageBody: String? = null
    ) : ResolvedAction()

    @Serializable
    data class CameraSnap(
        val cameraFacing: String = "front", // "front" or "back"
        val countdownSeconds: Int = 3
    ) : ResolvedAction()

    @Serializable
    data class FlashlightToggle(
        val turnOn: Boolean
    ) : ResolvedAction()

    @Serializable
    data class AppLaunch(
        val appName: String,
        val packageName: String? = null
    ) : ResolvedAction()

    @Serializable
    data class AlarmSet(
        val timeExpression: String,
        val label: String = "Mahesh Voice Alarm"
    ) : ResolvedAction()

    @Serializable
    data class SpeakOnly(
        val text: String
    ) : ResolvedAction()
}

@Serializable
data class PendingDisambiguationContext(
    val intent: String,
    val targetName: String,
    val candidates: List<ContactEntity>,
    val partialData: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class PendingSlotContext(
    val intent: String,
    val missingSlot: String, // e.g. "message_body", "target_name"
    val targetName: String? = null,
    val targetPhone: String? = null,
    val partialData: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class VoiceFeedbackConfig(
    val speechRate: Float = 1.15f,
    val pitch: Float = 1.0f,
    val locale: String = "en-IN",
    val autoPlayTTS: Boolean = true
)

@Serializable
data class AppPermissionState(
    val hasAudioRecord: Boolean = false,
    val hasSystemOverlay: Boolean = false,
    val hasAccessibility: Boolean = false,
    val hasReadContacts: Boolean = false,
    val hasCallPhone: Boolean = false,
    val hasCamera: Boolean = false,
    val hasPostNotifications: Boolean = false
) {
    val isReadyForFullAutonomy: Boolean
        get() = hasAudioRecord && hasSystemOverlay && hasAccessibility && hasReadContacts && hasCallPhone
}

@Serializable
data class CommandHistoryItem(
    val id: String,
    val rawTranscript: String,
    val interpretedAction: String,
    val executionSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
