package com.yt_hsgw.taskio.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * タスクのステータス
 */
enum class TaskStatus {
    /** 未開始 */
    @Json(name = "not_started")
    NOT_STARTED,
    /** 進行中 */
    @Json(name = "in_progress")
    IN_PROGRESS,
    /** 完了 */
    @Json(name = "completed")
    COMPLETED
}

/**
 * カレンダー用タスクのAPIレスポンスモデル
 *
 * 期間指定でタスクを取得した際のレスポンスデータです。
 * 予定日が設定されたタスクの情報を含みます。
 *
 * @property id タスクID（UUID形式）
 * @property task_id 紐づくタスクID
 * @property task_title タスクタイトル
 * @property task_description タスク説明（任意）
 * @property memo メモ（任意）
 * @property created_at 作成日時
 * @property scheduled_date 予定日（任意）
 * @property status タスクのステータス
 */
@JsonClass(generateAdapter = true)
data class CalendarLogResponse(
    val id: String,
    val task_id: String,
    val task_title: String,
    val task_description: String? = null,
    val memo: String? = null,
    val created_at: String,
    val scheduled_date: String? = null,
    val status: TaskStatus = TaskStatus.NOT_STARTED
)

/**
 * カレンダー用ログ一覧のラッパーレスポンス
 *
 * @property logs ログ一覧
 */
@JsonClass(generateAdapter = true)
data class CalendarLogsResponse(
    val logs: List<CalendarLogResponse>
)
