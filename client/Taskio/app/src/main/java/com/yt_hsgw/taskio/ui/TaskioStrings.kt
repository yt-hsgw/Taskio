package com.yt_hsgw.taskio.ui

/**
 * Taskioアプリ全体で使用する文字列リソース
 * 
 * 多言語対応時はAndroidのstrings.xmlに移行することを推奨
 */
object TaskioStrings {
    // Common
    const val APP_NAME = "Taskio"
    const val BUTTON_CREATE = "作成"
    const val BUTTON_CANCEL = "キャンセル"
    const val BUTTON_OK = "OK"
    const val BUTTON_RETRY = "再試行"
    
    // Task
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
    
    // Task Actions
    const val ACTION_START = "開始"
    const val ACTION_END = "終了"
    
    // Empty State
    const val EMPTY_TASKS_MESSAGE = "この日のタスクはありません"
    
    // Date
    const val TODAY = "今日"
    const val DATE_FORMAT_MONTH_DAY = "M月d日"
    
    // Days of Week (Short)
    const val SUNDAY_SHORT = "日"
    const val MONDAY_SHORT = "月"
    const val TUESDAY_SHORT = "火"
    const val WEDNESDAY_SHORT = "水"
    const val THURSDAY_SHORT = "木"
    const val FRIDAY_SHORT = "金"
    const val SATURDAY_SHORT = "土"
    
    // Content Descriptions
    const val CD_RECURRING = "繰り返し"
    const val CD_CLEAR_DATE = "日付をクリア"
    const val CD_SELECT_DATE = "日付を選択"
    
    // Validation
    const val VALIDATION_TITLE_REQUIRED = "タイトルを入力してください"
    const val VALIDATION_TITLE_MAX_LENGTH = "タイトルは255文字以内で入力してください"
    
    // Error Messages
    const val ERROR_NETWORK = "ネットワークエラーが発生しました"
    const val ERROR_SERVER = "サーバーエラーが発生しました"
    const val ERROR_TASK_CREATE_FAILED = "タスクの作成に失敗しました"
    
    // Success Messages
    const val SUCCESS_TASK_CREATED = "タスクを作成しました"
    
    /**
     * 曜日の短縮名を取得
     * @param dayOfWeek 曜日 (1=月曜日, 7=日曜日、java.time.DayOfWeek準拠)
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
}
