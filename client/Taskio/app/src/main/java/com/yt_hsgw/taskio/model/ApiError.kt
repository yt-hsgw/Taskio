package com.yt_hsgw.taskio.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    val error: String,
    val message: String
)