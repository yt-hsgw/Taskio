//! API エラー処理モジュール
//!
//! このモジュールは、API全体で使用されるエラー型と
//! エラーレスポンスのフォーマットを提供します。

use axum::{
    http::StatusCode,
    response::{IntoResponse, Response},
    Json,
};
use serde_json::json;
use thiserror::Error;

// ─────────────────────────────
// エラーメッセージ定数
// ─────────────────────────────

/// エラータイプ名
mod error_types {
    pub const NOT_FOUND: &str = "NotFound";
    pub const BAD_REQUEST: &str = "BadRequest";
    pub const INVALID_UUID: &str = "InvalidUuid";
    pub const DATABASE_ERROR: &str = "DatabaseError";
    pub const INTERNAL_ERROR: &str = "InternalError";
}

/// エラーメッセージ
mod error_messages {
    pub const INVALID_UUID_FORMAT: &str = "Invalid UUID format";
    pub const INTERNAL_SERVER_ERROR: &str = "Internal server error";
}

// ─────────────────────────────
// ApiError 定義
// ─────────────────────────────

/// API エラー型
///
/// すべてのAPIエラーをこの型で表現します。
/// Axumの`IntoResponse`トレイトを実装しており、
/// 適切なHTTPレスポンスに変換されます。
///
/// # Example
///
/// ```rust
/// use crate::errors::ApiError;
///
/// fn get_task(id: &str) -> Result<Task, ApiError> {
///     let uuid = Uuid::parse_str(id)?;
///     tasks.get(&uuid).ok_or_else(|| ApiError::not_found("Task"))
/// }
/// ```
#[derive(Debug, Error)]
pub enum ApiError {
    /// リソースが見つからない
    #[error("Not found: {0}")]
    NotFound(String),

    /// リクエストが不正
    #[error("Bad request: {0}")]
    BadRequest(String),

    /// UUID形式が不正
    #[error("Invalid UUID: {0}")]
    InvalidUuid(String),

    /// データベースエラー
    #[error("Database error: {0}")]
    Database(String),

    /// 内部サーバーエラー
    #[error("Internal server error: {0}")]
    Internal(String),
}

impl ApiError {
    /// リソースが見つからないエラーを生成
    ///
    /// # Arguments
    ///
    /// * `resource` - リソース名（例: "Task", "TaskLog"）
    pub fn not_found(resource: &str) -> Self {
        ApiError::NotFound(resource.to_string())
    }

    /// リクエスト不正エラーを生成
    ///
    /// # Arguments
    ///
    /// * `msg` - エラーメッセージ
    pub fn bad_request(msg: &str) -> Self {
        ApiError::BadRequest(msg.to_string())
    }

    /// UUID不正エラーを生成
    pub fn invalid_uuid() -> Self {
        ApiError::InvalidUuid(error_messages::INVALID_UUID_FORMAT.to_string())
    }

    /// データベースエラーを生成
    ///
    /// # Arguments
    ///
    /// * `msg` - エラーメッセージ
    #[allow(dead_code)]
    pub fn database(msg: &str) -> Self {
        ApiError::Database(msg.to_string())
    }

    /// 内部エラーを生成
    ///
    /// # Arguments
    ///
    /// * `msg` - エラーメッセージ
    #[allow(dead_code)]
    pub fn internal(msg: &str) -> Self {
        ApiError::Internal(msg.to_string())
    }
}

impl IntoResponse for ApiError {
    fn into_response(self) -> Response {
        let (status, error_type, message) = match self {
            ApiError::NotFound(msg) => (
                StatusCode::NOT_FOUND,
                error_types::NOT_FOUND,
                msg,
            ),
            ApiError::BadRequest(msg) => (
                StatusCode::BAD_REQUEST,
                error_types::BAD_REQUEST,
                msg,
            ),
            ApiError::InvalidUuid(msg) => (
                StatusCode::BAD_REQUEST,
                error_types::INVALID_UUID,
                msg,
            ),
            ApiError::Database(msg) => {
                tracing::error!("Database error: {}", msg);
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    error_types::DATABASE_ERROR,
                    error_messages::INTERNAL_SERVER_ERROR.to_string(),
                )
            }
            ApiError::Internal(msg) => {
                tracing::error!("Internal error: {}", msg);
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    error_types::INTERNAL_ERROR,
                    error_messages::INTERNAL_SERVER_ERROR.to_string(),
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

// ─────────────────────────────
// From 実装
// ─────────────────────────────

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

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_not_found_error() {
        let error = ApiError::not_found("Task");
        assert!(matches!(error, ApiError::NotFound(_)));
    }

    #[test]
    fn test_bad_request_error() {
        let error = ApiError::bad_request("Invalid input");
        assert!(matches!(error, ApiError::BadRequest(_)));
    }

    #[test]
    fn test_invalid_uuid_error() {
        let error = ApiError::invalid_uuid();
        assert!(matches!(error, ApiError::InvalidUuid(_)));
    }
}
