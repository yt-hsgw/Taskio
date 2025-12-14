package com.yt_hsgw.taskio.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskResponse(
    val id: String,
    val title: String,
    val description: String?
)