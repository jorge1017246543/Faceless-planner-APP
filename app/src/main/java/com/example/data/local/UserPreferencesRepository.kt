package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.DisplayCurrency
import com.example.domain.model.RevenueInputs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.plannerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "faceless_planner_preferences"
)

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromString(value: String?): ThemePreference {
            return entries.firstOrNull { it.name == value } ?: SYSTEM
        }
    }
}

data class PersistedDraft(
    val activeScenarioId: Long?,
    val activeScenarioName: String?,
    val activeScenarioIsSample: Boolean,
    val inputs: RevenueInputs
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val INITIAL_SEED_DONE = booleanPreferencesKey("initial_seed_done")

        val ACTIVE_SCENARIO_ID = longPreferencesKey("active_scenario_id")
        val ACTIVE_SCENARIO_NAME = stringPreferencesKey("active_scenario_name")
        val ACTIVE_SCENARIO_IS_SAMPLE = booleanPreferencesKey("active_scenario_is_sample")

        val DRAFT_LONG_VIEWS = longPreferencesKey("draft_long_views")
        val DRAFT_LONG_RPM = doublePreferencesKey("draft_long_rpm")
        val DRAFT_SHORTS_VIEWS = longPreferencesKey("draft_shorts_views")
        val DRAFT_SHORTS_RPM = doublePreferencesKey("draft_shorts_rpm")
        val DRAFT_DEAL_COUNT = intPreferencesKey("draft_deal_count")
        val DRAFT_DEAL_FEE = doublePreferencesKey("draft_deal_fee")
        val DRAFT_AFF_CLICKS = longPreferencesKey("draft_aff_clicks")
        val DRAFT_AFF_CONV = doublePreferencesKey("draft_aff_conv")
        val DRAFT_AFF_AOV = doublePreferencesKey("draft_aff_aov")
        val DRAFT_AFF_COMM = doublePreferencesKey("draft_aff_comm")
        val DRAFT_PROD_SALES = intPreferencesKey("draft_prod_sales")
        val DRAFT_PROD_AOV = doublePreferencesKey("draft_prod_aov")
        val DRAFT_EXPENSES = doublePreferencesKey("draft_expenses")
        val DRAFT_TAX_PCT = doublePreferencesKey("draft_tax_pct")
    }

    val selectedCurrency: Flow<DisplayCurrency> = context.plannerDataStore.data.map { prefs ->
        DisplayCurrency.fromCode(prefs[Keys.CURRENCY_CODE])
    }

    val themePreference: Flow<ThemePreference> = context.plannerDataStore.data.map { prefs ->
        ThemePreference.fromString(prefs[Keys.THEME_PREFERENCE])
    }

    suspend fun setSelectedCurrency(currency: DisplayCurrency) {
        context.plannerDataStore.edit { prefs ->
            prefs[Keys.CURRENCY_CODE] = currency.code
        }
    }

    suspend fun setThemePreference(preference: ThemePreference) {
        context.plannerDataStore.edit { prefs ->
            prefs[Keys.THEME_PREFERENCE] = preference.name
        }
    }

    suspend fun isInitialSeedDone(): Boolean {
        return context.plannerDataStore.data.first()[Keys.INITIAL_SEED_DONE] ?: false
    }

    suspend fun markInitialSeedDone() {
        context.plannerDataStore.edit { prefs ->
            prefs[Keys.INITIAL_SEED_DONE] = true
        }
    }

    suspend fun readSavedDraft(): PersistedDraft? {
        val prefs = context.plannerDataStore.data.first()
        val hasAnyDraft = prefs.contains(Keys.DRAFT_LONG_VIEWS) ||
            prefs.contains(Keys.ACTIVE_SCENARIO_ID)
        if (!hasAnyDraft) return null

        val scenarioId = prefs[Keys.ACTIVE_SCENARIO_ID]?.takeIf { it > 0L }
        return PersistedDraft(
            activeScenarioId = scenarioId,
            activeScenarioName = prefs[Keys.ACTIVE_SCENARIO_NAME],
            activeScenarioIsSample = prefs[Keys.ACTIVE_SCENARIO_IS_SAMPLE] ?: false,
            inputs = RevenueInputs(
                longFormViews = prefs[Keys.DRAFT_LONG_VIEWS] ?: 0L,
                longFormRpm = prefs[Keys.DRAFT_LONG_RPM] ?: 0.0,
                shortsViews = prefs[Keys.DRAFT_SHORTS_VIEWS] ?: 0L,
                shortsRpm = prefs[Keys.DRAFT_SHORTS_RPM] ?: 0.0,
                sponsorshipDealCount = prefs[Keys.DRAFT_DEAL_COUNT] ?: 0,
                sponsorshipAvgFee = prefs[Keys.DRAFT_DEAL_FEE] ?: 0.0,
                affiliateLinkClicks = prefs[Keys.DRAFT_AFF_CLICKS] ?: 0L,
                affiliateConversionRatePct = prefs[Keys.DRAFT_AFF_CONV] ?: 0.0,
                affiliateAvgOrderValue = prefs[Keys.DRAFT_AFF_AOV] ?: 0.0,
                affiliateCommissionRatePct = prefs[Keys.DRAFT_AFF_COMM] ?: 0.0,
                productSalesCount = prefs[Keys.DRAFT_PROD_SALES] ?: 0,
                productAvgOrderValue = prefs[Keys.DRAFT_PROD_AOV] ?: 0.0,
                monthlyExpenses = prefs[Keys.DRAFT_EXPENSES] ?: 0.0,
                taxReservePct = prefs[Keys.DRAFT_TAX_PCT] ?: 0.0
            )
        )
    }

    suspend fun saveDraft(
        activeScenarioId: Long?,
        activeScenarioName: String?,
        activeScenarioIsSample: Boolean,
        inputs: RevenueInputs
    ) {
        context.plannerDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_SCENARIO_ID] = activeScenarioId ?: 0L
            if (activeScenarioName != null) {
                prefs[Keys.ACTIVE_SCENARIO_NAME] = activeScenarioName
            } else {
                prefs.remove(Keys.ACTIVE_SCENARIO_NAME)
            }
            prefs[Keys.ACTIVE_SCENARIO_IS_SAMPLE] = activeScenarioIsSample

            prefs[Keys.DRAFT_LONG_VIEWS] = inputs.longFormViews
            prefs[Keys.DRAFT_LONG_RPM] = inputs.longFormRpm
            prefs[Keys.DRAFT_SHORTS_VIEWS] = inputs.shortsViews
            prefs[Keys.DRAFT_SHORTS_RPM] = inputs.shortsRpm
            prefs[Keys.DRAFT_DEAL_COUNT] = inputs.sponsorshipDealCount
            prefs[Keys.DRAFT_DEAL_FEE] = inputs.sponsorshipAvgFee
            prefs[Keys.DRAFT_AFF_CLICKS] = inputs.affiliateLinkClicks
            prefs[Keys.DRAFT_AFF_CONV] = inputs.affiliateConversionRatePct
            prefs[Keys.DRAFT_AFF_AOV] = inputs.affiliateAvgOrderValue
            prefs[Keys.DRAFT_AFF_COMM] = inputs.affiliateCommissionRatePct
            prefs[Keys.DRAFT_PROD_SALES] = inputs.productSalesCount
            prefs[Keys.DRAFT_PROD_AOV] = inputs.productAvgOrderValue
            prefs[Keys.DRAFT_EXPENSES] = inputs.monthlyExpenses
            prefs[Keys.DRAFT_TAX_PCT] = inputs.taxReservePct
        }
    }
}
