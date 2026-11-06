package com.haircloud

import com.haircloud.utils.CredentialsValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Las reglas deben coincidir con las del servidor (backend/app/schemas.py). */
class CredentialsValidatorTest {

    @Test
    fun passwordPolicy() {
        assertTrue(CredentialsValidator.isPasswordValid("Clave1234"))
        assertTrue(CredentialsValidator.isPasswordValid("p@ssw0rd!"))
        assertFalse("demasiado corta", CredentialsValidator.isPasswordValid("abc12"))
        assertFalse("sin números", CredentialsValidator.isPasswordValid("solamenteletras"))
        assertFalse("sin letras", CredentialsValidator.isPasswordValid("12345678"))
        assertFalse("con espacios", CredentialsValidator.isPasswordValid("clave 1234"))
        assertFalse("más de 72 bytes", CredentialsValidator.isPasswordValid("a1" + "x".repeat(71)))
    }

    @Test
    fun usernamePolicy() {
        assertTrue(CredentialsValidator.isUsernameValid("usuario_1"))
        assertTrue(CredentialsValidator.isUsernameValid("ana.lopez"))
        assertFalse(CredentialsValidator.isUsernameValid("corto"))
        assertFalse(CredentialsValidator.isUsernameValid("con espacio"))
        assertFalse(CredentialsValidator.isUsernameValid("email@dominio"))
        assertFalse(CredentialsValidator.isUsernameValid("a".repeat(21)))
    }

    @Test
    fun phonePolicy() {
        assertTrue(CredentialsValidator.isPhoneValid(""))
        assertTrue(CredentialsValidator.isPhoneValid("600111222"))
        assertFalse(CredentialsValidator.isPhoneValid("600 111 222"))
        assertFalse(CredentialsValidator.isPhoneValid("12345"))
    }
}
