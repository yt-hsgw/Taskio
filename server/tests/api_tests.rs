//! API統合テスト
//!
//! Taskio APIエンドポイントの統合テストを提供します。

use axum::{
    body::Body,
    http::{Request, StatusCode},
};
use http_body_util::BodyExt;
use serde_json::{json, Value};
use std::sync::Arc;
use tower::ServiceExt;

use taskio_server::{build_router, AppState};

// ─────────────────────────────
// ヘルパー関数
// ─────────────────────────────

/// テスト用アプリケーションを作成
fn create_test_app() -> axum::Router {
    let state = Arc::new(AppState::new());
    build_router(state)
}

/// レスポンスボディをJSONとして取得
async fn body_to_json(body: Body) -> Value {
    let bytes = body.collect().await.unwrap().to_bytes();
    serde_json::from_slice(&bytes).unwrap_or(Value::Null)
}

// ─────────────────────────────
// ヘルスチェックテスト
// ─────────────────────────────

#[tokio::test]
async fn test_health_check() {
    let app = create_test_app();

    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/health")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);
}

// ─────────────────────────────
// タスク作成テスト
// ─────────────────────────────

#[tokio::test]
async fn test_create_task_success() {
    let app = create_test_app();

    let payload = json!({
        "title": "Test Task",
        "description": "This is a test task"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::CREATED);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["title"], "Test Task");
    assert_eq!(body["description"], "This is a test task");
    assert!(body["id"].is_string());
    assert_eq!(body["is_active"], true);
}

#[tokio::test]
async fn test_create_task_minimal() {
    let app = create_test_app();

    // タイトルのみの最小リクエスト
    let payload = json!({
        "title": "Minimal Task"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::CREATED);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["title"], "Minimal Task");
    assert!(body["description"].is_null() || body.get("description").is_none());
}

#[tokio::test]
async fn test_create_task_with_repeat_days() {
    let app = create_test_app();

    let payload = json!({
        "title": "Recurring Task",
        "repeat_days": [1, 3, 5]
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::CREATED);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["is_recurring"], true);
    assert_eq!(body["repeat_days"], json!([1, 3, 5]));
}

// ─────────────────────────────
// タスク一覧テスト
// ─────────────────────────────

#[tokio::test]
async fn test_list_tasks() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let payload = json!({
        "title": "Task 1"
    });

    let _ = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    // タスク一覧を取得
    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/tasks")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert!(body.is_array());
    assert_eq!(body.as_array().unwrap().len(), 1);
}

// ─────────────────────────────
// タスク詳細テスト
// ─────────────────────────────

#[tokio::test]
async fn test_get_task_success() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let payload = json!({
        "title": "Task to Get"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // タスク詳細を取得
    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["title"], "Task to Get");
}

#[tokio::test]
async fn test_get_task_not_found() {
    let app = create_test_app();

    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/tasks/00000000-0000-0000-0000-000000000000")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NOT_FOUND);
}

#[tokio::test]
async fn test_get_task_invalid_uuid() {
    let app = create_test_app();

    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/tasks/invalid-uuid")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::BAD_REQUEST);
}

// ─────────────────────────────
// タスク更新テスト
// ─────────────────────────────

