package com.example.debt.app.ui.items

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.debt.R
import com.example.debt.data.model.Debt
import com.example.debt.data.model.TransactionType
import com.example.debt.ui.theme.AppColors
import com.example.debt.utils.PreferenceCache
import com.example.debt.ui.theme.ThemeMode
import com.example.debt.ui.theme.cardColorById
import com.example.debt.utils.getTransactionName
import com.example.debt.utils.openTelegramChat
import com.example.debt.utils.setColorDate

const val PLUS = "+"
const val MINUS = "-"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DebtorCard(
    isMine: Boolean = false,
    debt: Debt,
    onPaymentClick: (Debt) -> Unit,
    onEditClick: (Debt) -> Unit,
    onDeleteDebtorClick: (Debt) -> Unit
) {

    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    var showHistory by remember { mutableStateOf(false) }
    val cardColor = remember(debt.id) {
        cardColorById(debt.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onEditClick(debt)
                    },
                    onTap = { /* Обычный клик -- TODO  */ },
                    onDoubleTap = { /* Двойной клик -- TODO для открытия тг */ },
                    onPress = { /* Начало нажатия (еще не отпустили палец) -- TODO просмотр инф. / оплата */ },
                )
            },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (PreferenceCache.selectedTheme == ThemeMode.COLOR) {
                cardColor
            } else {
                AppColors.surface
            }
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.background
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.background)
                    .padding(start = 8.dp, end = 4.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        contentDescription = "",
                        painter = if (isMine) painterResource(R.drawable.ic_wallet_up) else painterResource(R.drawable.ic_wallet_down),
                        tint = if (isMine) AppColors.error else AppColors.success
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = debt.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                }
                Icon(
                    contentDescription = "",
                    painter = painterResource(R.drawable.ic_delete_outline),
                    tint = AppColors.textPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                        .clickable(
                            indication = null,
                            interactionSource = null
                        ) { onDeleteDebtorClick(debt) }
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp, bottom = 8.dp)
                    .clickable(
                        indication = null,
                        interactionSource = null
                    ) { showHistory = !showHistory },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.debt_label, debt.debtAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )

                Spacer(modifier = Modifier.size(8.dp))

                Icon(
                    contentDescription = "",
                    painter = painterResource(if (showHistory) R.drawable.ic_double_arrow_down else R.drawable.ic_double_arrow_up),
                    tint = AppColors.textPrimary
                )
            }

            AnimatedVisibility(visible = showHistory) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .padding(8.dp),
                ) {
                    items(debt.transactions.size) { transactionInd ->
                        val transaction = debt.transactions[debt.transactions.size - 1 - transactionInd]
                        val operator = if (transaction.type == TransactionType.PAYMENT) PLUS else MINUS
                        val color = if (transaction.type == TransactionType.PAYMENT) AppColors.success else AppColors.error
                        var trueOperator = ""
                        var trueColor = Color.White

                        if (isMine) {
                            trueOperator = if (operator == PLUS) MINUS else PLUS
                            trueColor = if (color == AppColors.error) AppColors.success else AppColors.error
                        } else {
                            trueOperator = operator
                            trueColor = color
                        }

                        Text(

                            text = "${transaction.type.getTransactionName(context)} - ${transaction.date}",

                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = "$trueOperator ${transaction.amount} ₽",
                            style = MaterialTheme.typography.titleLarge,
                            color = trueColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Divider(Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {

            Text(
                text = stringResource(R.string.loan_date_label, debt.loanDate),
                style = MaterialTheme.typography.bodyMedium
            )

            if (debt.returnDate?.setColorDate() != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.return_date_label, debt.returnDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = debt.returnDate.setColorDate() ?: Color.White
                )
            }

            if (!debt.comment.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.comment_label, debt.comment ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            shape = RoundedCornerShape(50.dp),
                            color = AppColors.background
                        )
                        .clickable(
                            indication = null,
                            interactionSource = null
                        ) { onPaymentClick(debt) },
                    contentAlignment = Alignment.Center
                ) {
                    val color = if (PreferenceCache.selectedTheme == ThemeMode.COLOR) cardColor else AppColors.accentPrimary
                    Image(
                        contentDescription = "",
                        painter = painterResource(R.drawable.ic_pay),
                        colorFilter = ColorFilter.tint(color),
                        modifier = Modifier
                            .size(24.dp)
                    )
                }

                if (!debt.telegramNick.isNullOrEmpty()) {
                    Image(
                        contentDescription = "",
                        painter = painterResource(R.drawable.ic_telegram),
                        colorFilter = ColorFilter
                            .tint(if (PreferenceCache.selectedTheme == ThemeMode.COLOR) {
                                cardColor
                            } else {
                                AppColors.surface
                            }),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                shape = RoundedCornerShape(50.dp),
                                color = AppColors.background
                            )
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ) { debt.telegramNick.openTelegramChat() }
                    )
                }
            }
        }
    }
}