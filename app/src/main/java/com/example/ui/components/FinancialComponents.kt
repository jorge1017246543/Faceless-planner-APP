package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.DisplayCurrency

@Composable
fun EstimateDisclaimerBanner(
    modifier: Modifier = Modifier,
    text: String = "Estimación referencial basada únicamente en tus datos. No representa ingresos reales ni garantizados de YouTube."
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Aviso de estimación",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun CurrencyPillButton(
    currency: DisplayCurrency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("currency_selector_button"),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AttachMoney,
                contentDescription = "Moneda de visualización",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = currency.code,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun CurrencySelectorDialog(
    selectedCurrency: DisplayCurrency,
    onSelectCurrency: (DisplayCurrency) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Moneda de visualización",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Todos los montos ingresados y resultados usarán esta moneda sin realizar conversiones ni aplicar tasas de cambio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                DisplayCurrency.entries.forEach { currency ->
                    val selected = currency == selectedCurrency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clickable(role = Role.RadioButton) {
                                onSelectCurrency(currency)
                                onDismiss()
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .testTag("currency_option_${currency.code}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currency.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                        if (selected) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Seleccionada",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun ValidatedNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    helperText: String,
    errorText: String?,
    testTag: String,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false,
    prefixText: String? = null,
    suffixText: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            label = { Text(label) },
            placeholder = { Text("0") },
            prefix = prefixText?.let { prefix ->
                {
                    Text(
                        text = "$prefix ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            suffix = suffixText?.let { suffix ->
                {
                    Text(
                        text = " $suffix",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            supportingText = {
                Text(
                    text = errorText ?: helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (errorText != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            isError = errorText != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
                imeAction = imeAction
            ),
            shape = MaterialTheme.shapes.small
        )
    }
}

@Composable
fun ResetCalculatorDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Restablecer calculadora?") },
        text = {
            Text(
                "Todos los campos volverán a cero y se desvinculará el escenario actual. Tus escenarios guardados permanecerán intactos."
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_reset_button")
            ) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_reset_button")
            ) {
                Text("Restablecer")
            }
        }
    )
}

@Composable
fun SaveScenarioDialog(
    initialName: String,
    initialNotes: String,
    title: String,
    confirmLabel: String,
    onConfirm: (name: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var notes by rememberSaveable { mutableStateOf(initialNotes) }
    var showNameError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(60)
                        if (it.isNotBlank()) showNameError = false
                    },
                    label = { Text("Nombre del escenario") },
                    placeholder = { Text("Ej. Canal de finanzas (meta Q3)") },
                    isError = showNameError,
                    supportingText = if (showNameError) {
                        { Text("Escribe un nombre para identificar este escenario.") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scenario_name_input")
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(180) },
                    label = { Text("Notas opcionales") },
                    placeholder = { Text("Ej. 8 videos largos al mes y 1 patrocinio") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scenario_notes_input")
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        showNameError = true
                    } else {
                        onConfirm(name.trim(), notes.trim())
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_save_scenario_button")
            ) {
                Text(confirmLabel)
            }
        }
    )
}

@Composable
fun DeleteScenarioDialog(
    scenarioName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar escenario?") },
        text = {
            Text("Se eliminará \"$scenarioName\" de este dispositivo. Esta acción no se puede deshacer.")
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_scenario_button")
            ) {
                Text("Eliminar")
            }
        }
    )
}
