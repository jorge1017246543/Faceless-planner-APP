package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.DisplayCurrency
import com.example.domain.model.RevenueBreakdown
import com.example.domain.model.RevenueInputs
import com.example.domain.model.RevenueSourceShare
import com.example.ui.components.CurrencyFormatters
import com.example.ui.components.EstimateDisclaimerBanner
import com.example.ui.components.RevenueShareChartCard
import com.example.ui.state.PlannerUiState
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PositiveEmerald
import com.example.ui.theme.TaxAmber

@Composable
fun DashboardScreen(
    uiState: PlannerUiState,
    isWideScreen: Boolean,
    onNavigateToCalculator: () -> Unit,
    onOpenSaveScenario: () -> Unit,
    onLoadSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val breakdown = uiState.breakdown
    val currency = uiState.selectedCurrency
    val isCompletelyEmpty = !breakdown.hasAnyRevenue && uiState.inputs.monthlyExpenses == 0.0

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ActiveScenarioStatusRow(
                    scenarioName = uiState.activeScenarioName,
                    isSample = uiState.activeScenarioIsSample,
                    hasUnsavedChanges = uiState.hasUnsavedChanges,
                    onSaveClick = onOpenSaveScenario
                )
            }

            item {
                EstimateDisclaimerBanner()
            }

            if (isCompletelyEmpty) {
                item {
                    DashboardEmptyStateCard(
                        onGoToCalculator = onNavigateToCalculator,
                        onLoadSample = onLoadSampleData
                    )
                }
            } else {
                if (isWideScreen) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                HeroNetIncomeCard(
                                    breakdown = breakdown,
                                    currency = currency
                                )
                                AnnualProjectionCard(
                                    breakdown = breakdown,
                                    currency = currency
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                RevenueShareChartCard(
                                    breakdown = breakdown,
                                    currency = currency
                                )
                                AdFormatsBreakdownSection(
                                    inputs = uiState.inputs,
                                    breakdown = breakdown,
                                    currency = currency
                                )
                            }
                        }
                    }
                } else {
                    item {
                        HeroNetIncomeCard(
                            breakdown = breakdown,
                            currency = currency
                        )
                    }
                    item {
                        AnnualProjectionCard(
                            breakdown = breakdown,
                            currency = currency
                        )
                    }
                    item {
                        AdFormatsBreakdownSection(
                            inputs = uiState.inputs,
                            breakdown = breakdown,
                            currency = currency
                        )
                    }
                    item {
                        RevenueShareChartCard(
                            breakdown = breakdown,
                            currency = currency
                        )
                    }
                }

                item {
                    SourceBreakdownListCard(
                        shares = breakdown.shares,
                        totalGrossRevenue = breakdown.totalGrossRevenue,
                        currency = currency
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToCalculator,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_edit_inputs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Calculate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar variables")
                        }
                        OutlinedButton(
                            onClick = onOpenSaveScenario,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_save_scenario_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun ActiveScenarioStatusRow(
    scenarioName: String?,
    isSample: Boolean,
    hasUnsavedChanges: Boolean,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = scenarioName ?: "Borrador personalizado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSample) {
                    BadgePill(
                        text = "Datos de ejemplo",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                if (hasUnsavedChanges) {
                    BadgePill(
                        text = "Sin guardar",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        FilledTonalButton(
            onClick = onSaveClick,
            modifier = Modifier.testTag("top_save_scenario_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = "Guardar escenario",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Guardar")
        }
    }
}

@Composable
private fun BadgePill(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun HeroNetIncomeCard(
    breakdown: RevenueBreakdown,
    currency: DisplayCurrency
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A493B),
            Color(0xFF0F2C27)
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INGRESO NETO MENSUAL ESTIMADO",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFA7F3D0)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Estimación",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = CurrencyFormatters.formatMoney(breakdown.estimatedNetIncome, currency),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (breakdown.estimatedNetIncome >= 0.0) {
                        Color.White
                    } else {
                        Color(0xFFFDA4AF)
                    },
                    modifier = Modifier.testTag("hero_net_income_value")
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.14f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroMetricItem(
                        label = "Bruto mensual",
                        value = CurrencyFormatters.formatMoney(breakdown.totalGrossRevenue, currency),
                        valueColor = Color(0xFFECFDF5),
                        modifier = Modifier.weight(1f),
                        testTag = "hero_gross_revenue_value"
                    )
                    HeroMetricItem(
                        label = "Gastos",
                        value = CurrencyFormatters.formatMoney(breakdown.monthlyExpenses, currency),
                        valueColor = Color(0xFFFECDD3),
                        modifier = Modifier.weight(1f),
                        testTag = "hero_expenses_value"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroMetricItem(
                        label = "Utilidad antes de imp.",
                        value = CurrencyFormatters.formatMoney(breakdown.estimatedProfitBeforeTax, currency),
                        valueColor = Color.White,
                        modifier = Modifier.weight(1f),
                        testTag = "hero_profit_before_tax_value"
                    )
                    HeroMetricItem(
                        label = "Reserva fiscal est.",
                        value = CurrencyFormatters.formatMoney(breakdown.estimatedTaxReserve, currency),
                        valueColor = Color(0xFFFDE68A),
                        modifier = Modifier.weight(1f),
                        testTag = "hero_tax_reserve_value"
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricItem(
    label: String,
    value: String,
    valueColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFD1FAE5).copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun AnnualProjectionCard(
    breakdown: RevenueBreakdown,
    currency: DisplayCurrency
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Proyección anual estimada (×12)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calculada multiplicando tu estimación mensual actual por 12 meses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Bruto anual est.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatters.formatMoney(breakdown.annualGrossRevenue, currency),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("annual_gross_value")
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Neto anual est.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = CurrencyFormatters.formatMoney(breakdown.annualNetIncome, currency),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("annual_net_value")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdFormatsBreakdownSection(
    inputs: RevenueInputs,
    breakdown: RevenueBreakdown,
    currency: DisplayCurrency
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdFormatCard(
            title = "Anuncios largos",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Movie,
                    contentDescription = "Videos largos",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            },
            viewsText = "${CurrencyFormatters.formatCount(inputs.longFormViews)} vistas",
            rpmText = "RPM: ${CurrencyFormatters.formatMoney(inputs.longFormRpm, currency)}",
            revenueText = CurrencyFormatters.formatMoney(breakdown.longFormAdRevenue, currency),
            modifier = Modifier.weight(1f),
            testTag = "long_form_ad_revenue_card"
        )

        AdFormatCard(
            title = "Anuncios Shorts",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.PlayCircle,
                    contentDescription = "YouTube Shorts",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            viewsText = "${CurrencyFormatters.formatCount(inputs.shortsViews)} vistas",
            rpmText = "RPM: ${CurrencyFormatters.formatMoney(inputs.shortsRpm, currency)}",
            revenueText = CurrencyFormatters.formatMoney(breakdown.shortsAdRevenue, currency),
            modifier = Modifier.weight(1f),
            testTag = "shorts_ad_revenue_card"
        )
    }
}

@Composable
private fun AdFormatCard(
    title: String,
    icon: @Composable () -> Unit,
    viewsText: String,
    rpmText: String,
    revenueText: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag(testTag),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                icon()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = revenueText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = viewsText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = rpmText,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SourceBreakdownListCard(
    shares: List<RevenueSourceShare>,
    totalGrossRevenue: Double,
    currency: DisplayCurrency
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Desglose por fuente de ingreso",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            shares.forEachIndexed { index, share ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CurrencyFormatters.colorForSource(share.type))
                        )
                        Column {
                            Text(
                                text = share.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = CurrencyFormatters.formatPercentage(share.sharePercentage) + " del ingreso bruto",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = CurrencyFormatters.formatMoney(share.amount, currency),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (index < shares.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total ingreso bruto estimado",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = CurrencyFormatters.formatMoney(totalGrossRevenue, currency),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PositiveEmerald
                )
            }
        }
    }
}

@Composable
private fun DashboardEmptyStateCard(
    onGoToCalculator: () -> Unit,
    onLoadSample: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_empty_state"),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AutoGraph,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = "Aún no hay datos en tu proyección",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Ingresa las vistas estimadas, RPM, patrocinios, afiliados o ventas de tu canal faceless para ver el cálculo mensual y anual al instante.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGoToCalculator,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_state_go_calculator")
                ) {
                    Text("Abrir calculadora")
                }
                OutlinedButton(
                    onClick = onLoadSample,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_state_load_sample")
                ) {
                    Text("Cargar datos de ejemplo")
                }
            }
        }
    }
}
