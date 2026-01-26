//! タスクログモデル
//!
//! タスクの実行ログを表すデータ構造を定義します。
//! 開始時刻、終了時刻、継続時間などを管理します。

use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

/// タスクログエンティティ
///
/// タスクの実行記録を表します。
/// 開始時刻と終了時刻から継続時間が計算されます。
///
/// # Fields
///
/// * `id` - ログの一意識別子
/// * `task_id` - 関連するタスクのID
/// * `start_at` - 開始時刻
/// * `end_at` - 終了時刻（任意）
/// * `duration_min` - 継続時間（分）
/// * `memo` - メモ（任意）
/// * `target_date` - 対象日（繰り返しタスクの場合、どの日のログかを示す）
/// * `is_completed` - 完了済みかどうか
/// * `created_at` - 作成日時
/// * `updated_at` - 更新日時
#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct TaskLog {
    pub id: Uuid,
    pub task_id: Uuid,
    pub start_at: DateTime<Utc>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub end_at: Option<DateTime<Utc>>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub duration_min: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub memo: Option<String>,
    pub target_date: DateTime<Utc>,
    pub is_completed: bool,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

impl TaskLog {
    /// 新しいタスクログを作成
    ///
    /// # Arguments
    ///
    /// * `task_id` - 関連するタスクのID
    /// * `start_at` - 開始時刻（Noneの場合は現在時刻）
    /// * `end_at` - 終了時刻（任意）
    /// * `memo` - メモ（任意）
    /// * `target_date` - 対象日（Noneの場合は現在時刻）
    ///
    /// # Returns
    ///
    /// 新しいTaskLogインスタンス
    pub fn new(
        task_id: Uuid,
        start_at: Option<DateTime<Utc>>,
        end_at: Option<DateTime<Utc>>,
        memo: Option<String>,
        target_date: Option<DateTime<Utc>>,
    ) -> Self {
        let now = Utc::now();
        let start = start_at.unwrap_or(now);
        let target = target_date.unwrap_or(now);

        // 継続時間の計算
        let (end, duration) = match end_at {
            Some(e) => {
                let duration = Self::calculate_duration(start, e);
                (Some(e), Some(duration))
            }
            None => (None, None),
        };

        Self {
            id: Uuid::new_v4(),
            task_id,
            start_at: start,
            end_at: end,
            duration_min: duration,
            memo,
            target_date: target,
            is_completed: end.is_some(),
            created_at: now,
            updated_at: now,
        }
    }

    /// 開始時刻と終了時刻から継続時間（分）を計算
    ///
    /// # Arguments
    ///
    /// * `start` - 開始時刻
    /// * `end` - 終了時刻
    ///
    /// # Returns
    ///
    /// 継続時間（分）
    pub fn calculate_duration(start: DateTime<Utc>, end: DateTime<Utc>) -> i64 {
        end.signed_duration_since(start).num_minutes()
    }

    /// 終了時刻を設定し、継続時間を自動計算
    ///
    /// # Arguments
    ///
    /// * `end` - 終了時刻
    #[allow(dead_code)]
    pub fn set_end_time(&mut self, end: DateTime<Utc>) {
        self.end_at = Some(end);
        self.duration_min = Some(Self::calculate_duration(self.start_at, end));
        self.is_completed = true;
        self.updated_at = Utc::now();
    }

    /// ログ情報を更新
    ///
    /// # Arguments
    ///
    /// * `start_at` - 新しい開始時刻（Noneの場合は変更なし）
    /// * `end_at` - 新しい終了時刻（Noneの場合は変更なし）
    /// * `memo` - 新しいメモ（Noneの場合は変更なし）
    /// * `is_completed` - 新しい完了状態（Noneの場合は変更なし）
    pub fn update(
        &mut self,
        start_at: Option<DateTime<Utc>>,
        end_at: Option<DateTime<Utc>>,
        memo: Option<String>,
        is_completed: Option<bool>,
    ) {
        if let Some(start) = start_at {
            self.start_at = start;
        }

        if let Some(end) = end_at {
            self.end_at = Some(end);
            self.duration_min = Some(Self::calculate_duration(self.start_at, end));
        }

        if let Some(m) = memo {
            self.memo = Some(m);
        }

        if let Some(completed) = is_completed {
            self.is_completed = completed;
        }

        self.updated_at = Utc::now();
    }
}

/// タスクログ作成リクエスト
///
/// 新規タスクログ作成時にクライアントから送信されるデータ構造。
#[derive(Deserialize, Debug)]
pub struct CreateTaskLog {
    /// 開始時刻（任意、Noneの場合は現在時刻）
    pub start_at: Option<DateTime<Utc>>,
    /// 終了時刻（任意）
    pub end_at: Option<DateTime<Utc>>,
    /// メモ（任意）
    pub memo: Option<String>,
    /// 対象日（任意、Noneの場合は現在時刻）
    pub target_date: Option<DateTime<Utc>>,
    /// 完了状態（任意）
    pub is_completed: Option<bool>,
}

impl CreateTaskLog {
    /// CreateTaskLogからTaskLogを生成
    ///
    /// # Arguments
    ///
    /// * `task_id` - 関連するタスクのID
    pub fn into_task_log(self, task_id: Uuid) -> TaskLog {
        TaskLog::new(
            task_id,
            self.start_at,
            self.end_at,
            self.memo,
            self.target_date,
        )
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_new_task_log() {
        let task_id = Uuid::new_v4();
        let log = TaskLog::new(task_id, None, None, None, None);

        assert_eq!(log.task_id, task_id);
        assert!(log.end_at.is_none());
        assert!(log.duration_min.is_none());
        assert!(!log.is_completed);
    }

    #[test]
    fn test_task_log_with_end_time() {
        let task_id = Uuid::new_v4();
        let start = Utc::now();
        let end = start + chrono::Duration::hours(1);
        let log = TaskLog::new(task_id, Some(start), Some(end), None, None);

        assert!(log.end_at.is_some());
        assert_eq!(log.duration_min, Some(60));
        assert!(log.is_completed);
    }

    #[test]
    fn test_set_end_time() {
        let task_id = Uuid::new_v4();
        let mut log = TaskLog::new(task_id, None, None, None, None);
        
        let end = Utc::now() + chrono::Duration::minutes(30);
        log.set_end_time(end);

        assert!(log.end_at.is_some());
        assert!(log.duration_min.is_some());
        assert!(log.is_completed);
    }

    #[test]
    fn test_calculate_duration() {
        let start = Utc::now();
        let end = start + chrono::Duration::minutes(45);
        
        let duration = TaskLog::calculate_duration(start, end);
        assert_eq!(duration, 45);
    }
}
