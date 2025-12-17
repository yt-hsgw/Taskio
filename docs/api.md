# Taskio API Specification

> **Status:** Draft（更新前提）  
> 本ドキュメントは Requirements.md / UseCases.md に基づき作成された API 仕様書である。  
> 実装の進行や要件変更に応じて、随時更新されることを前提とする。

---

## 1. 概要

- ベース URL: `/api/v1`
- 通信方式: REST
- データ形式: JSON
- 認証: なし（初期段階）

---

## 2. 共通仕様

### 2.1 HTTP ヘッダー

```http
Content-Type: application/json
```

---

### 2.2 共通レスポンス

#### 正常系

- 200 OK
- 201 Created
- 204 No Content

#### 異常系

| ステータス | 説明 |
|---|---|
| 400 Bad Request | リクエスト不正 |
| 404 Not Found | リソースが存在しない |
| 500 Internal Server Error | サーバー内部エラー |

```json
{
  "error": "error message"
}
```

---

## 3. Health Check

### GET /health

#### 説明

サーバーの稼働確認を行う。

#### レスポンス例（200）

```json
"ok"
```

---

## 4. Task API

### 4.1 タスク作成

#### POST /tasks

##### リクエスト

```json
{
  "title": "Workout",
  "description": "Gym training"
}
```

##### レスポンス（201）

```json
{
  "id": "uuid",
  "title": "Workout",
  "description": "Gym training"
}
```

---

### 4.2 タスク一覧取得

#### GET /tasks

##### レスポンス（200）

```json
[
  {
    "id": "uuid",
    "title": "Workout",
    "description": "Gym training"
  }
]
```

---

### 4.3 タスク詳細取得

#### GET /tasks/{task_id}

##### レスポンス（200）

```json
{
  "id": "uuid",
  "title": "Workout",
  "description": "Gym training"
}
```

---

### 4.4 タスク更新

#### PUT /tasks/{task_id}

##### リクエスト

```json
{
  "title": "Workout updated",
  "description": "Gym training"
}
```

##### レスポンス（200）

```json
{
  "id": "uuid",
  "title": "Workout updated",
  "description": "Gym training"
}
```

---

### 4.5 タスク削除

#### DELETE /tasks/{task_id}

##### レスポンス（204）

---

## 5. TaskLog API

### 5.1 タスクログ作成（開始）

#### POST /tasks/{task_id}/logs

##### リクエスト

```json
{
  "started_at": "2025-01-01T10:00:00Z"
}
```

##### レスポンス（201）

```json
{
  "id": "uuid",
  "task_id": "uuid",
  "started_at": "2025-01-01T10:00:00Z",
  "ended_at": null,
  "note": null
}
```

---

### 5.2 タスクログ一覧取得

#### GET /tasks/{task_id}/logs

##### レスポンス（200）

```json
[
  {
    "id": "uuid",
    "task_id": "uuid",
    "started_at": "2025-01-01T10:00:00Z",
    "ended_at": "2025-01-01T11:00:00Z",
    "note": null
  }
]
```

---

### 5.3 タスクログ詳細取得

#### GET /logs/{log_id}

##### レスポンス（200）

```json
{
  "id": "uuid",
  "task_id": "uuid",
  "started_at": "2025-01-01T10:00:00Z",
  "ended_at": "2025-01-01T11:00:00Z",
  "note": null
}
```

---

### 5.4 タスクログ更新（終了）

#### PUT /logs/{log_id}

##### リクエスト

```json
{
  "ended_at": "2025-01-01T11:00:00Z",
  "note": "Good session"
}
```

##### レスポンス（200）

```json
{
  "id": "uuid",
  "task_id": "uuid",
  "started_at": "2025-01-01T10:00:00Z",
  "ended_at": "2025-01-01T11:00:00Z",
  "note": "Good session"
}
```

---

### 5.5 タスクログ削除

#### DELETE /logs/{log_id}

##### レスポンス（204）

---

## 6. 補足・方針

- 日時は ISO-8601（UTC）で扱う
- バリデーションは最小限とする
- エラー表現は将来的に拡張予定

---

## 7. 更新方針

- 本ドキュメントは Draft とする
- 実装と同時に随時更新する
- 正本は GitHub リポジトリ内の本ファイルとする
