package com.yt_hsgw.taskio.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yt_hsgw.taskio.components.calendar.CalendarGrid
import com.yt_hsgw.taskio.components.calendar.CalendarHeader
import com.yt_hsgw.taskio.components.calendar.CalendarLegend
import com.yt_hsgw.taskio.components.calendar.DailyTaskList
import com.yt_hsgw.taskio.model.CalendarLogResponse
import com.yt_hsgw.taskio.ui.theme.TaskioTheme
import com.yt_hsgw.taskio.viewmodel.CalendarUiState
import com.yt_hsgw.taskio.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth

/**
 * カレンダー画面
 *
 * 月単位のカレンダーと選択日のタスクログを表示します。
 *
 * @param viewModel CalendarViewModel
 * @param modifier Modifier
 */
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // エラーメッセージ表示
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    CalendarScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        selectedDateLogs = viewModel.getLogsForSelectedDate(),
        onDateSelected = { viewModel.selectDate(it) },
        onPreviousMonth = { viewModel.navigateToPreviousMonth() },
        onNextMonth = { viewModel.navigateToNextMonth() },
        onTodayClick = { viewModel.navigateToToday() },
        modifier = modifier
    )
}

/**
 * カレンダー画面のコンテンツ（Stateless）
 *
 * @param uiState UI状態
 * @param snackbarHostState Snackbar状態
 * @param selectedDateLogs 選択日のログ一覧
 * @param onDateSelected 日付選択時のコールバック
 * @param onPreviousMonth 前月ボタンクリック時のコールバック
 * @param onNextMonth 次月ボタンクリック時のコールバック
 * @param onTodayClick 「今日」ボタンクリック時のコールバック
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScreenContent(
    uiState: CalendarUiState,
    snackbarHostState: SnackbarHostState,
    selectedDateLogs: List<CalendarLogResponse>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // カレンダーセクション
                CalendarSection(
                    yearMonth = uiState.currentMonth,
                    selectedDate = uiState.selectedDate,
                    datesWithLogs = uiState.datesWithLogs,
                    onDateSelected = onDateSelected,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onTodayClick = onTodayClick
                )

                // 区切り線
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // タスク一覧セクション
                DailyTaskList(
                    selectedDate = uiState.selectedDate,
                    logs = selectedDateLogs,
                    modifier = Modifier.weight(1f)
                )
            }

            // ローディングインジケーター
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * カレンダーセクション
 *
 * ヘッダー、グリッド、凡例を含むカレンダー表示部分
 */
@Composable
private fun CalendarSection(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    datesWithLogs: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 月ヘッダー
        CalendarHeader(
            yearMonth = yearMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onTodayClick = onTodayClick
        )

        // カレンダーグリッド
        CalendarGrid(
            yearMonth = yearMonth,
            selectedDate = selectedDate,
            datesWithLogs = datesWithLogs,
            onDateSelected = onDateSelected
        )

        // 凡例
        CalendarLegend()
    }
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    TaskioTheme {
        CalendarScreenContent(
            uiState = CalendarUiState(
                currentMonth = YearMonth.of(2026, 1),
                selectedDate = LocalDate.of(2026, 1, 25),
                datesWithLogs = setOf(
                    LocalDate.of(2026, 1, 20),
                    LocalDate.of(2026, 1, 21),
                    LocalDate.of(2026, 1, 23),
                    LocalDate.of(2026, 1, 25)
                ),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            selectedDateLogs = listOf(
                CalendarLogResponse(
                    id = "1",
                    task_id = "task-1",
                    task_title = "ジム",
                    task_description = "24hジムでのトレーニング",
                    memo = "脚トレ中心",
                    created_at = "2026-01-25T18:00:00"
                ),
                CalendarLogResponse(
                    id = "2",
                    task_id = "task-2",
                    task_title = "読書",
                    task_description = null,
                    memo = null,
                    created_at = "2026-01-25T07:00:00"
                )
            ),
            onDateSelected = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onTodayClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenEmptyPreview() {
    TaskioTheme {
        CalendarScreenContent(
            uiState = CalendarUiState(
                currentMonth = YearMonth.now(),
                selectedDate = LocalDate.now(),
                datesWithLogs = emptySet(),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            selectedDateLogs = emptyList(),
            onDateSelected = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onTodayClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenLoadingPreview() {
    TaskioTheme {
        CalendarScreenContent(
            uiState = CalendarUiState(
                isLoading = true
            ),
            snackbarHostState = SnackbarHostState(),
            selectedDateLogs = emptyList(),
            onDateSelected = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onTodayClick = {}
        )
    }
}