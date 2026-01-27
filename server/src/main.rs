//! Taskio Server
//!
//! タスクログ管理APIサーバーのエントリーポイント。
//!
//! # Architecture
//!
//! - **Framework**: Axum
//! - **Runtime**: Tokio
//! - **Storage**: In-memory (将来的にSQLx + PostgreSQL)
//!
//! # API Endpoints
//!
//! ## Health
//! - `GET /api/v1/health` - ヘルスチェック
//!
//! ## Tasks
//! - `GET /api/v1/tasks` - タスク一覧
//! - `POST /api/v1/tasks` - タスク作成
//! - `GET /api/v1/tasks/{task_id}` - タスク詳細
//! - `PUT /api/v1/tasks/{task_id}` - タスク更新
//! - `DELETE /api/v1/tasks/{task_id}` - タスク削除
//!
//! ## Task Logs
//! - `GET /api/v1/tasks/{task_id}/logs` - 特定タスクのログ一覧
//! - `POST /api/v1/tasks/{task_id}/logs` - ログ作成
//! - `GET /api/v1/logs?from=YYYY-MM-DD&to=YYYY-MM-DD` - 期間指定ログ取得（カレンダー用）
//! - `GET /api/v1/logs/{log_id}` - ログ詳細
//! - `PUT /api/v1/logs/{log_id}` - ログ更新
//! - `DELETE /api/v1/logs/{log_id}` - ログ削除

use axum::{
    routing::{delete, get, post, put},
    Router,
};
use std::net::SocketAddr;
use std::sync::Arc;
use tower_http::cors::{Any, CorsLayer};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

mod config;
mod errors;
mod models;
mod routes;
mod state;
mod utils;

use state::AppState;

// ─────────────────────────────
// 定数
// ─────────────────────────────

/// APIバージョンプレフィックス
const API_PREFIX: &str = "/api/v1";

/// デフォルトのログレベル
const DEFAULT_LOG_LEVEL: &str = "taskio_server=debug";

/// 環境変数：ログレベル
const ENV_RUST_LOG: &str = "RUST_LOG";

// ─────────────────────────────
// メイン関数
// ─────────────────────────────

#[tokio::main]
async fn main() {
    // ロギング初期化
    init_tracing();

    // アプリケーション状態の初期化
    let state = Arc::new(AppState::new());

    // ルーターの構築
    let app = build_router(state);

    // サーバー起動
    let addr = SocketAddr::from(([0, 0, 0, 0], config::get_port()));
    tracing::info!(%addr, "Server starting");

    let listener = tokio::net::TcpListener::bind(addr)
        .await
        .expect("Failed to bind address");

    axum::serve(listener, app)
        .await
        .expect("Server failed to start");
}

// ─────────────────────────────
// 初期化関数
// ─────────────────────────────

/// トレーシング（ロギング）を初期化
fn init_tracing() {
    tracing_subscriber::registry()
        .with(tracing_subscriber::EnvFilter::new(
            std::env::var(ENV_RUST_LOG).unwrap_or_else(|_| DEFAULT_LOG_LEVEL.to_string()),
        ))
        .with(tracing_subscriber::fmt::layer())
        .init();
}

/// アプリケーションルーターを構築
///
/// CORSミドルウェアとすべてのAPIルートを設定します。
fn build_router(state: Arc<AppState>) -> Router {
    // CORSミドルウェア（開発用に全許可）
    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    // APIルート
    let api = build_api_routes();

    Router::new()
        .nest(API_PREFIX, api)
        .with_state(state)
        .layer(cors)
}

/// APIルートを構築
fn build_api_routes() -> Router<Arc<AppState>> {
    Router::new()
        // Health
        .route("/health", get(routes::health::health_check))
        // Tasks
        .route("/tasks", get(routes::tasks::list_tasks))
        .route("/tasks", post(routes::tasks::create_task))
        .route("/tasks/:task_id", get(routes::tasks::get_task))
        .route("/tasks/:task_id", put(routes::tasks::update_task))
        .route("/tasks/:task_id", delete(routes::tasks::delete_task))
        // Task Logs (per task)
        .route(
            "/tasks/:task_id/logs",
            get(routes::task_logs::list_logs_for_task).post(routes::task_logs::create_log_for_task),
        )
        // Calendar Logs (date range query) - 新規追加
        .route("/logs", get(routes::calendar::get_logs_by_date_range))
        // Task Logs (direct)
        .route("/logs/:log_id", get(routes::task_logs::get_log))
        .route("/logs/:log_id", put(routes::task_logs::update_log))
        .route("/logs/:log_id", delete(routes::task_logs::delete_log))
}