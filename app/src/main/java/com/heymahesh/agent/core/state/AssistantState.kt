package com.heymahesh.agent.core.state

import com.heymahesh.agent.core.models.ContactEntity
import com.heymahesh.agent.core.models.ResolvedAction
import kotlinx.serialization.Serializable

/**
 * High-performance Sealed Interface representing all lifecycle states of Mahesh.
 */
sealed interface AssistantState {
    object Idle : AssistantState
    object Listening : AssistantState
    data class Thinking(val rawQuery: String) : AssistantState
    
    data class Disambiguating(
        val question: String,
        val targetName: String,
        val candidates: List<ContactEntity>,
        val originalIntent: String
    ) : AssistantState

    data class MissingSlot(
        val question: String,
        val missingSlotName: String,
        val targetEntity: String
    ) : AssistantState

    data class Executing(
        val action: ResolvedAction,
        val statusMessage: String
    ) : AssistantState

    data class Speaking(
        val spokenText: String,
        val willResumeListening: Boolean = false
    ) : AssistantState

    data class Error(
        val errorMessage: String,
        val canRetry: Boolean = true
    ) : AssistantState
}
