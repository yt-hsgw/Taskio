use std::collections::HashMap;
use tokio::sync::Mutex;
use uuid::Uuid;

use crate::models::task::Task;
use crate::models::task_log::TaskLog;

pub struct AppState {
    pub tasks: Mutex<HashMap<Uuid, Task>>,
    pub task_logs: Mutex<HashMap<Uuid, TaskLog>>,
}

impl AppState {
    pub fn new() -> Self {
        Self {
            tasks: Mutex::new(HashMap::new()),
            task_logs: Mutex::new(HashMap::new()),
        }
    }
}
