package com.heymahesh.agent

import com.heymahesh.agent.core.data.SessionRepository
import com.heymahesh.agent.core.models.ContactEntity
import com.heymahesh.agent.core.models.ResolvedAction
import com.heymahesh.agent.core.reasoning.DefaultReasoningEngine
import com.heymahesh.agent.core.state.AssistantState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReasoningEngineUnitTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var reasoningEngine: DefaultReasoningEngine

    private val sampleContacts = listOf(
        ContactEntity("1", "Ramalingam College", "+91 98451 11111", "College"),
        ContactEntity("2", "Ramalingam Uncle", "+91 98452 22222", "Family"),
        ContactEntity("3", "Ramalingam Jio", "+91 98453 33333", "Work"),
        ContactEntity("4", "Rahul Sharma", "+91 97410 44444", "Friend"),
        ContactEntity("5", "Sneha CR", "+91 98800 55555", "College")
    )

    private val contactsProvider: suspend (String) -> List<ContactEntity> = { query ->
        sampleContacts.filter { it.name.contains(query, ignoreCase = true) }
    }

    @Before
    fun setUp() {
        sessionRepository = SessionRepository()
        reasoningEngine = DefaultReasoningEngine(sessionRepository)
    }

    @Test
    fun testSingleContactCallDirectExecution() = runBlocking {
        val state = reasoningEngine.processUserSpeech("Hey Mahesh, call to Rahul", contactsProvider)

        assertTrue("Expected Executing state", state is AssistantState.Executing)
        val action = (state as AssistantState.Executing).action
        assertTrue(action is ResolvedAction.PhoneCall)
        assertEquals("Rahul Sharma", (action as ResolvedAction.PhoneCall).contactName)
    }

    @Test
    fun testMultiContactDisambiguationAndResolution() = runBlocking {
        // Step 1: User asks to call Ramalingam (3 exist)
        val turn1 = reasoningEngine.processUserSpeech("Hey Mahesh, call Ramalingam", contactsProvider)
        assertTrue("Expected Disambiguating state", turn1 is AssistantState.Disambiguating)
        val disambig = turn1 as AssistantState.Disambiguating
        assertEquals(3, disambig.candidates.size)

        // Step 2: User responds "College one"
        val turn2 = reasoningEngine.processUserSpeech("College one", contactsProvider)
        assertTrue("Expected Executing state", turn2 is AssistantState.Executing)
        val action = (turn2 as AssistantState.Executing).action
        assertTrue(action is ResolvedAction.PhoneCall)
        assertEquals("Ramalingam College", (action as ResolvedAction.PhoneCall).contactName)
        assertEquals("+91 98451 11111", (action as ResolvedAction.PhoneCall).phoneNumber)
    }

    @Test
    fun testWhatsAppMissingSlotAndFill() = runBlocking {
        // Step 1: User says "send WhatsApp to Sneha" without message text
        val turn1 = reasoningEngine.processUserSpeech("Hey Mahesh, send message to Sneha on WhatsApp", contactsProvider)
        assertTrue("Expected MissingSlot state", turn1 is AssistantState.MissingSlot)
        assertEquals("message_body", (turn1 as AssistantState.MissingSlot).missingSlotName)

        // Step 2: User supplies the message text
        val turn2 = reasoningEngine.processUserSpeech("We are waiting at the canteen", contactsProvider)
        assertTrue("Expected Executing state", turn2 is AssistantState.Executing)
        val action = (turn2 as AssistantState.Executing).action
        assertTrue(action is ResolvedAction.WhatsAppMessage)
        assertEquals("Sneha CR", (action as ResolvedAction.WhatsAppMessage).recipientName)
        assertEquals("We are waiting at the canteen", (action as ResolvedAction.WhatsAppMessage).messageBody)
    }

    @Test
    fun testWhatsAppFullSingleShotIntent() = runBlocking {
        val state = reasoningEngine.processUserSpeech(
            "Hey Mahesh, message Rahul saying we are leaving now",
            contactsProvider
        )

        assertTrue(state is AssistantState.Executing)
        val action = (state as AssistantState.Executing).action
        assertTrue(action is ResolvedAction.WhatsAppMessage)
        assertEquals("Rahul Sharma", (action as ResolvedAction.WhatsAppMessage).recipientName)
        assertEquals("we are leaving now", (action as ResolvedAction.WhatsAppMessage).messageBody)
    }

    @Test
    fun testCameraCountdownSelfie() = runBlocking {
        val state = reasoningEngine.processUserSpeech("Hey Mahesh, take a selfie in 5 seconds", contactsProvider)

        assertTrue(state is AssistantState.Executing)
        val action = (state as AssistantState.Executing).action
        assertTrue(action is ResolvedAction.CameraSnap)
        assertEquals("front", (action as ResolvedAction.CameraSnap).cameraFacing)
        assertEquals(5, (action as ResolvedAction.CameraSnap).countdownSeconds)
    }

    @Test
    fun testFlashlightToggle() = runBlocking {
        val state = reasoningEngine.processUserSpeech("Hey Mahesh, turn on flashlight", contactsProvider)

        assertTrue(state is AssistantState.Executing)
        val action = (state as AssistantState.Executing).action
        assertTrue(action is ResolvedAction.FlashlightToggle)
        assertTrue((action as ResolvedAction.FlashlightToggle).turnOn)
    }
}
