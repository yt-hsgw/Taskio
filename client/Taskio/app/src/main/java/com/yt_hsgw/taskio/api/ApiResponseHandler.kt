package com.yt_hsgw.taskio.api

import com.squareup.moshi.Moshi
import com.yt_hsgw.taskio.model.ApiErrorResponse
import retrofit2.Response

object ApiResponseHandler {
    private val moshi = Moshi.Builder().build()
    private val errorAdapter = moshi.adapter(ApiErrorResponse::class.java)

    fun <T> handleResponse(response: Response<T>): ApiResult<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    code = response.code(),
                    message = "Empty response body"
                )
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = errorBody?.let {
                try {
                    errorAdapter.fromJson(it)?.message
                } catch (e: Exception) {
                    null
                }
            } ?: "Unknown error (${response.code()})"

            ApiResult.Error(
                code = response.code(),
                message = errorMessage
            )
        }
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
        return try {
            handleResponse(apiCall())
        } catch (e: Exception) {
            ApiResult.Exception(e)
        }
    }
}