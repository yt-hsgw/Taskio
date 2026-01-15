package com.yt_hsgw.taskio.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskRequest(
    val title: String,
    val description: String?,
    val due_date: String?,
    val scheduled_date: String?,
    val repeat_days: List<Int>?
)