package com.yt_hsgw.taskio.model

/**
 * タスクのドメインモデル
 *
 * APIレスポンスから変換されたアプリ内で使用するタスクデータ。
 *
 * @property id タスクID
 * @property title タスクタイトル
 * @property description タスクの説明（nullable）
 * @property createdAt 作成日時（ISO-8601形式）
 * @property isStarted 開始済みかどうか
 * @property isFinished 終了済みかどうか
 * @property dueDate 期限日時（nullable、ISO-8601形式）
 * @property scheduledDate 予定日時（nullable、ISO-8601形式）
 * @property repeatDays 繰り返し曜日のリスト（0=日曜日, 6=土曜日）
 * @property isRecurring 繰り返しタスクかどうか
 */
data class TaskItem(
    val id: String,
    val title: String,
    val description: String?,
    val createdAt: String,
    val isStarted: Boolean = false,
    val isFinished: Boolean = false,
    val dueDate: String? = null,
    val scheduledDate: String? = null,
    val repeatDays: List<Int>? = null,
    val isRecurring: Boolean = false
)

/**
 * TaskResponseをTaskItemに変換
 *
 * @receiver 変換元のTaskResponse
 * @return 変換後のTaskItem
 */
fun TaskResponse.toTaskItem() = TaskItem(
    id = id,
    title = title,
    description = description,
    createdAt = created_at,
    dueDate = due_date,
    scheduledDate = scheduled_date,
    repeatDays = repeat_days,
    isRecurring = is_recurring
)
