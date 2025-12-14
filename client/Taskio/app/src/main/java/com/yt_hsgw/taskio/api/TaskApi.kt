package com.yt_hsgw.taskio.api

import com.yt_hsgw.taskio.model.TaskRequest
import com.yt_hsgw.taskio.model.TaskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface TaskApi {

    @GET("health")
    suspend fun health(): Response<String>

    @POST("tasks")
    suspend fun createTask(@Body req: TaskRequest): Response<TaskResponse>

    @GET("tasks")
    suspend fun listTasks(): Response<List<TaskResponse>>
}