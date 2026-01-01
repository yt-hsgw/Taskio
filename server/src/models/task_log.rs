use serde::{Serialize, Deserialize};
use uuid::Uuid;
use chrono::{DateTime, Utc};

#[derive(Serialize, Deserialize, Clone)]
pub struct TaskLog {
    pub id: Uuid,
    pub task_id: Uuid,
    pub start_at: DateTime<Utc>,
    pub end_at: Option<DateTime<Utc>>,
    pub duration_min: Option<i64>,
    pub memo: Option<String>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

impl TaskLog {
    /// 開始時刻と終了時刻から継続時間（分）を計算
    pub fn calculate_duration(start: DateTime<Utc>, end: DateTime<Utc>) -> i64 {
        (end.signed_duration_since(start)).num_minutes()
    }

    /// 終了時刻を設定し、継続時間を自動計算
    pub fn set_end_time(&mut self, end: DateTime<Utc>) {
        self.end_at = Some(end);
        self.duration_min = Some(Self::calculate_duration(self.start_at, end));
        self.updated_at = Utc::now();
    }
}

#[derive(Deserialize)]
pub struct CreateTaskLog {
    pub start_at: Option<DateTime<Utc>>,
    pub end_at: Option<DateTime<Utc>>,
    pub memo: Option<String>,
}
