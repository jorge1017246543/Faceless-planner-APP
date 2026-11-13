package com.example.data.local

import com.example.domain.model.RevenueInputs
import kotlinx.coroutines.flow.Flow

class ScenarioRepository(
    private val scenarioDao: ScenarioDao,
    private val preferencesRepository: UserPreferencesRepository
) {
    val allScenarios: Flow<List<ScenarioEntity>> = scenarioDao.observeAllScenarios()

    suspend fun ensureSampleScenarioSeeded(): ScenarioEntity? {
        if (preferencesRepository.isInitialSeedDone()) return null
        val count = scenarioDao.getScenarioCount()
        if (count == 0) {
            val sample = createSampleScenarioTemplate()
            val insertedId = scenarioDao.insertScenario(sample)
            preferencesRepository.markInitialSeedDone()
            return sample.copy(id = insertedId)
        }
        preferencesRepository.markInitialSeedDone()
        return null
    }

    suspend fun saveNewScenario(
        name: String,
        notes: String,
        inputs: RevenueInputs,
        isSample: Boolean = false
    ): Long {
        val entity = ScenarioEntity.fromInputs(
            name = name.trim(),
            notes = notes.trim(),
            isSample = isSample,
            inputs = inputs
        )
        return scenarioDao.insertScenario(entity)
    }

    suspend fun updateScenario(scenario: ScenarioEntity) {
        scenarioDao.updateScenario(
            scenario.copy(
                name = scenario.name.trim(),
                notes = scenario.notes.trim(),
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun duplicateScenario(source: ScenarioEntity): Long {
        val copyName = "${source.name} (Copia)"
        val duplicated = source.copy(
            id = 0L,
            name = copyName,
            isSample = false,
            updatedAtMillis = System.currentTimeMillis()
        )
        return scenarioDao.insertScenario(duplicated)
    }

    suspend fun deleteScenario(id: Long) {
        scenarioDao.deleteScenarioById(id)
    }

    companion object {
        fun createSampleScenarioTemplate(): ScenarioEntity = ScenarioEntity(
            name = "Canal de Datos y Documentales",
            notes = "Datos de ejemplo editables para explorar el cálculo. Sustituye estos valores por las métricas reales de tu canal.",
            isSample = true,
            longFormViews = 180_000L,
            longFormRpm = 3.50,
            shortsViews = 650_000L,
            shortsRpm = 0.06,
            sponsorshipDealCount = 1,
            sponsorshipAvgFee = 450.0,
            affiliateLinkClicks = 2_400L,
            affiliateConversionRatePct = 2.5,
            affiliateAvgOrderValue = 48.0,
            affiliateCommissionRatePct = 10.0,
            productSalesCount = 24,
            productAvgOrderValue = 19.0,
            monthlyExpenses = 320.0,
            taxReservePct = 20.0
        )
    }
}
