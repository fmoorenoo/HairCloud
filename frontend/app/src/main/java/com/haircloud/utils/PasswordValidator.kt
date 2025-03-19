package com.haircloud.utils

object PasswordValidator {
    /* Requisitos de contraseña:
     - Al menos 4 caracteres
     - Al menos un número
    */
    fun isValid(password: String): Boolean {
        return password.length >= 4 && password.any { it.isDigit() }
    }
}
