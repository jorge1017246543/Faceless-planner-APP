package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.DisplayCurrency
import com.example.domain.model.RevenueBreakdown
import com.example.domain.model.RevenueSourceShare
import com.example.ui.theme.MonoFontFamily

@Composable
fun RevenueShareChartCard(
    breakdown: RevenueBreakdown,
    currency: DisplayCurrency,
    modifier: Modifier = Modifier
) {
    val activeShares = breakdown.shares.filter { it.amount > 0.0 }
    val chartDescription = if (activeShares.isEmpty()) {
        "Gráfico de distribución de ingresos sin montos registrados."
    } else {
        activeShares.joinToString(", ") {
            "${it.label}: ${CurrencyFormatters.formatPercentage(it.sharePercentage)}"
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Participación por fuente",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Proporción estimada de cada fuente sobre el ingreso bruto mensual.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!breakdown.hasAnyRevenue) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ingresa vistas o ventas en la calculadora para visualizar la distribución de tus fuentes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    RevenueDonutCanvas(
                        shares = activeShares,
                        totalGrossFormatted = CurrencyFormatters.formatMoneyShort(
                            breakdown.totalGrossRevenue,
                            currency
                        ),
                        accessibilitySummary = chartDescription,
                        modifier = Modifier.size(144.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        breakdown.shares.forEach { share ->
                            ShareLegendRow(
                                share = share,
                                currency = currency
                            )
                        }
                    }
                }

                // Horizontal stacked proportion bar for instant visual scanning
                StackedProportionBar(
                    shares = activeShares,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
            }
        }
    }
}

@Composable
private fun RevenueDonutCanvas(
    shares: List<RevenueSourceShare>,
    totalGrossFormatted: String,
    accessibilitySummary: String,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 450),
        label = "donut_sweep"
    )

    Box(
        modifier = modifier.semantics { contentDescription = accessibilitySummary },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 22.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            val totalPct = shares.sumOf { it.sharePercentage }.coerceAtLeast(0.01)
            var currentAngle = -90f
            val gapDegrees = if (shares.size > 1) 2.5f else 0f

            shares.forEach { share ->
                val rawSweep = ((share.sharePercentage / totalPct) * 360.0).toFloat() * animatedProgress
                val sweep = (rawSweep - gapDegrees).coerceAtLeast(1.5f)
                drawArc(
                    color = CurrencyFormatters.colorForSource(share.type),
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                currentAngle += rawSweep
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Bruto",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = totalGrossFormatted,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StackedProportionBar(
    shares: List<RevenueSourceShare>,
    modifier: Modifier = Modifier
) {
    val totalPct = shares.sumOf { it.sharePercentage }.coerceAtLeast(0.01)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        shares.forEach { share ->
            val weight = (share.sharePercentage / totalPct).toFloat().coerceAtLeast(0.02f)
            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(10.dp)
                    .background(CurrencyFormatters.colorForSource(share.type))
            )
        }
    }
}

@Composable
private fun ShareLegendRow(
    share: RevenueSourceShare,
    currency: DisplayCurrency
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(CurrencyFormatters.colorForSource(share.type))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = share.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${CurrencyFormatters.formatPercentage(share.sharePercentage)} · ${
                CurrencyFormatters.formatMoneyShort(share.amount, currency)
            }",
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
