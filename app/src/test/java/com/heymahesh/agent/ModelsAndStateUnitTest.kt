package com.heymahesh.agent

import com.heymahesh.agent.core.data.SessionRepository
import com.heymahesh.agent.core.models.*
import com.heymahesh.agent.core.state.AssistantState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ModelsAndStateUnitTest {

    @Test
    fun testContactEntitySerialization() {
        val contact = ContactEntity(
            id = "c_101",
            name = "Ramalingam College",
            phoneNumber = "+91 98450 11223",
            tag = "College"
        )
        val repository = SessionRepository()
        val json = repository.jsonSerializer.encodeToString(ContactEntity.serializer(), contact)
        val decoded = repository.jsonSerializer.decodeFromString(ContactEntity.serializer(), json)

        assertEquals(contact.name, decoded.name)
        assertEquals(contact.phoneNumber, decoded.phoneNumber)
        assertEquals(contact.tag, decoded.tag)
    }

    @Test
    fun testResolvedActionTypes() {
        val callAction: ResolvedAction = ResolvedAction.PhoneCall("Rahul", "+91 99999 11111", true)
        val whatsappAction: ResolvedAction = ResolvedAction.WhatsAppMessage("Sneha", null, "Reaching in 5 mins")

        assertTrue(callAction is ResolvedAction.PhoneCall)
        assertTrue(whatsappAction is ResolvedAction.WhatsAppMessage)
        assertEquals("Reaching in 5 mins", (whatsappAction as ResolvedAction.WhatsAppMessage).messageBody)
    }

    @Test
    fun testSessionRepositoryContextRetention() = runBlocking {
        val repo = SessionRepository()
        val candidates = listOf(
            ContactEntity("1", "Ramalingam College", "9845111111", "College"),
            ContactEntity("2", "Ramalingam Uncle", "9845222222", "Family")
        )

        repo.setDisambiguation(PendingDisambiguationContext("call", "Ramalingam", candidates))
        val active = repo.getDisambiguation()

        assertNotNull(active)
        assertEquals("Ramalingam", active?.targetName)
        assertEquals(2, active?.candidates?.size)

        repo.clearSession()
        assertNull(repo.getDisambiguation())
    }

    @Test
    fun testAppPermissionReadiness() {
        val notReady = AppPermissionState(hasAudioRecord = true, hasSystemOverlay = false)
        assertFalse(notReady.isReadyForFullAutonomy)

        val ready = AppPermissionState(
            hasAudioRecord = true,
            hasSystemOverlay = true,
            hasAccessibility = true,
            hasReadContacts = true,
            hasCallPhone = true
        )
        assertTrue(ready.isReadyForFullAutonomy)
    }
}
