use axum::{extract::{State, Path}, Json, http::StatusCode};
use uuid::Uuid;
use chrono::Utc;

use crate::state::AppState;
use crate::models::task::{Task, CreateTask};

pub async fn list_tasks(State(state): State<std::sync::Arc<AppState>>) -> Json<Vec<Task>> {
    let tasks = state.tasks.lock().await;
    let vec: Vec<Task> = tasks.values().cloned().collect();
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
        name: payload.name,
        description: payload.description,
        is_active: true,
        created_at: now,
        updated_at: now,
    };

    state.tasks.lock().await.insert(id, task.clone());
    (StatusCode::CREATED, Json(task))
}

pub async fn get_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
) -> Result<Json<Task>, (StatusCode, Json<serde_json::Value>)> {
    let id = match Uuid::parse_str(&task_id) {
        Ok(u) => u,
        Err(_) => {
            return Err((
                StatusCode::BAD_REQUEST,
                Json(serde_json::json!({"error":"BadRequest","message":"invalid uuid"})),
            ));
        }
    };

    let tasks = state.tasks.lock().await;
    match tasks.get(&id) {
        Some(t) => Ok(Json(t.clone())),
        None => Err((
            StatusCode::NOT_FOUND,
            Json(serde_json::json!({"error":"NotFound","message":"task not found"})),
        )),
    }
}

pub async fn update_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
    Json(payload): Json<CreateTask>,
) -> Result<Json<Task>, (StatusCode, Json<serde_json::Value>)> {
    let id = match Uuid::parse_str(&task_id) {
        Ok(u) => u,
        Err(_) => {
            return Err((
                StatusCode::BAD_REQUEST,
                Json(serde_json::json!({"error":"BadRequest","message":"invalid uuid"})),
            ));
        }
    };

    let mut tasks = state.tasks.lock().await;
    match tasks.get_mut(&id) {
        Some(t) => {
            t.name = payload.name;
            t.description = payload.description;
            t.updated_at = Utc::now();
            Ok(Json(t.clone()))
        }
        None => Err((
            StatusCode::NOT_FOUND,
            Json(serde_json::json!({"error":"NotFound","message":"task not found"})),
        )),
    }
}

pub async fn delete_task(
    State(state): State<std::sync::Arc<AppState>>,
    Path(task_id): Path<String>,
) -> (StatusCode, Json<serde_json::Value>) {
    let id = match Uuid::parse_str(&task_id) {
        Ok(u) => u,
        Err(_) => {
            return (StatusCode::BAD_REQUEST, Json(serde_json::json!({"error":"BadRequest","message":"invalid uuid"})));
        }
    };

    let mut tasks = state.tasks.lock().await;
    if let Some(t) = tasks.get_mut(&id) {
        t.is_active = false;
        t.updated_at = Utc::now();
        (StatusCode::OK, Json(serde_json::json!({"result":"ok"})))
    } else {
        (StatusCode::NOT_FOUND, Json(serde_json::json!({"error":"NotFound","message":"task not found"})))
    }
}
