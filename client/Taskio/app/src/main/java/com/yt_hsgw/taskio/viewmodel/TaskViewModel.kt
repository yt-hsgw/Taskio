package com.yt_hsgw.taskio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.api.RetrofitClient
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.model.TaskRequest
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
 */
data class TaskDayState(
    val isStarted: Boolean = false,
    val isFinished: Boolean = false,
    val startedAt: LocalDateTime? = null,
    val finishedAt: LocalDateTime? = null
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
     * - 繰り返しタスク: 選択日の曜日が repeat_days に含まれていればマッチ
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
                    task.repeatDays.contains(dayOfWeekIndex)
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
     *
     * 取得に失敗した場合はモックデータを使用します（開発用）。
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
                    // TODO: モックをコメントアウト（サーバー通信確認用）
                    // loadMockTasks()
                    _uiState.update { it.copy(loading = false, errorMessage = "タスクの取得に失敗しました: ${response.code()}") }
                }
            } catch (e: Exception) {
                // TODO: モックをコメントアウト（サーバー通信確認用）
                // loadMockTasks()
                _uiState.update { it.copy(loading = false, errorMessage = "サーバー接続エラー: ${e.message}") }
            }
        }
    }

    // TODO: モックをコメントアウト（サーバー通信確認用）
    // /**
    //  * モックタスクデータを読み込み（開発用）
    //  */
    // private fun loadMockTasks() {
    //     val today = LocalDate.now()
    //     val mockTasks = listOf(
    //         TaskItem(
    //             id = "1",
    //             title = "ジム",
    //             description = "脚トレ",
    //             createdAt = today.toString(),
    //             repeatDays = listOf(1, 3, 5),
    //             isRecurring = true
    //         ),
    //         TaskItem(
    //             id = "2",
    //             title = "読書",
    //             description = "1時間",
    //             createdAt = today.toString(),
    //             repeatDays = listOf(0, 1, 2, 3, 4, 5, 6),
    //             isRecurring = true
    //         ),
    //         TaskItem(
    //             id = "3",
    //             title = "歯医者",
    //             description = "定期検診",
    //             createdAt = today.toString(),
    //             scheduledDate = today.plusDays(2).toString(),
    //             isRecurring = false
    //         ),
    //         TaskItem(
    //             id = "4",
    //             title = "買い物",
    //             description = null,
    //             createdAt = today.toString(),
    //             isRecurring = false
    //         )
    //     )
    //     _uiState.update { it.copy(tasks = mockTasks, loading = false) }
    // }

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
    }

    // ─────────────────────────────
    // タスク状態管理
    // ─────────────────────────────

    /**
     * タスクの開始状態をトグル
     *
     * 選択中の日付に対してタスクの開始状態を切り替えます。
     *
     * @param taskId タスクID
     */
    fun toggleStart(taskId: String) {
        val date = _uiState.value.selectedDate
        val key = createTaskDayKey(taskId, date)

        _uiState.update { state ->
            val currentState = state.taskDayStates[key] ?: TaskDayState()
            val newState = currentState.copy(
                isStarted = !currentState.isStarted,
                startedAt = if (!currentState.isStarted) LocalDateTime.now() else null
            )
            state.copy(taskDayStates = state.taskDayStates + (key to newState))
        }
    }

    /**
     * タスクの終了状態をトグル
     *
     * 選択中の日付に対してタスクの終了状態を切り替えます。
     * 開始していない場合は終了できません。
     *
     * @param taskId タスクID
     */
    fun toggleFinish(taskId: String) {
        val date = _uiState.value.selectedDate
        val key = createTaskDayKey(taskId, date)

        _uiState.update { state ->
            val currentState = state.taskDayStates[key] ?: TaskDayState()
            if (!currentState.isStarted) return@update state

            val newState = currentState.copy(
                isFinished = !currentState.isFinished,
                finishedAt = if (!currentState.isFinished) LocalDateTime.now() else null
            )
            state.copy(taskDayStates = state.taskDayStates + (key to newState))
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
     * サーバーが利用できない場合はローカルで追加します（開発用）。
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
            val scheduledDateString = currentState.scheduledDate?.let {
                it.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
            }
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
                // TODO: モックをコメントアウト（サーバー通信確認用）
                // addTaskLocally(currentTitle, currentDescription, currentState)
                _uiState.update { it.copy(loading = false, errorMessage = "タスクの作成に失敗しました: ${response.code()}") }
            }
        } catch (e: Exception) {
            // TODO: モックをコメントアウト（サーバー通信確認用）
            // addTaskLocally(currentTitle, currentDescription, currentState)
            _uiState.update { it.copy(loading = false, errorMessage = "サーバー接続エラー: ${e.message}") }
        }
    }

    // TODO: モックをコメントアウト（サーバー通信確認用）
    // /**
    //  * タスクをローカルで追加（開発用）
    //  */
    // private fun addTaskLocally(title: String, description: String?, state: TaskUiState) {
    //     val newTask = TaskItem(
    //         id = System.currentTimeMillis().toString(),
    //         title = title,
    //         description = description,
    //         createdAt = LocalDate.now().toString(),
    //         scheduledDate = state.scheduledDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    //         repeatDays = if (state.isRecurring) state.selectedDays.toList() else null,
    //         isRecurring = state.isRecurring
    //     )
    //
    //     _uiState.update { currentState ->
    //         currentState.copy(
    //             tasks = currentState.tasks + newTask,
    //             loading = false,
    //             title = "",
    //             description = "",
    //             scheduledDate = null,
    //             isRecurring = false,
    //             selectedDays = emptySet(),
    //             taskCreated = true
    //         )
    //     }
    // }

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
}

// ─────────────────────────────
// Constants
// ─────────────────────────────

/** StateFlow購読タイムアウト（ミリ秒） */
private const val FLOW_TIMEOUT_MS = 5000L
