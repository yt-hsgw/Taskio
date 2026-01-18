package com.yt_hsgw.taskio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.api.RetrofitClient
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.model.TaskRequest
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

data class TaskUiState(
    val tasks: List<TaskItem> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarDates: List<LocalDate> = emptyList(),
    val title: String = "",
    val description: String = "",
    val scheduledDate: LocalDateTime? = null,
    val isRecurring: Boolean = false,
    val selectedDays: Set<Int> = emptySet(),
    // 日付ごとのタスク状態を管理: Key = "taskId_yyyy-MM-dd", Value = TaskDayState
    val taskDayStates: Map<String, TaskDayState> = emptyMap(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val taskCreated: Boolean = false
)

/**
 * 特定の日付におけるタスクの状態
 */
data class TaskDayState(
    val isStarted: Boolean = false,
    val isFinished: Boolean = false,
    val startedAt: LocalDateTime? = null,
    val finishedAt: LocalDateTime? = null
)

/**
 * タスクとその日の状態を組み合わせたデータクラス
 */
data class TaskWithDayState(
    val task: TaskItem,
    val dayState: TaskDayState
) {
    val id: String get() = task.id
    val title: String get() = task.title
    val description: String? get() = task.description
    val isRecurring: Boolean get() = task.isRecurring
    val isStarted: Boolean get() = dayState.isStarted
    val isFinished: Boolean get() = dayState.isFinished
}

class TaskViewModel : ViewModel() {

    companion object {
        /**
         * タスクIDと日付からキーを生成
         */
        fun createTaskDayKey(taskId: String, date: LocalDate): String {
            return "${taskId}_${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        }
    }

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    // 選択された日付に基づいたタスクのフィルタリング（日付別状態付き）
    val filteredTasks: StateFlow<List<TaskWithDayState>> = _uiState
        .map { state ->
            val filtered = filterTasksForDate(state.tasks, state.selectedDate)
            filtered.map { task ->
                val key = createTaskDayKey(task.id, state.selectedDate)
                val dayState = state.taskDayStates[key] ?: TaskDayState()
                TaskWithDayState(
                    task = task,
                    dayState = dayState
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        setupCalendar()
        fetchTasks()
    }

    /**
     * 指定された日付に該当するタスクをフィルタリング
     * - 繰り返しタスク: 選択日の曜日が repeat_days に含まれていればマッチ
     * - 単発タスク: scheduled_date が選択日と一致すればマッチ
     * - 日付未指定タスク: すべての日に表示
     */
    private fun filterTasksForDate(tasks: List<TaskItem>, date: LocalDate): List<TaskItem> {
        val dayOfWeekIndex = when (date.dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }

        return tasks.filter { task ->
            when {
                // 繰り返しタスク
                task.isRecurring && task.repeatDays != null -> {
                    task.repeatDays.contains(dayOfWeekIndex)
                }
                // 予定日が設定されているタスク
                task.scheduledDate != null -> {
                    try {
                        val taskDate = LocalDate.parse(
                            task.scheduledDate.substringBefore("T")
                        )
                        taskDate == date
                    } catch (e: Exception) {
                        false
                    }
                }
                // 日付未指定のタスク（すべての日に表示）
                else -> true
            }
        }
    }

    private fun setupCalendar() {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
        _uiState.update { it.copy(calendarDates = weekDates, selectedDate = today) }
    }

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
                    // APIエラー時はモックデータを使用（開発用）
                    loadMockTasks()
                }
            } catch (e: Exception) {
                // ネットワークエラー時はモックデータを使用（開発用）
                loadMockTasks()
            }
        }
    }

    private fun loadMockTasks() {
        val today = LocalDate.now()
        val mockTasks = listOf(
            TaskItem(
                id = "1",
                title = "ジム",
                description = "脚トレ",
                createdAt = today.toString(),
                repeatDays = listOf(1, 3, 5), // 月・水・金
                isRecurring = true
            ),
            TaskItem(
                id = "2",
                title = "読書",
                description = "1時間",
                createdAt = today.toString(),
                repeatDays = listOf(0, 1, 2, 3, 4, 5, 6), // 毎日
                isRecurring = true
            ),
            TaskItem(
                id = "3",
                title = "歯医者",
                description = "定期検診",
                createdAt = today.toString(),
                scheduledDate = today.plusDays(2).toString(),
                isRecurring = false
            ),
            TaskItem(
                id = "4",
                title = "買い物",
                description = null,
                createdAt = today.toString(),
                isRecurring = false
            )
        )
        _uiState.update { it.copy(tasks = mockTasks, loading = false) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    /**
     * タスクの開始状態をトグル（選択中の日付に対して）
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
            state.copy(
                taskDayStates = state.taskDayStates + (key to newState)
            )
        }
    }

    /**
     * タスクの終了状態をトグル（選択中の日付に対して）
     */
    fun toggleFinish(taskId: String) {
        val date = _uiState.value.selectedDate
        val key = createTaskDayKey(taskId, date)

        _uiState.update { state ->
            val currentState = state.taskDayStates[key] ?: TaskDayState()
            // 開始していない場合は終了できない
            if (!currentState.isStarted) return@update state

            val newState = currentState.copy(
                isFinished = !currentState.isFinished,
                finishedAt = if (!currentState.isFinished) LocalDateTime.now() else null
            )
            state.copy(
                taskDayStates = state.taskDayStates + (key to newState)
            )
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun updateDescription(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun updateScheduledDate(date: LocalDateTime?) {
        _uiState.update { it.copy(scheduledDate = date) }
    }

    fun toggleRecurring(isRecurring: Boolean) {
        _uiState.update {
            it.copy(
                isRecurring = isRecurring,
                selectedDays = if (!isRecurring) emptySet() else it.selectedDays,
                scheduledDate = if (isRecurring) null else it.scheduledDate
            )
        }
    }

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

    suspend fun createTask() {
        val currentTitle = _uiState.value.title.trim()
        val currentDescription = _uiState.value.description.trim().takeIf { it.isNotEmpty() }
        val currentState = _uiState.value

        if (currentTitle.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "タイトルを入力してください") }
            return
        }

        if (currentState.isRecurring && currentState.selectedDays.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "繰り返しタスクは少なくとも1つの曜日を選択してください") }
            return
        }

        _uiState.update { it.copy(loading = true, errorMessage = null, taskCreated = false) }

        try {
            val request = TaskRequest(
                title = currentTitle,
                description = currentDescription,
                due_date = null,
                scheduled_date = currentState.scheduledDate?.format(
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                ),
                repeat_days = if (currentState.isRecurring) {
                    currentState.selectedDays.toList()
                } else null
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
                // APIが使えない場合はローカルで追加（開発用）
                addTaskLocally(currentTitle, currentDescription, currentState)
            }
        } catch (e: Exception) {
            // ネットワークエラー時はローカルで追加（開発用）
            addTaskLocally(currentTitle, currentDescription, currentState)
        }
    }

    private fun addTaskLocally(title: String, description: String?, state: TaskUiState) {
        val newTask = TaskItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            description = description,
            createdAt = LocalDate.now().toString(),
            scheduledDate = state.scheduledDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            repeatDays = if (state.isRecurring) state.selectedDays.toList() else null,
            isRecurring = state.isRecurring
        )

        _uiState.update { currentState ->
            currentState.copy(
                tasks = currentState.tasks + newTask,
                loading = false,
                title = "",
                description = "",
                scheduledDate = null,
                isRecurring = false,
                selectedDays = emptySet(),
                taskCreated = true
            )
        }
    }

    fun resetTaskCreated() {
        _uiState.update { it.copy(taskCreated = false) }
    }
}