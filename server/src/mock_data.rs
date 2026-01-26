//! モックデータモジュール
//!
//! 開発・テスト用のモックデータを提供します。

use chrono::{Duration, Utc};

use crate::models::task::Task;
use crate::models::task_log::TaskLog;
use crate::state::AppState;

/// モックデータでAppStateを初期化
///
/// 開発環境用にサンプルのタスクとタスクログを追加します。
pub async fn initialize_mock_data(state: &AppState) {
    // モックタスクを作成
    let tasks = create_mock_tasks();
    
    // タスクを追加
    {
        let mut task_store = state.tasks.lock().await;
        for task in &tasks {
            task_store.insert(task.id, task.clone());
        }
    }
    
    // モックログを作成・追加
    let logs = create_mock_logs(&tasks);
    {
        let mut log_store = state.task_logs.lock().await;
        for log in logs {
            log_store.insert(log.id, log);
        }
    }
    
    tracing::info!(
        task_count = tasks.len(),
        "Initialized mock data"
    );
}

/// モックタスクを作成
fn create_mock_tasks() -> Vec<Task> {
    vec![
        // 繰り返しタスク: ジム（月・水・金）
        Task::new(
            "ジム".to_string(),
            Some("24hジムでのトレーニング".to_string()),
            None,
            None,
            Some(vec![1, 3, 5]), // 月・水・金
        ),
        // 繰り返しタスク: ランニング（火・木）
        Task::new(
            "ランニング".to_string(),
            Some("朝のジョギング 5km".to_string()),
            None,
            None,
            Some(vec![2, 4]), // 火・木
        ),
        // 繰り返しタスク: キックボクシング（土）
        Task::new(
            "キックボクシング".to_string(),
            Some("ジムでのキックボクシングレッスン".to_string()),
            None,
            None,
            Some(vec![6]), // 土
        ),
        // 繰り返しタスク: 資格勉強（毎日）
        Task::new(
            "資格勉強".to_string(),
            Some("AWS認定資格の勉強".to_string()),
            None,
            None,
            Some(vec![0, 1, 2, 3, 4, 5, 6]), // 毎日
        ),
        // 繰り返しタスク: 読書（平日）
        Task::new(
            "読書".to_string(),
            Some("毎日30分の読書習慣".to_string()),
            None,
            None,
            Some(vec![1, 2, 3, 4, 5]), // 平日
        ),
        // 単発タスク: 歯医者
        Task::new(
            "歯医者".to_string(),
            Some("定期検診".to_string()),
            None,
            Some(Utc::now() + Duration::days(3)),
            None,
        ),
    ]
}

/// モックログを作成
fn create_mock_logs(tasks: &[Task]) -> Vec<TaskLog> {
    let mut logs = Vec::new();
    let now = Utc::now();

    for task in tasks {
        // 繰り返しタスクの場合、過去1週間分のログを作成
        if task.is_recurring {
            if let Some(ref repeat_days) = task.repeat_days {
                for days_ago in 0..7 {
                    let target_date = now - Duration::days(days_ago);
                    let day_of_week = target_date.format("%u").to_string()
                        .parse::<u8>()
                        .unwrap_or(0);
                    // 日曜日は0に変換（chronoは1-7を使用）
                    let day_index = if day_of_week == 7 { 0 } else { day_of_week };

                    // その曜日が繰り返し日に含まれている場合
                    if repeat_days.contains(&day_index) {
                        let start_time = target_date
                            .date_naive()
                            .and_hms_opt(9, 0, 0)
                            .map(|dt| dt.and_utc());

                        // 3パターン: 未開始、開始中、完了済み
                        // days_ago % 3 == 0: 完了済み（過去の日）
                        // days_ago % 3 == 1: 開始中
                        // days_ago % 3 == 2: 未開始（ログなし）
                        match days_ago % 3 {
                            0 => {
                                // 完了済み: 終了時刻あり
                                let end_time = target_date
                                    .date_naive()
                                    .and_hms_opt(10, 0, 0)
                                    .map(|dt| dt.and_utc());

                                let log = TaskLog::new(
                                    task.id,
                                    start_time,
                                    end_time,
                                    Some(format!("{}完了", task.title)),
                                    Some(target_date),
                                );
                                logs.push(log);
                            }
                            1 => {
                                // 開始中: 終了時刻なし
                                let log = TaskLog::new(
                                    task.id,
                                    start_time,
                                    None, // 終了時刻なし = 進行中
                                    Some(format!("{}開始中", task.title)),
                                    Some(target_date),
                                );
                                logs.push(log);
                            }
                            _ => {
                                // 未開始: ログを作成しない
                            }
                        }
                    }
                }
            }
        }

        // 単発タスク（scheduled_dateがある場合）もログを作成
        if !task.is_recurring {
            if let Some(scheduled_date) = task.scheduled_date {
                // 予定日が今日または過去の場合、ログを作成
                if scheduled_date <= now {
                    let start_time = scheduled_date
                        .date_naive()
                        .and_hms_opt(9, 0, 0)
                        .map(|dt| dt.and_utc());

                    let end_time = scheduled_date
                        .date_naive()
                        .and_hms_opt(10, 0, 0)
                        .map(|dt| dt.and_utc());

                    let log = TaskLog::new(
                        task.id,
                        start_time,
                        end_time,
                        Some(format!("{}完了", task.title)),
                        Some(scheduled_date),
                    );
                    logs.push(log);
                }
            }
        }
    }

    logs
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_create_mock_tasks() {
        let tasks = create_mock_tasks();
        assert!(!tasks.is_empty());
        
        // ジムタスクの確認
        let gym_task = tasks.iter().find(|t| t.title == "ジム");
        assert!(gym_task.is_some());
        assert!(gym_task.unwrap().is_recurring);
    }

    #[test]
    fn test_create_mock_logs() {
        let tasks = create_mock_tasks();
        let logs = create_mock_logs(&tasks);
        
        // ログが作成されていることを確認
        assert!(!logs.is_empty());
    }

    #[tokio::test]
    async fn test_initialize_mock_data() {
        let state = AppState::new();
        initialize_mock_data(&state).await;
        
        assert!(state.task_count().await > 0);
    }
}
