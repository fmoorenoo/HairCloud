package com.haircloud.utils

object PasswordValidator {
    /* Requisitos de contraseña:
     - Al menos 5 caracteres
     - Al menos un número
    */
    fun isValid(password: String): Boolean {
        return password.length >= 5 && password.any { it.isDigit() }
    }
}
