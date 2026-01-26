package com.yt_hsgw.taskio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.api.RetrofitClient
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.model.TaskLogRequest
import com.yt_hsgw.taskio.model.TaskRequest
import com.yt_hsgw.taskio.model.TaskStatus
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.viewmodel.LogViewModel
import com.yt_hsgw.taskio.viewmodel.TaskUpdateEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * タスク画面のUI状態
 *
 * @property tasks タスク一覧
 * @property selectedDate 選択中の日付
 * @property calendarDates カレンダーに表示する日付リスト
 * @property title 新規タスクのタイトル
 * @property description 新規タスクの説明
 * @property scheduledDate 新規タスクの予定日時
 * @property isRecurring 繰り返しタスクかどうか
 * @property selectedDays 選択された繰り返し曜日
 * @property taskDayStates 日付ごとのタスク状態マップ
 * @property loading ローディング状態
 * @property errorMessage エラーメッセージ
 * @property taskCreated タスク作成完了フラグ
 */
data class TaskUiState(
    val tasks: List<TaskItem> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarDates: List<LocalDate> = emptyList(),
    val title: String = "",
    val description: String = "",
    val scheduledDate: LocalDateTime? = null,
    val isRecurring: Boolean = false,
    val selectedDays: Set<Int> = emptySet(),
    val taskDayStates: Map<String, TaskDayState> = emptyMap(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val taskCreated: Boolean = false
)

/**
 * 特定の日付におけるタスクの状態
 *
 * @property isStarted タスクが開始されているかどうか
 * @property isFinished タスクが終了しているかどうか
 * @property startedAt 開始時刻
 * @property finishedAt 終了時刻
 * @property logId サーバー上のログID（開始時に取得）
 */
data class TaskDayState(
    val isStarted: Boolean = false,
    val isFinished: Boolean = false,
    val startedAt: LocalDateTime? = null,
    val finishedAt: LocalDateTime? = null,
    val logId: String? = null
)

/**
 * タスクとその日の状態を組み合わせたデータクラス
 *
 * UI表示用にタスク情報と日別状態を結合します。
 *
 * @property task タスク情報
 * @property dayState 当日の状態
 */
data class TaskWithDayState(
    val task: TaskItem,
    val dayState: TaskDayState
) {
    /** タスクID */
    val id: String get() = task.id

    /** タスクタイトル */
    val title: String get() = task.title

    /** タスク説明 */
    val description: String? get() = task.description

    /** 繰り返しタスクかどうか */
    val isRecurring: Boolean get() = task.isRecurring

    /** 開始済みかどうか */
    val isStarted: Boolean get() = dayState.isStarted

    /** 終了済みかどうか */
    val isFinished: Boolean get() = dayState.isFinished
}

/**
 * タスク管理用ViewModel
 *
 * タスクのCRUD操作、日付選択、タスク状態の管理を担当します。
 */
class TaskViewModel : ViewModel() {

    companion object {
        /** 週の日数 */
        private const val DAYS_IN_WEEK = 7

        /**
         * タスクIDと日付からユニークキーを生成
         *
         * @param taskId タスクID
         * @param date 日付
         * @return "taskId_yyyy-MM-dd" 形式のキー
         */
        fun createTaskDayKey(taskId: String, date: LocalDate): String {
            return "${taskId}_${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        }
    }

    private val _uiState = MutableStateFlow(TaskUiState())

    /** UI状態のStateFlow */
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    /**
     * 選択された日付に基づいてフィルタリングされたタスク一覧
     *
     * タスクと日別状態を結合した[TaskWithDayState]のリストを提供します。
     */
    val filteredTasks: StateFlow<List<TaskWithDayState>> = _uiState
        .map { state ->
            val filtered = filterTasksForDate(state.tasks, state.selectedDate)
            filtered.map { task ->
                val key = createTaskDayKey(task.id, state.selectedDate)
                val dayState = state.taskDayStates[key] ?: TaskDayState()
                TaskWithDayState(task = task, dayState = dayState)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MS), emptyList())

    init {
        setupCalendar()
        fetchTasks()
        fetchTaskLogsForSelectedDate()
        viewModelScope.launch {
            LogViewModel.globalTaskUpdateEvent.collect { event ->
                handleTaskUpdateEvent(event)
            }
        }
    }

    // ─────────────────────────────
    // タスクフィルタリング
    // ─────────────────────────────

    /**
     * 指定された日付に該当するタスクをフィルタリング
     *
     * フィルタリングルール:
     * - 繰り返しタスク: 選択日の曜日が repeat_days に含まれており、かつ予定日以降であればマッチ
     * - 単発タスク: scheduled_date が選択日と一致すればマッチ
     * - 日付未指定タスク: すべての日に表示
     *
     * @param tasks フィルタリング対象のタスク一覧
     * @param date フィルタリング基準日
     * @return フィルタリング済みタスク一覧
     */
    private fun filterTasksForDate(tasks: List<TaskItem>, date: LocalDate): List<TaskItem> {
        val dayOfWeekIndex = date.dayOfWeek.toCalendarIndex()

        return tasks.filter { task ->
            when {
                task.isRecurring && task.repeatDays != null -> {
                    // 曜日が一致し、かつ予定日（開始日）以降であること
                    val isMatchingDay = task.repeatDays.contains(dayOfWeekIndex)
                    val isOnOrAfterStartDate = task.scheduledDate?.let { scheduled ->
                        runCatching {
                            val startDate = LocalDate.parse(scheduled.substringBefore("T"))
                            !date.isBefore(startDate)
                        }.getOrDefault(true)
                    } ?: true
                    isMatchingDay && isOnOrAfterStartDate
                }
                task.scheduledDate != null -> {
                    runCatching {
                        val taskDate = LocalDate.parse(task.scheduledDate.substringBefore("T"))
                        taskDate == date
                    }.getOrDefault(false)
                }
                else -> true
            }
        }
    }

    /**
     * DayOfWeekをカレンダーインデックスに変換
     *
     * 0=日曜日, 1=月曜日, ..., 6=土曜日
     */
    private fun DayOfWeek.toCalendarIndex(): Int = when (this) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }

    // ─────────────────────────────
    // カレンダー設定
    // ─────────────────────────────

    /**
     * カレンダーの初期設定
     *
     * 今週の日曜日から土曜日までの日付リストを生成します。
     */
    private fun setupCalendar() {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val weekDates = (0 until DAYS_IN_WEEK).map { startOfWeek.plusDays(it.toLong()) }
        _uiState.update { it.copy(calendarDates = weekDates, selectedDate = today) }
    }

    // ─────────────────────────────
    // タスク取得
    // ─────────────────────────────

    /**
     * サーバーからタスク一覧を取得
     */
    fun fetchTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            try {
                val response = RetrofitClient.api.listTasks()
                if (response.isSuccessful && response.body() != null) {
                    val tasks = response.body()!!.map { taskResponse ->
                        TaskItem(
                            id = taskResponse.id,
                            title = taskResponse.title,
                            description = taskResponse.description,
                            createdAt = taskResponse.created_at,
                            scheduledDate = taskResponse.scheduled_date,
                            repeatDays = taskResponse.repeat_days,
                            isRecurring = taskResponse.is_recurring
                        )
                    }
                    _uiState.update { it.copy(tasks = tasks, loading = false) }
                } else {
                    _uiState.update { it.copy(loading = false, errorMessage = "タスクの取得に失敗しました: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, errorMessage = "サーバー接続エラー: ${e.message}") }
            }
        }
    }

    // ─────────────────────────────
    // 日付選択
    // ─────────────────────────────

    /**
     * 日付を選択
     *
     * @param date 選択する日付
     */
    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        fetchTaskLogsForSelectedDate()
    }

    // ─────────────────────────────
    // タスクログ取得
    // ─────────────────────────────

    /**
     * 選択中の日付のタスクログをサーバーから取得
     *
     * カレンダー画面と同期するため、サーバーからログを取得して
     * タスクの状態（開始/終了）を反映します。
     */
    private fun fetchTaskLogsForSelectedDate() {
        viewModelScope.launch {
            try {
                val date = _uiState.value.selectedDate
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val response = RetrofitClient.api.getLogsByDateRange(dateStr, dateStr)
                if (response.isSuccessful && response.body() != null) {
                    val logs = response.body()!!.logs
                    _uiState.update { state ->
                        var newTaskDayStates = state.taskDayStates
                        for (log in logs) {
                            val key = createTaskDayKey(log.task_id, date)
                            val dayState = when (log.status) {
                                TaskStatus.NOT_STARTED -> TaskDayState()
                                TaskStatus.IN_PROGRESS -> TaskDayState(
                                    isStarted = true,
                                    isFinished = false,
                                    logId = log.id
                                )
                                TaskStatus.COMPLETED -> TaskDayState(
                                    isStarted = true,
                                    isFinished = true,
                                    logId = log.id
                                )
                            }
                            newTaskDayStates = newTaskDayStates + (key to dayState)
                        }
                        state.copy(taskDayStates = newTaskDayStates)
                    }
                }
            } catch (e: Exception) {
                // ログ取得エラーは無視（ネットワークエラー時など）
            }
        }
    }

    // ─────────────────────────────
    // タスク状態管理
    // ─────────────────────────────

    /**
     * タスクの開始状態をトグル
     *
     * 選択中の日付に対してタスクの開始状態を切り替えます。
     * サーバーにタスクログを作成してカレンダー画面と同期します。
     *
     * @param taskId タスクID
     */
    fun toggleStart(taskId: String) {
        val date = _uiState.value.selectedDate
        val key = createTaskDayKey(taskId, date)
        val currentState = _uiState.value.taskDayStates[key] ?: TaskDayState()

        if (currentState.isStarted) {
            // 既に開始済みの場合は開始状態を解除（ローカルのみ）
            _uiState.update { state ->
                val newState = TaskDayState(
                    isStarted = false,
                    startedAt = null,
                    logId = null
                )
                state.copy(taskDayStates = state.taskDayStates + (key to newState))
            }
            return
        }

        // 開始する場合はサーバーにログを作成
        viewModelScope.launch {
            try {
                val now = LocalDateTime.now()
                val targetDate = date.atStartOfDay()
                val request = TaskLogRequest(
                    start_at = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z",
                    target_date = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
                )

                val response = RetrofitClient.api.createTaskLog(taskId, request)
                if (response.isSuccessful && response.body() != null) {
                    val logResponse = response.body()!!
                    _uiState.update { state ->
                        val newState = TaskDayState(
                            isStarted = true,
                            isFinished = false,
                            startedAt = now,
                            logId = logResponse.id
                        )
                        state.copy(taskDayStates = state.taskDayStates + (key to newState))
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "タスクの開始に失敗しました: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "サーバー接続エラー: ${e.message}") }
            }
        }
    }

    /**
     * タスクの終了状態をトグル
     *
     * 選択中の日付に対してタスクの終了状態を切り替えます。
     * 開始していない場合は終了できません。
     * サーバーのタスクログを更新してカレンダー画面と同期します。
     *
     * @param taskId タスクID
     */
    fun toggleFinish(taskId: String) {
        val date = _uiState.value.selectedDate
        val key = createTaskDayKey(taskId, date)

        viewModelScope.launch {
            // コルーチン内で最新の状態を取得
            val currentState = _uiState.value.taskDayStates[key] ?: TaskDayState()

            if (!currentState.isStarted) return@launch

            val logId = currentState.logId
            if (logId == null) {
                // ログIDがない場合はローカルのみ更新
                _uiState.update { state ->
                    val latestState = state.taskDayStates[key] ?: TaskDayState()
                    val newState = latestState.copy(
                        isFinished = !latestState.isFinished,
                        finishedAt = if (!latestState.isFinished) LocalDateTime.now() else null
                    )
                    state.copy(taskDayStates = state.taskDayStates + (key to newState))
                }
                return@launch
            }

            try {
                val now = LocalDateTime.now()
                val isCompleting = !currentState.isFinished

                val request = TaskLogRequest(
                    end_at = if (isCompleting) now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z" else null,
                    is_completed = isCompleting
                )

                val response = RetrofitClient.api.updateTaskLog(logId, request)
                if (response.isSuccessful) {
                    _uiState.update { state ->
                        val latestState = state.taskDayStates[key] ?: TaskDayState()
                        val newState = latestState.copy(
                            isFinished = isCompleting,
                            finishedAt = if (isCompleting) now else null
                        )
                        state.copy(taskDayStates = state.taskDayStates + (key to newState))
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "タスクの終了に失敗しました: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "サーバー接続エラー: ${e.message}") }
            }
        }
    }

    // ─────────────────────────────
    // 入力状態管理
    // ─────────────────────────────

    /**
     * タイトルを更新
     *
     * @param newTitle 新しいタイトル
     */
    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    /**
     * 説明を更新
     *
     * @param newDesc 新しい説明
     */
    fun updateDescription(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    /**
     * エラーメッセージをクリア
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 予定日を更新
     *
     * @param date 新しい予定日時
     */
    fun updateScheduledDate(date: LocalDateTime?) {
        _uiState.update { it.copy(scheduledDate = date) }
    }

    /**
     * 繰り返し設定をトグル
     *
     * @param isRecurring 繰り返しを有効にするかどうか
     */
    fun toggleRecurring(isRecurring: Boolean) {
        _uiState.update {
            it.copy(
                isRecurring = isRecurring,
                selectedDays = if (!isRecurring) emptySet() else it.selectedDays,
                scheduledDate = if (isRecurring) null else it.scheduledDate
            )
        }
    }

    /**
     * 曜日選択をトグル
     *
     * @param day 曜日インデックス（0=日曜日, 6=土曜日）
     */
    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val newDays = if (state.selectedDays.contains(day)) {
                state.selectedDays - day
            } else {
                state.selectedDays + day
            }
            state.copy(selectedDays = newDays)
        }
    }

    // ─────────────────────────────
    // タスク作成
    // ─────────────────────────────

    /**
     * タスクを作成
     *
     * バリデーションを行い、サーバーにタスク作成リクエストを送信します。
     */
    suspend fun createTask() {
        val currentTitle = _uiState.value.title.trim()
        val currentDescription = _uiState.value.description.trim().takeIf { it.isNotEmpty() }
        val currentState = _uiState.value

        // バリデーション
        if (currentTitle.isEmpty()) {
            _uiState.update { it.copy(errorMessage = TaskioStrings.VALIDATION_TITLE_REQUIRED) }
            return
        }

        if (currentState.isRecurring && currentState.selectedDays.isEmpty()) {
            _uiState.update { it.copy(errorMessage = TaskioStrings.VALIDATION_RECURRING_DAYS_REQUIRED) }
            return
        }

        _uiState.update { it.copy(loading = true, errorMessage = null, taskCreated = false) }

        try {
            // サーバーはDateTime<Utc>を期待しているため、末尾にZを付加してUTC形式にする
            // 予定日が未設定の場合は選択中の日付を使用
            val scheduledDateString = (currentState.scheduledDate
                ?: currentState.selectedDate.atStartOfDay()
            ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"

            val request = TaskRequest(
                title = currentTitle,
                description = currentDescription,
                due_date = null,
                scheduled_date = scheduledDateString,
                repeat_days = if (currentState.isRecurring) currentState.selectedDays.toList() else null
            )

            val response = RetrofitClient.api.createTask(request)

            if (response.isSuccessful && response.body() != null) {
                val taskResponse = response.body()!!
                val newTask = TaskItem(
                    id = taskResponse.id,
                    title = taskResponse.title,
                    description = taskResponse.description,
                    createdAt = taskResponse.created_at,
                    scheduledDate = taskResponse.scheduled_date,
                    repeatDays = taskResponse.repeat_days,
                    isRecurring = taskResponse.is_recurring
                )

                _uiState.update { state ->
                    state.copy(
                        tasks = state.tasks + newTask,
                        loading = false,
                        title = "",
                        description = "",
                        scheduledDate = null,
                        isRecurring = false,
                        selectedDays = emptySet(),
                        taskCreated = true
                    )
                }
            } else {
                _uiState.update { it.copy(loading = false, errorMessage = "タスクの作成に失敗しました: ${response.code()}") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(loading = false, errorMessage = "サーバー接続エラー: ${e.message}") }
        }
    }

    /**
     * タスク更新イベントを処理
     *
     * LogViewModelからのタスク更新を受けてローカル状態を更新します。
     */
    private fun handleTaskUpdateEvent(event: TaskUpdateEvent) {
        when (event) {
            is TaskUpdateEvent.TaskUpdated -> {
                // ローカルのタスクリストを更新
                _uiState.update { state ->
                    val updatedTasks = state.tasks.map { task ->
                        if (task.id == event.task.id) event.task else task
                    }
                    state.copy(tasks = updatedTasks)
                }
            }
            is TaskUpdateEvent.TaskDeleted -> {
                // タスクをリストから削除
                _uiState.update { state ->
                    val updatedTasks = state.tasks.filter { it.id != event.taskId }
                    state.copy(tasks = updatedTasks)
                }
            }
        }
    }

    /**
     * タスク作成完了フラグをリセット
     */
    fun resetTaskCreated() {
        _uiState.update { it.copy(taskCreated = false) }
    }

    /**
     * ダイアログの入力状態をクリア
     */
    fun clearDialogState() {
        _uiState.update {
            it.copy(
                title = "",
                description = "",
                scheduledDate = null,
                isRecurring = false,
                selectedDays = emptySet()
            )
        }
    }

    /**
     * タスク作成ダイアログを開く
     *
     * 選択中の日付を予定日の初期値として設定します。
     */
    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                title = "",
                description = "",
                scheduledDate = it.selectedDate.atStartOfDay(),
                isRecurring = false,
                selectedDays = emptySet()
            )
        }
    }
}

// ─────────────────────────────
// Constants
// ─────────────────────────────

/** StateFlow購読タイムアウト（ミリ秒） */
private const val FLOW_TIMEOUT_MS = 5000L
