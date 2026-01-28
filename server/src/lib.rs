//! Taskio Server Library
//!
//! タスクログ管理APIサーバーのライブラリクレート。
//! 統合テストからアクセス可能な公開APIを提供します。

use axum::{
    routing::{delete, get, post, put},
    Router,
};
use std::sync::Arc;
use tower_http::cors::{Any, CorsLayer};

pub mod config;
pub mod errors;
pub mod models;
pub mod routes;
pub mod state;
pub mod utils;

pub use state::AppState;

/// APIバージョンプレフィックス
pub const API_PREFIX: &str = "/api/v1";

/// アプリケーションルーターを構築
///
/// CORSミドルウェアとすべてのAPIルートを設定します。
pub fn build_router(state: Arc<AppState>) -> Router {
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
        // Calendar Logs (date range query)
        .route("/logs", get(routes::calendar::get_logs_by_date_range))
        // Task Logs (direct)
        .route("/logs/:log_id", get(routes::task_logs::get_log))
        .route("/logs/:log_id", put(routes::task_logs::update_log))
        .route("/logs/:log_id", delete(routes::task_logs::delete_log))
}
