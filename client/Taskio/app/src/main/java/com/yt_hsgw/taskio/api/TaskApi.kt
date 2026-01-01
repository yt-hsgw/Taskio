package com.yt_hsgw.taskio.api

import com.yt_hsgw.taskio.model.TaskRequest
import com.yt_hsgw.taskio.model.TaskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskApi {

    @GET("health")
    suspend fun health(): Response<String>

    @POST("tasks")
    suspend fun createTask(@Body req: TaskRequest): Response<TaskResponse>

    @GET("tasks")
    suspend fun listTasks(): Response<List<TaskResponse>>

    @GET("tasks/{task_id}")
    suspend fun getTask(@Path("task_id") taskId: String): Response<TaskResponse>

    @DELETE("tasks/{task_id}")
    suspend fun deleteTask(@Path("task_id") taskId: String): Response<Unit>
}