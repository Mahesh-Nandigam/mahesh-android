package com.heymahesh.agent.core.reasoning

import com.heymahesh.agent.core.data.SessionRepository
import com.heymahesh.agent.core.models.ContactEntity
import com.heymahesh.agent.core.models.PendingDisambiguationContext
import com.heymahesh.agent.core.models.PendingSlotContext
import com.heymahesh.agent.core.models.ResolvedAction
import com.heymahesh.agent.core.state.AssistantState
import java.util.Locale

interface ReasoningEngine {
    suspend fun processUserSpeech(
        rawTranscript: String,
        availableContactsProvider: suspend (String) -> List<ContactEntity>
    ): AssistantState
}

class DefaultReasoningEngine(
    private val sessionRepository: SessionRepository
) : ReasoningEngine {

    override suspend fun processUserSpeech(
        rawTranscript: String,
        availableContactsProvider: suspend (String) -> List<ContactEntity>
    ): AssistantState {
        val cleanInput = rawTranscript.trim()
        val lower = cleanInput.lowercase(Locale.ROOT)

        // -------------------------------------------------------------
        // STEP 1: Check active session for pending multi-turn states
        // -------------------------------------------------------------
        val pendingDisambiguation = sessionRepository.getDisambiguation()
        if (pendingDisambiguation != null) {
            return handleDisambiguationSelection(lower, pendingDisambiguation)
        }

        val pendingSlot = sessionRepository.getPendingSlot()
        if (pendingSlot != null) {
            return handleMissingSlotFill(cleanInput, pendingSlot)
        }

        // -------------------------------------------------------------
        // STEP 2: Strip Wake-Word & Filler Prefixes
        // -------------------------------------------------------------
        val stripped = stripWakeWordAndFillers(lower)

        // -------------------------------------------------------------
        // STEP 3: Classify & Route Intent
        // -------------------------------------------------------------

        // INTENT A: PHONE CALL
        if (isPhoneCallIntent(stripped)) {
            return handlePhoneCallIntent(stripped, availableContactsProvider)
        }

        // INTENT B: WHATSAPP MESSAGE
        if (isWhatsAppIntent(stripped)) {
            return handleWhatsAppIntent(stripped, availableContactsProvider)
        }

        // INTENT C: CAMERA SNAP / SELFIE
        if (isCameraIntent(stripped)) {
            return handleCameraIntent(stripped)
        }

        // INTENT D: FLASHLIGHT / TORCH
        if (isFlashlightIntent(stripped)) {
            return handleFlashlightIntent(stripped)
        }

        // INTENT E: INSTAGRAM DM / OPEN
        if (isInstagramIntent(stripped)) {
            return AssistantState.Executing(
                action = ResolvedAction.InstagramDM(),
                statusMessage = "Opening Instagram DMs for you."
            )
        }

        // INTENT F: APP LAUNCH
        if (isAppLaunchIntent(stripped)) {
            val appName = extractAppName(stripped)
            return AssistantState.Executing(
                action = ResolvedAction.AppLaunch(appName = appName),
                statusMessage = "Opening $appName right away."
            )
        }

        // INTENT G: ALARM / TIMER
        if (isAlarmIntent(stripped)) {
            return AssistantState.Executing(
                action = ResolvedAction.AlarmSet(timeExpression = stripped),
                statusMessage = "Setting alarm as requested."
            )
        }

        // DEFAULT: Chit-Chat / Fallback
        return AssistantState.Speaking(
            spokenText = "I'm Mahesh, your sovereign Android AI. Ask me to call a friend, send a WhatsApp message, or snap a photo!",
            willResumeListening = false
        )
    }

    // -------------------------------------------------------------
    // MULTI-TURN RESOLVERS
    // -------------------------------------------------------------

    private suspend fun handleDisambiguationSelection(
        choiceText: String,
        context: PendingDisambiguationContext
    ): AssistantState {
        var selectedContact: ContactEntity? = null

        // Match by tag (e.g. "college", "uncle", "jio", "work")
        for (cand in context.candidates) {
            if (choiceText.contains(cand.tag.lowercase()) || choiceText.contains(cand.name.lowercase())) {
                selectedContact = cand
                break
            }
        }

        // Match by ordinal (e.g. "first", "second", "1", "2", "one", "two")
        if (selectedContact == null) {
            val ordinalMap = mapOf("first" to 0, "1" to 0, "one" to 0, "second" to 1, "2" to 1, "two" to 1, "third" to 2, "3" to 2)
            for ((key, index) in ordinalMap) {
                if (choiceText.contains(key) && index < context.candidates.size) {
                    selectedContact = context.candidates[index]
                    break
                }
            }
        }

        if (selectedContact != null) {
            sessionRepository.clearSession()
            return when (context.intent) {
                "call" -> AssistantState.Executing(
                    action = ResolvedAction.PhoneCall(
                        contactName = selectedContact.name,
                        phoneNumber = selectedContact.phoneNumber,
                        useSpeakerphone = true
                    ),
                    statusMessage = "Calling ${selectedContact.name} on speaker now."
                )
                "whatsapp" -> {
                    val messageBody = context.partialData["message_body"]
                    if (!messageBody.isNullOrBlank()) {
                        AssistantState.Executing(
                            action = ResolvedAction.WhatsAppMessage(
                                recipientName = selectedContact.name,
                                phoneNumber = selectedContact.phoneNumber,
                                messageBody = messageBody
                            ),
                            statusMessage = "Sending WhatsApp message to ${selectedContact.name}."
                        )
                    } else {
                        sessionRepository.setPendingSlot(
                            PendingSlotContext(
                                intent = "whatsapp",
                                missingSlot = "message_body",
                                targetName = selectedContact.name,
                                targetPhone = selectedContact.phoneNumber
                            )
                        )
                        AssistantState.MissingSlot(
                            question = "Got it, what message should I send to ${selectedContact.name}?",
                            missingSlotName = "message_body",
                            targetEntity = selectedContact.name
                        )
                    }
                }
                else -> AssistantState.Speaking("Action resolved for ${selectedContact.name}.")
            }
        }

        val optionsPrompt = context.candidates.joinToString(", ") { "${it.name} (${it.tag})" }
        return AssistantState.Disambiguating(
            question = "I didn't catch that. Which one: $optionsPrompt?",
            targetName = context.targetName,
            candidates = context.candidates,
            originalIntent = context.intent
        )
    }

    private suspend fun handleMissingSlotFill(
        rawInput: String,
        context: PendingSlotContext
    ): AssistantState {
        sessionRepository.clearSession()
        return if (context.missingSlot == "message_body") {
            val recipient = context.targetName ?: "your contact"
            AssistantState.Executing(
                action = ResolvedAction.WhatsAppMessage(
                    recipientName = recipient,
                    phoneNumber = context.targetPhone,
                    messageBody = rawInput
                ),
                statusMessage = "Sending '$rawInput' to $recipient on WhatsApp right now."
            )
        } else {
            AssistantState.Speaking("Thanks, proceeding with your request.")
        }
    }

    // -------------------------------------------------------------
    // INTENT HANDLERS
    // -------------------------------------------------------------

    private suspend fun handlePhoneCallIntent(
        query: String,
        contactsProvider: suspend (String) -> List<ContactEntity>
    ): AssistantState {
        val targetName = extractTargetName(query, listOf("call", "dial", "ring", "phone", "to", "on speaker"))
        if (targetName.isBlank()) {
            sessionRepository.setPendingSlot(
                PendingSlotContext(intent = "call", missingSlot = "target_name")
            )
            return AssistantState.MissingSlot(
                question = "Who would you like me to call?",
                missingSlotName = "target_name",
                targetEntity = ""
            )
        }

        val matches = contactsProvider(targetName)
        return when {
            matches.isEmpty() -> AssistantState.Speaking("I couldn't find any contact named $targetName.")
            matches.size == 1 -> {
                val target = matches[0]
                AssistantState.Executing(
                    action = ResolvedAction.PhoneCall(
                        contactName = target.name,
                        phoneNumber = target.phoneNumber,
                        useSpeakerphone = true
                    ),
                    statusMessage = "Calling ${target.name} on speaker now."
                )
            }
            else -> {
                sessionRepository.setDisambiguation(
                    PendingDisambiguationContext(
                        intent = "call",
                        targetName = targetName,
                        candidates = matches
                    )
                )
                val options = matches.joinToString(", ") { "${it.name} (${it.tag})" }
                AssistantState.Disambiguating(
                    question = "I found ${matches.size} contacts for $targetName: $options. Which one should I ring?",
                    targetName = targetName,
                    candidates = matches,
                    originalIntent = "call"
                )
            }
        }
    }

    private suspend fun handleWhatsAppIntent(
        query: String,
        contactsProvider: suspend (String) -> List<ContactEntity>
    ): AssistantState {
        // Check for pattern: "message Rahul that I am coming"
        val regexWithBody = Regex("""(?:message|whatsapp|text)\s+(?:to\s+)?([a-zA-Z0-9\s]+?)\s+(?:that|saying|to say|with text)\s+(.+)""")
        val matchResult = regexWithBody.find(query)

        if (matchResult != null) {
            val rawName = matchResult.groupValues[1].trim()
            val messageBody = matchResult.groupValues[2].trim()
            val candidates = contactsProvider(rawName)
            val recipientName = if (candidates.isNotEmpty()) candidates[0].name else rawName
            val recipientPhone = if (candidates.isNotEmpty()) candidates[0].phoneNumber else null

            return AssistantState.Executing(
                action = ResolvedAction.WhatsAppMessage(
                    recipientName = recipientName,
                    phoneNumber = recipientPhone,
                    messageBody = messageBody
                ),
                statusMessage = "Sending '$messageBody' to $recipientName on WhatsApp."
            )
        }

        // Pattern: "message Rahul" (Missing message body)
        val rawName = extractTargetName(query, listOf("send", "message", "whatsapp", "text", "to", "on whatsapp"))
        val candidates = contactsProvider(rawName)

        if (candidates.size > 1) {
            sessionRepository.setDisambiguation(
                PendingDisambiguationContext(
                    intent = "whatsapp",
                    targetName = rawName,
                    candidates = candidates
                )
            )
            val options = candidates.joinToString(", ") { it.name }
            return AssistantState.Disambiguating(
                question = "I found multiple contacts for $rawName: $options. Which one?",
                targetName = rawName,
                candidates = candidates,
                originalIntent = "whatsapp"
            )
        }

        val targetName = if (candidates.isNotEmpty()) candidates[0].name else rawName
        val targetPhone = if (candidates.isNotEmpty()) candidates[0].phoneNumber else null

        sessionRepository.setPendingSlot(
            PendingSlotContext(
                intent = "whatsapp",
                missingSlot = "message_body",
                targetName = targetName,
                targetPhone = targetPhone
            )
        )

        return AssistantState.MissingSlot(
            question = "You told me to message $targetName on WhatsApp, but what should I say?",
            missingSlotName = "message_body",
            targetEntity = targetName
        )
    }

    private fun handleCameraIntent(query: String): AssistantState {
        val isFront = query.contains("selfie") || query.contains("front")
        val countdown = when {
            query.contains("5 second") || query.contains("five") -> 5
            query.contains("instant") || query.contains("now") -> 0
            else -> 3
        }
        val facing = if (isFront) "front" else "back"

        return AssistantState.Executing(
            action = ResolvedAction.CameraSnap(cameraFacing = facing, countdownSeconds = countdown),
            statusMessage = "Opening $facing camera and taking photo in $countdown seconds. Smile!"
        )
    }

    private fun handleFlashlightIntent(query: String): AssistantState {
        val turnOff = query.contains("off") || query.contains("disable")
        return AssistantState.Executing(
            action = ResolvedAction.FlashlightToggle(turnOn = !turnOff),
            statusMessage = if (turnOff) "Turning off flashlight." else "Turning on flashlight."
        )
    }

    // -------------------------------------------------------------
    // UTILITY HELPERS
    // -------------------------------------------------------------

    private fun stripWakeWordAndFillers(input: String): String {
        var res = input
        val prefixes = listOf("hey mahesh", "ok mahesh", "mahesh", "yo mahesh", "bro mahesh")
        for (prefix in prefixes) {
            if (res.startsWith(prefix)) {
                res = res.removePrefix(prefix).trim()
            }
        }
        val fillers = listOf("please", "can you", "could you", "just", "fast", "quickly", "bro")
        for (filler in fillers) {
            res = res.replace(Regex("""\b$filler\b"""), "").trim()
        }
        return res
    }

    private fun extractTargetName(query: String, removeKeywords: List<String>): String {
        var clean = query
        for (kw in removeKeywords) {
            clean = clean.replace(Regex("""\b$kw\b"""), "")
        }
        return clean.trim().replace(Regex("""\s+"""), " ")
    }

    private fun extractAppName(query: String): String {
        return query.removePrefix("open").removePrefix("launch").trim()
    }

    private fun isPhoneCallIntent(q: String) = q.contains("call") || q.contains("dial") || q.contains("ring")
    private fun isWhatsAppIntent(q: String) = q.contains("whatsapp") || q.contains("message") || q.contains("text")
    private fun isCameraIntent(q: String) = q.contains("photo") || q.contains("picture") || q.contains("selfie") || q.contains("camera") || q.contains("snap")
    private fun isFlashlightIntent(q: String) = q.contains("flashlight") || q.contains("torch")
    private fun isInstagramIntent(q: String) = q.contains("instagram") || q.contains("insta")
    private fun isAppLaunchIntent(q: String) = q.startsWith("open") || q.startsWith("launch")
    private fun isAlarmIntent(q: String) = q.contains("alarm") || q.contains("wake me") || q.contains("timer")
}
