//! タスクモデル
//!
//! タスクの作成、保存、シリアライズに使用されるデータ構造を定義します。

use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

/// タスクエンティティ
///
/// ユーザーが実行する作業単位を表します。
/// 繰り返し設定や予定日を持つことができます。
///
/// # Fields
///
/// * `id` - タスクの一意識別子
/// * `title` - タスクタイトル（必須）
/// * `description` - タスクの説明（任意）
/// * `is_active` - アクティブ状態（論理削除に使用）
/// * `due_date` - 期限日時
/// * `scheduled_date` - 予定日時
/// * `repeat_days` - 繰り返し曜日（0=日曜日, 6=土曜日）
/// * `is_recurring` - 繰り返しタスクかどうか
/// * `created_at` - 作成日時
/// * `updated_at` - 更新日時
#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct Task {
    pub id: Uuid,
    pub title: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub description: Option<String>,
    pub is_active: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub due_date: Option<DateTime<Utc>>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub scheduled_date: Option<DateTime<Utc>>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub repeat_days: Option<Vec<u8>>,
    pub is_recurring: bool,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

impl Task {
    /// 新しいタスクを作成
    ///
    /// # Arguments
    ///
    /// * `title` - タスクタイトル
    /// * `description` - タスクの説明（任意）
    /// * `due_date` - 期限日時（任意）
    /// * `scheduled_date` - 予定日時（任意）
    /// * `repeat_days` - 繰り返し曜日（任意）
    ///
    /// # Returns
    ///
    /// 新しいTaskインスタンス
    pub fn new(
        title: String,
        description: Option<String>,
        due_date: Option<DateTime<Utc>>,
        scheduled_date: Option<DateTime<Utc>>,
        repeat_days: Option<Vec<u8>>,
    ) -> Self {
        let now = Utc::now();
        let is_recurring = repeat_days.as_ref().map_or(false, |days| !days.is_empty());

        Self {
            id: Uuid::new_v4(),
            title,
            description,
            is_active: true,
            due_date,
            scheduled_date,
            repeat_days,
            is_recurring,
            created_at: now,
            updated_at: now,
        }
    }

    /// タスクを論理削除
    pub fn soft_delete(&mut self) {
        self.is_active = false;
        self.updated_at = Utc::now();
    }

    /// タスク情報を更新
    ///
    /// # Arguments
    ///
    /// * `title` - 新しいタイトル
    /// * `description` - 新しい説明
    /// * `due_date` - 新しい期限日時
    /// * `scheduled_date` - 新しい予定日時
    /// * `repeat_days` - 新しい繰り返し曜日
    pub fn update(
        &mut self,
        title: String,
        description: Option<String>,
        due_date: Option<DateTime<Utc>>,
        scheduled_date: Option<DateTime<Utc>>,
        repeat_days: Option<Vec<u8>>,
    ) {
        self.title = title;
        self.description = description;
        self.due_date = due_date;
        self.scheduled_date = scheduled_date;
        self.is_recurring = repeat_days.as_ref().map_or(false, |days| !days.is_empty());
        self.repeat_days = repeat_days;
        self.updated_at = Utc::now();
    }
}

/// タスク作成リクエスト
///
/// 新規タスク作成時にクライアントから送信されるデータ構造。
#[derive(Deserialize, Debug)]
pub struct CreateTask {
    /// タスクタイトル（必須）
    pub title: String,
    /// タスクの説明（任意）
    pub description: Option<String>,
    /// 期限日時（任意）
    pub due_date: Option<DateTime<Utc>>,
    /// 予定日時（任意）
    pub scheduled_date: Option<DateTime<Utc>>,
    /// 繰り返し曜日（任意、0=日曜日, 6=土曜日）
    pub repeat_days: Option<Vec<u8>>,
}

impl CreateTask {
    /// CreateTaskからTaskを生成
    pub fn into_task(self) -> Task {
        Task::new(
            self.title,
            self.description,
            self.due_date,
            self.scheduled_date,
            self.repeat_days,
        )
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_new_task() {
        let task = Task::new(
            "Test Task".to_string(),
            Some("Description".to_string()),
            None,
            None,
            None,
        );

        assert_eq!(task.title, "Test Task");
        assert!(task.is_active);
        assert!(!task.is_recurring);
    }

    #[test]
    fn test_recurring_task() {
        let task = Task::new(
            "Recurring Task".to_string(),
            None,
            None,
            None,
            Some(vec![1, 3, 5]), // 月・水・金
        );

        assert!(task.is_recurring);
        assert_eq!(task.repeat_days, Some(vec![1, 3, 5]));
    }

    #[test]
    fn test_soft_delete() {
        let mut task = Task::new("Task".to_string(), None, None, None, None);
        assert!(task.is_active);

        task.soft_delete();
        assert!(!task.is_active);
    }
}
