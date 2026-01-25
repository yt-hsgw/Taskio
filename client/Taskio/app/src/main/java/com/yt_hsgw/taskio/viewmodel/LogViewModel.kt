package com.yt_hsgw.taskio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.api.RetrofitClient
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.model.TaskRequest
import com.yt_hsgw.taskio.model.toTaskItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ログ画面のUI状態
 */
data class LogUiState(
    val tasks: List<TaskItem> = emptyList(),
    val weeklyProgress: Map<String, Map<Int, Boolean>> = emptyMap(),
    val loading: Boolean = false,
    val updating: Boolean = false,
    val errorMessage: String? = null,
    val taskUpdated: Boolean = false,
    // 編集ダイアログの状態
    val showEditDialog: Boolean = false,
    val editingTask: TaskItem? = null,
    val editTitle: String = "",
    val editDescription: String = "",
    val editScheduledDate: LocalDate? = null,
    val editIsRecurring: Boolean = false,
    val editSelectedDays: Set<Int> = emptySet()
)

/**
 * タスク更新イベント
 *
 * 他のViewModelに更新を通知するためのイベント
 */
sealed class TaskUpdateEvent {
    data class TaskUpdated(val task: TaskItem) : TaskUpdateEvent()
    data class TaskDeleted(val taskId: String) : TaskUpdateEvent()
}

/**
 * ログ画面用ViewModel
 *
 * タスク一覧の表示と編集機能を提供します。
 * タスク更新時には他の画面（Home等）にも更新を通知します。
 */
class LogViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    // タスク更新イベント（他のViewModelが購読可能）
    private val _taskUpdateEvent = MutableSharedFlow<TaskUpdateEvent>()
    val taskUpdateEvent = _taskUpdateEvent.asSharedFlow()

    init {
        fetchTasksAndLogs()
    }

    /**
     * タスクとログを取得
     */
    fun fetchTasksAndLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }

            try {
                val response = RetrofitClient.api.listTasks()
                if (response.isSuccessful) {
                    val taskResponses = response.body() ?: emptyList()
                    val tasks = taskResponses.map { it.toTaskItem() }
                    
                    // 各タスクの週間進捗を計算
                    val weeklyProgress = calculateWeeklyProgress(tasks)

                    _uiState.update {
                        it.copy(
                            tasks = tasks,
                            weeklyProgress = weeklyProgress,
                            loading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = "タスクの取得に失敗しました"
                        )
                    }
                }
            } catch (e: Exception) {
                // TODO: モックをコメントアウト（サーバー通信確認用）
                // val mockTasks = createMockTasks()
                // val weeklyProgress = calculateWeeklyProgress(mockTasks)
                // _uiState.update {
                //     it.copy(
                //         tasks = mockTasks,
                //         weeklyProgress = weeklyProgress,
                //         loading = false
                //     )
                // }
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = "サーバー接続エラー: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 週間進捗を計算
     */
    private fun calculateWeeklyProgress(tasks: List<TaskItem>): Map<String, Map<Int, Boolean>> {
        val today = LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek.value % 7 // 日曜=0

        return tasks.associate { task ->
            val repeatDays = task.repeatDays ?: emptyList()
            val progress = (0 until 7).associateWith { dayIndex ->
                // 今週の該当曜日が過去または今日で、repeatDaysに含まれていれば完了扱い
                val isPastOrToday = dayIndex <= currentDayOfWeek
                val isScheduledDay = repeatDays.contains(dayIndex)
                // 実際のログデータがあれば完了、なければ仮の状態
                isPastOrToday && isScheduledDay
            }
            task.id to progress
        }
    }

    // ─────────────────────────────
    // 編集ダイアログ操作
    // ─────────────────────────────

    /**
     * 編集ダイアログを開く
     */
    fun openEditDialog(task: TaskItem) {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                editingTask = task,
                editTitle = task.title,
                editDescription = task.description ?: "",
                editScheduledDate = task.scheduledDate?.let { dateStr -> parseDate(dateStr) },
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
                showEditDialog = false,
                editingTask = null,
                editTitle = "",
                editDescription = "",
                editScheduledDate = null,
                editIsRecurring = false,
                editSelectedDays = emptySet()
            )
        }
    }

    /**
     * タイトルを更新
     */
    fun updateEditTitle(title: String) {
        _uiState.update { it.copy(editTitle = title) }
    }

    /**
     * 説明を更新
     */
    fun updateEditDescription(description: String) {
        _uiState.update { it.copy(editDescription = description) }
    }

    /**
     * 予定日を更新
     */
    fun updateEditScheduledDate(date: LocalDate?) {
        _uiState.update { it.copy(editScheduledDate = date) }
    }

    /**
     * 繰り返しフラグをトグル
     */
    fun toggleEditRecurring(isRecurring: Boolean) {
        _uiState.update { 
            it.copy(
                editIsRecurring = isRecurring,
                // 繰り返しOFFの場合は曜日選択をクリア
                editSelectedDays = if (isRecurring) it.editSelectedDays else emptySet(),
                // 繰り返しONの場合は予定日をクリア
                editScheduledDate = if (isRecurring) null else it.editScheduledDate
            )
        }
    }

    /**
     * 曜日をトグル
     */
    fun toggleEditDay(dayIndex: Int) {
        _uiState.update { state ->
            val newDays = if (state.editSelectedDays.contains(dayIndex)) {
                state.editSelectedDays - dayIndex
            } else {
                state.editSelectedDays + dayIndex
            }
            state.copy(editSelectedDays = newDays)
        }
    }

    /**
     * タスクを更新
     */
    fun updateTask() {
        val currentState = _uiState.value
        val editingTask = currentState.editingTask ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(updating = true) }

            try {
                // サーバーはDateTime<Utc>を期待しているため、日付にT00:00:00Zを付加してUTC形式にする
                val scheduledDateString = currentState.editScheduledDate?.let {
                    "${it}T00:00:00Z"
                }
                val request = TaskRequest(
                    title = currentState.editTitle,
                    description = currentState.editDescription.ifBlank { null },
                    due_date = null,
                    scheduled_date = scheduledDateString,
                    repeat_days = if (currentState.editIsRecurring) {
                        currentState.editSelectedDays.toList().sorted()
                    } else {
                        null
                    }
                )

                val response = RetrofitClient.api.updateTask(editingTask.id, request)
                
                if (response.isSuccessful) {
                    val updatedTaskResponse = response.body()
                    
                    if (updatedTaskResponse != null) {
                        val updatedTask = updatedTaskResponse.toTaskItem()
                        
                        // ローカルのタスクリストを更新
                        val updatedTasks = currentState.tasks.map { task ->
                            if (task.id == editingTask.id) updatedTask else task
                        }
                        val weeklyProgress = calculateWeeklyProgress(updatedTasks)

                        _uiState.update {
                            it.copy(
                                tasks = updatedTasks,
                                weeklyProgress = weeklyProgress,
                                updating = false,
                                taskUpdated = true,
                                showEditDialog = false,
                                editingTask = null
                            )
                        }

                        // 他のViewModelに更新を通知
                        _taskUpdateEvent.emit(TaskUpdateEvent.TaskUpdated(updatedTask))
                        // グローバルイベントも発行
                        emitGlobalTaskUpdate(TaskUpdateEvent.TaskUpdated(updatedTask))
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            updating = false,
                            errorMessage = "タスクの更新に失敗しました"
                        )
                    }
                }
            } catch (e: Exception) {
                // TODO: モックをコメントアウト（サーバー通信確認用）
                // // オフライン時はローカルで更新を反映
                // val updatedTask = editingTask.copy(
                //     title = currentState.editTitle,
                //     description = currentState.editDescription.ifBlank { null },
                //     scheduledDate = currentState.editScheduledDate?.toString(),
                //     isRecurring = currentState.editIsRecurring,
                //     repeatDays = if (currentState.editIsRecurring) {
                //         currentState.editSelectedDays.toList().sorted()
                //     } else {
                //         null
                //     }
                // )
                //
                // val updatedTasks = currentState.tasks.map { task ->
                //     if (task.id == editingTask.id) updatedTask else task
                // }
                // val weeklyProgress = calculateWeeklyProgress(updatedTasks)
                //
                // _uiState.update {
                //     it.copy(
                //         tasks = updatedTasks,
                //         weeklyProgress = weeklyProgress,
                //         updating = false,
                //         taskUpdated = true,
                //         showEditDialog = false,
                //         editingTask = null
                //     )
                // }
                //
                // // 他のViewModelに更新を通知（オフライン時も）
                // _taskUpdateEvent.emit(TaskUpdateEvent.TaskUpdated(updatedTask))
                // emitGlobalTaskUpdate(TaskUpdateEvent.TaskUpdated(updatedTask))
                _uiState.update {
                    it.copy(
                        updating = false,
                        errorMessage = "サーバー接続エラー: ${e.message}"
                    )
                }
            }
        }
    }

    // ─────────────────────────────
    // ユーティリティ
    // ─────────────────────────────

    /**
     * エラーをクリア
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * タスク更新フラグをリセット
     */
    fun resetTaskUpdated() {
        _uiState.update { it.copy(taskUpdated = false) }
    }

    /**
     * 日付文字列をパース
     */
    private fun parseDate(dateString: String): LocalDate? {
        return try {
            // ISO-8601形式の日付をパース（"2025-01-25" or "2025-01-25T10:30:00"）
            LocalDate.parse(dateString.substringBefore("T"))
        } catch (e: Exception) {
            null
        }
    }

    // TODO: モックをコメントアウト（サーバー通信確認用）
    // /**
    //  * モックデータを作成
    //  */
    // private fun createMockTasks(): List<TaskItem> {
    //     return listOf(
    //         TaskItem(
    //             id = "mock-1",
    //             title = "ジム",
    //             description = "24hジムでのトレーニング",
    //             createdAt = LocalDate.now().toString(),
    //             repeatDays = listOf(1, 3, 5),
    //             isRecurring = true
    //         ),
    //         TaskItem(
    //             id = "mock-2",
    //             title = "ランニング",
    //             description = "朝のジョギング 5km",
    //             createdAt = LocalDate.now().toString(),
    //             repeatDays = listOf(2, 4),
    //             isRecurring = true
    //         ),
    //         TaskItem(
    //             id = "mock-3",
    //             title = "キックボクシング",
    //             description = "ジムでのキックボクシングレッスン",
    //             createdAt = LocalDate.now().toString(),
    //             repeatDays = listOf(6),
    //             isRecurring = true
    //         ),
    //         TaskItem(
    //             id = "mock-4",
    //             title = "資格勉強",
    //             description = "AWS認定資格の勉強。毎日1時間は確保する。\n" +
    //                 "ソリューションアーキテクトアソシエイトを目標に。",
    //             createdAt = LocalDate.now().toString(),
    //             repeatDays = listOf(0, 1, 2, 3, 4, 5, 6),
    //             isRecurring = true
    //         )
    //     )
    // }

    companion object {
        // シングルトンのイベントバス（複数ViewModel間でタスク更新を共有）
        private val _globalTaskUpdateEvent = MutableSharedFlow<TaskUpdateEvent>(extraBufferCapacity = 1)
        val globalTaskUpdateEvent = _globalTaskUpdateEvent.asSharedFlow()

        /**
         * グローバルなタスク更新を発行
         */
        suspend fun emitGlobalTaskUpdate(event: TaskUpdateEvent) {
            _globalTaskUpdateEvent.emit(event)
        }
    }
}
