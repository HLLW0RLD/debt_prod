package com.example.debt.app.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debt.R
import com.example.debt.domain.Debt
import com.example.debt.app.ui.items.DebtorCard
import com.example.debt.app.ui.items.DebtorForm
import com.example.debt.ui.utils.LogUtils.debugLog
import com.example.debt.domain.utils.validateTelegram
import com.example.debt.ui.items.AnimatedFloatingActionButton
import com.example.debt.ui.items.FilterDropdown
import com.example.debt.ui.items.DebtPaymentDialog
import com.example.debt.ui.items.CustomDialog
import com.example.debt.ui.screens.main.DebtUiState
import com.example.debt.ui.screens.main.DebtorsFeedViewModel
import com.example.debt.ui.screens.main.FilterType
import com.example.debt.ui.screens.settings.Settings
import com.example.debt.ui.theme.AppColors
import com.example.debt.data.local.PreferenceCache
import com.example.debt.ui.theme.interfaceColorById
import com.example.debt.ui.utils.LocalNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object DebtorsFeed

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtorsFeedScreen(
    debtorsFeedViewModel: DebtorsFeedViewModel = koinViewModel()
) {
    val navController = LocalNavController.current

    val debtors by debtorsFeedViewModel.debtors.collectAsState()
    val refresh by debtorsFeedViewModel.refresh.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedDebt by remember { mutableStateOf<Debt?>(null) }

    var editedDebt by remember { mutableStateOf<Debt?>(null) }

    var isMineDebtsState by remember { mutableStateOf(false) }

    var editedDebtorBGcolor by remember { mutableStateOf<Color?>(null) }

    var currentFilter  by remember { mutableStateOf(FilterType.ACTIVE) }

    val tabs = listOf(
        stringResource(R.string.tab_all),
        stringResource(R.string.tab_owe_me),
        stringResource(R.string.tab_i_owe)
    )
    var selectedTab by remember { mutableIntStateOf(0) }

    var showThemeDialog by remember { mutableStateOf(false) }

    val scrollThreshold = 56f
    var accumulatedScroll by remember { mutableFloatStateOf(0f) }

    var extended by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.Drag) return Offset.Zero
                accumulatedScroll += available.y

                when {
                    accumulatedScroll > scrollThreshold &&
                            !extended -> {
                        extended = true
                        accumulatedScroll = 0f
                    }

                    accumulatedScroll < -scrollThreshold &&
                            extended -> {
                        extended = false
                        accumulatedScroll = 0f
                    }
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                accumulatedScroll = 0f
                return Velocity.Zero
            }
        }
    }

    val pagerState = rememberPagerState(
        initialPage = selectedTab,
        pageCount = { tabs.size }
    )

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
        isMineDebtsState = selectedTab == 2
    }

    PullToRefreshBox(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize()
            .background(AppColors.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
        isRefreshing = refresh,
        onRefresh = {
            debtorsFeedViewModel.loadAllDebts()
        },
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .background(AppColors.background),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .background(AppColors.background)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.debt_title),
                    fontSize = 24.sp,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    val all = stringResource(R.string.tab_all)
                    val active = stringResource(R.string.active)
                    val paid = stringResource(R.string.paid)

                    FilterDropdown(
                        currentFilter = currentFilter,
                        filterOptions = listOf(FilterType.ALL, FilterType.ACTIVE, FilterType.PAID),
                        onFilterSelected = { newFilter ->
                            currentFilter = newFilter
                        },
                        getFilterText = { filter ->
                            when (filter) {
                                FilterType.ALL -> all
                                FilterType.ACTIVE -> active
                                FilterType.PAID -> paid
                            }
                        }
                    )

                    IconButton(onClick = {
                        navController.navigate(Settings)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.gear),
                            tint = AppColors.textPrimary,
                            contentDescription = "",
                        )
                    }
                }
            }

            Divider(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppColors.textPrimary)
            )

            when (val state = debtors) {

                is DebtUiState.Success -> {

                    AnimatedVisibility(
                        visible = extended,
                    ) {
                        TabRow(
                            modifier = Modifier.padding(8.dp),
                            selectedTabIndex = selectedTab,
                            indicator = {},
                            divider = {},
                            containerColor = AppColors.background,
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedTab == index

                                Box(
                                    modifier = Modifier
                                        .background(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) {
                                                AppColors.textPrimary
                                            } else {
                                                AppColors.background
                                            }
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            scope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) {
                                            AppColors.background
                                        } else {
                                            AppColors.textPrimary
                                        },
                                    )
                                }
                            }
                        }
                    }

                    val data = state.debtors

                    if (data.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = null
                                ) {
                                    showBottomSheet = true
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                modifier = Modifier
                                    .size(200.dp),
                                painter = painterResource(R.drawable.ic_trade),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(AppColors.accentPrimary)
                            )
                            Text(
                                color = AppColors.textPrimary,
                                text = stringResource(R.string.empty_debts),
                                fontSize = 24.sp,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                            )
                        }
                    } else {

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->

                            val filteredDebtors = when (page) {
                                1 -> data.filter { !it.isMine }
                                2 -> data.filter { it.isMine }
                                else -> data
                            }.filter { debtor ->
                                when (currentFilter) {
                                    FilterType.ACTIVE -> debtor.debtAmount > 0
                                    FilterType.PAID -> debtor.debtAmount == 0.0
                                    FilterType.ALL -> true
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .background(AppColors.background)
                                    .fillMaxSize()
                                    .padding(6.dp),
                            ) {
                                item { Spacer(Modifier.size(12.dp)) }

                                items(filteredDebtors.size) { index ->
                                    val debtor = filteredDebtors[filteredDebtors.size - 1 - index]

                                    DebtorCard(
                                        isMine = debtor.isMine,
                                        debt = debtor,
                                        onDeleteDebtorClick = {
                                            selectedDebt = it
                                            showDeleteDialog = true
                                        },
                                        onPaymentClick = { debtor ->
                                            editedDebtorBGcolor = if (PreferenceCache.isColoredTheme)
                                                interfaceColorById(debtor.id)
                                            else null
                                            selectedDebt = debtor
                                            showPaymentDialog = true
                                        },
                                        onEditClick = { debtor ->
                                            editedDebtorBGcolor = if (PreferenceCache.isColoredTheme)
                                                interfaceColorById(debtor.id)
                                            else null
                                            editedDebt = debtor
                                            isMineDebtsState = debtor.isMine
                                            showBottomSheet = true

                                            debugLog(debtor.isMine)
                                        }
                                    )
                                }
                                item { Spacer(Modifier.size(52.dp)) }
                            }
                        }
                    }
                }

                is DebtUiState.Loading -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()

                        Text(
                            color = AppColors.textPrimary,
                            text = stringResource(R.string.loading_debts),
                            fontSize = 24.sp,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                        )
                    }
                }

                is DebtUiState.Error -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ) {
                                debtorsFeedViewModel.loadAllDebts(false)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            modifier = Modifier
                                .size(250.dp),
                            painter = painterResource(R.drawable.ic_money),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(AppColors.accentPrimary)
                        )
                        Text(
                            color = AppColors.textPrimary,
                            text = stringResource(R.string.error_loading),
                            fontSize = 24.sp,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                        )
                    }
                }
            }
        }

        AnimatedFloatingActionButton(
            modifier = Modifier
                .padding(bottom = 30.dp, end = 16.dp)
                .align(Alignment.BottomEnd),
//            visible = fabVisible && extended,
            visible = extended,
            onClick = {
                isMineDebtsState = selectedTab == 2
                editedDebt = null
                showBottomSheet = true
            },
            icon = painterResource(R.drawable.cross_add),
            enterDelay = 200
        )

        if (showPaymentDialog) {
            selectedDebt?.let { debtor ->
                DebtPaymentDialog(
                    debt = debtor,
                    color = editedDebtorBGcolor,
                    onDismiss = {
                        showPaymentDialog = false
                        editedDebtorBGcolor = null
                    },
                    onPayment = { amount, isDebt ->
                        debtor.let { debtor ->
                            if (isDebt) {
                                debtorsFeedViewModel.addDebt(debtor.id, amount)
                            } else {
                                debtorsFeedViewModel.payDebt(debtor.id, amount)
                            }
                        }
                    }
                )
            }
        }

        if (showDeleteDialog) {
            CustomDialog(
                onDismiss = { showDeleteDialog = false },
                onCancel = { showDeleteDialog = false },
                onConfirm = {
                    debtorsFeedViewModel.deleteDebtor(selectedDebt!!.id)
                    showDeleteDialog = false
                }
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    editedDebt = null
                    editedDebtorBGcolor = null
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = AppColors.background,
            ) {
                DebtorForm(
                    debt = editedDebt,
                    isMine = isMineDebtsState,
                    color = editedDebtorBGcolor ?: AppColors.accentPrimary,
                    onSaveComplete = { newDebt ->

                        if (editedDebt != null) {
                            debtorsFeedViewModel.updateDebt(
                                id = newDebt.id,
                                name = newDebt.name,
                                isMine = newDebt.isMine,
                                telegramNick = newDebt.telegramNick,
                                debtAmount = newDebt.debtAmount,
                                returnDate = newDebt.returnDate,
                                comment = newDebt.comment,
                            )
                            showBottomSheet = false
                        } else {
                            if (newDebt.telegramNick.isNullOrEmpty()) {
                                debtorsFeedViewModel.createDebt(
                                    name = newDebt.name,
                                    isMine = newDebt.isMine,
                                    telegramNick = newDebt.telegramNick,
                                    debtAmount = newDebt.debtAmount,
                                    returnDate = newDebt.returnDate,
                                    comment = newDebt.comment,
                                )
                                showBottomSheet = false
                            } else {
                                if (newDebt.telegramNick.validateTelegram()) {
                                    debtorsFeedViewModel.createDebt(
                                        name = newDebt.name,
                                        isMine = newDebt.isMine,
                                        telegramNick = newDebt.telegramNick,
                                        debtAmount = newDebt.debtAmount,
                                        returnDate = newDebt.returnDate,
                                        comment = newDebt.comment,
                                    )
                                    showBottomSheet = false
                                } else {
                                    debtorsFeedViewModel. showError("nickname  not valid")
                                }
                            }
                        }
                        editedDebt = null
                    }
                )
            }
        }

    }
}

fun nextFilter(currentFilter: FilterType): FilterType {
    return when (currentFilter) {
        FilterType.ALL -> FilterType.ACTIVE
        FilterType.ACTIVE -> FilterType.PAID
        FilterType.PAID -> FilterType.ALL
    }
}