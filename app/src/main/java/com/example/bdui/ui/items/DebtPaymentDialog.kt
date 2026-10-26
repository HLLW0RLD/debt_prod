package com.example.bdui.ui.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bdui.R
import com.example.bdui.domain.model.Debt
import com.example.bdui.ui.theme.AppColors

@Composable
fun DebtPaymentDialog(
    debt: Debt,
    color: Color? = null,
    onDismiss: () -> Unit,
    onPayment: (Double, Boolean) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var isDebt by remember { mutableStateOf(false) }

    AlertDialog(
        containerColor = AppColors.background,
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.payment_dialog_title)) },
        text = {
            Column {
                Text("${stringResource(R.string.debtor_label)}: ${debt.name}")
                Text("${stringResource(R.string.current_debt_label)}: ${debt.debtAmount}")

                Spacer(Modifier.height(16.dp))

                TextField(
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.background,
                        unfocusedContainerColor = AppColors.background,
                        focusedLabelColor = color ?: AppColors.textPrimary,
                        unfocusedLabelColor = color ?: AppColors.textSecondary,
                        cursorColor = color ?: AppColors.textPrimary,
                        unfocusedIndicatorColor = color ?: AppColors.accentPrimary,
                        focusedIndicatorColor = color ?: AppColors.accentPrimary,
                    ),
                    value = amount,
                    onValueChange = { amount = it
                        amount = it
                            .trim()
                            .replace("-", "") },
                    label = { Text(stringResource(R.string.amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(Modifier.width(16.dp))
                    CustomRadioButton(
                        selected = !isDebt,
                        onClick = { isDebt = false },
                        color = color,
                        text = stringResource(R.string.payment_loan)
                    )

                    Spacer(Modifier.width(16.dp))
                    CustomRadioButton(
                        selected = isDebt,
                        onClick = { isDebt = true },
                        color = color,
                        text = stringResource(R.string.new_loan)
                    )
                }
            }
        },
        confirmButton = {
            CustomTextButton(
                color = color,
                text = stringResource(R.string.confirm_button),
                enabled = amount.toDoubleOrNull() != null
            ) {
                amount.toDoubleOrNull()?.let {
                    onPayment(it, isDebt)
                    onDismiss()
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel_button),
                    color = AppColors.textPrimary
                )
            }
        }
    )
}