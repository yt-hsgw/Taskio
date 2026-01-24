package com.yt_hsgw.taskio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.api.RetrofitClient
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.model.TaskLogResponse
import com.yt_hsgw.taskio.model.TaskRequest
import com.yt_hsgw.taskio.model.toTaskItem
import com.yt_hsgw.taskio.ui.TaskioStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * ログ画面のUI状態
 *
 * @property tasks タスク一覧
 * @property taskLogs タスクIDをキーとしたログマップ
 * @property weeklyProgress 週間の実行状態（タスクID -> 曜日インデックス -> 実行済み）
 * @property loading ローディング状態
 * @property errorMessage エラーメッセージ
 * @property editingTask 編集中のタスク
 * @property showEditDialog 編集ダイアログ表示フラグ
 * @property editTitle 編集用タイトル
 * @property editDescription 編集用説明
 * @property editIsRecurring 編集用繰り返しフラグ
 * @property editSelectedDays 編集用選択曜日
 * @property taskUpdated タスク更新完了フラグ
 */
data class LogUiState(
    val tasks: List<TaskItem> = emptyList(),
    val taskLogs: Map<String, List<TaskLogResponse>> = emptyMap(),
    val weeklyProgress: Map<String, Map<Int, Boolean>> = emptyMap(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val editingTask: TaskItem? = null,
    val showEditDialog: Boolean = false,
    val editTitle: String = "",
    val editDescription: String = "",
    val editIsRecurring: Boolean = false,
    val editSelectedDays: Set<Int> = emptySet(),
    val taskUpdated: Boolean = false
)

/**
 * ログ画面用ViewModel
 *
 * タスク一覧の取得、週間進捗の計算、タスク編集を管理します。
 */
class LogViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())

    /** UI状態のStateFlow */
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        fetchTasksAndLogs()
    }

    // ─────────────────────────────
    // データ取得
    // ─────────────────────────────

    /**
     * タスクとログを取得
     */
    fun fetchTasksAndLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }

            try {
                // タスク一覧を取得
                val tasksResponse = RetrofitClient.api.listTasks()
                if (tasksResponse.isSuccessful && tasksResponse.body() != null) {
                    val tasks = tasksResponse.body()!!.map { it.toTaskItem() }
                    _uiState.update { it.copy(tasks = tasks) }

                    // 各タスクのログを取得
                    fetchLogsForTasks(tasks)
                } else {
                    // モックデータを使用
                    loadMockData()
                }
            } catch (e: Exception) {
                // サーバー接続失敗時はモックデータを使用
                loadMockData()
            }

            _uiState.update { it.copy(loading = false) }
        }
    }

    /**
     * 各タスクのログを取得
     */
    private suspend fun fetchLogsForTasks(tasks: List<TaskItem>) {
        val logsMap = mutableMapOf<String, List<TaskLogResponse>>()
        val progressMap = mutableMapOf<String, Map<Int, Boolean>>()

        for (task in tasks) {
            try {
                val logsResponse = RetrofitClient.api.getTaskLogs(task.id)
                if (logsResponse.isSuccessful && logsResponse.body() != null) {
                    val logs = logsResponse.body()!!
                    logsMap[task.id] = logs
                    progressMap[task.id] = calculateWeeklyProgress(task, logs)
                }
            } catch (e: Exception) {
                // ログ取得失敗時は空リスト
                logsMap[task.id] = emptyList()
                progressMap[task.id] = calculateWeeklyProgress(task, emptyList())
            }
        }

        _uiState.update {
            it.copy(
                taskLogs = logsMap,
                weeklyProgress = progressMap
            )
        }
    }

    /**
     * 週間進捗を計算
     *
     * @param task タスク
     * @param logs タスクログ
     * @return 曜日インデックス（0=日曜日）をキーとした実行済みフラグマップ
     */
    private fun calculateWeeklyProgress(
        task: TaskItem,
        logs: List<TaskLogResponse>
    ): Map<Int, Boolean> {
        val progress = mutableMapOf<Int, Boolean>()
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

        // 日曜日から土曜日までの7日間
        for (dayOffset in 0 until DAYS_IN_WEEK) {
            val date = startOfWeek.plusDays(dayOffset.toLong())
            val dayIndex = dayOffset // 0=日曜日, 6=土曜日

            // その日にログがあるかチェック
            val hasLog = logs.any { log ->
                try {
                    log.start_at?.let {
                        val logDate = LocalDate.parse(it.substringBefore("T"))
                        logDate == date
                    } ?: false
                } catch (e: Exception) {
                    false
                }
            }

            progress[dayIndex] = hasLog
        }

        return progress
    }

    /**
     * モックデータを読み込み
     */
    private fun loadMockData() {
        val today = LocalDate.now()
        val mockTasks = listOf(
            TaskItem(
                id = "1",
                title = "ジム",
                description = "24hジムでのトレーニング",
                createdAt = today.toString(),
                repeatDays = listOf(1, 3, 5),
                isRecurring = true
            ),
            TaskItem(
                id = "2",
                title = "ランニング",
                description = "朝のジョギング 5km",
                createdAt = today.toString(),
                repeatDays = listOf(2, 4),
                isRecurring = true
            ),
            TaskItem(
                id = "3",
                title = "キックボクシング",
                description = "ジムでのキックボクシングレッスン",
                createdAt = today.toString(),
                repeatDays = listOf(6),
                isRecurring = true
            ),
            TaskItem(
                id = "4",
                title = "資格勉強",
                description = "AWS認定資格の勉強",
                createdAt = today.toString(),
                repeatDays = listOf(0, 1, 2, 3, 4, 5, 6),
                isRecurring = true
            ),
            TaskItem(
                id = "5",
                title = "読書",
                description = "毎日30分の読書習慣",
                createdAt = today.toString(),
                repeatDays = listOf(1, 2, 3, 4, 5),
                isRecurring = true
            )
        )

        // モック週間進捗（ランダムに実行済みを設定）
        val mockProgress = mockTasks.associate { task ->
            task.id to (0 until DAYS_IN_WEEK).associateWith { dayIndex ->
                // 繰り返し曜日に含まれている場合、50%の確率で実行済み
                task.repeatDays?.contains(dayIndex) == true && dayIndex % 2 == 1
            }
        }

        _uiState.update {
            it.copy(
                tasks = mockTasks,
                weeklyProgress = mockProgress
            )
        }
    }

    // ─────────────────────────────
    // タスク編集
    // ─────────────────────────────

    /**
     * 編集ダイアログを開く
     *
     * @param task 編集対象のタスク
     */
    fun openEditDialog(task: TaskItem) {
        _uiState.update {
            it.copy(
                editingTask = task,
                showEditDialog = true,
                editTitle = task.title,
                editDescription = task.description ?: "",
                editIsRecurring = task.isRecurring,
                editSelectedDays = task.repeatDays?.toSet() ?: emptySet()
            )
        }
    }

    /**
     * 編集ダイアログを閉じる
     */
    fun closeEditDialog() {
        _uiState.update {
            it.copy(
                editingTask = null,
                showEditDialog = false,
                editTitle = "",
                editDescription = "",
                editIsRecurring = false,
                editSelectedDays = emptySet()
            )
        }
    }

    /**
     * 編集タイトルを更新
     */
    fun updateEditTitle(title: String) {
        _uiState.update { it.copy(editTitle = title) }
    }

    /**
     * 編集説明を更新
     */
    fun updateEditDescription(description: String) {
        _uiState.update { it.copy(editDescription = description) }
    }

    /**
     * 編集繰り返しフラグをトグル
     */
    fun toggleEditRecurring(isRecurring: Boolean) {
        _uiState.update {
            it.copy(
                editIsRecurring = isRecurring,
                editSelectedDays = if (!isRecurring) emptySet() else it.editSelectedDays
            )
        }
    }

    /**
     * 編集曜日選択をトグル
     */
    fun toggleEditDay(day: Int) {
        _uiState.update { state ->
            val newDays = if (state.editSelectedDays.contains(day)) {
                state.editSelectedDays - day
            } else {
                state.editSelectedDays + day
            }
            state.copy(editSelectedDays = newDays)
        }
    }

    /**
     * タスクを更新
     */
    fun updateTask() {
        val editingTask = _uiState.value.editingTask ?: return
        val title = _uiState.value.editTitle.trim()
        val description = _uiState.value.editDescription.trim().takeIf { it.isNotEmpty() }
        val isRecurring = _uiState.value.editIsRecurring
        val selectedDays = _uiState.value.editSelectedDays

        // バリデーション
        if (title.isEmpty()) {
            _uiState.update { it.copy(errorMessage = TaskioStrings.VALIDATION_TITLE_REQUIRED) }
            return
        }

        if (isRecurring && selectedDays.isEmpty()) {
            _uiState.update { it.copy(errorMessage = TaskioStrings.VALIDATION_RECURRING_DAYS_REQUIRED) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }

            try {
                val request = TaskRequest(
                    title = title,
                    description = description,
                    due_date = null,
                    scheduled_date = null,
                    repeat_days = if (isRecurring) selectedDays.toList() else null
                )

                val response = RetrofitClient.api.updateTask(editingTask.id, request)

                if (response.isSuccessful && response.body() != null) {
                    val updatedTask = response.body()!!.toTaskItem()

                    // タスクリストを更新
                    _uiState.update { state ->
                        val updatedTasks = state.tasks.map {
                            if (it.id == editingTask.id) updatedTask else it
                        }
                        state.copy(
                            tasks = updatedTasks,
                            loading = false,
                            showEditDialog = false,
                            editingTask = null,
                            taskUpdated = true
                        )
                    }
                } else {
                    // サーバーエラー時はローカル更新
                    updateTaskLocally(editingTask.id, title, description, isRecurring, selectedDays)
                }
            } catch (e: Exception) {
                // ネットワークエラー時はローカル更新
                updateTaskLocally(editingTask.id, title, description, isRecurring, selectedDays)
            }
        }
    }

    /**
     * ローカルでタスクを更新（サーバー接続失敗時）
     */
    private fun updateTaskLocally(
        taskId: String,
        title: String,
        description: String?,
        isRecurring: Boolean,
        selectedDays: Set<Int>
    ) {
        _uiState.update { state ->
            val updatedTasks = state.tasks.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        title = title,
                        description = description,
                        isRecurring = isRecurring,
                        repeatDays = if (isRecurring) selectedDays.toList() else null
                    )
                } else {
                    task
                }
            }
            state.copy(
                tasks = updatedTasks,
                loading = false,
                showEditDialog = false,
                editingTask = null,
                taskUpdated = true
            )
        }
    }

    /**
     * タスク更新完了フラグをリセット
     */
    fun resetTaskUpdated() {
        _uiState.update { it.copy(taskUpdated = false) }
    }

    /**
     * エラーメッセージをクリア
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        /** 週の日数 */
        private const val DAYS_IN_WEEK = 7
    }
}
