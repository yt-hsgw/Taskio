package com.yt_hsgw.taskio.api

import com.yt_hsgw.taskio.model.TaskLogResponse
import com.yt_hsgw.taskio.model.TaskRequest
import com.yt_hsgw.taskio.model.TaskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Taskio REST API インターフェース
 *
 * タスクとタスクログのCRUD操作を提供します。
 */
interface TaskApi {

    // ─────────────────────────────
    // Health
    // ─────────────────────────────

    /**
     * ヘルスチェック
     */
    @GET("health")
    suspend fun health(): Response<String>

    // ─────────────────────────────
    // Tasks
    // ─────────────────────────────

    /**
     * タスク作成
     *
     * @param req タスク作成リクエスト
     * @return 作成されたタスク
     */
    @POST("tasks")
    suspend fun createTask(@Body req: TaskRequest): Response<TaskResponse>

    /**
     * タスク一覧取得
     *
     * @return タスク一覧
     */
    @GET("tasks")
    suspend fun listTasks(): Response<List<TaskResponse>>

    /**
     * タスク詳細取得
     *
     * @param taskId タスクID
     * @return タスク詳細
     */
    @GET("tasks/{task_id}")
    suspend fun getTask(@Path("task_id") taskId: String): Response<TaskResponse>

    /**
     * タスク更新
     *
     * @param taskId タスクID
     * @param req タスク更新リクエスト
     * @return 更新されたタスク
     */
    @PUT("tasks/{task_id}")
    suspend fun updateTask(
        @Path("task_id") taskId: String,
        @Body req: TaskRequest
    ): Response<TaskResponse>

    /**
     * タスク削除
     *
     * @param taskId タスクID
     */
    @DELETE("tasks/{task_id}")
    suspend fun deleteTask(@Path("task_id") taskId: String): Response<Unit>

    // ─────────────────────────────
    // Task Logs
    // ─────────────────────────────

    /**
     * 特定タスクのログ一覧取得
     *
     * @param taskId タスクID
     * @return ログ一覧
     */
    @GET("tasks/{task_id}/logs")
    suspend fun getTaskLogs(@Path("task_id") taskId: String): Response<List<TaskLogResponse>>

    /**
     * 全タスクのログを取得（簡易版：各タスクのログを取得）
     * 注意: この実装はクライアント側で各タスクのログを個別に取得します
     */
    // 注: サーバー側に全ログ取得APIが必要な場合は別途追加
}
