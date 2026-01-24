package com.yt_hsgw.taskio.ui

/**
 * Taskioアプリ全体で使用する文字列リソース
 *
 * このオブジェクトはアプリ内で使用されるすべての文字列定数を一元管理します。
 * 多言語対応時はAndroidのstrings.xmlに移行することを推奨します。
 *
 * @see [Android String Resources](https://developer.android.com/guide/topics/resources/string-resource)
 */
object TaskioStrings {
    // ─────────────────────────────
    // Common / 共通
    // ─────────────────────────────
    const val APP_NAME = "Taskio"
    const val BUTTON_CREATE = "作成"
    const val BUTTON_CANCEL = "キャンセル"
    const val BUTTON_OK = "OK"
    const val BUTTON_RETRY = "再試行"
    const val BUTTON_DELETE = "削除"
    const val BUTTON_CLEAR = "クリア"
    const val BUTTON_SAVE = "保存"
    const val BUTTON_EDIT = "編集"

    // ─────────────────────────────
    // Task / タスク
    // ─────────────────────────────
    const val TASK_ADD = "タスクを追加"
    const val TASK_NEW = "新しいタスク"
    const val TASK_TITLE = "タイトル"
    const val TASK_TITLE_REQUIRED = "タイトル *"
    const val TASK_DESCRIPTION = "説明"
    const val TASK_DESCRIPTION_OPTIONAL = "説明（任意）"
    const val TASK_SCHEDULED_DATE = "予定日"
    const val TASK_SELECT_DATE = "日付を選択"
    const val TASK_RECURRING = "繰り返し"
    const val TASK_SELECT_RECURRING_DAYS = "繰り返す曜日を選択"
    const val TASK_LIST = "タスク一覧"
    const val TASK_CREATOR = "タスク作成"
    const val TASK_COUNT_FORMAT = "%d tasks"
    const val TASK_CREATED_AT_PREFIX = "作成: "
    const val TASK_EDIT = "タスクを編集"
    const val TASK_DETAIL = "詳細"
    const val TASK_NOT_RECURRING = "単発タスク"

    // ─────────────────────────────
    // Task Actions / タスクアクション
    // ─────────────────────────────
    const val ACTION_START = "開始"
    const val ACTION_END = "終了"

    // ─────────────────────────────
    // Empty State / 空状態
    // ─────────────────────────────
    const val EMPTY_TASKS_MESSAGE = "この日のタスクはありません"
    const val EMPTY_TASKS_HINT = "タスクがありません\n上の入力欄から作成できます"
    const val EMPTY_LOG_MESSAGE = "登録されているタスクがありません"

    // ─────────────────────────────
    // Date / 日付
    // ─────────────────────────────
    const val TODAY = "今日"
    const val DATE_FORMAT_MONTH_DAY = "M月d日"
    const val DATE_FORMAT_FULL = "yyyy/MM/dd"
    const val DATE_FORMAT_WITH_TIME = "yyyy/MM/dd HH:mm"
    const val DATE_FORMAT_WITH_DAY_OF_WEEK = "yyyy/MM/dd (E)"

    // ─────────────────────────────
    // DateTime Picker / 日時選択
    // ─────────────────────────────
    const val DATETIME_SET_FORMAT = "%sを設定"
    const val DATETIME_SELECT_TIME = "時刻を選択"

    // ─────────────────────────────
    // Days of Week (Short) / 曜日（短縮）
    // ─────────────────────────────
    const val SUNDAY_SHORT = "日"
    const val MONDAY_SHORT = "月"
    const val TUESDAY_SHORT = "火"
    const val WEDNESDAY_SHORT = "水"
    const val THURSDAY_SHORT = "木"
    const val FRIDAY_SHORT = "金"
    const val SATURDAY_SHORT = "土"

    // ─────────────────────────────
    // Dialog / ダイアログ
    // ─────────────────────────────
    const val DIALOG_DELETE_TASK_TITLE = "タスクを削除"
    const val DIALOG_DELETE_TASK_MESSAGE_FORMAT = "「%s」を削除しますか？"

    // ─────────────────────────────
    // Content Descriptions / コンテンツ説明
    // ─────────────────────────────
    const val CD_RECURRING = "繰り返し"
    const val CD_CLEAR_DATE = "日付をクリア"
    const val CD_SELECT_DATE = "日付を選択"
    const val CD_DELETE_TASK = "タスクを削除"
    const val CD_EDIT_TASK = "タスクを編集"
    const val CD_WEEKLY_PROGRESS = "週間進捗"

    // ─────────────────────────────
    // Validation / バリデーション
    // ─────────────────────────────
    const val VALIDATION_TITLE_REQUIRED = "タイトルを入力してください"
    const val VALIDATION_TITLE_MAX_LENGTH = "タイトルは255文字以内で入力してください"
    const val VALIDATION_RECURRING_DAYS_REQUIRED = "繰り返しタスクは少なくとも1つの曜日を選択してください"

