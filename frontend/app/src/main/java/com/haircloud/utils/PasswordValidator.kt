package com.haircloud.utils

object PasswordValidator {
    /* Requisitos de contraseña:
     - Al menos 4 letras
     - Al menos un número
    */
    fun isValid(password: String): Boolean {
        val letterCount = password.count { it.isLetter() }
        val hasNumber = password.any { it.isDigit() }

        return letterCount >= 4 && hasNumber
    }
}
