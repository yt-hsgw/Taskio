# Taskio - Task Logging & Visualization App

個人のトレーニング・生活習慣・日常タスクを  
**「いつ」「どれくらい」「結果（グラフ）」** で可視化するための  
ログ記録アプリです。

---

## 📌 技術スタック

### **Client (Android)**
| カテゴリ | 技術 |
|---------|-----|
| 言語 | Kotlin |
| UI | Jetpack Compose (Material3) |
| アーキテクチャ | MVVM (ViewModel + StateFlow) |
| 通信 | Retrofit + Moshi |
| 非同期処理 | Kotlin Coroutines |
| ナビゲーション | Jetpack Navigation Compose |

### **Server (Backend)**
| カテゴリ | 技術 |
|---------|-----|
| 言語 | Rust |
| フレームワーク | Axum |
| 非同期ランタイム | Tokio |
| シリアライズ | Serde (JSON) |
| データベース | SQLx + PostgreSQL/SQLite (予定) |

---

## 📂 プロジェクト構成

```
Taskio/
├── README.md
├── .gitignore
├── docs/                          # 仕様書
│   ├── Requirements.md            # 要求仕様書
│   ├── UseCase.md                 # ユースケース仕様書
│   ├── API.md                     # REST API仕様書
│   └── Architecture.md            # アーキテクチャ設計書
├── client/                        # Androidアプリ
│   └── Taskio/
│       └── app/src/main/java/com/yt_hsgw/taskio/
│           ├── MainActivity.kt
│           ├── MainApp.kt
│           ├── api/               # API通信層
│           │   ├── TaskApi.kt
│           │   ├── RetrofitClient.kt
│           │   └── ApiResult.kt
│           ├── model/             # データモデル
│           │   ├── TaskItem.kt
│           │   ├── TaskRequest.kt
│           │   ├── TaskResponse.kt
│           │   └── TaskLogResponse.kt
│           ├── viewmodel/         # ビューモデル
│           │   ├── TaskViewModel.kt
│           │   └── LogViewModel.kt
│           ├── screens/           # 画面
│           │   ├── HomeScreen.kt
│           │   ├── LogScreen.kt
│           │   ├── CalendarScreen.kt
│           │   ├── ProfileScreen.kt
│           │   └── SplashScreen.kt
│           ├── components/        # UIコンポーネント
│           │   ├── TaskCard.kt
│           │   ├── LogTaskCard.kt
│           │   ├── WeeklyProgressIndicator.kt
│           │   ├── CreateTaskDialog.kt
│           │   ├── TaskEditDialog.kt
│           │   ├── DateSelector.kt
│           │   ├── DayOfWeekSelector.kt
│           │   └── WeekCalendar.kt
│           ├── navigation/        # ナビゲーション
│           │   └── Navigation.kt
│           ├── ui/                # UI関連
│           │   ├── TaskioStrings.kt
│           │   └── theme/
│           └── utils/             # ユーティリティ
│               └── DateTimeUtils.kt
└── server/                        # Rustサーバー
    ├── Cargo.toml
    ├── src/
    │   ├── main.rs
    │   └── routes/
    └── .env.example
```

---

## 🎯 プロジェクトの目的

### **Objectives**
1. **Kotlin（Android開発）をモダン構成で学ぶ**
   - Jetpack Compose + Material3
   - MVVM + StateFlow
   - クリーンアーキテクチャの基礎
2. **Rust（Axum）でモダンなAPIサーバーを学ぶ**
3. Android ↔ Rust で実際に連携しながら、Web API の本質を理解する
4. 将来の拡張（認証・統計・グラフ・通知）に耐えられる形で構築する

---

## 🚦 開発進捗状況

### Phase 1: MVP ✅ 完了

| 機能 | 状態 | 説明 |
|-----|------|-----|
| Task CRUD | ✅ | 作成・一覧・詳細・編集・削除 |
| TaskLog CRUD | ✅ | ログの作成・一覧・編集・削除 |
| API通信 | ✅ | Retrofit + Axum連携 |
| 基本UI | ✅ | Material3ベースのデザイン |

### Phase 2: UI/UX改善 🔄 進行中

