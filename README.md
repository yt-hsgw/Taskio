# TaskLog App (Android + Rust)

個人のトレーニング・生活習慣・日常タスクを  
**「いつ」「どれくらい」「結果（グラフ）」** で可視化するための  
ログ記録アプリです。

---

## 📌 技術スタック

### **Client (Android)**
- Kotlin
- Android Jetpack (ViewModel / LiveData / Compose or XML)
- Retrofit（Rust API と通信）
- Coroutines
- Jetpack Navigation（予定）

### **Server (Backend)**
- Rust
- Axum（Webフレームワーク）
- Tokio（Async Runtime）
- Serde（JSON）
- SQLx（将来的に導入予定）
- SQLite or PostgreSQL（後で決定）

---

## 📂 プロジェクト構成
```
Taskio/
├── README.md
├── .gitignore
├── client/
│ ├── app/
│ ├── build.gradle.kts
│ ├── settings.gradle.kts
│ └── README.md
└── server/
├── Cargo.toml
├── src/
│ ├── main.rs
│ └── routes/
├── README.md
└── .env.example
```

---

## 🎯 プロジェクトの目的

このプロジェクトでは、以下の2つの言語・技術を体系的に習得することを目的とします。

### **Objectives**
1. **Kotlin（Android開発）をモダン構成で学ぶ**
2. **Rust（Axum）でモダンなAPIサーバーを学ぶ**
3. Android ↔ Rust で実際に連携しながら、Web API の本質を理解する
4. 将来の拡張（認証・統計・グラフ・通知）に耐えられる形で構築する

---

## 🚦 現在の進行状況（2025）
- [x] アプリ方針策定
- [x] リポジトリ構成
- [ ] Rust(Axum) 初期構築
- [ ] Android（Kotlin）初期構築
- [ ] API 仕様書（draft）
- [ ] DB 設計
- [ ] クライアント – サーバー接続
- [ ] グラフ画面実装

---

## 📌 今後の開発フロー

1. **Rust（Axum）で API の skeleton を作成**
2. **Android側で Retrofit 通信層を用意**
3. Rust API と接続 → ジム・タスクログを登録/取得
4. グラフ画面・カレンダー画面を追加
5. 認証（Token）を追加
6. スケジュール通知・分析機能を追加

---

## 📄 ライセンス
MIT（変更可能）

