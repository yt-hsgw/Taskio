use axum::{extract::{State, Path}, Json, http::StatusCode};
use uuid::Uuid;
use chrono::{Utc};

use crate::state::AppState;
use crate::models::task_log::{TaskLog, CreateTaskLog};
use crate::errors::ApiError;
use crate::utils::{parse_uuid, ApiResult};

/// 特定タスクのログ一覧を取得
pub async fn list_logs_for_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
) -> ApiResult<Json<Vec<TaskLog>>> {
    let id = parse_uuid(&task_id)?;

    // タスクの存在確認
    let tasks = state.tasks.lock().await;
    if !tasks.contains_key(&id) || !tasks.get(&id).unwrap().is_active {
        return Err(ApiError::not_found("Task"));
    }
    drop(tasks);

    let logs = state.task_logs.lock().await;
    let vec: Vec<TaskLog> = logs.values()
        .filter(|l| l.task_id == id)
        .cloned()
        .collect();
    
    Ok(Json(vec))
}

/// 新規ログを作成（タスク開始）
pub async fn create_log_for_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
    Json(payload): Json<CreateTaskLog>,
) -> ApiResult<(StatusCode, Json<TaskLog>)> {
    let id = parse_uuid(&task_id)?;

    // タスクの存在確認
    let tasks = state.tasks.lock().await;
    if !tasks.contains_key(&id) || !tasks.get(&id).unwrap().is_active {
        return Err(ApiError::not_found("Task"));
    }
    drop(tasks);

    let now = Utc::now();
    let start = payload.start_at.unwrap_or(now);
    
    // 継続時間の計算
    let (end, duration) = match payload.end_at {
        Some(e) => {
            let duration = (e.signed_duration_since(start)).num_minutes();
            (Some(e), Some(duration))
        }
        None => (None, None),
    };

    let log = TaskLog {
        id: Uuid::new_v4(),
        task_id: id,
        start_at: start,
        end_at: end,
        duration_min: duration,
        memo: payload.memo,
        created_at: now,
        updated_at: now,
    };

    tracing::info!("Created log: id={}, task_id={}", log.id, log.task_id);
    
    state.task_logs.lock().await.insert(log.id, log.clone());
    Ok((StatusCode::CREATED, Json(log)))
}

/// ログ詳細を取得
pub async fn get_log(
    State(state): State<std::sync::Arc<AppState>>,
    Path(log_id): Path<String>,
) -> ApiResult<Json<TaskLog>> {
    let id = parse_uuid(&log_id)?;

    let logs = state.task_logs.lock().await;
    match logs.get(&id) {
        Some(l) => Ok(Json(l.clone())),
        None => Err(ApiError::not_found("TaskLog")),
    }
}

/// ログを更新（タスク終了）
pub async fn update_log(
    State(state): State<std::sync::Arc<AppState>>,
    Path(log_id): Path<String>,
    Json(payload): Json<CreateTaskLog>,
) -> ApiResult<Json<TaskLog>> {
    let id = parse_uuid(&log_id)?;

    let mut logs = state.task_logs.lock().await;
    match logs.get_mut(&id) {
        Some(l) => {
            // 開始時刻の更新
            if let Some(start) = payload.start_at {
                l.start_at = start;
            }
            
            // 終了時刻の更新と継続時間の計算
            if let Some(end) = payload.end_at {
                l.end_at = Some(end);
                l.duration_min = Some((end.signed_duration_since(l.start_at)).num_minutes());
            }
            
            // メモの更新
            if payload.memo.is_some() {
                l.memo = payload.memo.clone();
            }
            
            l.updated_at = Utc::now();
            
            tracing::info!("Updated log: id={}", id);
            Ok(Json(l.clone()))
        }
        None => Err(ApiError::not_found("TaskLog")),
    }
}

/// ログを削除
pub async fn delete_log(
    State(state): State<std::sync::Arc<AppState>>,
    Path(log_id): Path<String>,
) -> ApiResult<StatusCode> {
    let id = parse_uuid(&log_id)?;

    let mut logs = state.task_logs.lock().await;
    if logs.remove(&id).is_some() {
        tracing::info!("Deleted log: id={}", id);
        Ok(StatusCode::NO_CONTENT)
    } else {
        Err(ApiError::not_found("TaskLog"))
    }
}