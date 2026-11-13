package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.RevenueInputs

@Entity(tableName = "scenarios")
data class ScenarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val notes: String = "",
    val isSample: Boolean = false,
    val longFormViews: Long,
    val longFormRpm: Double,
    val shortsViews: Long,
    val shortsRpm: Double,
    val sponsorshipDealCount: Int,
    val sponsorshipAvgFee: Double,
    val affiliateLinkClicks: Long,
    val affiliateConversionRatePct: Double,
    val affiliateAvgOrderValue: Double,
    val affiliateCommissionRatePct: Double,
    val productSalesCount: Int,
    val productAvgOrderValue: Double,
    val monthlyExpenses: Double,
    val taxReservePct: Double,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toRevenueInputs(): RevenueInputs = RevenueInputs(
        longFormViews = longFormViews,
        longFormRpm = longFormRpm,
        shortsViews = shortsViews,
        shortsRpm = shortsRpm,
        sponsorshipDealCount = sponsorshipDealCount,
        sponsorshipAvgFee = sponsorshipAvgFee,
        affiliateLinkClicks = affiliateLinkClicks,
        affiliateConversionRatePct = affiliateConversionRatePct,
        affiliateAvgOrderValue = affiliateAvgOrderValue,
        affiliateCommissionRatePct = affiliateCommissionRatePct,
        productSalesCount = productSalesCount,
        productAvgOrderValue = productAvgOrderValue,
        monthlyExpenses = monthlyExpenses,
        taxReservePct = taxReservePct
    )

    companion object {
        fun fromInputs(
            id: Long = 0L,
            name: String,
            notes: String = "",
            isSample: Boolean = false,
            inputs: RevenueInputs
        ): ScenarioEntity = ScenarioEntity(
            id = id,
            name = name,
            notes = notes,
            isSample = isSample,
            longFormViews = inputs.longFormViews,
            longFormRpm = inputs.longFormRpm,
            shortsViews = inputs.shortsViews,
            shortsRpm = inputs.shortsRpm,
            sponsorshipDealCount = inputs.sponsorshipDealCount,
            sponsorshipAvgFee = inputs.sponsorshipAvgFee,
            affiliateLinkClicks = inputs.affiliateLinkClicks,
            affiliateConversionRatePct = inputs.affiliateConversionRatePct,
            affiliateAvgOrderValue = inputs.affiliateAvgOrderValue,
            affiliateCommissionRatePct = inputs.affiliateCommissionRatePct,
            productSalesCount = inputs.productSalesCount,
            productAvgOrderValue = inputs.productAvgOrderValue,
            monthlyExpenses = inputs.monthlyExpenses,
            taxReservePct = inputs.taxReservePct,
            updatedAtMillis = System.currentTimeMillis()
        )
    }
}