    // ─────────────────────────────
    // Error Messages / エラーメッセージ
    // ─────────────────────────────
    const val ERROR_NETWORK = "ネットワークエラーが発生しました"
    const val ERROR_SERVER = "サーバーエラーが発生しました"
    const val ERROR_TASK_CREATE_FAILED = "タスクの作成に失敗しました"
    const val ERROR_TASK_UPDATE_FAILED = "タスクの更新に失敗しました"
    const val ERROR_UNKNOWN = "不明なエラーが発生しました"

    // ─────────────────────────────
    // Success Messages / 成功メッセージ
    // ─────────────────────────────
    const val SUCCESS_TASK_CREATED = "タスクを作成しました"
    const val SUCCESS_TASK_UPDATED = "タスクを更新しました"

    // ─────────────────────────────
    // Screen Titles / 画面タイトル
    // ─────────────────────────────
    const val SCREEN_HOME = "Home"
    const val SCREEN_LOG = "Log"
    const val SCREEN_CALENDAR = "Calendar"
    const val SCREEN_PROFILE = "Profile"

    // ─────────────────────────────
    // Placeholder Messages / プレースホルダー
    // ─────────────────────────────
    const val PLACEHOLDER_CALENDAR = "Calendar and statistics will appear here"
    const val PLACEHOLDER_LOG = "Task logs will appear here"
    const val PLACEHOLDER_PROFILE = "User profile settings will appear here"

    // ─────────────────────────────
    // Splash Screen / スプラッシュ画面
    // ─────────────────────────────
    const val SPLASH_TAGLINE = "Your personal task manager"

    // ─────────────────────────────
    // Log Screen / ログ画面
    // ─────────────────────────────
    const val LOG_WEEKLY_SCHEDULE = "週間スケジュール"
    const val LOG_COMPLETION_RATE = "達成率"

    /**
     * 曜日の短縮名を取得
     *
     * @param dayOfWeek 曜日 (1=月曜日, 7=日曜日、java.time.DayOfWeek準拠)
     * @return 曜日の短縮名（例: "月", "火"）
     */
    fun getDayOfWeekShort(dayOfWeek: Int): String = when (dayOfWeek) {
        1 -> MONDAY_SHORT
        2 -> TUESDAY_SHORT
        3 -> WEDNESDAY_SHORT
        4 -> THURSDAY_SHORT
        5 -> FRIDAY_SHORT
        6 -> SATURDAY_SHORT
        7 -> SUNDAY_SHORT
        else -> ""
    }

    /**
     * 曜日インデックスから曜日名を取得（0=日曜日）
     *
     * @param dayIndex 曜日インデックス（0=日曜日, 6=土曜日）
     * @return 曜日の短縮名
     */
    fun getDayOfWeekByIndex(dayIndex: Int): String = when (dayIndex) {
        0 -> SUNDAY_SHORT
        1 -> MONDAY_SHORT
        2 -> TUESDAY_SHORT
        3 -> WEDNESDAY_SHORT
        4 -> THURSDAY_SHORT
        5 -> FRIDAY_SHORT
        6 -> SATURDAY_SHORT
        else -> ""
    }

    /**
     * 繰り返し曜日リストを文字列に変換
     *
     * @param repeatDays 繰り返し曜日のリスト（0=日曜日, 6=土曜日）
     * @return フォーマット済み文字列（例: "月・水・金"）
     */
    fun formatRepeatDays(repeatDays: List<Int>?): String {
        if (repeatDays.isNullOrEmpty()) return TASK_NOT_RECURRING
        
        // 日〜土の順序でソート
        val sorted = repeatDays.sorted()
        return sorted.joinToString("・") { getDayOfWeekByIndex(it) }
    }

    /**
     * タスク数のフォーマット済み文字列を取得
     *
     * @param count タスク数
     * @return フォーマット済み文字列（例: "5 tasks"）
     */
    fun formatTaskCount(count: Int): String = TASK_COUNT_FORMAT.format(count)

    /**
     * 削除確認メッセージのフォーマット済み文字列を取得
     *
     * @param taskTitle タスクタイトル
     * @return フォーマット済み文字列（例: "「買い物」を削除しますか？"）
     */
    fun formatDeleteConfirmMessage(taskTitle: String): String =
        DIALOG_DELETE_TASK_MESSAGE_FORMAT.format(taskTitle)

    /**
     * 日時設定ボタンのラベルを取得
     *
     * @param label ラベル名
     * @return フォーマット済み文字列（例: "期限を設定"）
     */
    fun formatDateTimeSetLabel(label: String): String =
        DATETIME_SET_FORMAT.format(label)
}
