use axum::{Json, http::StatusCode};
use serde::Serialize;

#[derive(Serialize)]
pub struct HealthResponse {
    status: String,
    message: String,
}

pub async fn health_check() -> (StatusCode, Json<HealthResponse>) {
    (
        StatusCode::OK, 
        Json(HealthResponse {
            status: "ok".to_string(),
            message: "Taskio API is running".to_string(),
        })
    )
}