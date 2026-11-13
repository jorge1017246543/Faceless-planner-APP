package com.example.ui.state

import com.example.domain.calculator.InputValidator
import com.example.domain.model.RevenueInputs
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class FieldErrors(
    val longFormViews: String? = null,
    val longFormRpm: String? = null,
    val shortsViews: String? = null,
    val shortsRpm: String? = null,
    val sponsorshipDealCount: String? = null,
    val sponsorshipAvgFee: String? = null,
    val affiliateLinkClicks: String? = null,
    val affiliateConversionRatePct: String? = null,
    val affiliateAvgOrderValue: String? = null,
    val affiliateCommissionRatePct: String? = null,
    val productSalesCount: String? = null,
    val productAvgOrderValue: String? = null,
    val monthlyExpenses: String? = null,
    val taxReservePct: String? = null
) {
    val hasAnyError: Boolean
        get() = listOfNotNull(
            longFormViews,
            longFormRpm,
            shortsViews,
            shortsRpm,
            sponsorshipDealCount,
            sponsorshipAvgFee,
            affiliateLinkClicks,
            affiliateConversionRatePct,
            affiliateAvgOrderValue,
            affiliateCommissionRatePct,
            productSalesCount,
            productAvgOrderValue,
            monthlyExpenses,
            taxReservePct
        ).isNotEmpty()
}

data class CalculatorFormState(
    val longFormViewsText: String = "",
    val longFormRpmText: String = "",
    val shortsViewsText: String = "",
    val shortsRpmText: String = "",
    val sponsorshipDealCountText: String = "",
    val sponsorshipAvgFeeText: String = "",
    val affiliateLinkClicksText: String = "",
    val affiliateConversionRateText: String = "",
    val affiliateAvgOrderValueText: String = "",
    val affiliateCommissionRateText: String = "",
    val productSalesCountText: String = "",
    val productAvgOrderValueText: String = "",
    val monthlyExpensesText: String = "",
    val taxReservePctText: String = "",
    val errors: FieldErrors = FieldErrors()
) {
    fun toValidatedInputs(): Pair<RevenueInputs, FieldErrors> {
        val lfViews = InputValidator.validateLongCount(longFormViewsText)
        val lfRpm = InputValidator.validateMonetaryAmount(longFormRpmText)
        val shViews = InputValidator.validateLongCount(shortsViewsText)
        val shRpm = InputValidator.validateMonetaryAmount(shortsRpmText)
        val deals = InputValidator.validateIntCount(sponsorshipDealCountText)
        val dealFee = InputValidator.validateMonetaryAmount(sponsorshipAvgFeeText)
        val clicks = InputValidator.validateLongCount(affiliateLinkClicksText)
        val convPct = InputValidator.validatePercentage(affiliateConversionRateText)
        val affAov = InputValidator.validateMonetaryAmount(affiliateAvgOrderValueText)
        val commPct = InputValidator.validatePercentage(affiliateCommissionRateText)
        val prodSales = InputValidator.validateIntCount(productSalesCountText)
        val prodAov = InputValidator.validateMonetaryAmount(productAvgOrderValueText)
        val expenses = InputValidator.validateMonetaryAmount(monthlyExpensesText)
        val taxPct = InputValidator.validatePercentage(taxReservePctText)

        val inputs = RevenueInputs(
            longFormViews = lfViews.value,
            longFormRpm = lfRpm.value,
            shortsViews = shViews.value,
            shortsRpm = shRpm.value,
            sponsorshipDealCount = deals.value,
            sponsorshipAvgFee = dealFee.value,
            affiliateLinkClicks = clicks.value,
            affiliateConversionRatePct = convPct.value,
            affiliateAvgOrderValue = affAov.value,
            affiliateCommissionRatePct = commPct.value,
            productSalesCount = prodSales.value,
            productAvgOrderValue = prodAov.value,
            monthlyExpenses = expenses.value,
            taxReservePct = taxPct.value
        )

        val fieldErrors = FieldErrors(
            longFormViews = lfViews.errorMessage,
            longFormRpm = lfRpm.errorMessage,
            shortsViews = shViews.errorMessage,
            shortsRpm = shRpm.errorMessage,
            sponsorshipDealCount = deals.errorMessage,
            sponsorshipAvgFee = dealFee.errorMessage,
            affiliateLinkClicks = clicks.errorMessage,
            affiliateConversionRatePct = convPct.errorMessage,
            affiliateAvgOrderValue = affAov.errorMessage,
            affiliateCommissionRatePct = commPct.errorMessage,
            productSalesCount = prodSales.errorMessage,
            productAvgOrderValue = prodAov.errorMessage,
            monthlyExpenses = expenses.errorMessage,
            taxReservePct = taxPct.errorMessage
        )

        return inputs to fieldErrors
    }

    companion object {
        private val plainDecimalFormat = DecimalFormat(
            "0.##",
            DecimalFormatSymbols(Locale.US)
        )

        fun fromInputs(inputs: RevenueInputs): CalculatorFormState {
            fun formatLong(v: Long): String = if (v <= 0L) "" else v.toString()
            fun formatInt(v: Int): String = if (v <= 0) "" else v.toString()
            fun formatDouble(v: Double): String =
                if (v <= 0.0) "" else plainDecimalFormat.format(v)

            return CalculatorFormState(
                longFormViewsText = formatLong(inputs.longFormViews),
                longFormRpmText = formatDouble(inputs.longFormRpm),
                shortsViewsText = formatLong(inputs.shortsViews),
                shortsRpmText = formatDouble(inputs.shortsRpm),
                sponsorshipDealCountText = formatInt(inputs.sponsorshipDealCount),
                sponsorshipAvgFeeText = formatDouble(inputs.sponsorshipAvgFee),
                affiliateLinkClicksText = formatLong(inputs.affiliateLinkClicks),
                affiliateConversionRateText = formatDouble(inputs.affiliateConversionRatePct),
                affiliateAvgOrderValueText = formatDouble(inputs.affiliateAvgOrderValue),
                affiliateCommissionRateText = formatDouble(inputs.affiliateCommissionRatePct),
                productSalesCountText = formatInt(inputs.productSalesCount),
                productAvgOrderValueText = formatDouble(inputs.productAvgOrderValue),
                monthlyExpensesText = formatDouble(inputs.monthlyExpenses),
                taxReservePctText = formatDouble(inputs.taxReservePct)
            )
        }
    }
}
