use axum::{
    http::StatusCode,
    response::{IntoResponse, Response},
    Json,
};
use serde_json::json;
use thiserror::Error;

#[derive(Debug, Error)]
pub enum ApiError {
    #[error("Not found: {0}")]
    NotFound(String),

    #[error("Bad request: {0}")]
    BadRequest(String),

    #[error("Invalid UUID: {0}")]
    InvalidUuid(String),

    #[error("Database error: {0}")]
    Database(String),

    #[error("Internal server error: {0}")]
    Internal(String),
}

impl ApiError {
    pub fn not_found(resource: &str) -> Self {
        ApiError::NotFound(resource.to_string())
    }

    pub fn bad_request(msg: &str) -> Self {
        ApiError::BadRequest(msg.to_string())
    }

    pub fn invalid_uuid() -> Self {
        ApiError::InvalidUuid("Invalid UUID format".to_string())
    }
}

impl IntoResponse for ApiError {
    fn into_response(self) -> Response {
        let (status, error_type, message) = match self {
            ApiError::NotFound(msg) => (StatusCode::NOT_FOUND, "NotFound", msg),
            ApiError::BadRequest(msg) => (StatusCode::BAD_REQUEST, "BadRequest", msg),
            ApiError::InvalidUuid(msg) => (StatusCode::BAD_REQUEST, "InvalidUuid", msg),
            ApiError::Database(msg) => {
                tracing::error!("Database error: {}", msg);
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    "DatabaseError",
                    "Internal server error".to_string(),
                )
            }
            ApiError::Internal(msg) => {
                tracing::error!("Internal error: {}", msg);
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    "InternalError",
                    "Internal server error".to_string(),
                )
            }
        };

        let body = Json(json!({
            "error": error_type,
            "message": message,
        }));

        (status, body).into_response()
    }
}

// From implementations for common error types
impl From<uuid::Error> for ApiError {
    fn from(_: uuid::Error) -> Self {
        ApiError::invalid_uuid()
    }
}

// 将来のDB実装用
// impl From<sqlx::Error> for ApiError {
//     fn from(err: sqlx::Error) -> Self {
//         ApiError::Database(err.to_string())
//     }
// }