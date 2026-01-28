# Taskio UI テスト

## 概要

このディレクトリには、Taskio Android アプリの UI テスト（インストゥルメンテーションテスト）が含まれています。

## テスト構成

```
androidTest/
├── java/com/yt_hsgw/taskio/
│   ├── ExampleInstrumentedTest.kt    # サンプルテスト
│   ├── ui/
│   │   ├── HomeScreenTest.kt         # ホーム画面テスト
│   │   ├── CalendarScreenTest.kt     # カレンダー画面テスト
│   │   ├── LogScreenTest.kt          # ログ画面テスト
│   │   └── NavigationTest.kt         # ナビゲーションテスト
│   ├── components/
│   │   └── TaskCardTest.kt           # TaskCardコンポーネントテスト
│   ├── e2e/
│   │   └── TaskCreationFlowTest.kt   # E2Eフローテスト
│   └── util/
│       └── ComposeTestRule.kt        # テストユーティリティ
```

## テスト実行方法

### 前提条件

1. Android エミュレータまたは実機が接続されていること
2. サーバーが起動していること（API テストの場合）

### エミュレータの起動

```bash
# 利用可能なエミュレータを確認
emulator -list-avds

# エミュレータを起動（例: Pixel_6_API_34）
emulator -avd Pixel_6_API_34
```

### 全 UI テストの実行

```bash
cd client/Taskio
./gradlew connectedAndroidTest
```

### 特定のテストクラスのみ実行

```bash
# HomeScreenテストのみ
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.yt_hsgw.taskio.ui.HomeScreenTest

# NavigationTestのみ
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.yt_hsgw.taskio.ui.NavigationTest

# TaskCardTestのみ
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.yt_hsgw.taskio.components.TaskCardTest
```

### テスト結果の確認

テスト結果は以下のパスに出力されます：

```
app/build/reports/androidTests/connected/index.html
```

ブラウザで開いて結果を確認できます：

```bash
open app/build/reports/androidTests/connected/index.html
```

## テストの種類

### 1. 画面テスト（ui/）

各画面の表示と基本的な操作をテストします。

- **HomeScreenTest**: FAB、週間カレンダー、タスク作成ダイアログ
- **CalendarScreenTest**: カレンダーグリッド、月移動、凡例
- **LogScreenTest**: タスクリスト、空状態
- **NavigationTest**: ボトムナビゲーション、画面遷移

### 2. コンポーネントテスト（components/）

個別の UI コンポーネントをテストします。

- **TaskCardTest**: タスクカードの表示、開始/終了ボタン

### 3. E2E テスト（e2e/）

ユーザーの一連の操作フローをテストします。

- **TaskCreationFlowTest**: タスク作成の完全なフロー

## 注意事項

### サーバー接続

- 一部のテストは実際の API 呼び出しを行います
- テスト前にサーバーが起動していることを確認してください：

```bash
cd server
cargo run
```

### テスト環境

- テストはデバイスの状態（既存データ）に依存する場合があります
- 一貫した結果を得るには、テスト用のモックサーバーの使用を推奨します

### TestTag の使用

UI 要素の特定には `TestTags.kt` で定義された定数を使用できます：

```kotlin
// main/java/com/yt_hsgw/taskio/ui/TestTags.kt
object TestTags {
    const val HOME_FAB = "home_fab"
    const val HOME_TASK_LIST = "home_task_list"
    // ...
}
```

Composable に testTag を追加する場合：

```kotlin
Modifier.testTag(TestTags.HOME_FAB)
```

テストでの使用：

```kotlin
composeTestRule.onNodeWithTag(TestTags.HOME_FAB).performClick()
```

## トラブルシューティング

### エミュレータが見つからない

```bash
# Android SDK のパスを確認
echo $ANDROID_HOME

# エミュレータのパスを追加
export PATH=$PATH:$ANDROID_HOME/emulator
```

### テストがタイムアウトする

```bash
# タイムアウトを延長（デフォルト: 60秒）
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.timeout=120000
```

### サーバー接続エラー

- エミュレータからローカルホストに接続する場合は `10.0.2.2` を使用
- または、サーバーの IP アドレスを直接指定
