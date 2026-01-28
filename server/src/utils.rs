//! ユーティリティモジュール
//!
//! API全体で使用される共通のヘルパー関数と型を定義します。

use crate::errors::ApiError;
use uuid::Uuid;

/// APIハンドラの戻り値型
///
/// 成功時は`T`、エラー時は`ApiError`を返します。
///
/// # Example
///
/// ```rust,ignore
/// use taskio_server::utils::ApiResult;
/// use axum::Json;
///
/// async fn get_item(id: String) -> ApiResult<Json<Item>> {
///     let uuid = parse_uuid(&id)?;
///     // ...
/// }
/// ```
pub type ApiResult<T> = Result<T, ApiError>;

/// UUID文字列をパース
///
/// 文字列形式のUUIDをパースし、`Uuid`型に変換します。
/// パースに失敗した場合は`ApiError::InvalidUuid`を返します。
///
/// # Arguments
///
/// * `id_str` - パース対象のUUID文字列
///
/// # Returns
///
/// * `Ok(Uuid)` - パース成功
/// * `Err(ApiError)` - パース失敗
///
/// # Example
///
/// ```rust,ignore
/// use taskio_server::utils::parse_uuid;
///
/// let uuid = parse_uuid("550e8400-e29b-41d4-a716-446655440000")?;
/// ```
pub fn parse_uuid(id_str: &str) -> ApiResult<Uuid> {
    Uuid::parse_str(id_str).map_err(|_| ApiError::invalid_uuid())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_parse_uuid_valid() {
        let uuid_str = "550e8400-e29b-41d4-a716-446655440000";
        let result = parse_uuid(uuid_str);
        
        assert!(result.is_ok());
    }

    #[test]
    fn test_parse_uuid_invalid() {
        let invalid_str = "not-a-uuid";
        let result = parse_uuid(invalid_str);
        
        assert!(result.is_err());
    }

    #[test]
    fn test_parse_uuid_empty() {
        let result = parse_uuid("");
        
        assert!(result.is_err());
    }
}
