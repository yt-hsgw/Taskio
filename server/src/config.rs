//! 設定モジュール
//!
//! 環境変数からの設定読み込みを提供します。

use dotenvy::dotenv;
use std::env;

// ─────────────────────────────
// 定数
// ─────────────────────────────

/// ポート番号の環境変数名
const ENV_PORT: &str = "PORT";

/// デフォルトポート番号
const DEFAULT_PORT: u16 = 8080;

// ─────────────────────────────
// 設定関数
// ─────────────────────────────

/// サーバーポート番号を取得
///
/// 環境変数`PORT`からポート番号を読み込みます。
/// 設定されていない場合はデフォルト値（8080）を使用します。
///
/// # Returns
///
/// ポート番号
///
/// # Panics
///
/// ポート番号が数値としてパースできない場合にパニックします。
///
/// # Example
///
/// ```rust,ignore
/// use taskio_server::config::get_port;
///
/// let port = get_port();
/// println!("Server will listen on port {}", port);
/// ```
pub fn get_port() -> u16 {
    // .envファイルを読み込み（存在しない場合は無視）
    dotenv().ok();

    env::var(ENV_PORT)
        .unwrap_or_else(|_| DEFAULT_PORT.to_string())
        .parse()
        .expect("PORT must be a valid number")
}

/// サーバー設定
///
/// 将来的な拡張用の設定構造体。
/// 現在はポート番号のみですが、将来的にはデータベース接続情報なども含める予定です。
#[derive(Debug, Clone)]
#[allow(dead_code)]
pub struct ServerConfig {
    /// サーバーポート番号
    pub port: u16,
    /// ホストアドレス
    pub host: String,
}

impl ServerConfig {
    /// 環境変数から設定を読み込み
    #[allow(dead_code)]
    pub fn from_env() -> Self {
        dotenv().ok();

        Self {
            port: get_port(),
            host: env::var("HOST").unwrap_or_else(|_| DEFAULT_HOST.to_string()),
        }
    }
}

impl Default for ServerConfig {
    fn default() -> Self {
        Self {
            port: DEFAULT_PORT,
            host: DEFAULT_HOST.to_string(),
        }
    }
}

/// デフォルトホストアドレス
const DEFAULT_HOST: &str = "0.0.0.0";

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_default_port() {
        // 環境変数がない場合のデフォルト値をテスト
        let config = ServerConfig::default();
        assert_eq!(config.port, DEFAULT_PORT);
    }

    #[test]
    fn test_default_host() {
        let config = ServerConfig::default();
        assert_eq!(config.host, DEFAULT_HOST);
    }
}
