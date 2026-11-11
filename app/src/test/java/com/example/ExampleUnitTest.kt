package com.example

import com.example.data.auth.SecurityUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun emailValidation_worksCorrectly() {
        assertTrue(SecurityUtils.isValidEmail("player@example.com"))
        assertTrue(SecurityUtils.isValidEmail("ff.pro.player@gaming.org"))
        assertFalse(SecurityUtils.isValidEmail("invalid-email"))
        assertFalse(SecurityUtils.isValidEmail("player@"))
        assertFalse(SecurityUtils.isValidEmail("@example.com"))
    }

    @Test
    fun passwordValidation_enforcesSecurityRules() {
        assertNull(SecurityUtils.validatePassword("Player123"))
        assertNotNull(SecurityUtils.validatePassword("short"))
        assertNotNull(SecurityUtils.validatePassword("nouppercaseornumber"))
        assertNotNull(SecurityUtils.validatePassword("12345678"))
    }

    @Test
    fun passwordHashing_generatesConsistentAndSecureVerifications() {
        val salt = SecurityUtils.generateSalt()
        assertEquals(32, salt.length)

        val hash = SecurityUtils.hashPassword("Secret123", salt)
        assertEquals(64, hash.length)

        assertTrue(SecurityUtils.verifyPassword("Secret123", salt, hash))
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", salt, hash))
    }
}
