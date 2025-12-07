# Taskio Server — Architecture Document

## Overview
This document describes the architecture of the Taskio server (Rust + Axum), focused on sprint 1 (minimal viable server).
The server exposes a REST API for managing Tasks and TaskLogs. This architecture favors fast iteration and testability:
initial implementation uses in-memory storage (thread-safe) to allow immediate verification. Future iterations will replace
the storage layer with SQLx + SQLite/Postgres.

## Goals
- Provide a clear module layout and routing for Axum.
- Keep handlers small and focused.
- Provide an easy-to-replace storage abstraction for later DB integration.
- Expose API under `/api/v1/*`.

## High-level design
- **Language & runtime**: Rust + Tokio async runtime.
- **HTTP framework**: Axum.
- **Data models**: `Task` and `TaskLog` (UUID-based).
- **Storage**: In-memory `AppState` (Arc + tokio::Mutex). Replaceable with DB repository.
- **Routing**: Modularized under `routes/*`.
- **API prefix**: `/api/v1`

## Directory layout
```
server/
├── Cargo.toml
└── src/
    ├── main.rs            # App bootstrap, router composition
    ├── config.rs          # Environment config helpers
    ├── state.rs           # Application shared state (in-memory store)
    ├── models/
    │   ├── mod.rs
    │   ├── task.rs
    │   └── task_log.rs
    └── routes/
        ├── mod.rs
        ├── health.rs
        ├── tasks.rs
        └── task_logs.rs
```

## AppState / Storage
`AppState` currently holds:
- `tasks: HashMap<Uuid, Task>`
- `task_logs: HashMap<Uuid, TaskLog>`

Access is protected by `tokio::sync::Mutex`. For production, replace AppState with a `Repository` trait backed by SQLx.

## Routing
All API routes are prefixed with `/api/v1`:

- `GET  /api/v1/health`
- `GET  /api/v1/tasks`
- `POST /api/v1/tasks`
- `GET  /api/v1/tasks/{task_id}`
- `PUT  /api/v1/tasks/{task_id}`
- `DELETE /api/v1/tasks/{task_id}`
- `GET  /api/v1/tasks/{task_id}/logs`
- `POST /api/v1/tasks/{task_id}/logs`
- `GET  /api/v1/logs/{log_id}`
- `PUT  /api/v1/logs/{log_id}`
- `DELETE /api/v1/logs/{log_id}`

## Error handling
Handlers return appropriate HTTP status codes:
- 200 OK, 201 Created, 400 BadRequest, 404 NotFound, 500 InternalServerError.
Error shape: `{ "error": "BadRequest", "message": "detail" }`.

## Next steps to replace in-memory storage with DB
1. Add SQLx and connection pool in `state.rs` or a separate `db` module.
2. Implement `Repository` trait with methods (create_task, list_tasks, create_log, etc.)
3. Update handlers to call repository methods (async DB).
4. Add migrations (e.g., with sqlx-cli or refinery).

