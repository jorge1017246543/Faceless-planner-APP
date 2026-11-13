package com.example.domain.calculator

import com.example.domain.model.RevenueBreakdown
import com.example.domain.model.RevenueInputs
import com.example.domain.model.RevenueSourceShare
import com.example.domain.model.RevenueSourceType
import java.math.BigDecimal
import java.math.RoundingMode

object RevenueCalculator {

    private val thousand = BigDecimal(1000)
    private val hundred = BigDecimal(100)
    private val twelve = BigDecimal(12)
    private val maxSafeAmount = BigDecimal("9999999999999.99")

    fun calculate(inputs: RevenueInputs): RevenueBreakdown {
        val longFormViews = inputs.longFormViews.coerceAtLeast(0L).toSafeDecimal()
        val longFormRpm = inputs.longFormRpm.coerceNonNegativeFinite().toSafeDecimal()

        val shortsViews = inputs.shortsViews.coerceAtLeast(0L).toSafeDecimal()
        val shortsRpm = inputs.shortsRpm.coerceNonNegativeFinite().toSafeDecimal()

        val dealCount = inputs.sponsorshipDealCount.coerceAtLeast(0).toSafeDecimal()
        val avgFee = inputs.sponsorshipAvgFee.coerceNonNegativeFinite().toSafeDecimal()

        val clicks = inputs.affiliateLinkClicks.coerceAtLeast(0L).toSafeDecimal()
        val convRate = inputs.affiliateConversionRatePct.coercePercentage().toSafeDecimal()
            .divide(hundred, 8, RoundingMode.HALF_UP)
        val affiliateAov = inputs.affiliateAvgOrderValue.coerceNonNegativeFinite().toSafeDecimal()
        val commRate = inputs.affiliateCommissionRatePct.coercePercentage().toSafeDecimal()
            .divide(hundred, 8, RoundingMode.HALF_UP)

        val productSales = inputs.productSalesCount.coerceAtLeast(0).toSafeDecimal()
        val productAov = inputs.productAvgOrderValue.coerceNonNegativeFinite().toSafeDecimal()

        val expenses = inputs.monthlyExpenses.coerceNonNegativeFinite().toSafeDecimal()
        val taxPct = inputs.taxReservePct.coercePercentage().toSafeDecimal()
            .divide(hundred, 8, RoundingMode.HALF_UP)

        // Core formulas
        val longFormRev = longFormViews
            .divide(thousand, 8, RoundingMode.HALF_UP)
            .multiply(longFormRpm)
            .clampSafe()

        val shortsRev = shortsViews
            .divide(thousand, 8, RoundingMode.HALF_UP)
            .multiply(shortsRpm)
            .clampSafe()

        val sponsorshipRev = dealCount
            .multiply(avgFee)
            .clampSafe()

        val affiliateRev = clicks
            .multiply(convRate)
            .multiply(affiliateAov)
            .multiply(commRate)
            .clampSafe()

        val productRev = productSales
            .multiply(productAov)
            .clampSafe()

        val totalGross = longFormRev
            .add(shortsRev)
            .add(sponsorshipRev)
            .add(affiliateRev)
            .add(productRev)
            .clampSafe()

        val profitBeforeTax = totalGross.subtract(expenses)

        val taxReserve = if (profitBeforeTax > BigDecimal.ZERO) {
            profitBeforeTax.multiply(taxPct).clampSafe()
        } else {
            BigDecimal.ZERO
        }

        val netIncome = profitBeforeTax.subtract(taxReserve)
        val annualGross = totalGross.multiply(twelve).clampSafe()
        val annualNet = netIncome.multiply(twelve)

        val shares = buildShares(
            longFormRev = longFormRev,
            shortsRev = shortsRev,
            sponsorshipRev = sponsorshipRev,
            affiliateRev = affiliateRev,
            productRev = productRev,
            totalGross = totalGross
        )

        return RevenueBreakdown(
            longFormAdRevenue = longFormRev.toSafeDouble(),
            shortsAdRevenue = shortsRev.toSafeDouble(),
            sponsorshipRevenue = sponsorshipRev.toSafeDouble(),
            affiliateRevenue = affiliateRev.toSafeDouble(),
            productRevenue = productRev.toSafeDouble(),
            totalGrossRevenue = totalGross.toSafeDouble(),
            monthlyExpenses = expenses.toSafeDouble(),
            estimatedProfitBeforeTax = profitBeforeTax.toSafeDouble(),
            estimatedTaxReserve = taxReserve.toSafeDouble(),
            estimatedNetIncome = netIncome.toSafeDouble(),
            annualGrossRevenue = annualGross.toSafeDouble(),
            annualNetIncome = annualNet.toSafeDouble(),
            shares = shares
        )
    }

    private fun buildShares(
        longFormRev: BigDecimal,
        shortsRev: BigDecimal,
        sponsorshipRev: BigDecimal,
        affiliateRev: BigDecimal,
        productRev: BigDecimal,
        totalGross: BigDecimal
    ): List<RevenueSourceShare> {
        fun pct(part: BigDecimal): Double {
            if (totalGross <= BigDecimal.ZERO) return 0.0
            return part.multiply(hundred)
                .divide(totalGross, 4, RoundingMode.HALF_UP)
                .toDouble()
                .coerceIn(0.0, 100.0)
        }

        return listOf(
            RevenueSourceShare(
                type = RevenueSourceType.LONG_FORM_ADS,
                label = "Videos largos",
                amount = longFormRev.toSafeDouble(),
                sharePercentage = pct(longFormRev)
            ),
            RevenueSourceShare(
                type = RevenueSourceType.SHORTS_ADS,
                label = "YouTube Shorts",
                amount = shortsRev.toSafeDouble(),
                sharePercentage = pct(shortsRev)
            ),
            RevenueSourceShare(
                type = RevenueSourceType.SPONSORSHIPS,
                label = "Patrocinios",
                amount = sponsorshipRev.toSafeDouble(),
                sharePercentage = pct(sponsorshipRev)
            ),
            RevenueSourceShare(
                type = RevenueSourceType.AFFILIATES,
                label = "Afiliados",
                amount = affiliateRev.toSafeDouble(),
                sharePercentage = pct(affiliateRev)
            ),
            RevenueSourceShare(
                type = RevenueSourceType.PRODUCTS,
                label = "Productos",
                amount = productRev.toSafeDouble(),
                sharePercentage = pct(productRev)
            )
        )
    }

    private fun Long.toSafeDecimal(): BigDecimal = BigDecimal.valueOf(this)
    private fun Int.toSafeDecimal(): BigDecimal = BigDecimal.valueOf(this.toLong())
    private fun Double.toSafeDecimal(): BigDecimal = BigDecimal.valueOf(this)

    private fun Double.coerceNonNegativeFinite(): Double =
        if (isNaN() || isInfinite() || this < 0.0) 0.0 else this.coerceAtMost(1e12)

    private fun Double.coercePercentage(): Double =
        if (isNaN() || isInfinite()) 0.0 else this.coerceIn(0.0, 100.0)

    private fun BigDecimal.clampSafe(): BigDecimal =
        if (this > maxSafeAmount) maxSafeAmount else if (this < BigDecimal.ZERO) BigDecimal.ZERO else this

    private fun BigDecimal.toSafeDouble(): Double =
        this.setScale(2, RoundingMode.HALF_UP).toDouble().let {
            if (it.isNaN() || it.isInfinite()) 0.0 else it
        }
}
