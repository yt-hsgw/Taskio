//! カレンダー関連エンドポイント
//!
//! カレンダー画面で使用するログ一括取得機能を提供します。

use axum::{
    extract::{Query, State},
    http::StatusCode,
    Json,
};
use chrono::{DateTime, Datelike, NaiveDate, Utc};
use serde::{Deserialize, Serialize};
use std::sync::Arc;

use crate::state::AppState;

// ─────────────────────────────
// リクエスト/レスポンス型
// ─────────────────────────────

/// ログ検索クエリパラメータ
#[derive(Debug, Deserialize)]
pub struct LogsQuery {
    /// 開始日 (YYYY-MM-DD形式)
    pub from: String,
    /// 終了日 (YYYY-MM-DD形式)
    pub to: String,
}

/// タスクのステータス
#[derive(Debug, Serialize, Clone, Copy, PartialEq, Eq)]
#[serde(rename_all = "snake_case")]
pub enum TaskStatus {
    /// 未開始
    NotStarted,
    /// 進行中（開始済み、未完了）
    InProgress,
    /// 完了
    Completed,
}

/// カレンダー用タスクレスポンス
#[derive(Debug, Serialize)]
pub struct CalendarLogResponse {
    /// タスクID（予定日用）
    pub id: String,
    /// タスクID
    pub task_id: String,
    /// タスクタイトル
    pub task_title: String,
    /// タスク説明
    pub task_description: Option<String>,
    /// メモ
    pub memo: Option<String>,
    /// 作成日時
    pub created_at: DateTime<Utc>,
    /// 予定日
    pub scheduled_date: Option<DateTime<Utc>>,
    /// タスクステータス
    pub status: TaskStatus,
}

/// ログ一覧レスポンス
#[derive(Debug, Serialize)]
pub struct CalendarLogsResponse {
    pub logs: Vec<CalendarLogResponse>,
}

// ─────────────────────────────
// ハンドラ
// ─────────────────────────────

/// 期間指定でログを一括取得
///
/// # Endpoint
///
/// `GET /api/v1/logs?from=YYYY-MM-DD&to=YYYY-MM-DD`
///
/// # Query Parameters
///
/// * `from` - 開始日（YYYY-MM-DD形式）
/// * `to` - 終了日（YYYY-MM-DD形式）
///
/// # Returns
///
/// * `200 OK` - 指定期間内のすべてのログ（タスク情報付き）
/// * `400 Bad Request` - 日付形式が不正
pub async fn get_logs_by_date_range(
    State(state): State<Arc<AppState>>,
    Query(query): Query<LogsQuery>,
) -> Result<Json<CalendarLogsResponse>, (StatusCode, String)> {
    // 日付をパース
    let from_date = NaiveDate::parse_from_str(&query.from, "%Y-%m-%d")
        .map_err(|_| {
            (
                StatusCode::BAD_REQUEST,
                "Invalid 'from' date format. Use YYYY-MM-DD".to_string(),
            )
        })?;

    let to_date = NaiveDate::parse_from_str(&query.to, "%Y-%m-%d")
        .map_err(|_| {
            (
                StatusCode::BAD_REQUEST,
                "Invalid 'to' date format. Use YYYY-MM-DD".to_string(),
            )
        })?;

    // from <= to のバリデーション
    if from_date > to_date {
        return Err((
            StatusCode::BAD_REQUEST,
            "'from' date must be before or equal to 'to' date".to_string(),
        ));
    }

    // タスクとログを取得
    let tasks = state.tasks.lock().await;
    let task_logs = state.task_logs.lock().await;

    let mut calendar_logs: Vec<CalendarLogResponse> = Vec::new();

    // ヘルパー関数: 指定タスクの指定日のログからステータスを決定
    let get_status_for_date = |task_id: &uuid::Uuid, target_date: NaiveDate| -> TaskStatus {
        for log in task_logs.values() {
            if &log.task_id == task_id {
                let log_date = log.target_date.date_naive();
                if log_date == target_date {
                    return if log.is_completed {
                        TaskStatus::Completed
                    } else {
                        TaskStatus::InProgress
                    };
                }
            }
        }
        TaskStatus::NotStarted
    };

    // タスクを追加（予定日タスクと繰り返しタスク）
    tracing::debug!("Total tasks in storage: {}", tasks.len());
    for task in tasks.values() {
        tracing::debug!(
            "Task: {} (id={}), is_active={}, scheduled_date={:?}, repeat_days={:?}",
            task.title,
            task.id,
            task.is_active,
            task.scheduled_date,
            task.repeat_days
        );

        if !task.is_active {
            continue;
        }

        // 1. 予定日（scheduled_date）が設定されたタスク
        if let Some(scheduled_date) = task.scheduled_date {
            let task_date = scheduled_date.date_naive();

            if task_date >= from_date && task_date <= to_date {
                let status = get_status_for_date(&task.id, task_date);
                tracing::debug!("Adding scheduled task '{}' to calendar with status {:?}", task.title, status);
                calendar_logs.push(CalendarLogResponse {
                    id: task.id.to_string(),
                    task_id: task.id.to_string(),
                    task_title: task.title.clone(),
                    task_description: task.description.clone(),
                    memo: None,
                    created_at: task.created_at,
                    scheduled_date: Some(scheduled_date),
                    status,
                });
            }
        }

        // 2. 繰り返しタスク（repeat_daysが設定されている）
        if let Some(ref repeat_days) = task.repeat_days {
            if !repeat_days.is_empty() {
                // 期間内の各日をチェック
                let mut current_date = from_date;
                while current_date <= to_date {
                    // 曜日を取得（0=日曜, 1=月曜, ..., 6=土曜）
                    let weekday = current_date.weekday().num_days_from_sunday() as u8;

                    if repeat_days.contains(&weekday) {
                        let scheduled_datetime = current_date
                            .and_hms_opt(0, 0, 0)
                            .unwrap()
                            .and_utc();

                        let status = get_status_for_date(&task.id, current_date);
                        tracing::debug!(
                            "Adding recurring task '{}' for date {} (weekday={}) with status {:?}",
                            task.title,
                            current_date,
                            weekday,
                            status
                        );

                        calendar_logs.push(CalendarLogResponse {
                            id: format!("{}-{}", task.id, current_date),
                            task_id: task.id.to_string(),
                            task_title: task.title.clone(),
                            task_description: task.description.clone(),
                            memo: None,
                            created_at: task.created_at,
                            scheduled_date: Some(scheduled_datetime),
                            status,
                        });
                    }

                    current_date = current_date.succ_opt().unwrap_or(current_date);
                }
            }
        }
    }

    // 予定日でソート（新しい順）
    calendar_logs.sort_by(|a, b| b.scheduled_date.cmp(&a.scheduled_date));

    tracing::debug!(
        from = %query.from,
        to = %query.to,
        count = calendar_logs.len(),
        "Retrieved logs by date range"
    );

    Ok(Json(CalendarLogsResponse { logs: calendar_logs }))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_date_parsing() {
        let date = NaiveDate::parse_from_str("2026-01-25", "%Y-%m-%d");
        assert!(date.is_ok());
        assert_eq!(date.unwrap().to_string(), "2026-01-25");
    }

    #[test]
    fn test_invalid_date_parsing() {
        let date = NaiveDate::parse_from_str("invalid", "%Y-%m-%d");
        assert!(date.is_err());
    }
}