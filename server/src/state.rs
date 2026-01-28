//! アプリケーション状態モジュール
//!
//! インメモリストレージとアプリケーション状態を管理します。
//! 将来的にはデータベースに置き換えることを想定しています。

use std::collections::HashMap;
use tokio::sync::Mutex;
use uuid::Uuid;

use crate::models::task::Task;
use crate::models::task_log::TaskLog;

/// アプリケーション共有状態
///
/// すべてのルートハンドラで共有される状態を保持します。
/// `Arc<AppState>`として使用され、スレッドセーフなアクセスを提供します。
///
/// # Example
///
/// ```rust,ignore
/// use std::sync::Arc;
/// use taskio_server::state::AppState;
///
/// let state = Arc::new(AppState::new());
/// ```
///
/// # Future Work
///
/// 将来的にはSQLxを使用したデータベース接続に置き換える予定です。
/// その際、`Repository`トレイトを実装し、依存性注入パターンを使用します。
pub struct AppState {
    /// タスクストレージ
    ///
    /// キー: タスクID、値: タスクエンティティ
    pub tasks: Mutex<HashMap<Uuid, Task>>,

    /// タスクログストレージ
    ///
    /// キー: ログID、値: タスクログエンティティ
    pub task_logs: Mutex<HashMap<Uuid, TaskLog>>,
}

impl AppState {
    /// 新しいAppStateインスタンスを作成
    ///
    /// 空のストレージで初期化されます。
    ///
    /// # Returns
    ///
    /// 新しいAppStateインスタンス
    pub fn new() -> Self {
        Self {
            tasks: Mutex::new(HashMap::new()),
            task_logs: Mutex::new(HashMap::new()),
        }
    }

    /// タスク数を取得
    ///
    /// # Returns
    ///
    /// 登録されているタスクの総数
    #[allow(dead_code)]
    pub async fn task_count(&self) -> usize {
        self.tasks.lock().await.len()
    }

    /// タスクログ数を取得
    ///
    /// # Returns
    ///
    /// 登録されているタスクログの総数
    #[allow(dead_code)]
    pub async fn task_log_count(&self) -> usize {
        self.task_logs.lock().await.len()
    }

    /// すべてのデータをクリア（テスト用）
    #[cfg(test)]
    pub async fn clear(&self) {
        self.tasks.lock().await.clear();
        self.task_logs.lock().await.clear();
    }
}

impl Default for AppState {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_new_app_state() {
        let state = AppState::new();
        
        assert_eq!(state.task_count().await, 0);
        assert_eq!(state.task_log_count().await, 0);
    }

    #[tokio::test]
    async fn test_clear() {
        let state = AppState::new();
        
        // タスクを追加
        let task = Task::new(
            "Test".to_string(),
            None,
            None,
            None,
            None,
        );
        state.tasks.lock().await.insert(task.id, task);
        
        assert_eq!(state.task_count().await, 1);
        
        // クリア
        state.clear().await;
        
        assert_eq!(state.task_count().await, 0);
    }
}
