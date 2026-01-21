//! タスク管理エンドポイント
//!
//! タスクのCRUD操作を提供するエンドポイントを定義します。

use axum::{
    extract::{Path, State},
    http::StatusCode,
    Json,
};
use std::sync::Arc;

use crate::errors::ApiError;
use crate::models::task::{CreateTask, Task};
use crate::state::AppState;
use crate::utils::{parse_uuid, ApiResult};

// ─────────────────────────────
// リソース名定数
// ─────────────────────────────

const RESOURCE_TASK: &str = "Task";

// ─────────────────────────────
// ハンドラ
// ─────────────────────────────

/// タスク一覧を取得
///
/// アクティブなタスクのみを返します。
///
/// # Endpoint
///
/// `GET /api/v1/tasks`
///
/// # Returns
///
/// * `200 OK` - タスク一覧
///
/// # Example Response
///
/// ```json
/// [
///     {
///         "id": "uuid",
///         "title": "Workout",
///         "description": "Gym training",
///         "is_active": true,
///         ...
///     }
/// ]
/// ```
pub async fn list_tasks(State(state): State<Arc<AppState>>) -> Json<Vec<Task>> {
    let tasks = state.tasks.lock().await;
    let active_tasks: Vec<Task> = tasks
        .values()
        .filter(|t| t.is_active)
        .cloned()
        .collect();
    
    Json(active_tasks)
}

/// タスクを作成
///
/// # Endpoint
///
/// `POST /api/v1/tasks`
///
/// # Request Body
///
/// ```json
/// {
///     "title": "Workout",
///     "description": "Gym training",
///     "scheduled_date": "2025-01-15T10:00:00Z",
///     "repeat_days": [1, 3, 5]
/// }
/// ```
///
/// # Returns
///
/// * `201 Created` - 作成されたタスク
pub async fn create_task(
    State(state): State<Arc<AppState>>,
    Json(payload): Json<CreateTask>,
) -> (StatusCode, Json<Task>) {
    let task = payload.into_task();

    tracing::info!(
        task_id = %task.id,
        title = %task.title,
        "Creating task"
    );

    state.tasks.lock().await.insert(task.id, task.clone());
    
    (StatusCode::CREATED, Json(task))
}

/// タスク詳細を取得
///
/// # Endpoint
///
/// `GET /api/v1/tasks/{task_id}`
///
/// # Path Parameters
///
/// * `task_id` - タスクID（UUID形式）
///
/// # Returns
///
/// * `200 OK` - タスク詳細
/// * `404 Not Found` - タスクが存在しない
pub async fn get_task(
    State(state): State<Arc<AppState>>,
    Path(task_id): Path<String>,
) -> ApiResult<Json<Task>> {
    let id = parse_uuid(&task_id)?;

    let tasks = state.tasks.lock().await;
    match tasks.get(&id) {
        Some(task) if task.is_active => Ok(Json(task.clone())),
        _ => Err(ApiError::not_found(RESOURCE_TASK)),
    }
}

/// タスクを更新
///
/// # Endpoint
///
/// `PUT /api/v1/tasks/{task_id}`
///
/// # Path Parameters
///
/// * `task_id` - タスクID（UUID形式）
///
/// # Request Body
///
/// ```json
/// {
///     "title": "Updated Workout",
///     "description": "Updated description"
/// }
/// ```
///
/// # Returns
///
/// * `200 OK` - 更新されたタスク
/// * `404 Not Found` - タスクが存在しない
pub async fn update_task(
    State(state): State<Arc<AppState>>,
    Path(task_id): Path<String>,
    Json(payload): Json<CreateTask>,
) -> ApiResult<Json<Task>> {
    let id = parse_uuid(&task_id)?;

    let mut tasks = state.tasks.lock().await;
    match tasks.get_mut(&id) {
        Some(task) if task.is_active => {
            task.update(
                payload.title,
                payload.description,
                payload.due_date,
                payload.scheduled_date,
                payload.repeat_days,
            );

            tracing::info!(task_id = %id, "Updated task");
            Ok(Json(task.clone()))
        }
        _ => Err(ApiError::not_found(RESOURCE_TASK)),
    }
}

/// タスクを削除（論理削除）
///
/// 物理削除ではなく、`is_active`フラグをfalseに設定します。
///
/// # Endpoint
///
/// `DELETE /api/v1/tasks/{task_id}`
///
/// # Path Parameters
///
/// * `task_id` - タスクID（UUID形式）
///
/// # Returns
///
/// * `204 No Content` - 削除成功
/// * `404 Not Found` - タスクが存在しない
pub async fn delete_task(
    State(state): State<Arc<AppState>>,
    Path(task_id): Path<String>,
) -> ApiResult<StatusCode> {
    let id = parse_uuid(&task_id)?;

    let mut tasks = state.tasks.lock().await;
    match tasks.get_mut(&id) {
        Some(task) if task.is_active => {
            task.soft_delete();
            tracing::info!(task_id = %id, "Deleted task");
            Ok(StatusCode::NO_CONTENT)
        }
        _ => Err(ApiError::not_found(RESOURCE_TASK)),
    }
}
