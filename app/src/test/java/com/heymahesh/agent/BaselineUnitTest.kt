package com.heymahesh.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaselineUnitTest {
    @Test
    fun testAppIdentity() {
        val appName = "Mahesh"
        assertEquals("Mahesh", appName)
    }

    @Test
    fun testEnvironmentSanity() {
        assertTrue(true)
    }
}
