package com.example

import com.example.domain.calculator.InputValidator
import com.example.domain.calculator.RevenueCalculator
import com.example.domain.model.RevenueInputs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RevenueCalculatorTest {

    @Test
    fun `calculates all revenue streams, profit, tax reserve, and annual totals accurately`() {
        val inputs = RevenueInputs(
            longFormViews = 200_000L,
            longFormRpm = 4.50,          // 200 * 4.50 = 900.00
            shortsViews = 500_000L,
            shortsRpm = 0.08,            // 500 * 0.08 = 40.00
            sponsorshipDealCount = 2,
            sponsorshipAvgFee = 500.0,   // 1000.00
            affiliateLinkClicks = 1_000L,
            affiliateConversionRatePct = 5.0,
            affiliateAvgOrderValue = 40.0,
            affiliateCommissionRatePct = 10.0, // 1000 * 0.05 * 40 * 0.10 = 200.00
            productSalesCount = 15,
            productAvgOrderValue = 20.0, // 300.00
            monthlyExpenses = 440.0,
            taxReservePct = 25.0
        )

        val result = RevenueCalculator.calculate(inputs)

        assertEquals(900.0, result.longFormAdRevenue, 0.01)
        assertEquals(40.0, result.shortsAdRevenue, 0.01)
        assertEquals(1000.0, result.sponsorshipRevenue, 0.01)
        assertEquals(200.0, result.affiliateRevenue, 0.01)
        assertEquals(300.0, result.productRevenue, 0.01)

        // Gross = 900 + 40 + 1000 + 200 + 300 = 2440.00
        assertEquals(2440.0, result.totalGrossRevenue, 0.01)
        // Profit before tax = 2440 - 440 = 2000.00
        assertEquals(2000.0, result.estimatedProfitBeforeTax, 0.01)
        // Tax reserve = 2000 * 25% = 500.00
        assertEquals(500.0, result.estimatedTaxReserve, 0.01)
        // Net income = 2000 - 500 = 1500.00
        assertEquals(1500.0, result.estimatedNetIncome, 0.01)
        // Annual = 2440 * 12 = 29280, 1500 * 12 = 18000
        assertEquals(29280.0, result.annualGrossRevenue, 0.01)
        assertEquals(18000.0, result.annualNetIncome, 0.01)
    }

    @Test
    fun `tax reserve is zero when expenses exceed gross revenue`() {
        val inputs = RevenueInputs(
            longFormViews = 10_000L,
            longFormRpm = 2.0, // 20.00
            monthlyExpenses = 100.0,
            taxReservePct = 30.0
        )

        val result = RevenueCalculator.calculate(inputs)

        assertEquals(20.0, result.totalGrossRevenue, 0.01)
        assertEquals(-80.0, result.estimatedProfitBeforeTax, 0.01)
        assertEquals(0.0, result.estimatedTaxReserve, 0.01)
        assertEquals(-80.0, result.estimatedNetIncome, 0.01)
    }

    @Test
    fun `validator treats blanks as zero and rejects negative or out of range percentages`() {
        assertEquals(0L, InputValidator.validateLongCount("").value)
        assertEquals(0.0, InputValidator.validateMonetaryAmount("   ").value, 0.001)

        val negativeCount = InputValidator.validateLongCount("-50")
        assertNotNull(negativeCount.errorMessage)
        assertEquals(0L, negativeCount.value)

        val overPct = InputValidator.validatePercentage("125")
        assertNotNull(overPct.errorMessage)
        assertEquals(100.0, overPct.value, 0.001)

        val validPct = InputValidator.validatePercentage("22.5")
        assertNull(validPct.errorMessage)
        assertEquals(22.5, validPct.value, 0.001)
    }

    @Test
    fun `very large input values do not crash or produce NaN`() {
        val inputs = RevenueInputs(
            longFormViews = 999_999_999_999L,
            longFormRpm = 500.0,
            taxReservePct = 100.0
        )
        val result = RevenueCalculator.calculate(inputs)
        assertTrue(result.totalGrossRevenue.isFinite())
        assertTrue(result.estimatedNetIncome.isFinite())
    }
}
