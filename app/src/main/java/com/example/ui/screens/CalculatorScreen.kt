package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.DisplayCurrency
import com.example.ui.components.CurrencyFormatters
import com.example.ui.components.EstimateDisclaimerBanner
import com.example.ui.components.ResetCalculatorDialog
import com.example.ui.components.ValidatedNumberField
import com.example.ui.state.CalculatorField
import com.example.ui.state.PlannerUiState
import com.example.ui.theme.MonoFontFamily

@Composable
fun CalculatorScreen(
    uiState: PlannerUiState,
    onFieldChanged: (CalculatorField, String) -> Unit,
    onResetConfirmed: () -> Unit,
    onUpdateActiveScenario: () -> Unit,
    onOpenSaveAsNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    val form = uiState.formState
    val errors = form.errors
    val currency = uiState.selectedCurrency
    val breakdown = uiState.breakdown

    if (showResetDialog) {
        ResetCalculatorDialog(
            onConfirm = onResetConfirmed,
            onDismiss = { showResetDialog = false }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Live sticky summary header so calculations update visibly right as the user types
        LiveCalculatorSummaryBar(
            grossRevenue = breakdown.totalGrossRevenue,
            netIncome = breakdown.estimatedNetIncome,
            currency = currency,
            scenarioName = uiState.activeScenarioName,
            isSample = uiState.activeScenarioIsSample
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    EstimateDisclaimerBanner(
                        text = "Los campos vacíos se toman como 0. Todos los montos usan ${currency.code} sin conversión automática."
                    )
                }

                // 1. Long-form Ads
                item {
                    CalculatorSectionCard(
                        title = "Videos largos (AdSense / YouTube)",
                        subtitle = "El RPM representa el ingreso estimado para el creador por cada 1,000 vistas totales (no se aplica porcentaje adicional de vistas monetizadas).",
                        icon = Icons.Outlined.Movie,
                        subtotalText = CurrencyFormatters.formatMoney(breakdown.longFormAdRevenue, currency)
                    ) {
                        ValidatedNumberField(
                            label = "Vistas mensuales de videos largos",
                            value = form.longFormViewsText,
                            onValueChange = { onFieldChanged(CalculatorField.LONG_FORM_VIEWS, it) },
                            helperText = "Total de visualizaciones estimadas al mes en formato largo.",
                            errorText = errors.longFormViews,
                            testTag = "input_long_form_views",
                            isDecimal = false
                        )
                        ValidatedNumberField(
                            label = "RPM estimado de videos largos",
                            value = form.longFormRpmText,
                            onValueChange = { onFieldChanged(CalculatorField.LONG_FORM_RPM, it) },
                            helperText = "Ingreso estimado por cada 1,000 vistas en ${currency.code}.",
                            errorText = errors.longFormRpm,
                            testTag = "input_long_form_rpm",
                            isDecimal = true,
                            prefixText = currency.symbol
                        )
                    }
                }

                // 2. YouTube Shorts Ads
                item {
                    CalculatorSectionCard(
                        title = "YouTube Shorts",
                        subtitle = "Calculado directamente sobre vistas totales de Shorts divididas entre 1,000 por tu RPM de Shorts.",
                        icon = Icons.Outlined.PlayCircle,
                        subtotalText = CurrencyFormatters.formatMoney(breakdown.shortsAdRevenue, currency)
                    ) {
                        ValidatedNumberField(
                            label = "Vistas mensuales de Shorts",
                            value = form.shortsViewsText,
                            onValueChange = { onFieldChanged(CalculatorField.SHORTS_VIEWS, it) },
                            helperText = "Total de vistas estimadas al mes en videos verticales Shorts.",
                            errorText = errors.shortsViews,
                            testTag = "input_shorts_views",
                            isDecimal = false
                        )
                        ValidatedNumberField(
                            label = "RPM estimado de Shorts",
                            value = form.shortsRpmText,
                            onValueChange = { onFieldChanged(CalculatorField.SHORTS_RPM, it) },
                            helperText = "Ingreso estimado del creador por cada 1,000 vistas de Shorts.",
                            errorText = errors.shortsRpm,
                            testTag = "input_shorts_rpm",
                            isDecimal = true,
                            prefixText = currency.symbol
                        )
                    }
                }

                // 3. Sponsorships
                item {
                    CalculatorSectionCard(
                        title = "Patrocinios directos",
                        subtitle = "Menciones de marcas o integraciones pagadas dentro de tus videos.",
                        icon = Icons.Outlined.Campaign,
                        subtotalText = CurrencyFormatters.formatMoney(breakdown.sponsorshipRevenue, currency)
                    ) {
                        ValidatedNumberField(
                            label = "Acuerdos de patrocinio al mes",
                            value = form.sponsorshipDealCountText,
                            onValueChange = { onFieldChanged(CalculatorField.SPONSORSHIP_DEAL_COUNT, it) },
                            helperText = "Cantidad estimada de integraciones patrocinadas por mes.",
                            errorText = errors.sponsorshipDealCount,
                            testTag = "input_sponsorship_deals",
                            isDecimal = false
                        )
                        ValidatedNumberField(
                            label = "Tarifa promedio por acuerdo",
                            value = form.sponsorshipAvgFeeText,
                            onValueChange = { onFieldChanged(CalculatorField.SPONSORSHIP_AVG_FEE, it) },
                            helperText = "Monto promedio acordado por cada patrocinio en ${currency.code}.",
                            errorText = errors.sponsorshipAvgFee,
                            testTag = "input_sponsorship_fee",
                            isDecimal = true,
                            prefixText = currency.symbol
                        )
                    }
                }

                // 4. Affiliates
                item {
                    CalculatorSectionCard(
                        title = "Marketing de afiliados",
                        subtitle = "Ingresos por enlaces en la descripción o comentarios fijados.",
                        icon = Icons.Outlined.Link,
                        subtotalText = CurrencyFormatters.formatMoney(breakdown.affiliateRevenue, currency)
                    ) {
                        ValidatedNumberField(
                            label = "Clics mensuales en enlaces",
                            value = form.affiliateLinkClicksText,
                            onValueChange = { onFieldChanged(CalculatorField.AFFILIATE_CLICKS, it) },
                            helperText = "Cantidad mensual estimada de clics en tus enlaces de afiliado.",
                            errorText = errors.affiliateLinkClicks,
                            testTag = "input_affiliate_clicks",
                            isDecimal = false
                        )
                        ValidatedNumberField(
                            label = "Tasa de conversión (%)",
                            value = form.affiliateConversionRateText,
                            onValueChange = { onFieldChanged(CalculatorField.AFFILIATE_CONVERSION_RATE, it) },
                            helperText = "Porcentaje de clics que terminan en compra (0 % a 100 %).",
                            errorText = errors.affiliateConversionRatePct,
                            testTag = "input_affiliate_conversion",
                            isDecimal = true,
                            suffixText = "%"
                        )
                        ValidatedNumberField(
                            label = "Valor promedio del pedido",
                            value = form.affiliateAvgOrderValueText,
                            onValueChange = { onFieldChanged(CalculatorField.AFFILIATE_AVG_ORDER_VALUE, it) },
                            helperText = "Ticket promedio de compra del producto recomendado.",
                            errorText = errors.affiliateAvgOrderValue,
                            testTag = "input_affiliate_aov",
                            isDecimal = true,
                            prefixText = currency.symbol
                        )
                        ValidatedNumberField(
                            label = "Tasa de comisión (%)",
                            value = form.affiliateCommissionRateText,
                            onValueChange = { onFieldChanged(CalculatorField.AFFILIATE_COMMISSION_RATE, it) },
                            helperText = "Porcentaje de comisión que paga el programa (0 % a 100 %).",
                            errorText = errors.affiliateCommissionRatePct,
                            testTag = "input_affiliate_commission",
                            isDecimal = true,
                            suffixText = "%"
                        )
                    }
                }

                // 5. Own / Digital Products
                item {
                    CalculatorSectionCard(
                        title = "Productos propios o digitales",
                        subtitle = "Plantillas, guías, cursos o mercancía vendida directamente.",
                        icon = Icons.Outlined.ShoppingBag,
                        subtotalText = CurrencyFormatters.formatMoney(breakdown.productRevenue, currency)
                    ) {
                        ValidatedNumberField(
                            label = "Ventas mensuales de productos",
                            value = form.productSalesCountText,
                            onValueChange = { onFieldChanged(CalculatorField.PRODUCT_SALES_COUNT, it) },
                            helperText = "Unidades vendidas estimadas al mes.",
                            errorText = errors.productSalesCount,
                            testTag = "input_product_sales",
                            isDecimal = false
                        )
                        ValidatedNumberField(
                            label = "Valor promedio del pedido",
                            value = form.productAvgOrderValueText,
                            onValueChange = { onFieldChanged(CalculatorField.PRODUCT_AVG_ORDER_VALUE, it) },
                            helperText = "Precio o ticket promedio por venta en ${currency.code}.",
                            errorText = errors.productAvgOrderValue,
                            testTag = "input_product_aov",
                            isDecimal = true,
                            prefixText = currency.symbol
                        )
                    }
                }

                // 6. Expenses & Tax Reserve
                item {
                    CalculatorSectionCard(
                        title = "Gastos y reserva fiscal (Opcional)",
                        subtitle = "Guiones, locución, edición, miniaturas, software y una reserva orientativa para impuestos. No constituye asesoría fiscal.",
                        icon = Icons.Outlined.AccountBalance,
                        subtotalText = "Neto: ${CurrencyFormatters.formatMoney(breakdown.estimatedNetIncome, currency)}"
                    ) {
                        ValidatedNumberField(
                            label = "Gastos mensuales de producción",
                            value = form.monthlyExpensesText,
                            onValueChange = { onFieldChanged(CalculatorField.MONTHLY_EXPENSES, it) },
                            helperText = "Opcional. Se resta del ingreso bruto antes de la reserva fiscal.",
                            errorText = errors.monthlyExpenses,
                            testTag = "input_monthly_expenses",
                            isDecimal = true,
                            prefixText = currency.symbol
                        )
                        ValidatedNumberField(
                            label = "Porcentaje de reserva fiscal (%)",
                            value = form.taxReservePctText,
                            onValueChange = { onFieldChanged(CalculatorField.TAX_RESERVE_PCT, it) },
                            helperText = "Opcional (0 % a 100 %). Se aplica solo si la utilidad antes de impuestos es positiva.",
                            errorText = errors.taxReservePct,
                            testTag = "input_tax_reserve_pct",
                            isDecimal = true,
                            suffixText = "%",
                            imeAction = ImeAction.Done
                        )
                    }
                }

                // Action Buttons
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.activeScenarioId != null) {
                            Button(
                                onClick = onUpdateActiveScenario,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("update_active_scenario_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardar cambios en \"${uiState.activeScenarioName}\"")
                            }
                            FilledTonalButton(
                                onClick = onOpenSaveAsNew,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_as_new_scenario_button")
                            ) {
                                Text("Guardar como escenario nuevo")
                            }
                        } else {
                            Button(
                                onClick = onOpenSaveAsNew,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_new_scenario_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardar escenario")
                            }
                        }

                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_calculator_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restablecer calculadora")
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun LiveCalculatorSummaryBar(
    grossRevenue: Double,
    netIncome: Double,
    currency: DisplayCurrency,
    scenarioName: String?,
    isSample: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        append(scenarioName ?: "Cálculo actual")
                        if (isSample) append(" (Ejemplo)")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Estimación en vivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bruto mensual est.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatters.formatMoney(grossRevenue, currency),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("live_bar_gross")
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Neto mensual est.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatters.formatMoney(netIncome, currency),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (netIncome >= 0.0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("live_bar_net")
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculatorSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    subtotalText: String,
    content: @Composable () -> Unit
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = subtotalText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            content()
        }
    }
}
