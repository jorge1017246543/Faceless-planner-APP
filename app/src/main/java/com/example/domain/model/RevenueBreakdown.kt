package com.example.domain.model

enum class RevenueSourceType {
    LONG_FORM_ADS,
    SHORTS_ADS,
    SPONSORSHIPS,
    AFFILIATES,
    PRODUCTS
}

data class RevenueSourceShare(
    val type: RevenueSourceType,
    val label: String,
    val amount: Double,
    val sharePercentage: Double
)

data class RevenueBreakdown(
    val longFormAdRevenue: Double,
    val shortsAdRevenue: Double,
    val sponsorshipRevenue: Double,
    val affiliateRevenue: Double,
    val productRevenue: Double,
    val totalGrossRevenue: Double,
    val monthlyExpenses: Double,
    val estimatedProfitBeforeTax: Double,
    val estimatedTaxReserve: Double,
    val estimatedNetIncome: Double,
    val annualGrossRevenue: Double,
    val annualNetIncome: Double,
    val shares: List<RevenueSourceShare>
) {
    val hasAnyRevenue: Boolean
        get() = totalGrossRevenue > 0.0
}
