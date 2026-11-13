package com.example.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ScenarioEntity
import com.example.data.local.ScenarioRepository
import com.example.data.local.ThemePreference
import com.example.data.local.UserPreferencesRepository
import com.example.domain.calculator.InputValidator
import com.example.domain.calculator.RevenueCalculator
import com.example.domain.model.DisplayCurrency
import com.example.domain.model.RevenueBreakdown
import com.example.domain.model.RevenueInputs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CalculatorField {
    LONG_FORM_VIEWS,
    LONG_FORM_RPM,
    SHORTS_VIEWS,
    SHORTS_RPM,
    SPONSORSHIP_DEAL_COUNT,
    SPONSORSHIP_AVG_FEE,
    AFFILIATE_CLICKS,
    AFFILIATE_CONVERSION_RATE,
    AFFILIATE_AVG_ORDER_VALUE,
    AFFILIATE_COMMISSION_RATE,
    PRODUCT_SALES_COUNT,
    PRODUCT_AVG_ORDER_VALUE,
    MONTHLY_EXPENSES,
    TAX_RESERVE_PCT
}

data class PlannerUiState(
    val selectedCurrency: DisplayCurrency = DisplayCurrency.USD,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val formState: CalculatorFormState = CalculatorFormState(),
    val inputs: RevenueInputs = RevenueInputs(),
    val breakdown: RevenueBreakdown = RevenueCalculator.calculate(RevenueInputs()),
    val activeScenarioId: Long? = null,
    val activeScenarioName: String? = null,
    val activeScenarioIsSample: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val scenarios: List<ScenarioEntity> = emptyList(),
    val selectedCompareIds: List<Long> = emptyList(),
    val isInitialized: Boolean = false,
    val snackbarMessage: String? = null
)

