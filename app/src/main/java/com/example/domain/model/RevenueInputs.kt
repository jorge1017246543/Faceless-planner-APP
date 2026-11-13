package com.example.domain.model

data class RevenueInputs(
    val longFormViews: Long = 0L,
    val longFormRpm: Double = 0.0,
    val shortsViews: Long = 0L,
    val shortsRpm: Double = 0.0,
    val sponsorshipDealCount: Int = 0,
    val sponsorshipAvgFee: Double = 0.0,
    val affiliateLinkClicks: Long = 0L,
    val affiliateConversionRatePct: Double = 0.0,
    val affiliateAvgOrderValue: Double = 0.0,
    val affiliateCommissionRatePct: Double = 0.0,
    val productSalesCount: Int = 0,
    val productAvgOrderValue: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val taxReservePct: Double = 0.0
)