| 機能 | 状態 | 説明 |
|-----|------|-----|
| HomeScreen | ✅ | 週間カレンダー + タスク一覧 |
| LogScreen | ✅ | タスク一覧 + 週間進捗表示 |
| タスク編集ダイアログ | ✅ | HomeScreenと統一されたUI |
| 曜日セレクター | ✅ | 円形の曜日選択UI |
| 週間進捗インジケーター | ✅ | 7日間の進捗表示 |
| CalendarScreen | ⏳ | プレースホルダー |
| ProfileScreen | ⏳ | プレースホルダー |

### Phase 3: 機能拡張（予定）

| 機能 | 状態 | 説明 |
|-----|------|-----|
| カレンダー表示 | ⏳ | 月間/週間カレンダービュー |
| グラフ・統計 | ⏳ | 達成率・継続率のグラフ |
| データベース移行 | ⏳ | SQLx + PostgreSQL/SQLite |

### Phase 4: 高度な機能（将来）

| 機能 | 状態 | 説明 |
|-----|------|-----|
| リマインダー | ⏳ | プッシュ通知 |
| 認証 | ⏳ | Token認証 |
| データエクスポート | ⏳ | CSV/JSON出力 |

---

## 📱 画面構成

```
┌─────────────────────────────────────┐
│            Taskio                   │
├─────────────────────────────────────┤
│                                     │
│  [Home]  [Log]  [Calendar] [Profile]│
│                                     │
└─────────────────────────────────────┘

Home: 今日のタスク + 週間カレンダー
Log: 登録タスク一覧 + 週間進捗
Calendar: カレンダービュー（実装予定）
Profile: 設定・統計（実装予定）
```

---

## 🔧 セットアップ

### Server (Rust)

```bash
cd server
cp .env.example .env
cargo run
```

サーバーは `http://localhost:3000` で起動します。

### Client (Android)

1. Android Studioで `client/Taskio` を開く
2. エミュレーターまたは実機で実行
3. `RetrofitClient.kt` のBASE_URLを環境に合わせて設定

```kotlin
// RetrofitClient.kt
private const val BASE_URL = "http://10.0.2.2:3000/api/v1/"  // エミュレーター用
// private const val BASE_URL = "http://192.168.x.x:3000/api/v1/"  // 実機用
```

---

## 📡 API エンドポイント

| Method | Endpoint | 説明 |
|--------|----------|-----|
| GET | `/health` | ヘルスチェック |
| POST | `/tasks` | タスク作成 |
| GET | `/tasks` | タスク一覧取得 |
| GET | `/tasks/{id}` | タスク詳細取得 |
| PUT | `/tasks/{id}` | タスク更新 |
| DELETE | `/tasks/{id}` | タスク削除 |
| GET | `/tasks/{id}/logs` | ログ一覧取得 |
| POST | `/tasks/{id}/logs` | ログ作成 |

詳細は `docs/API.md` を参照。

---

## 📋 仕様書

| ドキュメント | 説明 |
|------------|-----|
| `docs/Requirements.md` | 要求仕様書 - 機能要件・非機能要件 |
| `docs/UseCase.md` | ユースケース仕様書 - 利用シナリオ |
| `docs/API.md` | REST API仕様書 - エンドポイント詳細 |
| `docs/Architecture.md` | アーキテクチャ設計書 - システム構成 |

---

## 🎨 デザインシステム

### カラーパレット

| 名前 | 色 | 用途 |
|-----|-----|-----|
| Primary | `#6200EE` | メインアクション |
| Secondary | `#03DAC6` | サブアクション |
| Surface | `#FFFFFF` | カード背景 |
| Background | `#F5F5F5` | 画面背景 |
| OnSurface | `#1C1B1F` | テキスト |

### コンポーネント

- **TaskCard**: ホーム画面用タスクカード（開始/終了ボタン付き）
- **LogTaskCard**: ログ画面用タスクカード（週間進捗表示）
- **WeeklyProgressIndicator**: 7日間の進捗を円形で表示
- **DayOfWeekSelector**: 曜日選択用の円形ボタン
- **DateSelector**: Material3 DatePicker連携

---

## 📄 ライセンス

MIT License

---

## 👤 Author

yt_hsgw