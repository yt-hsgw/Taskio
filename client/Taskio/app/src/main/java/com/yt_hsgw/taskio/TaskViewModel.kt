package com.yt_hsgw.taskio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.api.RetrofitClient
import com.yt_hsgw.taskio.api.TaskApi
import com.yt_hsgw.taskio.model.TaskRequest
import com.yt_hsgw.taskio.model.TaskResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun createTask() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)

                val request = TaskRequest(
                    title = _uiState.value.title,
                    description = _uiState.value.description
                )

                val response = api.createTask(request)

                if (response.isSuccessful) {
                    listTasks()
                    _uiState.value = _uiState.value.copy(
                        title = "",
                        description = ""
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        errorMessage = response.message()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = e.message
                )
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
                println(_uiState.value)
            }
        }
    }

    fun listTasks() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)

                val response = api.listTasks()

                if (response.isSuccessful) {
                    // Corrected line: Map the API response to the model response
                    val tasksFromApi = response.body() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        tasks = tasksFromApi.map { apiTask ->
                            com.yt_hsgw.taskio.model.TaskResponse(
                                id = apiTask.id,
                                title = apiTask.title,
                                description = apiTask.description
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = response.message())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(loading = false)
            }
        }
    }
}