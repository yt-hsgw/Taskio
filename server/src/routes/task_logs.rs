use axum::{extract::{State, Path}, Json, http::StatusCode};
use uuid::Uuid;
use chrono::{Utc};

use crate::state::AppState;
use crate::models::task_log::{TaskLog, CreateTaskLog};

pub async fn list_logs_for_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
) -> Result<Json<Vec<TaskLog>>, (StatusCode, Json<serde_json::Value>)> {
    let id = match Uuid::parse_str(&task_id) {
        Ok(u) => u,
        Err(_) => {
            return Err((
                StatusCode::BAD_REQUEST,
                Json(serde_json::json!({"error":"BadRequest","message":"invalid uuid"})),
            ));
        }
    };

    let logs = state.task_logs.lock().await;
    let vec: Vec<TaskLog> = logs.values().cloned().filter(|l| l.task_id == id).collect();
    Ok(Json(vec))
}

pub async fn create_log_for_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
    Json(payload): Json<CreateTaskLog>,
) -> Result<(StatusCode, Json<TaskLog>), (StatusCode, Json<serde_json::Value>)> {
    let id = match Uuid::parse_str(&task_id) {
        Ok(u) => u,
        Err(_) => {
            return Err((
                StatusCode::BAD_REQUEST,
                Json(serde_json::json!({"error":"BadRequest","message":"invalid uuid"})),
            ));
        }
    };

    // verify task exists
    let tasks = state.tasks.lock().await;
    if !tasks.contains_key(&id) {
        return Err((
            StatusCode::NOT_FOUND,
            Json(serde_json::json!({"error":"NotFound","message":"task not found"})),
        ));
    }
    drop(tasks);

    let now = Utc::now();
    let start = payload.start_at.unwrap_or(now);
    let end = payload.end_at;
    let duration = match end {
        Some(e) => Some((e.signed_duration_since(start)).num_minutes()),
        None => None,
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

    state.task_logs.lock().await.insert(log.id, log.clone());
    Ok((StatusCode::CREATED, Json(log)))
}

pub async fn get_log(
    State(state): State<std::sync::Arc<AppState>>,
    Path(log_id): Path<String>,
) -> Result<Json<TaskLog>, (StatusCode, Json<serde_json::Value>)> {
    let id = match Uuid::parse_str(&log_id) {
        Ok(u) => u,
        Err(_) => {
            return Err((
                StatusCode::BAD_REQUEST,
                Json(serde_json::json!({"error":"BadRequest","message":"invalid uuid"})),
            ));
        }
    };

    let logs = state.task_logs.lock().await;
    match logs.get(&id) {
        Some(l) => Ok(Json(l.clone())),
        None => Err((
            StatusCode::NOT_FOUND,
            Json(serde_json::json!({"error":"NotFound","message":"log not found"})),
        )),
    }
}

pub async fn update_log(
    State(state): State<std::sync::Arc<AppState>>,
    Path(log_id): Path<String>,
    Json(payload): Json<CreateTaskLog>,
) -> Result<Json<TaskLog>, (StatusCode, Json<serde_json::Value>)> {
    let id = match Uuid::parse_str(&log_id) {
        Ok(u) => u,
        Err(_) => {
            return Err((
                StatusCode::BAD_REQUEST,
                Json(serde_json::json!({"error":"BadRequest","message":"invalid uuid"})),
            ));
        }
    };

    let mut logs = state.task_logs.lock().await;
    match logs.get_mut(&id) {
        Some(l) => {
            if let Some(start) = payload.start_at {
                l.start_at = start;
            }
            if let Some(end) = payload.end_at {
                l.end_at = Some(end);
                l.duration_min = Some((end.signed_duration_since(l.start_at)).num_minutes());
            }
            l.memo = payload.memo.clone();
            l.updated_at = Utc::now();
            Ok(Json(l.clone()))
        }
        None => Err((
            StatusCode::NOT_FOUND,
            Json(serde_json::json!({"error":"NotFound","message":"log not found"})),
        )),
    }
}

pub async fn delete_log(
    State(state): State<std::sync::Arc<AppState>>,
    Path(log_id): Path<String>,
) -> (StatusCode, Json<serde_json::Value>) {
    let id = match Uuid::parse_str(&log_id) {
        Ok(u) => u,
        Err(_) => {
            return (StatusCode::BAD_REQUEST, Json(serde_json::json!({"error":"BadRequest","message":"invalid uuid"})));
        }
    };

    let mut logs = state.task_logs.lock().await;
    if logs.remove(&id).is_some() {
        (StatusCode::OK, Json(serde_json::json!({"result":"ok"})))
    } else {
        (StatusCode::NOT_FOUND, Json(serde_json::json!({"error":"NotFound","message":"log not found"})))
    }
}
