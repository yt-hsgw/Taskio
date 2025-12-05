use axum::{routing::get, Router};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

mod routes;
mod config;

#[tokio::main] 
async fn main() {
    // ログの初期化
    tracing_subscriber::registry()
    .with(tracing_subscriber::EnvFilter::new(
        std::env::var("RUST_LOG").unwrap_or_else(|_| "taskio-server=debug".to_string()),
    ))
    .with(tracing_subscriber::fmt::layer())
    .init();

    // ルーターの設定
    let app = Router::new()
        .route("/health", get(routes::health::health_check));

    // ポートの読み込み(.env → default: 3000)
    let port = config::get_port();
    let addr = format!("0.0.0.0:{}", port);

    tracing::info!("🚀 Server running at http://{}", addr);

    let listener = tokio::net::TcpListener::bind(addr)
        .await
        .expect("Failed to bind to address");

    axum::serve(listener, app)
        .await
        .expect("Failed to start server");
}
