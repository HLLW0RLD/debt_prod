package com.example.debt.app.ui.items

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debt.R
import com.example.debt.domain.model.Debt
import com.example.debt.ui.items.CustomDateTimePickerField
import com.example.debt.ui.items.CustomOutlinedTextField
import com.example.debt.ui.items.CustomRadioButton
import com.example.debt.ui.items.CustomTextButton
import com.example.debt.ui.theme.AppColors

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DebtorForm(
    debt: Debt? = null,
    isMine: Boolean = false,
    color: Color = AppColors.accentPrimary,
    onSaveComplete: (Debt) -> Unit
) {

    val isEdit = debt != null

    val name = remember { mutableStateOf(debt?.name ?: "") }
    val telegramNick = remember { mutableStateOf(debt?.telegramNick ?: "") }
    val debtAmount = remember { mutableStateOf(debt?.debtAmount?.toString() ?: "") }
    val returnDate = remember { mutableStateOf(debt?.returnDate ?: "") }
    val comment = remember { mutableStateOf(debt?.comment ?: "") }
    val isMineState = remember { mutableStateOf(debt?.isMine ?: isMine) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.background)
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .background(AppColors.background)
                .fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.debtor_form_title),
                fontSize = 24.sp,
                color = AppColors.textPrimary,
            )
//            IconButton(
//                onClick = {}
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.gear),
//                    tint = AppColors.textPrimary,
//                    contentDescription = "",
//                )
//            }
        }
        Spacer(modifier = Modifier.height(8.dp))


        CustomOutlinedTextField(
            cursorColor = color,
            focusedLabelColor = color,
            focusedBorderColor = color,
            unfocusedBorderColor = color,
            focusedPlaceholderColor = color,
            shape = RoundedCornerShape(12.dp),
            value = name.value,
            onValueChange = { name.value = it },
            label = stringResource(R.string.label_name_debt),
            maxCharacters = 20,
            showCharacterCounter = true,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            CustomRadioButton(
                selected = !isMineState.value,
                onClick = { isMineState.value = false },
                color = color,
                text = stringResource(R.string.radio_owe_to_me)
            )

            CustomRadioButton(
                selected = isMineState.value,
                onClick = { isMineState.value = true },
                color = color,
                text = stringResource(R.string.radio_i_owe)
            )
        }

        CustomOutlinedTextField(
            cursorColor = color,
            focusedLabelColor = color,
            focusedBorderColor = color,
            unfocusedBorderColor = color,
            focusedPlaceholderColor = color,
            shape = RoundedCornerShape(12.dp),
            value = telegramNick.value,
            onValueChange = { telegramNick.value = it },
            label = stringResource(R.string.label_telegram_nick),
            maxCharacters = 20,
            showCharacterCounter = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomOutlinedTextField(
            cursorColor = color,
            focusedLabelColor = color,
            focusedBorderColor = color,
            unfocusedBorderColor = color,
            focusedPlaceholderColor = color,
            shape = RoundedCornerShape(12.dp),
            value = debtAmount.value,
            enabled = debt == null,
            onValueChange = {
                debtAmount.value = it
                    .trim()
                    .replace("-", "")
            },
            label = stringResource(R.string.label_debt_amount),
            keyboardType = KeyboardType.Number,
            maxCharacters = 20,
            showCharacterCounter = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomDateTimePickerField(
            label = stringResource(R.string.label_return_date),
            date = returnDate.value,
            iconColor = color,
            focusedLabelColor = color,
            focusedBorderColor = color,
            unfocusedBorderColor = color,
            focusedPlaceholderColor = color,
            shape = RoundedCornerShape(12.dp),
            onDateChanged = { returnDate.value = it },
            modifier = Modifier
                .fillMaxWidth()
        )

        CustomOutlinedTextField(
            cursorColor = color,
            focusedLabelColor = color,
            focusedBorderColor = color,
            unfocusedBorderColor = color,
            focusedPlaceholderColor = color,
            shape = RoundedCornerShape(12.dp),
            value = comment.value,
            onValueChange = { comment.value = it },
            label = stringResource(R.string.label_comment_optional),
            maxCharacters = 100,
            showCharacterCounter = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextButton(
            color = color,
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.save_button),
            enabled = name.value.isNotBlank() && debtAmount.value.toDoubleOrNull() != null,
        ) {
            if (isEdit) {
                val debt = Debt(
                    id = debt.id,
                    isMine = isMineState.value,
                    telegramNick = telegramNick.value.ifEmpty { debt.telegramNick },
                    name = name.value.ifEmpty { debt.name },
                    debtAmount = debt.debtAmount,
                    returnDate = returnDate.value.ifEmpty { debt.returnDate },
                    comment = comment.value.ifEmpty { debt.comment }
                )
                onSaveComplete(debt)
            } else {
                val amount = debtAmount.value.toDoubleOrNull() ?: 0.0
                val debt = Debt(
                    telegramNick = telegramNick.value,
                    isMine = isMineState.value,
                    name = name.value,
                    debtAmount = amount,
                    returnDate = returnDate.value,
                    comment = comment.value
                )
                onSaveComplete(debt)
            }
        }
    }
}