#[tokio::test]
async fn test_update_task() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let payload = json!({
        "title": "Original Title"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // タスクを更新
    let update_payload = json!({
        "title": "Updated Title",
        "description": "New description"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("PUT")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(update_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["title"], "Updated Title");
    assert_eq!(body["description"], "New description");
}

// ─────────────────────────────
// タスク削除テスト
// ─────────────────────────────

#[tokio::test]
async fn test_delete_task() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let payload = json!({
        "title": "Task to Delete"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // タスクを削除
    let response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("DELETE")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NO_CONTENT);

    // 削除後にタスクが取得できないことを確認
    let get_response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(get_response.status(), StatusCode::NOT_FOUND);
}

// ─────────────────────────────
// タスクログ作成テスト
// ─────────────────────────────

#[tokio::test]
async fn test_create_log() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let task_payload = json!({
        "title": "Task with Log"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // ログを作成
    let log_payload = json!({
        "start_at": "2025-01-01T10:00:00Z",
        "target_date": "2025-01-01T00:00:00Z"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("POST")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(log_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::CREATED);

    let body = body_to_json(response.into_body()).await;
    assert!(body["id"].is_string());
    assert_eq!(body["task_id"], task_id);
    assert_eq!(body["is_completed"], false);
}

// ─────────────────────────────
// タスクログ更新テスト
// ─────────────────────────────

#[tokio::test]
async fn test_update_log() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let task_payload = json!({
        "title": "Task with Log to Update"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // ログを作成
    let log_payload = json!({
        "start_at": "2025-01-01T10:00:00Z",
        "target_date": "2025-01-01T00:00:00Z"
    });

    let log_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(log_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let log_body = body_to_json(log_response.into_body()).await;
    let log_id = log_body["id"].as_str().unwrap();

    // ログを更新（終了時刻を設定）
    let update_payload = json!({
        "end_at": "2025-01-01T11:30:00Z",
        "is_completed": true
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("PUT")
                .uri(format!("/api/v1/logs/{}", log_id))
                .header("Content-Type", "application/json")
                .body(Body::from(update_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["is_completed"], true);
    assert_eq!(body["duration_min"], 90); // 1時間30分 = 90分
}

// ─────────────────────────────
// カレンダーログ取得テスト
// ─────────────────────────────

#[tokio::test]
async fn test_get_logs_by_date_range() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let task_payload = json!({
        "title": "Task for Calendar",
        "scheduled_date": "2025-01-15T00:00:00Z"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // ログを作成
    let log_payload = json!({
        "start_at": "2025-01-15T10:00:00Z",
        "target_date": "2025-01-15T00:00:00Z"
    });

    let _ = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(log_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    // カレンダーログを取得
    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/logs?from=2025-01-01&to=2025-01-31")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert!(body["logs"].is_array());
    assert!(body["logs"].as_array().unwrap().len() >= 1);
}

#[tokio::test]
async fn test_get_logs_by_date_range_empty() {
    let app = create_test_app();

    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/logs?from=2020-01-01&to=2020-01-31")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert!(body["logs"].is_array());
    assert_eq!(body["logs"].as_array().unwrap().len(), 0);
}

// ─────────────────────────────
// タスクログ削除テスト
// ─────────────────────────────

#[tokio::test]
async fn test_delete_log() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let task_payload = json!({
        "title": "Task with Log to Delete"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // ログを作成
    let log_payload = json!({
        "start_at": "2025-01-01T10:00:00Z",
        "target_date": "2025-01-01T00:00:00Z"
    });

    let log_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(log_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let log_body = body_to_json(log_response.into_body()).await;
    let log_id = log_body["id"].as_str().unwrap();

    // ログを削除
    let response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("DELETE")
                .uri(format!("/api/v1/logs/{}", log_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NO_CONTENT);

    // 削除後にログが取得できないことを確認
    let get_response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri(format!("/api/v1/logs/{}", log_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(get_response.status(), StatusCode::NOT_FOUND);
}

// ─────────────────────────────
// タスク別ログ一覧テスト
// ─────────────────────────────

#[tokio::test]
async fn test_list_logs_for_task() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let task_payload = json!({
        "title": "Task with Multiple Logs"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // 複数のログを作成
    for i in 1..=3 {
        let log_payload = json!({
            "start_at": format!("2025-01-0{}T10:00:00Z", i),
            "target_date": format!("2025-01-0{}T00:00:00Z", i)
        });

        let _ = app
            .clone()
            .oneshot(
                Request::builder()
                    .method("POST")
                    .uri(format!("/api/v1/tasks/{}/logs", task_id))
                    .header("Content-Type", "application/json")
                    .body(Body::from(log_payload.to_string()))
                    .unwrap(),
            )
            .await
            .unwrap();
    }

    // タスク別ログ一覧を取得
    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert!(body.is_array());
    assert_eq!(body.as_array().unwrap().len(), 3);
}

#[tokio::test]
async fn test_list_logs_for_task_empty() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成（ログなし）
    let task_payload = json!({
        "title": "Task without Logs"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // タスク別ログ一覧を取得
    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert!(body.is_array());
    assert_eq!(body.as_array().unwrap().len(), 0);
}

#[tokio::test]
async fn test_list_logs_for_nonexistent_task() {
    let app = create_test_app();

    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/tasks/00000000-0000-0000-0000-000000000000/logs")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NOT_FOUND);
}

// ─────────────────────────────
// 単一ログ取得テスト
// ─────────────────────────────

#[tokio::test]
async fn test_get_single_log() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let task_payload = json!({
        "title": "Task for Single Log Test"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // ログを作成
    let log_payload = json!({
        "start_at": "2025-01-15T10:00:00Z",
        "target_date": "2025-01-15T00:00:00Z",
        "memo": "Test memo"
    });

    let log_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(log_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let log_body = body_to_json(log_response.into_body()).await;
    let log_id = log_body["id"].as_str().unwrap();

    // 単一ログを取得
    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri(format!("/api/v1/logs/{}", log_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["id"], log_id);
    assert_eq!(body["task_id"], task_id);
    assert_eq!(body["memo"], "Test memo");
}

#[tokio::test]
async fn test_get_log_not_found() {
    let app = create_test_app();

    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/logs/00000000-0000-0000-0000-000000000000")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NOT_FOUND);
}

#[tokio::test]
async fn test_get_log_invalid_uuid() {
    let app = create_test_app();

    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/logs/invalid-uuid")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::BAD_REQUEST);
}

// ─────────────────────────────
// タスク更新エッジケーステスト
// ─────────────────────────────

#[tokio::test]
async fn test_update_task_to_recurring() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // 単発タスクを作成
    let payload = json!({
        "title": "Single Task"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();
    assert_eq!(create_body["is_recurring"], false);

    // 繰り返しタスクに更新
    let update_payload = json!({
        "title": "Recurring Task",
        "repeat_days": [1, 3, 5]
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("PUT")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(update_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["is_recurring"], true);
    assert_eq!(body["repeat_days"], json!([1, 3, 5]));
}

#[tokio::test]
async fn test_update_task_not_found() {
    let app = create_test_app();

    let update_payload = json!({
        "title": "Updated Title"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("PUT")
                .uri("/api/v1/tasks/00000000-0000-0000-0000-000000000000")
                .header("Content-Type", "application/json")
                .body(Body::from(update_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NOT_FOUND);
}

#[tokio::test]
async fn test_update_deleted_task() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let payload = json!({
        "title": "Task to Delete Then Update"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // タスクを削除
    let _ = app
        .clone()
        .oneshot(
            Request::builder()
                .method("DELETE")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    // 削除済みタスクを更新しようとする
    let update_payload = json!({
        "title": "Updated Title"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("PUT")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(update_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NOT_FOUND);
}

// ─────────────────────────────
// ログ更新エッジケーステスト
// ─────────────────────────────

#[tokio::test]
async fn test_update_log_with_memo() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let task_payload = json!({
        "title": "Task for Log Memo Test"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // ログを作成
    let log_payload = json!({
        "start_at": "2025-01-01T10:00:00Z",
        "target_date": "2025-01-01T00:00:00Z"
    });

    let log_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(log_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let log_body = body_to_json(log_response.into_body()).await;
    let log_id = log_body["id"].as_str().unwrap();

    // メモを追加して更新
    let update_payload = json!({
        "memo": "Added memo after the fact"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("PUT")
                .uri(format!("/api/v1/logs/{}", log_id))
                .header("Content-Type", "application/json")
                .body(Body::from(update_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::OK);

    let body = body_to_json(response.into_body()).await;
    assert_eq!(body["memo"], "Added memo after the fact");
}

#[tokio::test]
async fn test_update_log_not_found() {
    let app = create_test_app();

    let update_payload = json!({
        "end_at": "2025-01-01T11:00:00Z"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("PUT")
                .uri("/api/v1/logs/00000000-0000-0000-0000-000000000000")
                .header("Content-Type", "application/json")
                .body(Body::from(update_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NOT_FOUND);
}

// ─────────────────────────────
// 削除済みタスクのログテスト
// ─────────────────────────────

#[tokio::test]
async fn test_create_log_for_deleted_task() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // タスクを作成
    let task_payload = json!({
        "title": "Task to Delete"
    });

    let create_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("POST")
                .uri("/api/v1/tasks")
                .header("Content-Type", "application/json")
                .body(Body::from(task_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    let create_body = body_to_json(create_response.into_body()).await;
    let task_id = create_body["id"].as_str().unwrap();

    // タスクを削除
    let _ = app
        .clone()
        .oneshot(
            Request::builder()
                .method("DELETE")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    // 削除済みタスクにログを作成しようとする
    let log_payload = json!({
        "start_at": "2025-01-01T10:00:00Z",
        "target_date": "2025-01-01T00:00:00Z"
    });

    let response = app
        .oneshot(
            Request::builder()
                .method("POST")
                .uri(format!("/api/v1/tasks/{}/logs", task_id))
                .header("Content-Type", "application/json")
                .body(Body::from(log_payload.to_string()))
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NOT_FOUND);
}

// ─────────────────────────────
// タスク一覧フィルタリングテスト
// ─────────────────────────────

#[tokio::test]
async fn test_list_tasks_excludes_deleted() {
    let state = Arc::new(AppState::new());
    let app = build_router(state.clone());

    // 2つのタスクを作成
    for title in &["Active Task", "Task to Delete"] {
        let payload = json!({
            "title": title
        });

        let _ = app
            .clone()
            .oneshot(
                Request::builder()
                    .method("POST")
                    .uri("/api/v1/tasks")
                    .header("Content-Type", "application/json")
                    .body(Body::from(payload.to_string()))
                    .unwrap(),
            )
            .await
            .unwrap();
    }

    // タスク一覧を取得（2件）
    let list_response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/tasks")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    let list_body = body_to_json(list_response.into_body()).await;
    let tasks = list_body.as_array().unwrap();
    assert_eq!(tasks.len(), 2);

    // 2番目のタスクを削除
    let task_to_delete = tasks.iter().find(|t| t["title"] == "Task to Delete").unwrap();
    let task_id = task_to_delete["id"].as_str().unwrap();

    let _ = app
        .clone()
        .oneshot(
            Request::builder()
                .method("DELETE")
                .uri(format!("/api/v1/tasks/{}", task_id))
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    // タスク一覧を再取得（1件のみ）
    let response = app
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/tasks")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    let body = body_to_json(response.into_body()).await;
    let remaining_tasks = body.as_array().unwrap();
    assert_eq!(remaining_tasks.len(), 1);
    assert_eq!(remaining_tasks[0]["title"], "Active Task");
}
