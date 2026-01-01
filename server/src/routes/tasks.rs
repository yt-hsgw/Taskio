use axum::{extract::{State, Path}, Json, http::StatusCode};
use uuid::Uuid;
use chrono::Utc;

use crate::state::AppState;
use crate::models::task::{Task, CreateTask};
use crate::errors::ApiError;
use crate::utils::{parse_uuid, ApiResult};

/// タスク一覧を取得（アクティブなタスクのみ）
pub async fn list_tasks(State(state): State<std::sync::Arc<AppState>>) -> Json<Vec<Task>> {
    let tasks = state.tasks.lock().await;
    let vec: Vec<Task> = tasks.values()
        .filter(|t| t.is_active)  
        .cloned()
        .collect();
    Json(vec)
}

pub async fn create_task(
    State(state): State<std::sync::Arc<AppState>>,
    Json(payload): Json<CreateTask>,
) -> (StatusCode, Json<Task>) {
    let id = Uuid::new_v4();
    let now = Utc::now();
    let task = Task {
        id,
        title: payload.title,
        description: payload.description,
        is_active: true,
        created_at: now,
        updated_at: now,
    };

    tracing::info!("Creating task: id={}, title={}", task.id, task.title);
    
    state.tasks.lock().await.insert(id, task.clone());
    (StatusCode::CREATED, Json(task))
}

/// タスク詳細を取得
pub async fn get_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
) -> ApiResult<Json<Task>> {
    let id = parse_uuid(&task_id)?;

    let tasks = state.tasks.lock().await;
    match tasks.get(&id) {
        Some(t) if t.is_active => Ok(Json(t.clone())),
        Some(_) => Err(ApiError::not_found("Task")),  // 非アクティブは Not Found
        None => Err(ApiError::not_found("Task")),
    }
}

/// タスクを更新
pub async fn update_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
    Json(payload): Json<CreateTask>,
) -> ApiResult<Json<Task>> {
    let id = parse_uuid(&task_id)?;

    let mut tasks = state.tasks.lock().await;
    match tasks.get_mut(&id) {
        Some(t) if t.is_active => {
            t.title = payload.title;
            t.description = payload.description;
            t.updated_at = Utc::now();
            
            tracing::info!("Updated task: id={}", id);
            Ok(Json(t.clone()))
        }
        Some(_) => Err(ApiError::not_found("Task")),  // 非アクティブは更新不可
        None => Err(ApiError::not_found("Task")),
    }
}

/// タスクを削除（論理削除）
pub async fn delete_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
) -> ApiResult<StatusCode> {
    let id = parse_uuid(&task_id)?;

    let mut tasks = state.tasks.lock().await;
    match tasks.get_mut(&id) {
        Some(t) if t.is_active => {
            t.is_active = false;
            t.updated_at = Utc::now();
            
            tracing::info!("Deleted task: id={}", id);
            Ok(StatusCode::NO_CONTENT)  // 204 No Content
        }
        Some(_) => Err(ApiError::not_found("Task")),  // 既に削除済み
        None => Err(ApiError::not_found("Task")),
    }
}