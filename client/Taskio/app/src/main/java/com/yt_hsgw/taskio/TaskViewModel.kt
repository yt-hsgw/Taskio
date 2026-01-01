package com.yt_hsgw.taskio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.api.RetrofitClient
import com.yt_hsgw.taskio.api.TaskApi
import com.yt_hsgw.taskio.model.TaskRequest
import com.yt_hsgw.taskio.model.TaskResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskUiState(
    val title: String = "",
    val description: String = "",
    val tasks: List<TaskResponse> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null
)

class TaskViewModel(
    private val api: TaskApi = RetrofitClient.api
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState = _uiState.asStateFlow()

    // 初期化時にタスクを読み込む
    init {
        listTasks()
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun createTask() {
        // 入力チェック
        if (_uiState.value.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "タイトルを入力してください") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, errorMessage = null) }

                val request = TaskRequest(
                    title = _uiState.value.title.trim(),
                    description = _uiState.value.description.trim().ifBlank { null }
                )

                val response = api.createTask(request)

                if (response.isSuccessful) {
                    // 成功後、入力欄をクリアしてリストを再取得
                    _uiState.update { it.copy(title = "", description = "") }
                    listTasks()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.update { 
                        it.copy(
                            loading = false,
                            errorMessage = "タスクの作成に失敗しました: ${response.code()}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        loading = false,
                        errorMessage = "ネットワークエラー: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun listTasks() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, errorMessage = null) }

                val response = api.listTasks()

                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            tasks = tasks,
                            loading = false
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            loading = false,
                            errorMessage = "タスクの取得に失敗しました: ${response.code()}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        loading = false,
                        errorMessage = "ネットワークエラー: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                val response = api.deleteTask(taskId)
                
                if (response.isSuccessful) {
                    // 成功したらリストを更新
                    listTasks()
                } else {
                    _uiState.update {
                        it.copy(errorMessage = "タスクの削除に失敗しました")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "ネットワークエラー")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}