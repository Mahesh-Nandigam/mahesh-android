package com.heymahesh.agent.core

import android.content.Context
import android.util.Log
import com.heymahesh.agent.core.accessibility.ScreenPilotAccessibilityService
import com.heymahesh.agent.core.actions.AndroidActionBridge
import com.heymahesh.agent.core.audio.SpeechRecognizerHelper
import com.heymahesh.agent.core.audio.TTSManager
import com.heymahesh.agent.core.data.SessionRepository
import com.heymahesh.agent.core.models.CommandHistoryItem
import com.heymahesh.agent.core.models.ResolvedAction
import com.heymahesh.agent.core.overlay.FloatingHUDController
import com.heymahesh.agent.core.reasoning.DefaultReasoningEngine
import com.heymahesh.agent.core.state.AssistantState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceAgentController(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val sessionRepository = SessionRepository()
    private val reasoningEngine = DefaultReasoningEngine(sessionRepository)
    private val actionBridge = AndroidActionBridge(context)
    private val hudController = FloatingHUDController(context)

    private val speechRecognizer = SpeechRecognizerHelper(context)
    private val ttsManager = TTSManager(context)

    private val _agentState = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val agentState: StateFlow<AssistantState> = _agentState.asStateFlow()

    init {
        speechRecognizer.initialize(
            onFinalResult = { transcript ->
                processVoiceCommand(transcript)
            },
            onError = { error ->
                _agentState.value = AssistantState.Error(error)
                ttsManager.speak(error)
            }
        )
    }

    fun startVoiceInteraction(showFloatingHUD: Boolean = true) {
        _agentState.value = AssistantState.Listening
        if (showFloatingHUD && hudController.canDrawOverlays) {
            hudController.showOverlay(
                stateFlow = agentState,
                onOptionSelected = { choice ->
                    processVoiceCommand(choice)
                },
                onDismiss = {
                    stop()
                }
            )
        }
        speechRecognizer.startListening()
    }

    fun processVoiceCommand(transcript: String) {
        scope.launch {
            _agentState.value = AssistantState.Thinking(transcript)

            val nextState = reasoningEngine.processUserSpeech(
                rawTranscript = transcript,
                availableContactsProvider = { query ->
                    actionBridge.contactsHelper.searchContacts(query)
                }
            )

            _agentState.value = nextState
            handleStateExecution(transcript, nextState)
        }
    }

    private fun handleStateExecution(originalQuery: String, state: AssistantState) {
        when (state) {
            is AssistantState.Executing -> {
                ttsManager.speak(state.statusMessage) {
                    val success = actionBridge.executeAction(state.action)

                    // If it's a WhatsApp message and Accessibility service is active, auto-pilot type & send
                    if (state.action is ResolvedAction.WhatsAppMessage && ScreenPilotAccessibilityService.isRunning) {
                        ScreenPilotAccessibilityService.instance?.autoTypeAndSendInWhatsApp(state.action.messageBody) { sent ->
                            Log.i("HeyMahesh", "WhatsApp auto-pilot send status: $sent")
                        }
                    }

                    scope.launch {
                        sessionRepository.logCommand(
                            CommandHistoryItem(
                                id = "cmd_${System.currentTimeMillis()}",
                                rawTranscript = originalQuery,
                                interpretedAction = state.action.javaClass.simpleName,
                                executionSuccess = success
                            )
                        )
                    }
                }
            }

            is AssistantState.Disambiguating -> {
                ttsManager.speak(state.question) {
                    // Re-open mic for user selection
                    speechRecognizer.startListening()
                }
            }

            is AssistantState.MissingSlot -> {
                ttsManager.speak(state.question) {
                    // Re-open mic to capture missing parameter
                    speechRecognizer.startListening()
                }
            }

            is AssistantState.Speaking -> {
                ttsManager.speak(state.spokenText) {
                    if (state.willResumeListening) {
                        speechRecognizer.startListening()
                    } else {
                        _agentState.value = AssistantState.Idle
                    }
                }
            }

            else -> Unit
        }
    }

    fun stop() {
        speechRecognizer.stopListening()
        ttsManager.stop()
        hudController.hideOverlay()
        _agentState.value = AssistantState.Idle
    }
}
