//! ルーティングモジュール
//!
//! このモジュールは、APIのルートハンドラを定義します。
//!
//! # モジュール
//!
//! - [`health`] - ヘルスチェックエンドポイント
//! - [`tasks`] - タスクCRUDエンドポイント
//! - [`task_logs`] - タスクログCRUDエンドポイント
//! - [`calendar`] - カレンダー用ログ取得エンドポイント

pub mod calendar;
pub mod health;
pub mod task_logs;
pub mod tasks;