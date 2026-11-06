package com.haircloud.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiErrorParsingTest {

    @Test
    fun usesServerMessageAndCode() {
        val e = parseErrorBody("""{"error":"Ese horario ya está ocupado","code":"slot_unavailable"}""", 409, "def")
        assertEquals("Ese horario ya está ocupado", e.message)
        assertEquals("slot_unavailable", e.code)
        assertEquals(409, e.status)
    }

    @Test
    fun fallsBackOnInvalidBody() {
        assertEquals("def", parseErrorBody("<html>", 400, "def").message)
        assertEquals("Error del servidor. Inténtalo más tarde.", parseErrorBody("<html>", 502, "def").message)
    }

    @Test
    fun emptyBodiesGetFriendlyMessages() {
        assertEquals("unauthorized", parseErrorBody(null, 401, "def").code)
        assertEquals("rate_limited", parseErrorBody("", 429, "def").code)
        assertEquals("def", parseErrorBody("", 404, "def").message)
    }
}
