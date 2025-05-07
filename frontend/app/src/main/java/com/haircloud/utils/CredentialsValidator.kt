package com.haircloud.utils

object CredentialsValidator {
    /* Requisitos de contraseña:
     - Al menos 4 letras
     - Al menos un número
     - Solo puede contener letras, números y los símbolos: - / . / _
     - No puede contener espacios
    */
    fun isPasswordValid(password: String): Boolean {
        val letterCount = password.count { it.isLetter() }
        val hasNumber = password.any { it.isDigit() }
        val hasOnlyAllowedChars = password.all { it.isLetterOrDigit() || it in "-._" }
        val hasNoSpaces = !password.contains(" ")

        return letterCount >= 4 && hasNumber && hasOnlyAllowedChars && hasNoSpaces
    }

    /* Requisitos de nombre de usuario:
     - Mínimo 6 caracteres y máximo 20
     - Solo puede contener letras, números y los símbolos: . / _
     - No puede contener espacios
    */
    fun isUsernameValid(username: String): Boolean {
        val isCorrectLength = username.length in 6..20
        val hasOnlyAllowedChars = username.all { it.isLetterOrDigit() || it in "._" }
        val hasNoSpaces = !username.contains(" ")

        return isCorrectLength && hasOnlyAllowedChars && hasNoSpaces
    }

    /* Requisitos del número de teléfono:
     - Solo puede contener dígitos
     - Mínimo 9 caracteres y máximo 15
     - No puede contener espacios
     - Puede estar vacío
    */
    fun isPhoneValid(phone: String): Boolean {
        if (phone == "") return true
        val onlyDigits = phone.all { it.isDigit() }
        val correctLength = phone.length in 9..15
        return onlyDigits && correctLength
    }

    /* Requisitos del horario:
     - Formato HH:mm o HH:mm:ss
     - Entre 00:00 y 23:59 (y opcionalmente segundos entre 00-59)
    */
    fun isHourValid(time: String): Boolean {
        return Regex("""^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$""").matches(time)
    }

}
