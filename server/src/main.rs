use axum::{routing::get, Router, routing::post, routing::put, routing::delete};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};
use std::net::SocketAddr;
use std::sync::Arc;
use tower_http::cors::{Any, CorsLayer};

mod config;
mod state;
mod routes;
mod models;
mod utils;
mod errors;

use state::AppState;

#[tokio::main]
async fn main() {
    tracing_subscriber::registry()
        .with(tracing_subscriber::EnvFilter::new(
            std::env::var("RUST_LOG").unwrap_or_else(|_| "taskio-server=debug".to_string()),
        ))
        .with(tracing_subscriber::fmt::layer())
        .init();

    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    let state = Arc::new(AppState::new());

    let api = Router::new()
        .route("/health", get(routes::health::health_check))
        .route("/tasks", get(routes::tasks::list_tasks))
        .route("/tasks", post(routes::tasks::create_task))
        .route("/tasks/:task_id", get(routes::tasks::get_task))
        .route("/tasks/:task_id", put(routes::tasks::update_task))
        .route("/tasks/:task_id", delete(routes::tasks::delete_task))
        .route("/tasks/:task_id/logs", get(routes::task_logs::list_logs_for_task))
        .route("/tasks/:task_id/logs", post(routes::task_logs::create_log_for_task))
        .route("/logs/:log_id", get(routes::task_logs::get_log))
        .route("/logs/:log_id", put(routes::task_logs::update_log))
        .route("/logs/:log_id", delete(routes::task_logs::delete_log));

    let app = Router::new()
        .nest("/api/v1", api)
        .with_state(state)
        .layer(cors);

    let addr = SocketAddr::from(([0, 0, 0, 0], config::get_port()));
    tracing::info!(%addr, "Server running");
    
    // 修正箇所：TokioのリスナーとAxumのserve関数を使用
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();

    axum::serve(listener, app)
        .await
        .unwrap();
}