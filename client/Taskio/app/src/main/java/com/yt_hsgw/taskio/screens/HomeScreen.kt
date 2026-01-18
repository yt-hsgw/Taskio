package com.yt_hsgw.taskio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yt_hsgw.taskio.components.CreateTaskDialog
import com.yt_hsgw.taskio.components.TaskCard
import com.yt_hsgw.taskio.components.WeekCalendar
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioColors
import com.yt_hsgw.taskio.ui.theme.TaskioDimens
import com.yt_hsgw.taskio.ui.theme.TaskioTypography
import com.yt_hsgw.taskio.viewmodel.TaskViewModel
import com.yt_hsgw.taskio.viewmodel.TaskWithDayState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ホーム画面
 *
 * アプリのメイン画面。週間カレンダー、日付ヘッダー、タスク一覧を表示
 *
 * @param viewModel タスク管理用ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // エラーメッセージの表示
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // タスク作成完了時の処理
    LaunchedEffect(uiState.taskCreated) {
        if (uiState.taskCreated) {
            showCreateDialog = false
            viewModel.resetTaskCreated()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreateTaskFab(onClick = { showCreateDialog = true })
        }
    ) { innerPadding ->
        HomeContent(
            modifier = Modifier.padding(innerPadding),
            calendarDates = uiState.calendarDates,
            selectedDate = uiState.selectedDate,
            filteredTasks = filteredTasks,
            onDateSelected = viewModel::onDateSelected,
            onStartClick = viewModel::toggleStart,
            onFinishClick = viewModel::toggleFinish
        )

        // タスク作成ダイアログ
        if (showCreateDialog) {
            CreateTaskDialog(
                title = uiState.title,
                description = uiState.description,
                scheduledDate = uiState.scheduledDate?.toLocalDate(),
                isRecurring = uiState.isRecurring,
                selectedDays = uiState.selectedDays,
                loading = uiState.loading,
                onTitleChange = viewModel::updateTitle,
                onDescriptionChange = viewModel::updateDescription,
                onScheduledDateChange = { date ->
                    viewModel.updateScheduledDate(date?.atStartOfDay())
                },
                onRecurringToggle = viewModel::toggleRecurring,
                onDayToggle = viewModel::toggleDay,
                onDismiss = {
                    showCreateDialog = false
                    viewModel.clearDialogState()
                },
                onCreateTask = {
                    scope.launch { viewModel.createTask() }
                }
            )
        }
    }
}

/**
 * ホーム画面のメインコンテンツ
 */
@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    calendarDates: List<LocalDate>,
    selectedDate: LocalDate,
    filteredTasks: List<TaskWithDayState>,
    onDateSelected: (LocalDate) -> Unit,
    onStartClick: (Long) -> Unit,
    onFinishClick: (Long) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaskioColors.Background)
    ) {
        WeekCalendar(
            dates = calendarDates,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected
        )

        DateHeader(selectedDate = selectedDate)

        TaskList(
            tasks = filteredTasks,
            onStartClick = onStartClick,
            onFinishClick = onFinishClick
        )
    }
}

/**
 * タスク作成用FAB
 */
@Composable
private fun CreateTaskFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = TaskioColors.Primary,
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = TaskioStrings.TASK_ADD
        )
    }
}

/**
 * 日付ヘッダー
 *
 * 選択された日付を表示
 */
@Composable
private fun DateHeader(selectedDate: LocalDate) {
    val today = LocalDate.now()
    val displayText = if (selectedDate == today) {
        TaskioStrings.TODAY
    } else {
        selectedDate.format(DATE_HEADER_FORMATTER)
    }

    Text(
        text = displayText,
        modifier = Modifier.padding(
            horizontal = TaskioDimens.PaddingLarge,
            vertical = TaskioDimens.PaddingSmall
        ),
        fontSize = TaskioTypography.FontSizeXLarge,
        fontWeight = FontWeight.Bold,
        color = TaskioColors.TextPrimary
    )
}

/**
 * タスク一覧
 */
@Composable
private fun TaskList(
    tasks: List<TaskWithDayState>,
    onStartClick: (Long) -> Unit,
    onFinishClick: (Long) -> Unit
) {
    if (tasks.isEmpty()) {
        EmptyTasksView()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TaskioDimens.PaddingLarge),
            verticalArrangement = Arrangement.spacedBy(TaskioDimens.PaddingMedium),
            contentPadding = PaddingValues(bottom = FAB_BOTTOM_PADDING)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onStartClick = { onStartClick(task.id) },
                    onFinishClick = { onFinishClick(task.id) }
                )
            }
        }
    }
}

/**
 * タスクが空の時の表示
 */
@Composable
private fun EmptyTasksView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = TaskioStrings.EMPTY_TASKS_MESSAGE,
            fontSize = TaskioTypography.FontSizeDefault,
            color = TaskioColors.TextSecondary
        )
    }
}

// Constants
private val DATE_HEADER_FORMATTER = DateTimeFormatter.ofPattern(TaskioStrings.DATE_FORMAT_MONTH_DAY)
private val FAB_BOTTOM_PADDING = TaskioDimens.PaddingLarge * 5 // 80.dp
