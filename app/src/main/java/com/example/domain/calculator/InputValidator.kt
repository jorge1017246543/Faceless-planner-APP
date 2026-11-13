package com.example.domain.calculator

data class ValidationOutcome<T>(
    val value: T,
    val errorMessage: String? = null
)

object InputValidator {

    private const val MAX_COUNT_VALUE = 999_999_999_999L
    private const val MAX_INT_COUNT = 999_999
    private const val MAX_MONETARY_VALUE = 1_000_000_000_000.0

    fun sanitizeIntegerInput(raw: String): String {
        return raw.filter { it.isDigit() }.take(12)
    }

    fun sanitizeDecimalInput(raw: String): String {
        val normalized = raw.replace(',', '.')
        val builder = StringBuilder()
        var hasDecimalPoint = false
        for (ch in normalized) {
            if (ch.isDigit()) {
                builder.append(ch)
            } else if (ch == '.' && !hasDecimalPoint) {
                hasDecimalPoint = true
                builder.append('.')
            }
        }
        return builder.toString().take(15)
    }

    fun validateLongCount(raw: String): ValidationOutcome<Long> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ValidationOutcome(0L)
        if (trimmed.startsWith("-")) {
            return ValidationOutcome(0L, "No se permiten valores negativos.")
        }
        val parsed = trimmed.toLongOrNull()
            ?: return ValidationOutcome(0L, "Ingresa un número entero válido.")
        if (parsed < 0L) {
            return ValidationOutcome(0L, "No se permiten valores negativos.")
        }
        if (parsed > MAX_COUNT_VALUE) {
            return ValidationOutcome(MAX_COUNT_VALUE, "El valor supera el máximo permitido.")
        }
        return ValidationOutcome(parsed)
    }

    fun validateIntCount(raw: String): ValidationOutcome<Int> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ValidationOutcome(0)
        if (trimmed.startsWith("-")) {
            return ValidationOutcome(0, "No se permiten valores negativos.")
        }
        val parsed = trimmed.toIntOrNull()
            ?: return ValidationOutcome(0, "Ingresa un número entero válido.")
        if (parsed < 0) {
            return ValidationOutcome(0, "No se permiten valores negativos.")
        }
        if (parsed > MAX_INT_COUNT) {
            return ValidationOutcome(MAX_INT_COUNT, "El valor supera el máximo permitido.")
        }
        return ValidationOutcome(parsed)
    }

    fun validateMonetaryAmount(raw: String): ValidationOutcome<Double> {
        val trimmed = raw.trim().replace(',', '.')
        if (trimmed.isEmpty() || trimmed == ".") return ValidationOutcome(0.0)
        if (trimmed.startsWith("-")) {
            return ValidationOutcome(0.0, "No se permiten montos negativos.")
        }
        val parsed = trimmed.toDoubleOrNull()
            ?: return ValidationOutcome(0.0, "Ingresa un monto válido.")
        if (parsed.isNaN() || parsed.isInfinite()) {
            return ValidationOutcome(0.0, "Ingresa un monto válido.")
        }
        if (parsed < 0.0) {
            return ValidationOutcome(0.0, "No se permiten montos negativos.")
        }
        if (parsed > MAX_MONETARY_VALUE) {
            return ValidationOutcome(MAX_MONETARY_VALUE, "El monto supera el límite permitido.")
        }
        return ValidationOutcome(parsed)
    }

    fun validatePercentage(raw: String): ValidationOutcome<Double> {
        val trimmed = raw.trim().replace(',', '.')
        if (trimmed.isEmpty() || trimmed == ".") return ValidationOutcome(0.0)
        if (trimmed.startsWith("-")) {
            return ValidationOutcome(0.0, "El porcentaje debe estar entre 0 % y 100 %.")
        }
        val parsed = trimmed.toDoubleOrNull()
            ?: return ValidationOutcome(0.0, "Ingresa un porcentaje válido.")
        if (parsed.isNaN() || parsed.isInfinite()) {
            return ValidationOutcome(0.0, "Ingresa un porcentaje válido.")
        }
        if (parsed < 0.0 || parsed > 100.0) {
            return ValidationOutcome(
                value = parsed.coerceIn(0.0, 100.0),
                errorMessage = "El porcentaje debe estar entre 0 % y 100 %."
            )
        }
        return ValidationOutcome(parsed)
    }
}
