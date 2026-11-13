package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ScenarioEntity
import com.example.domain.calculator.RevenueCalculator
import com.example.domain.model.DisplayCurrency
import com.example.ui.components.CurrencyFormatters
import com.example.ui.components.DeleteScenarioDialog
import com.example.ui.components.EstimateDisclaimerBanner
import com.example.ui.components.SaveScenarioDialog
import com.example.ui.state.PlannerUiState
import com.example.ui.theme.MonoFontFamily

@Composable
fun ScenariosScreen(
    uiState: PlannerUiState,
    onLoadScenario: (ScenarioEntity) -> Unit,
    onEditScenarioValues: (ScenarioEntity) -> Unit,
    onRenameScenario: (ScenarioEntity, String, String) -> Unit,
    onDuplicateScenario: (ScenarioEntity) -> Unit,
    onDeleteScenario: (ScenarioEntity) -> Unit,
    onToggleCompareScenario: (Long) -> Unit,
    onOpenSaveNewScenario: () -> Unit,
    onLoadSampleScenario: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var editingMetaScenario by remember { mutableStateOf<ScenarioEntity?>(null) }
    var deletingScenario by remember { mutableStateOf<ScenarioEntity?>(null) }

    editingMetaScenario?.let { target ->
        SaveScenarioDialog(
            initialName = target.name,
            initialNotes = target.notes,
            title = "Editar nombre y notas",
            confirmLabel = "Guardar",
            onConfirm = { newName, newNotes ->
                onRenameScenario(target, newName, newNotes)
            },
            onDismiss = { editingMetaScenario = null }
        )
    }

    deletingScenario?.let { target ->
        DeleteScenarioDialog(
            scenarioName = target.name,
            onConfirm = { onDeleteScenario(target) },
            onDismiss = { deletingScenario = null }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 960.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("tab_saved_scenarios")
                ) {
                    Text("Mis escenarios (${uiState.scenarios.size})")
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("tab_compare_scenarios")
                ) {
                    Text("Comparar")
                }
            }
        }

        if (selectedTab == 0) {
            SavedScenariosList(
                scenarios = uiState.scenarios,
                activeScenarioId = uiState.activeScenarioId,
                currency = uiState.selectedCurrency,
                onOpenSaveNew = onOpenSaveNewScenario,
                onLoadSample = onLoadSampleScenario,
                onLoadIntoDashboard = onLoadScenario,
                onEditValues = onEditScenarioValues,
                onRename = { editingMetaScenario = it },
                onDuplicate = onDuplicateScenario,
                onRequestDelete = { deletingScenario = it }
            )
        } else {
            ScenarioComparisonView(
                scenarios = uiState.scenarios,
                selectedIds = uiState.selectedCompareIds,
                currency = uiState.selectedCurrency,
                onToggleSelection = onToggleCompareScenario,
                onDuplicateFirst = {
                    uiState.scenarios.firstOrNull()?.let(onDuplicateScenario)
                },
                onOpenSaveNew = onOpenSaveNewScenario
            )
        }
    }
}

