package com.heymahesh.agent.core.data

import com.heymahesh.agent.core.models.CommandHistoryItem
import com.heymahesh.agent.core.models.PendingDisambiguationContext
import com.heymahesh.agent.core.models.PendingSlotContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class SessionRepository {
    private val mutex = Mutex()

    private var activeDisambiguation: PendingDisambiguationContext? = null
    private var activePendingSlot: PendingSlotContext? = null

    private val _history = MutableStateFlow<List<CommandHistoryItem>>(emptyList())
    val history: StateFlow<List<CommandHistoryItem>> = _history.asStateFlow()

    val jsonSerializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    suspend fun setDisambiguation(context: PendingDisambiguationContext?) = mutex.withLock {
        activeDisambiguation = context
        if (context != null) activePendingSlot = null
    }

    suspend fun getDisambiguation(): PendingDisambiguationContext? = mutex.withLock {
        // Clear if expired (> 20 seconds old)
        activeDisambiguation?.let {
            if (System.currentTimeMillis() - it.timestamp > 20_000L) {
                activeDisambiguation = null
            }
        }
        return activeDisambiguation
    }

    suspend fun setPendingSlot(context: PendingSlotContext?) = mutex.withLock {
        activePendingSlot = context
        if (context != null) activeDisambiguation = null
    }

    suspend fun getPendingSlot(): PendingSlotContext? = mutex.withLock {
        activePendingSlot?.let {
            if (System.currentTimeMillis() - it.timestamp > 20_000L) {
                activePendingSlot = null
            }
        }
        return activePendingSlot
    }

    suspend fun clearSession() = mutex.withLock {
        activeDisambiguation = null
        activePendingSlot = null
    }

    suspend fun logCommand(item: CommandHistoryItem) = mutex.withLock {
        val currentList = _history.value.toMutableList()
        currentList.add(0, item) // Insert newest at top
        if (currentList.size > 100) currentList.removeAt(currentList.lastIndex)
        _history.value = currentList
    }
}
