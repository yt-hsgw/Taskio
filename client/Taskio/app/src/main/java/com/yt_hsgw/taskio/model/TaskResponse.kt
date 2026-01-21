package com.yt_hsgw.taskio.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskResponse(
    val id: String,
    val title: String,
    val description: String?,
    val is_active: Boolean,
    val due_date: String?,
    val scheduled_date: String?,
    val repeat_days: List<Int>?,
    val is_recurring: Boolean,
    val created_at: String,
    val updated_at: String
)