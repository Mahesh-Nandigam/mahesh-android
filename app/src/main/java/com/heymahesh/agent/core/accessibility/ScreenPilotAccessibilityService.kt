package com.heymahesh.agent.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*

class ScreenPilotAccessibilityService : AccessibilityService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        var instance: ScreenPilotAccessibilityService? = null
            private set

        val isRunning: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i("HeyMahesh", "🦾 Screen Pilot Accessibility Service Connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Active event monitoring
    }

    override fun onInterrupt() {
        Log.w("HeyMahesh", "Screen Pilot Accessibility Service Interrupted.")
    }

    override fun onDestroy() {
        instance = null
        serviceJob.cancel()
        super.onDestroy()
    }

    /**
     * Autonomous WhatsApp Pilot: Locates the message input box, types text, and clicks Send.
     */
    fun autoTypeAndSendInWhatsApp(message: String, onComplete: (Boolean) -> Unit) {
        serviceScope.launch {
            // Give WhatsApp UI 800ms to settle
            delay(800)
            val root = rootInActiveWindow
            if (root == null) {
                onComplete(false)
                return@launch
            }

            var textEntered = false

            // 1. Locate message input field by view ID or class name
            val inputNodes = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry")
            val targetInput = inputNodes.firstOrNull() ?: findFirstEditText(root)

            if (targetInput != null) {
                val args = Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        message
                    )
                }
                textEntered = targetInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            }

            if (textEntered) {
                delay(300) // Brief pause to allow send button to render
                val freshRoot = rootInActiveWindow ?: root
                val sendNodes = freshRoot.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
                val sendButton = sendNodes.firstOrNull() ?: findSendButton(freshRoot)

                if (sendButton != null) {
                    val clicked = sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    onComplete(clicked)
                    return@launch
                }
            }

            onComplete(false)
        }
    }

    private fun findFirstEditText(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.className?.toString()?.contains("EditText", ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val match = findFirstEditText(child)
            if (match != null) return match
        }
        return null
    }

    private fun findSendButton(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        if (desc.contains("send") || desc.contains("submit")) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val match = findSendButton(child)
            if (match != null) return match
        }
        return null
    }
}
