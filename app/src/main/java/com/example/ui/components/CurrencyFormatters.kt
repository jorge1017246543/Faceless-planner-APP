package com.example.ui.components

import com.example.domain.model.DisplayCurrency
import com.example.domain.model.RevenueSourceType
import com.example.ui.theme.ChartAffiliateColor
import com.example.ui.theme.ChartLongFormColor
import com.example.ui.theme.ChartProductColor
import com.example.ui.theme.ChartShortsColor
import com.example.ui.theme.ChartSponsorshipColor
import androidx.compose.ui.graphics.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatters {

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    fun formatMoney(amount: Double, currency: DisplayCurrency): String {
        val safe = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        val pattern = if (currency.decimalPlaces == 0) "#,##0" else "#,##0.00"
        val formatter = DecimalFormat(pattern, symbols)
        val formattedAbs = formatter.format(abs(safe))
        val sign = if (safe < -0.005) "-" else ""
        return "$sign${currency.code} ${currency.symbol}$formattedAbs"
    }

    fun formatMoneyShort(amount: Double, currency: DisplayCurrency): String {
        val safe = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        val absVal = abs(safe)
        val sign = if (safe < -0.005) "-" else ""
        val suffix: String
        val scaled: Double
        when {
            absVal >= 1_000_000_000.0 -> {
                scaled = absVal / 1_000_000_000.0
                suffix = "B"
            }
            absVal >= 1_000_000.0 -> {
                scaled = absVal / 1_000_000.0
                suffix = "M"
            }
            absVal >= 100_000.0 -> {
                scaled = absVal / 1_000.0
                suffix = "K"
            }
            else -> return formatMoney(safe, currency)
        }
        val compactFmt = DecimalFormat("#,##0.#", symbols)
        return "$sign${currency.symbol}${compactFmt.format(scaled)}$suffix ${currency.code}"
    }

    fun formatCount(value: Long): String {
        val formatter = DecimalFormat("#,##0", symbols)
        return formatter.format(value.coerceAtLeast(0L))
    }

    fun formatPercentage(pct: Double): String {
        val safe = if (pct.isNaN() || pct.isInfinite()) 0.0 else pct
        val formatter = DecimalFormat("0.#", symbols)
        return "${formatter.format(safe)} %"
    }

    fun colorForSource(type: RevenueSourceType): Color = when (type) {
        RevenueSourceType.LONG_FORM_ADS -> ChartLongFormColor
        RevenueSourceType.SHORTS_ADS -> ChartShortsColor
        RevenueSourceType.SPONSORSHIPS -> ChartSponsorshipColor
        RevenueSourceType.AFFILIATES -> ChartAffiliateColor
        RevenueSourceType.PRODUCTS -> ChartProductColor
    }
}
