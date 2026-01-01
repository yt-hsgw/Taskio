package com.yt_hsgw.taskio.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskLogResponse(
    val id: String,
    val task_id: String,
    val start_at: String,
    val end_at: String?,
    val duration_min: Long?,
    val memo: String?,
    val created_at: String,
    val updated_at: String
)

@JsonClass(generateAdapter = true)
data class CreateTaskLogRequest(
    val start_at: String? = null,
    val end_at: String? = null,
    val memo: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateTaskLogRequest(
    val start_at: String? = null,
    val end_at: String? = null,
    val memo: String? = null
)