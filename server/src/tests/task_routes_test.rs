use axum::{body::Body, http::{Request, StatusCode}};
use serde_json::json;
use tower::ServiceExt;

use taskio_server::app;

#[tokio::test]
async fn test_create_and_get_task() {
    let app = app().await;

    // Create Task
    let payload = json!({
        "name": "Test Task",
        "description": "This is a test task"
    });

    let response = app
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

    assert_eq!(response.status(), StatusCode::CREATED);

    // Extract ID
    let bytes = hyper::body::to_bytes(response.into_body()).await.unwrap();
    let created_task: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    let task_id = created_task["id"].as_str().unwrap();

    // GET /task/{id}
    let response = app
        .clone()
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
}

#[tokio::test]
async fn test_task_not_found() {
    let app = app().await;

    let response = app
        .clone()
        .oneshot(
            Request::builder()
                .method("GET")
                .uri("/api/v1/tasks/aaaaaaaa-bbbb-cccc-dddd-eeeeffffffff")
                .body(Body::empty())
                .unwrap(),
        )
        .await
        .unwrap();

    assert_eq!(response.status(), StatusCode::NOT_FOUND);
}