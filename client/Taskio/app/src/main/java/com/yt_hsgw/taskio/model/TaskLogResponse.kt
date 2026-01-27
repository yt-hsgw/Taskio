package com.yt_hsgw.taskio.model

import com.squareup.moshi.JsonClass

/**
 * タスクログのAPIレスポンスモデル
 *
 * サーバーから返されるタスクログデータを表現します。
 *
 * @property id ログID（UUID形式）
 * @property task_id 紐づくタスクID
 * @property start_at 開始日時（ISO 8601形式、任意）
 * @property end_at 終了日時（ISO 8601形式、任意）
 * @property duration_minutes 所要時間（分、任意）
 * @property memo メモ（任意）
 * @property created_at 作成日時
 * @property updated_at 更新日時
 */
@JsonClass(generateAdapter = true)
data class TaskLogResponse(
    val id: String,
    val task_id: String,
    val start_at: String? = null,
    val end_at: String? = null,
    val duration_minutes: Int? = null,
    val memo: String? = null,
    val created_at: String,
    val updated_at: String
)

/**
 * タスクログ作成リクエストモデル
 *
 * @property start_at 開始日時（ISO 8601形式、任意）
 * @property end_at 終了日時（ISO 8601形式、任意）
 * @property memo メモ（任意）
 * @property target_date 対象日（ISO 8601形式、任意）
 * @property is_completed 完了状態（任意）
 */
@JsonClass(generateAdapter = true)
data class TaskLogRequest(
    val start_at: String? = null,
    val end_at: String? = null,
    val memo: String? = null,
    val target_date: String? = null,
    val is_completed: Boolean? = null
)