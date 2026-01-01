use axum::{Json, http::StatusCode};
use crate::errors::ApiError;
use uuid::Uuid;

pub type ApiResult<T> = Result<T, ApiError>;

/// UUID文字列をパースするヘルパー関数
pub fn parse_uuid(id_str: &str) -> ApiResult<Uuid> {
    Uuid::parse_str(id_str).map_err(|_| ApiError::invalid_uuid())
}

/// Not Found エラーを返すヘルパー関数
pub fn not_found(resource: &str) -> (StatusCode, Json<serde_json::Value>) {
    (
        StatusCode::NOT_FOUND,
        Json(serde_json::json!({
            "error": "NotFound",
            "message": format!("{} not found", resource)
        })),
    )
}