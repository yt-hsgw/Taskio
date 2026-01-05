package com.yt_hsgw.taskio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.model.TaskItem
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
import java.time.temporal.TemporalAdjusters

data class TaskUiState(
    val tasks: List<TaskItem> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarDates: List<LocalDate> = emptyList(),
    val title: String = "",
    val description: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null
)

class TaskViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    // 選択された日付に基づいたタスクのフィルタリング
    val filteredTasks: StateFlow<List<TaskItem>> = _uiState
        .map { state ->
            state.tasks.filter { it.createdAt.startsWith(state.selectedDate.toString()) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        setupCalendar()
        fetchTasks()
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
            // API通信を想定。モックデータを作成
            val mockTasks = listOf(
                TaskItem("1", "ジム", "脚トレ", LocalDate.now().toString(), isStarted = true),
                TaskItem("2", "ランニング", "5km", LocalDate.now().toString(), isStarted = true, isFinished = true),
                TaskItem("3", "z", null, LocalDate.now().plusDays(1).toString()),
                TaskItem("4", "a", null, LocalDate.now().plusDays(1).toString()),
                TaskItem("5", "b", null, LocalDate.now().plusDays(1).toString()),
                TaskItem("6", "c", null, LocalDate.now().plusDays(1).toString()),
                TaskItem("7", "d", null, LocalDate.now().plusDays(1).toString()),
                TaskItem("8", "e", null, LocalDate.now().plusDays(1).toString()),
            )
            _uiState.update { it.copy(tasks = mockTasks, loading = false) }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun toggleStart(taskId: String) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { if (it.id == taskId) it.copy(isStarted = !it.isStarted) else it })
        }
    }

    fun toggleFinish(taskId: String) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { if (it.id == taskId) it.copy(isFinished = !it.isFinished) else it })
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

    suspend fun createTask() {
        // TaskScreen.kt のロジックをここに実装
        // 作成成功後に fetchTasks() を呼ぶ
    }
}