class PlannerViewModel(
    private val scenarioRepository: ScenarioRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(CalculatorFormState())
    private val _activeScenarioId = MutableStateFlow<Long?>(null)
    private val _activeScenarioName = MutableStateFlow<String?>(null)
    private val _activeScenarioIsSample = MutableStateFlow(false)
    private val _hasUnsavedChanges = MutableStateFlow(false)
    private val _selectedCompareIds = MutableStateFlow<List<Long>>(emptyList())
    private val _isInitialized = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private val scenariosFlow = scenarioRepository.allScenarios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currencyFlow = preferencesRepository.selectedCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DisplayCurrency.USD)

    private val themeFlow = preferencesRepository.themePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemePreference.SYSTEM)

    val uiState: StateFlow<PlannerUiState> = combine(
        combine(currencyFlow, themeFlow, scenariosFlow) { currency, theme, scenarios ->
            Triple(currency, theme, scenarios)
        },
        combine(
            _formState,
            _activeScenarioId,
            _activeScenarioName,
            _activeScenarioIsSample,
            _hasUnsavedChanges
        ) { form, scenarioId, scenarioName, isSample, dirty ->
            FormMeta(form, scenarioId, scenarioName, isSample, dirty)
        },
        combine(_selectedCompareIds, _isInitialized, _snackbarMessage) { compareIds, init, msg ->
            Triple(compareIds, init, msg)
        }
    ) { (currency, theme, scenarios), meta, (compareIds, init, msg) ->
        val (inputs, errors) = meta.form.toValidatedInputs()
        val formWithErrors = meta.form.copy(errors = errors)
        val breakdown = RevenueCalculator.calculate(inputs)

        // Keep comparison IDs valid against existing scenarios
        val validIds = scenarios.map { it.id }.toSet()
        val cleanedCompareIds = compareIds.filter { it in validIds }.ifEmpty {
            scenarios.take(2).map { it.id }
        }

        PlannerUiState(
            selectedCurrency = currency,
            themePreference = theme,
            formState = formWithErrors,
            inputs = inputs,
            breakdown = breakdown,
            activeScenarioId = meta.activeScenarioId,
            activeScenarioName = meta.activeScenarioName,
            activeScenarioIsSample = meta.activeScenarioIsSample,
            hasUnsavedChanges = meta.hasUnsavedChanges,
            scenarios = scenarios,
            selectedCompareIds = cleanedCompareIds,
            isInitialized = init,
            snackbarMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState()
    )

    init {
        viewModelScope.launch {
            val seededSample = scenarioRepository.ensureSampleScenarioSeeded()
            val savedDraft = preferencesRepository.readSavedDraft()

            if (savedDraft != null) {
                _formState.value = CalculatorFormState.fromInputs(savedDraft.inputs)
                _activeScenarioId.value = savedDraft.activeScenarioId
                _activeScenarioName.value = savedDraft.activeScenarioName
                _activeScenarioIsSample.value = savedDraft.activeScenarioIsSample
                _hasUnsavedChanges.value = false
            } else if (seededSample != null) {
                loadEntityIntoState(seededSample, markDirty = false)
                persistDraft(seededSample.id, seededSample.name, seededSample.isSample, seededSample.toRevenueInputs())
            }
            _isInitialized.value = true
        }
    }

    fun selectCurrency(currency: DisplayCurrency) {
        viewModelScope.launch {
            preferencesRepository.setSelectedCurrency(currency)
        }
    }

    fun cycleThemePreference() {
        val next = when (themeFlow.value) {
            ThemePreference.SYSTEM -> ThemePreference.LIGHT
            ThemePreference.LIGHT -> ThemePreference.DARK
            ThemePreference.DARK -> ThemePreference.SYSTEM
        }
        viewModelScope.launch {
            preferencesRepository.setThemePreference(next)
        }
    }

    fun setThemePreference(preference: ThemePreference) {
        viewModelScope.launch {
            preferencesRepository.setThemePreference(preference)
        }
    }

    fun onFieldChanged(field: CalculatorField, rawValue: String) {
        val current = _formState.value
        val updated = when (field) {
            CalculatorField.LONG_FORM_VIEWS ->
                current.copy(longFormViewsText = InputValidator.sanitizeIntegerInput(rawValue))
            CalculatorField.LONG_FORM_RPM ->
                current.copy(longFormRpmText = InputValidator.sanitizeDecimalInput(rawValue))
            CalculatorField.SHORTS_VIEWS ->
                current.copy(shortsViewsText = InputValidator.sanitizeIntegerInput(rawValue))
            CalculatorField.SHORTS_RPM ->
                current.copy(shortsRpmText = InputValidator.sanitizeDecimalInput(rawValue))
            CalculatorField.SPONSORSHIP_DEAL_COUNT ->
                current.copy(sponsorshipDealCountText = InputValidator.sanitizeIntegerInput(rawValue))
            CalculatorField.SPONSORSHIP_AVG_FEE ->
                current.copy(sponsorshipAvgFeeText = InputValidator.sanitizeDecimalInput(rawValue))
            CalculatorField.AFFILIATE_CLICKS ->
                current.copy(affiliateLinkClicksText = InputValidator.sanitizeIntegerInput(rawValue))
            CalculatorField.AFFILIATE_CONVERSION_RATE ->
                current.copy(affiliateConversionRateText = InputValidator.sanitizeDecimalInput(rawValue))
            CalculatorField.AFFILIATE_AVG_ORDER_VALUE ->
                current.copy(affiliateAvgOrderValueText = InputValidator.sanitizeDecimalInput(rawValue))
            CalculatorField.AFFILIATE_COMMISSION_RATE ->
                current.copy(affiliateCommissionRateText = InputValidator.sanitizeDecimalInput(rawValue))
            CalculatorField.PRODUCT_SALES_COUNT ->
                current.copy(productSalesCountText = InputValidator.sanitizeIntegerInput(rawValue))
            CalculatorField.PRODUCT_AVG_ORDER_VALUE ->
                current.copy(productAvgOrderValueText = InputValidator.sanitizeDecimalInput(rawValue))
            CalculatorField.MONTHLY_EXPENSES ->
                current.copy(monthlyExpensesText = InputValidator.sanitizeDecimalInput(rawValue))
            CalculatorField.TAX_RESERVE_PCT ->
                current.copy(taxReservePctText = InputValidator.sanitizeDecimalInput(rawValue))
        }

        _formState.value = updated
        if (_activeScenarioId.value != null) {
            _hasUnsavedChanges.value = true
        }
        val (validatedInputs, _) = updated.toValidatedInputs()
        viewModelScope.launch {
            persistDraft(
                _activeScenarioId.value,
                _activeScenarioName.value,
                _activeScenarioIsSample.value,
                validatedInputs
            )
        }
    }

    fun resetCalculator() {
        _formState.value = CalculatorFormState()
        _activeScenarioId.value = null
        _activeScenarioName.value = null
        _activeScenarioIsSample.value = false
        _hasUnsavedChanges.value = false
        viewModelScope.launch {
            persistDraft(null, null, false, RevenueInputs())
            showMessage("Calculadora restablecida en cero.")
        }
    }

    fun loadSampleIntoCalculator() {
        val sample = ScenarioRepository.createSampleScenarioTemplate()
        loadEntityIntoState(sample.copy(id = 0L), markDirty = false)
        viewModelScope.launch {
            persistDraft(null, sample.name, true, sample.toRevenueInputs())
            showMessage("Datos de ejemplo cargados.")
        }
    }

    fun loadScenario(scenario: ScenarioEntity) {
        loadEntityIntoState(scenario, markDirty = false)
        viewModelScope.launch {
            persistDraft(scenario.id, scenario.name, scenario.isSample, scenario.toRevenueInputs())
            showMessage("Escenario \"${scenario.name}\" cargado.")
        }
    }

    fun saveAsNewScenario(name: String, notes: String) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val (inputs, _) = _formState.value.toValidatedInputs()
        viewModelScope.launch {
            val newId = scenarioRepository.saveNewScenario(
                name = cleanName,
                notes = notes.trim(),
                inputs = inputs,
                isSample = false
            )
            _activeScenarioId.value = newId
            _activeScenarioName.value = cleanName
            _activeScenarioIsSample.value = false
            _hasUnsavedChanges.value = false
            persistDraft(newId, cleanName, false, inputs)
            showMessage("Escenario \"$cleanName\" guardado en el dispositivo.")
        }
    }

    fun updateActiveScenario() {
        val activeId = _activeScenarioId.value ?: return
        val existing = scenariosFlow.value.firstOrNull { it.id == activeId } ?: return
        val (inputs, _) = _formState.value.toValidatedInputs()
        val updated = ScenarioEntity.fromInputs(
            id = existing.id,
            name = existing.name,
            notes = existing.notes,
            isSample = existing.isSample,
            inputs = inputs
        )
        viewModelScope.launch {
            scenarioRepository.updateScenario(updated)
            _hasUnsavedChanges.value = false
            persistDraft(updated.id, updated.name, updated.isSample, inputs)
            showMessage("Cambios guardados en \"${updated.name}\".")
        }
    }

    fun renameAndUpdateNotes(scenario: ScenarioEntity, newName: String, newNotes: String) {
        val cleanName = newName.trim()
        if (cleanName.isEmpty()) return
        val updated = scenario.copy(
            name = cleanName,
            notes = newNotes.trim()
        )
        viewModelScope.launch {
            scenarioRepository.updateScenario(updated)
            if (_activeScenarioId.value == scenario.id) {
                _activeScenarioName.value = cleanName
                val (inputs, _) = _formState.value.toValidatedInputs()
                persistDraft(updated.id, cleanName, updated.isSample, inputs)
            }
            showMessage("Escenario actualizado.")
        }
    }

    fun duplicateScenario(scenario: ScenarioEntity) {
        viewModelScope.launch {
            scenarioRepository.duplicateScenario(scenario)
            showMessage("Escenario duplicado.")
        }
    }

    fun deleteScenario(scenario: ScenarioEntity) {
        viewModelScope.launch {
            scenarioRepository.deleteScenario(scenario.id)
            if (_activeScenarioId.value == scenario.id) {
                _activeScenarioId.value = null
                _activeScenarioName.value = null
                _activeScenarioIsSample.value = false
                _hasUnsavedChanges.value = false
                val (inputs, _) = _formState.value.toValidatedInputs()
                persistDraft(null, null, false, inputs)
            }
            _selectedCompareIds.update { ids -> ids.filterNot { it == scenario.id } }
            showMessage("Escenario \"${scenario.name}\" eliminado.")
        }
    }

    fun toggleCompareSelection(scenarioId: Long) {
        _selectedCompareIds.update { current ->
            if (scenarioId in current) {
                current.filterNot { it == scenarioId }
            } else {
                if (current.size >= 3) {
                    current.drop(1) + scenarioId
                } else {
                    current + scenarioId
                }
            }
        }
    }

    fun consumeSnackbarMessage() {
        _snackbarMessage.value = null
    }

    private fun loadEntityIntoState(scenario: ScenarioEntity, markDirty: Boolean) {
        _formState.value = CalculatorFormState.fromInputs(scenario.toRevenueInputs())
        _activeScenarioId.value = scenario.id.takeIf { it > 0L }
        _activeScenarioName.value = scenario.name
        _activeScenarioIsSample.value = scenario.isSample
        _hasUnsavedChanges.value = markDirty
    }

    private suspend fun persistDraft(
        scenarioId: Long?,
        scenarioName: String?,
        isSample: Boolean,
        inputs: RevenueInputs
    ) {
        preferencesRepository.saveDraft(
            activeScenarioId = scenarioId,
            activeScenarioName = scenarioName,
            activeScenarioIsSample = isSample,
            inputs = inputs
        )
    }

    private fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    private data class FormMeta(
        val form: CalculatorFormState,
        val activeScenarioId: Long?,
        val activeScenarioName: String?,
        val activeScenarioIsSample: Boolean,
        val hasUnsavedChanges: Boolean
    )

    class Factory(
        private val scenarioRepository: ScenarioRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlannerViewModel(scenarioRepository, preferencesRepository) as T
        }
    }
}
