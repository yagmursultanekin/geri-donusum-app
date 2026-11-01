package com.geridonusum.app.ui

enum class StrengthLevel(val label: String, val progress: Float) {
    ZAYIF("Zayıf", 0.33f),
    ORTA("Orta", 0.66f),
    GUCLU("Güçlü", 1f)
}

data class PasswordStrength(
    val level: StrengthLevel,
    val missing: List<String>
)

object PasswordChecker {

    // Çok bilinen şifreler uzun görünse de zayıf sayılır
    private val commonPasswords = setOf(
        "12345678", "123456789", "1234567890", "password", "password1",
        "qwerty123", "qwertyuiop", "11111111", "iloveyou", "abc12345",
        "sifre123", "parola123"
    )

    fun check(password: String): PasswordStrength {
        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }

        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (hasLower) score++
        if (hasUpper) score++
        if (hasDigit) score++
        if (hasSymbol) score++

        val level = when {
            password.lowercase() in commonPasswords -> StrengthLevel.ZAYIF
            password.length < 8 -> StrengthLevel.ZAYIF
            score >= 6 -> StrengthLevel.GUCLU
            score >= 4 -> StrengthLevel.ORTA
            else -> StrengthLevel.ZAYIF
        }

        val missing = buildList {
            if (password.length < 8) add("en az 8 karakter")
            else if (password.length < 12) add("12 veya daha fazla karakter")
            if (!hasUpper) add("büyük harf")
            if (!hasLower) add("küçük harf")
            if (!hasDigit) add("rakam")
            if (!hasSymbol) add("sembol (! ? . gibi)")
        }

        return PasswordStrength(level, missing)
    }
}