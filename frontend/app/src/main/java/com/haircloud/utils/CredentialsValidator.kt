package com.haircloud.utils

object CredentialsValidator {
    /* Requisitos de contraseña:
     - Al menos 4 letras
     - Al menos un número
     - Solo puede contener letras, números y los símbolos: - / . / _
    */
    fun isPasswordValid(password: String): Boolean {
        val letterCount = password.count { it.isLetter() }
        val hasNumber = password.any { it.isDigit() }
        val hasOnlyAllowedChars = password.all { it.isLetterOrDigit() || it in "-._" }

        return letterCount >= 4 && hasNumber && hasOnlyAllowedChars
    }

    /* Requisitos de nombre de usuario:
     - Mínimo 6 caracteres y máximo 20
     - Solo puede contener letras, números y los símbolos: . / _
    */
    fun isUsernameValid(username: String): Boolean {
        val isCorrectLength = username.length in 6..20
        val hasOnlyAllowedChars = username.all { it.isLetterOrDigit() || it in "._" }

        return isCorrectLength && hasOnlyAllowedChars
    }
}
