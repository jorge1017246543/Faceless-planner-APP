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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MonoFontFamily

@Composable
fun MethodologyScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("methodology_screen"),
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
                MethodologyCard(
                    title = "Fórmulas utilizadas",
                    icon = Icons.Outlined.Calculate
                ) {
                    FormulaItem(
                        title = "Anuncios en videos largos",
                        formula = "(Vistas mensuales largos ÷ 1,000) × RPM largos"
                    )
                    FormulaItem(
                        title = "Anuncios en YouTube Shorts",
                        formula = "(Vistas mensuales Shorts ÷ 1,000) × RPM Shorts"
                    )
                    FormulaItem(
                        title = "Patrocinios directos",
                        formula = "Cantidad de acuerdos × Tarifa promedio por acuerdo"
                    )
                    FormulaItem(
                        title = "Marketing de afiliados",
                        formula = "Clics × (Tasa conversión % ÷ 100) × Valor pedido × (Comisión % ÷ 100)"
                    )
                    FormulaItem(
                        title = "Productos propios o digitales",
                        formula = "Ventas mensuales × Valor promedio del pedido"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    FormulaItem(
                        title = "Ingreso bruto mensual total",
                        formula = "Anuncios largos + Shorts + Patrocinios + Afiliados + Productos"
                    )
                    FormulaItem(
                        title = "Utilidad antes de impuestos",
                        formula = "Ingreso bruto mensual − Gastos mensuales de producción"
                    )
                    FormulaItem(
                        title = "Reserva fiscal estimada",
                        formula = "máx(Utilidad antes de impuestos, 0) × (Reserva fiscal % ÷ 100)"
                    )
                    FormulaItem(
                        title = "Ingreso neto mensual estimado",
                        formula = "Utilidad antes de impuestos − Reserva fiscal estimada"
                    )
                    FormulaItem(
                        title = "Proyección anual estimada",
                        formula = "Ingreso mensual estimado × 12 meses"
                    )
                }
            }

            item {
                MethodologyCard(
                    title = "Diferencia entre RPM y CPM",
                    icon = Icons.Outlined.TrendingUp
                ) {
                    Text(
                        text = "Esta herramienta trata el RPM directamente como el ingreso neto que recibe el creador por cada 1,000 vistas totales del video o Short. Por ello, no se aplica ningún porcentaje adicional de vistas monetizadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "RPM (Ingresos por cada mil vistas)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Refleja cuánto gana el creador por cada 1,000 vistas totales (incluyendo vistas sin anuncios) después del reparto de ingresos con YouTube.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "CPM (Costo por cada mil impresiones)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Es el precio bruto que pagan los anunciantes por cada 1,000 impresiones de anuncio antes del reparto de la plataforma y antes de considerar vistas no monetizadas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                MethodologyCard(
                    title = "Factores que hacen variar el RPM",
                    icon = Icons.Outlined.Info
                ) {
                    Text(
                        text = "El RPM no es una cifra fija y puede cambiar mes a mes según múltiples variables:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BulletPoint("Ubicación geográfica e idioma de la audiencia principal.")
                    BulletPoint("Temática o nicho del canal y demanda de anunciantes.")
                    BulletPoint("Estacionalidad y meses de mayor o menor inversión publicitaria.")
                    BulletPoint("Formato del contenido (videos largos, duración y pausas publicitarias frente a Shorts).")
                    BulletPoint("Elegibilidad para monetización, cumplimiento de políticas y bloqueadores de anuncios.")
                }
            }

            item {
                MethodologyCard(
                    title = "Alcance de las estimaciones y reserva fiscal",
                    icon = Icons.Outlined.Balance
                ) {
                    Text(
                        text = "Todos los montos mostrados en la aplicación son estimaciones matemáticas basadas exclusivamente en los valores que tú ingresas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BulletPoint("Los ingresos reales en YouTube Studio pueden variar significativamente.")
                    BulletPoint("Los valores del escenario de ejemplo sirven únicamente como referencia ilustrativa para explorar la herramienta; no representan promedios ni promesas de la industria.")
                    BulletPoint("La reserva fiscal es una estimación personal de planificación financiera. No constituye asesoría fiscal, contable ni legal; consulta a un profesional tributario en tu país.")
                    BulletPoint("Cambiar de moneda ajusta únicamente el formato visual. No convierte divisas ni consulta tasas de cambio.")
                }
            }

            item {
                MethodologyCard(
                    title = "Privacidad y datos locales",
                    icon = Icons.Outlined.Lock
                ) {
                    Text(
                        text = "La aplicación funciona 100 % en tu dispositivo. Tus escenarios, métricas y preferencia de moneda se guardan localmente sin cuentas, sin conexión con la API de YouTube, sin nube y sin rastreo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MethodologyCard(
    title: String,
    icon: ImageVector,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun FormulaItem(
    title: String,
    formula: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formula,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
