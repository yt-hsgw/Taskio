//! ヘルスチェックエンドポイント
//!
//! サーバーの稼働状態を確認するためのエンドポイントを提供します。

use axum::{http::StatusCode, Json};
use serde::Serialize;

/// ヘルスチェックレスポンス
#[derive(Serialize)]
pub struct HealthResponse {
    /// ステータス（"ok" = 正常稼働）
    status: String,
}

impl HealthResponse {
    /// 正常なヘルスチェックレスポンスを生成
    fn ok() -> Self {
        Self {
            status: STATUS_OK.to_string(),
        }
    }
}

/// ヘルスチェック
///
/// サーバーの稼働状態を確認します。
///
/// # Endpoint
///
/// `GET /api/v1/health`
///
/// # Returns
///
/// * `200 OK` - サーバーが正常に稼働している
///
/// # Example Response
///
/// ```json
/// {
///     "status": "ok"
/// }
/// ```
pub async fn health_check() -> (StatusCode, Json<HealthResponse>) {
    (StatusCode::OK, Json(HealthResponse::ok()))
}

/// ステータス定数
const STATUS_OK: &str = "ok";

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_health_check() {
        let (status, Json(response)) = health_check().await;
        
        assert_eq!(status, StatusCode::OK);
        assert_eq!(response.status, "ok");
    }
}
