package com.yt_hsgw.taskio.api

/**
 * API呼び出し結果を表すシールドクラス
 *
 * 成功、エラー、例外の3つの状態を型安全に表現します。
 *
 * @param T 成功時のデータ型
 */
sealed class ApiResult<out T> {
    /**
     * API呼び出し成功
     *
     * @property data 取得したデータ
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * APIエラー（HTTPエラーレスポンス）
     *
     * @property code HTTPステータスコード
     * @property message エラーメッセージ
     */
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()

    /**
     * 例外発生（ネットワークエラー等）
     *
     * @property throwable 発生した例外
     */
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>()

    /**
     * 成功かどうかを判定
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * エラーかどうかを判定
     */
    val isError: Boolean get() = this is Error

    /**
     * 例外かどうかを判定
     */
    val isException: Boolean get() = this is Exception

    /**
     * 成功時のデータを取得（失敗時はnull）
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * 成功時のデータを取得（失敗時はデフォルト値）
     *
     * @param default デフォルト値
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }
}
