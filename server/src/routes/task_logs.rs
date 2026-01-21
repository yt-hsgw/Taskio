//! タスクログ管理エンドポイント
//!
//! タスクログのCRUD操作を提供するエンドポイントを定義します。
//! タスクの開始・終了記録を管理します。

use axum::{
    extract::{Path, State},
    http::StatusCode,
    Json,
};
use std::sync::Arc;

use crate::errors::ApiError;
use crate::models::task_log::{CreateTaskLog, TaskLog};
use crate::state::AppState;
use crate::utils::{parse_uuid, ApiResult};

// ─────────────────────────────
// リソース名定数
// ─────────────────────────────

const RESOURCE_TASK: &str = "Task";
const RESOURCE_TASK_LOG: &str = "TaskLog";

// ─────────────────────────────
// タスク関連のログハンドラ
// ─────────────────────────────

/// 特定タスクのログ一覧を取得
///
/// # Endpoint
///
/// `GET /api/v1/tasks/{task_id}/logs`
///
/// # Path Parameters
///
/// * `task_id` - タスクID（UUID形式）
///
/// # Returns
///
/// * `200 OK` - ログ一覧
/// * `404 Not Found` - タスクが存在しない
pub async fn list_logs_for_task(
    State(state): State<Arc<AppState>>,
    Path(task_id): Path<String>,
) -> ApiResult<Json<Vec<TaskLog>>> {
    let id = parse_uuid(&task_id)?;

    // タスクの存在確認
    {
        let tasks = state.tasks.lock().await;
        if !is_task_active(&tasks, &id) {
            return Err(ApiError::not_found(RESOURCE_TASK));
        }
    }

    let logs = state.task_logs.lock().await;
    let task_logs: Vec<TaskLog> = logs
        .values()
        .filter(|log| log.task_id == id)
        .cloned()
        .collect();

    Ok(Json(task_logs))
}

/// 新規ログを作成（タスク開始）
///
/// # Endpoint
///
/// `POST /api/v1/tasks/{task_id}/logs`
///
/// # Path Parameters
///
/// * `task_id` - タスクID（UUID形式）
///
/// # Request Body
///
/// ```json
/// {
///     "start_at": "2025-01-15T10:00:00Z",
///     "memo": "Morning session"
/// }
/// ```
///
/// # Returns
///
/// * `201 Created` - 作成されたログ
/// * `404 Not Found` - タスクが存在しない
pub async fn create_log_for_task(
    State(state): State<Arc<AppState>>,
    Path(task_id): Path<String>,
    Json(payload): Json<CreateTaskLog>,
) -> ApiResult<(StatusCode, Json<TaskLog>)> {
    let id = parse_uuid(&task_id)?;

    // タスクの存在確認
    {
        let tasks = state.tasks.lock().await;
        if !is_task_active(&tasks, &id) {
            return Err(ApiError::not_found(RESOURCE_TASK));
        }
    }

    let log = payload.into_task_log(id);

    tracing::info!(
        log_id = %log.id,
        task_id = %log.task_id,
        "Created task log"
    );

    state.task_logs.lock().await.insert(log.id, log.clone());

    Ok((StatusCode::CREATED, Json(log)))
}

// ─────────────────────────────
// ログ単体のハンドラ
// ─────────────────────────────

/// ログ詳細を取得
///
/// # Endpoint
///
/// `GET /api/v1/logs/{log_id}`
///
/// # Path Parameters
///
/// * `log_id` - ログID（UUID形式）
///
/// # Returns
///
/// * `200 OK` - ログ詳細
/// * `404 Not Found` - ログが存在しない
pub async fn get_log(
    State(state): State<Arc<AppState>>,
    Path(log_id): Path<String>,
) -> ApiResult<Json<TaskLog>> {
    let id = parse_uuid(&log_id)?;

    let logs = state.task_logs.lock().await;
    match logs.get(&id) {
        Some(log) => Ok(Json(log.clone())),
        None => Err(ApiError::not_found(RESOURCE_TASK_LOG)),
    }
}

/// ログを更新（タスク終了）
///
/// # Endpoint
///
/// `PUT /api/v1/logs/{log_id}`
///
/// # Path Parameters
///
/// * `log_id` - ログID（UUID形式）
///
/// # Request Body
///
/// ```json
/// {
///     "end_at": "2025-01-15T11:00:00Z",
///     "memo": "Good session"
/// }
/// ```
///
/// # Returns
///
/// * `200 OK` - 更新されたログ
/// * `404 Not Found` - ログが存在しない
pub async fn update_log(
    State(state): State<Arc<AppState>>,
    Path(log_id): Path<String>,
    Json(payload): Json<CreateTaskLog>,
) -> ApiResult<Json<TaskLog>> {
    let id = parse_uuid(&log_id)?;

    let mut logs = state.task_logs.lock().await;
    match logs.get_mut(&id) {
        Some(log) => {
            log.update(
                payload.start_at,
                payload.end_at,
                payload.memo,
                payload.is_completed,
            );

            tracing::info!(log_id = %id, "Updated task log");
            Ok(Json(log.clone()))
        }
        None => Err(ApiError::not_found(RESOURCE_TASK_LOG)),
    }
}

/// ログを削除
///
/// # Endpoint
///
/// `DELETE /api/v1/logs/{log_id}`
///
/// # Path Parameters
///
/// * `log_id` - ログID（UUID形式）
///
/// # Returns
///
/// * `204 No Content` - 削除成功
/// * `404 Not Found` - ログが存在しない
pub async fn delete_log(
    State(state): State<Arc<AppState>>,
    Path(log_id): Path<String>,
) -> ApiResult<StatusCode> {
    let id = parse_uuid(&log_id)?;

    let mut logs = state.task_logs.lock().await;
    if logs.remove(&id).is_some() {
        tracing::info!(log_id = %id, "Deleted task log");
        Ok(StatusCode::NO_CONTENT)
    } else {
        Err(ApiError::not_found(RESOURCE_TASK_LOG))
    }
}

// ─────────────────────────────
// ヘルパー関数
// ─────────────────────────────

/// タスクがアクティブかどうかを確認
fn is_task_active(
    tasks: &std::collections::HashMap<uuid::Uuid, crate::models::task::Task>,
    task_id: &uuid::Uuid,
) -> bool {
    tasks
        .get(task_id)
        .map_or(false, |task| task.is_active)
}