@Composable
private fun SavedScenariosList(
    scenarios: List<ScenarioEntity>,
    activeScenarioId: Long?,
    currency: DisplayCurrency,
    onOpenSaveNew: () -> Unit,
    onLoadSample: () -> Unit,
    onLoadIntoDashboard: (ScenarioEntity) -> Unit,
    onEditValues: (ScenarioEntity) -> Unit,
    onRename: (ScenarioEntity) -> Unit,
    onDuplicate: (ScenarioEntity) -> Unit,
    onRequestDelete: (ScenarioEntity) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = onOpenSaveNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_scenario_from_current_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cálculo actual como escenario")
                }
            }

            if (scenarios.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scenarios_empty_state"),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No hay escenarios guardados",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Guarda diferentes combinaciones de vistas, RPM y gastos en tu dispositivo para compararlas cuando quieras.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = onLoadSample,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cargar escenario de ejemplo")
                            }
                        }
                    }
                }
            } else {
                items(scenarios, key = { it.id }) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        isActive = scenario.id == activeScenarioId,
                        currency = currency,
                        onLoad = { onLoadIntoDashboard(scenario) },
                        onEditValues = { onEditValues(scenario) },
                        onRename = { onRename(scenario) },
                        onDuplicate = { onDuplicate(scenario) },
                        onDelete = { onRequestDelete(scenario) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ScenarioCard(
    scenario: ScenarioEntity,
    isActive: Boolean,
    currency: DisplayCurrency,
    onLoad: () -> Unit,
    onEditValues: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val breakdown = remember(scenario) {
        RevenueCalculator.calculate(scenario.toRevenueInputs())
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scenario_card_${scenario.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isActive) 2.dp else 1.dp,
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = scenario.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (scenario.isSample) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "Datos de ejemplo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (isActive) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Activo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (scenario.notes.isNotBlank()) {
                        Text(
                            text = scenario.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onRename,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("rename_scenario_${scenario.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Renombrar escenario"
                        )
                    }
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("duplicate_scenario_${scenario.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Duplicar escenario"
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_scenario_${scenario.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Eliminar escenario",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScenarioMetricCell(
                    label = "Bruto est.",
                    value = CurrencyFormatters.formatMoney(breakdown.totalGrossRevenue, currency),
                    modifier = Modifier.weight(1f)
                )
                ScenarioMetricCell(
                    label = "Gastos",
                    value = CurrencyFormatters.formatMoney(breakdown.monthlyExpenses, currency),
                    modifier = Modifier.weight(1f)
                )
                ScenarioMetricCell(
                    label = "Neto est.",
                    value = CurrencyFormatters.formatMoney(breakdown.estimatedNetIncome, currency),
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Largo: ${CurrencyFormatters.formatCount(scenario.longFormViews)} · RPM ${
                        CurrencyFormatters.formatMoney(scenario.longFormRpm, currency)
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Shorts: ${CurrencyFormatters.formatCount(scenario.shortsViews)} · RPM ${
                        CurrencyFormatters.formatMoney(scenario.shortsRpm, currency)
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onLoad,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("load_scenario_${scenario.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver resumen")
                }
                OutlinedButton(
                    onClick = onEditValues,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_scenario_${scenario.id}")
                ) {
                    Text("Editar valores")
                }
            }
        }
    }
}

@Composable
private fun ScenarioMetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = MonoFontFamily,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (highlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScenarioComparisonView(
    scenarios: List<ScenarioEntity>,
    selectedIds: List<Long>,
    currency: DisplayCurrency,
    onToggleSelection: (Long) -> Unit,
    onDuplicateFirst: () -> Unit,
    onOpenSaveNew: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EstimateDisclaimerBanner(
                    text = "Comparación basada en tus estimaciones locales. Selecciona entre 2 y 3 escenarios."
                )
            }

            if (scenarios.size < 2) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comparison_need_two_empty_state"),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CompareArrows,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "Necesitas al menos 2 escenarios para comparar",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Duplica un escenario existente o guarda tu cálculo actual con otro nombre para compararlos lado a lado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (scenarios.isNotEmpty()) {
                                    FilledTonalButton(
                                        onClick = onDuplicateFirst,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Duplicar actual")
                                    }
                                }
                                Button(
                                    onClick = onOpenSaveNew,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Guardar nuevo")
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Escenarios a comparar (máx. 3)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            scenarios.forEach { scenario ->
                                val isSelected = scenario.id in selectedIds
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggleSelection(scenario.id) },
                                    label = {
                                        Text(
                                            text = scenario.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "Seleccionado",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    modifier = Modifier.testTag("compare_chip_${scenario.id}")
                                )
                            }
                        }
                    }
                }

                val comparedScenarios = scenarios.filter { it.id in selectedIds }
                if (comparedScenarios.size < 2) {
                    item {
                        Text(
                            text = "Selecciona al menos 2 escenarios arriba para ver la tabla comparativa lado a lado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    item {
                        SideBySideComparisonTable(
                            comparedScenarios = comparedScenarios,
                            currency = currency
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SideBySideComparisonTable(
    comparedScenarios: List<ScenarioEntity>,
    currency: DisplayCurrency
) {
    val breakdowns = remember(comparedScenarios) {
        comparedScenarios.associate { it.id to RevenueCalculator.calculate(it.toRevenueInputs()) }
    }
    val maxNetIncome = breakdowns.values.maxOfOrNull { it.estimatedNetIncome } ?: 0.0

    val rows: List<Pair<String, (ScenarioEntity) -> String>> = listOf(
        "Vistas largos" to { CurrencyFormatters.formatCount(it.longFormViews) },
        "RPM largos" to { CurrencyFormatters.formatMoney(it.longFormRpm, currency) },
        "Vistas Shorts" to { CurrencyFormatters.formatCount(it.shortsViews) },
        "RPM Shorts" to { CurrencyFormatters.formatMoney(it.shortsRpm, currency) },
        "Patrocinios" to {
            val b = breakdowns[it.id]!!
            CurrencyFormatters.formatMoney(b.sponsorshipRevenue, currency)
        },
        "Afiliados" to {
            val b = breakdowns[it.id]!!
            CurrencyFormatters.formatMoney(b.affiliateRevenue, currency)
        },
        "Productos" to {
            val b = breakdowns[it.id]!!
            CurrencyFormatters.formatMoney(b.productRevenue, currency)
        },
        "Ingreso bruto" to {
            val b = breakdowns[it.id]!!
            CurrencyFormatters.formatMoney(b.totalGrossRevenue, currency)
        },
        "Gastos" to {
            val b = breakdowns[it.id]!!
            CurrencyFormatters.formatMoney(b.monthlyExpenses, currency)
        },
        "Reserva fiscal" to {
            val b = breakdowns[it.id]!!
            CurrencyFormatters.formatMoney(b.estimatedTaxReserve, currency)
        },
        "Neto mensual est." to {
            val b = breakdowns[it.id]!!
            CurrencyFormatters.formatMoney(b.estimatedNetIncome, currency)
        },
        "Neto anual est." to {
            val b = breakdowns[it.id]!!
            CurrencyFormatters.formatMoney(b.annualNetIncome, currency)
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("side_by_side_comparison_table"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Fixed Metric Label Column
            Column(
                modifier = Modifier
                    .width(128.dp)
                    .padding(vertical = 12.dp, horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier.height(54.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Métrica",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                rows.forEachIndexed { idx, (label, _) ->
                    val isNetRow = label == "Neto mensual est."
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = label,
                            style = if (isNetRow) {
                                MaterialTheme.typography.labelLarge
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                            fontWeight = if (isNetRow) FontWeight.Bold else FontWeight.Medium,
                            color = if (isNetRow) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (idx < rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }

            // Horizontally scrollable side-by-side scenario columns
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 12.dp)
            ) {
                comparedScenarios.forEach { scenario ->
                    val b = breakdowns[scenario.id]!!
                    val isTopNet = b.estimatedNetIncome == maxNetIncome && maxNetIncome > 0.0

                    Column(
                        modifier = Modifier
                            .width(156.dp)
                            .padding(horizontal = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.height(54.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = scenario.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (scenario.isSample) {
                                Text(
                                    text = "Ejemplo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            } else if (isTopNet) {
                                Text(
                                    text = "Mayor neto",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        rows.forEachIndexed { idx, (label, valueProvider) ->
                            val isNetRow = label == "Neto mensual est."
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = valueProvider(scenario),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = MonoFontFamily,
                                        fontWeight = if (isNetRow) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isNetRow && isTopNet) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (idx < rows.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
