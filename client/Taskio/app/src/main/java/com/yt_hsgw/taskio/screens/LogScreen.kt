package com.yt_hsgw.taskio.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yt_hsgw.taskio.components.LogTaskCard
import com.yt_hsgw.taskio.components.TaskEditDialog
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioTheme
import com.yt_hsgw.taskio.viewmodel.LogUiState
import com.yt_hsgw.taskio.viewmodel.LogViewModel
import java.time.LocalDate

/**
 * ログ画面
 *
 * 登録されているタスクの一覧と週間進捗を表示します。
 * タスクの編集も可能です。
 *
 * @param viewModel LogViewModel
 * @param modifier Modifier
 */
@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = viewModel()
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

    // タスク更新完了通知
    LaunchedEffect(uiState.taskUpdated) {
        if (uiState.taskUpdated) {
            snackbarHostState.showSnackbar(TaskioStrings.SUCCESS_TASK_UPDATED)
            viewModel.resetTaskUpdated()
        }
    }

    LogScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEditClick = { task -> viewModel.openEditDialog(task) },
        onEditTitleChange = { viewModel.updateEditTitle(it) },
        onEditDescriptionChange = { viewModel.updateEditDescription(it) },
        onEditScheduledDateChange = { viewModel.updateEditScheduledDate(it) },
        onEditRecurringChange = { viewModel.toggleEditRecurring(it) },
        onEditDayToggle = { viewModel.toggleEditDay(it) },
        onEditSave = { viewModel.updateTask() },
        onEditDismiss = { viewModel.closeEditDialog() },
        modifier = modifier
    )
}

/**
 * ログ画面のコンテンツ（Stateless）
 */
@Composable
private fun LogScreenContent(
    uiState: LogUiState,
    snackbarHostState: SnackbarHostState,
    onEditClick: (TaskItem) -> Unit,
    onEditTitleChange: (String) -> Unit,
    onEditDescriptionChange: (String) -> Unit,
    onEditScheduledDateChange: (LocalDate?) -> Unit,
    onEditRecurringChange: (Boolean) -> Unit,
    onEditDayToggle: (Int) -> Unit,
    onEditSave: () -> Unit,
    onEditDismiss: () -> Unit,
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
            when {
                uiState.loading -> {
                    // ローディング表示
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.tasks.isEmpty() -> {
                    // 空状態表示
                    EmptyLogContent(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    // タスク一覧表示
                    TaskLogList(
                        tasks = uiState.tasks,
                        weeklyProgress = uiState.weeklyProgress,
                        onEditClick = onEditClick
                    )
                }
            }
        }

        // 編集ダイアログ
        if (uiState.showEditDialog) {
            TaskEditDialog(
                title = uiState.editTitle,
                description = uiState.editDescription,
                scheduledDate = uiState.editScheduledDate,
                isRecurring = uiState.editIsRecurring,
                selectedDays = uiState.editSelectedDays,
                loading = uiState.updating,
                onTitleChange = onEditTitleChange,
                onDescriptionChange = onEditDescriptionChange,
                onScheduledDateChange = onEditScheduledDateChange,
                onRecurringChange = onEditRecurringChange,
                onDayToggle = onEditDayToggle,
                onSave = onEditSave,
                onDismiss = onEditDismiss
            )
        }
    }
}

/**
 * タスクログリスト
 */
@Composable
private fun TaskLogList(
    tasks: List<TaskItem>,
    weeklyProgress: Map<String, Map<Int, Boolean>>,
    onEditClick: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(ListPadding),
        verticalArrangement = Arrangement.spacedBy(ListItemSpacing)
    ) {
        items(
            items = tasks,
            key = { it.id }
        ) { task ->
            LogTaskCard(
                task = task,
                weeklyProgress = weeklyProgress[task.id] ?: emptyMap(),
                onEditClick = { onEditClick(task) }
            )
        }
    }
}

/**
 * 空状態コンテンツ
 */
@Composable
private fun EmptyLogContent(
    modifier: Modifier = Modifier
) {
    Text(
        text = TaskioStrings.EMPTY_LOG_MESSAGE,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(EmptyContentPadding)
    )
}

// ─────────────────────────────
// 定数
// ─────────────────────────────

private val ListPadding = 16.dp
private val ListItemSpacing = 12.dp
private val EmptyContentPadding = 32.dp

// ─────────────────────────────
// プレビュー
// ─────────────────────────────

@Preview(showBackground = true)
@Composable
private fun LogScreenContentPreview() {
    val mockTasks = listOf(
        TaskItem(
            id = "1",
            title = "ジム",
            description = "24hジムでのトレーニング",
            createdAt = "2025-01-24",
            repeatDays = listOf(1, 3, 5),
            isRecurring = true
        ),
        TaskItem(
            id = "2",
            title = "ランニング",
            description = "朝のジョギング 5km",
            createdAt = "2025-01-24",
            repeatDays = listOf(2, 4),
            isRecurring = true
        ),
        TaskItem(
            id = "3",
            title = "キックボクシング",
            description = "ジムでのキックボクシングレッスン",
            createdAt = "2025-01-24",
            repeatDays = listOf(6),
            isRecurring = true
        ),
        TaskItem(
            id = "4",
            title = "資格勉強",
            description = "AWS認定資格の勉強。毎日1時間は確保する。\n" +
                "ソリューションアーキテクトアソシエイトを目標に。\n" +
                "公式ドキュメントとUdemyの講座を活用。",
            createdAt = "2025-01-24",
            repeatDays = listOf(0, 1, 2, 3, 4, 5, 6),
            isRecurring = true
        )
    )

    val mockProgress = mapOf(
        "1" to mapOf(0 to false, 1 to true, 2 to false, 3 to true, 4 to false, 5 to false, 6 to false),
        "2" to mapOf(0 to false, 1 to false, 2 to true, 3 to false, 4 to true, 5 to false, 6 to false),
        "3" to mapOf(0 to false, 1 to false, 2 to false, 3 to false, 4 to false, 5 to false, 6 to true),
        "4" to mapOf(0 to true, 1 to true, 2 to false, 3 to true, 4 to false, 5 to true, 6 to false)
    )

    TaskioTheme {
        LogScreenContent(
            uiState = LogUiState(
                tasks = mockTasks,
                weeklyProgress = mockProgress,
                loading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onEditClick = {},
            onEditTitleChange = {},
            onEditDescriptionChange = {},
            onEditScheduledDateChange = {},
            onEditRecurringChange = {},
            onEditDayToggle = {},
            onEditSave = {},
            onEditDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogScreenEmptyPreview() {
    TaskioTheme {
        LogScreenContent(
            uiState = LogUiState(
                tasks = emptyList(),
                loading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onEditClick = {},
            onEditTitleChange = {},
            onEditDescriptionChange = {},
            onEditScheduledDateChange = {},
            onEditRecurringChange = {},
            onEditDayToggle = {},
            onEditSave = {},
            onEditDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogScreenLoadingPreview() {
    TaskioTheme {
        LogScreenContent(
            uiState = LogUiState(loading = true),
            snackbarHostState = SnackbarHostState(),
            onEditClick = {},
            onEditTitleChange = {},
            onEditDescriptionChange = {},
            onEditScheduledDateChange = {},
            onEditRecurringChange = {},
            onEditDayToggle = {},
            onEditSave = {},
            onEditDismiss = {}
        )
    }
}
