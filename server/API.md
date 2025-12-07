# Taskio API Specification

## Version

-   API Version: v1
-   Base URL: http://localhost:8080/api/v1

## Resources

-   **Task**: 管理したい行動の種類
-   **TaskLog**: タスクを実行した記録

## Data Models

### Task

-   id: UUID
-   name: string
-   description: string
-   is_active: bool
-   created_at: datetime
-   updated_at: datetime

### Task JSON Example

``` json
{
  "id": "a3b1eeec-982a-4fd4-93c4-9cdd6c61b8e9",
  "name": "Gym",
  "description": "24h ジムでのトレーニング",
  "is_active": true,
  "created_at": "2025-01-10T12:00:00Z",
  "updated_at": "2025-01-10T12:00:00Z"
}
```

### TaskLog

-   id: UUID
-   task_id: UUID
-   start_at: datetime
-   end_at: datetime
-   duration_min: int
-   memo: string
-   created_at: datetime
-   updated_at: datetime

### TaskLog JSON Example

``` json
{
  "id": "41ab5a9c-5d91-4b76-a1e3-bf9ad4e9286d",
  "task_id": "a3b1eeec-982a-4fd4-93c4-9cdd6c61b8e9",
  "start_at": "2025-01-12T18:00:00Z",
  "end_at": "2025-01-12T19:30:00Z",
  "duration_min": 90,
  "memo": "脚トレの日",
  "created_at": "2025-01-12T20:00:00Z",
  "updated_at": "2025-01-12T20:00:00Z"
}
```

## Endpoints

### Health Check

GET `/health`

### Tasks

-   POST `/tasks`
-   GET `/tasks`
-   GET `/tasks/{task_id}`
-   PUT `/tasks/{task_id}`
-   DELETE `/tasks/{task_id}`

### Task Logs

-   POST `/tasks/{task_id}/logs`
-   GET `/tasks/{task_id}/logs`
-   GET `/logs/{log_id}`
-   PUT `/logs/{log_id}`
-   DELETE `/logs/{log_id}`

## Status Codes

-   200 OK
-   201 Created
-   400 Validation Error
-   404 Not Found
-   500 Server Error
