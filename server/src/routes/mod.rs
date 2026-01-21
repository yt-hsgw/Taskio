//! ルーティングモジュール
//!
//! このモジュールは、APIのルートハンドラを定義します。
//!
//! # モジュール
//!
//! - [`health`] - ヘルスチェックエンドポイント
//! - [`tasks`] - タスクCRUDエンドポイント
//! - [`task_logs`] - タスクログCRUDエンドポイント

pub mod health;
pub mod task_logs;
pub mod tasks;
