package com.example.domain.model

enum class DisplayCurrency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val decimalPlaces: Int
) {
    USD("USD", "$", "Dólar estadounidense (USD)", 2),
    COP("COP", "$", "Peso colombiano (COP)", 0),
    MXN("MXN", "$", "Peso mexicano (MXN)", 2),
    EUR("EUR", "€", "Euro (EUR)", 2),
    BRL("BRL", "R$", "Real brasileño (BRL)", 2),
    ARS("ARS", "$", "Peso argentino (ARS)", 0);

    companion object {
        fun fromCode(code: String?): DisplayCurrency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
        }
    }
}
