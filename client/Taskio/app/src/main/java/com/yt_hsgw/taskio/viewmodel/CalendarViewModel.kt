package com.yt_hsgw.taskio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yt_hsgw.taskio.api.RetrofitClient
import com.yt_hsgw.taskio.model.CalendarLogResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * カレンダー画面のUI状態
 *
 * @property currentMonth 現在表示している月
 * @property selectedDate 選択している日付
 * @property logsMap 日付ごとのログマップ
 * @property datesWithLogs ログが存在する日付のセット
 * @property isLoading ローディング状態
 * @property errorMessage エラーメッセージ
 */
data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val logsMap: Map<LocalDate, List<CalendarLogResponse>> = emptyMap(),
    val datesWithLogs: Set<LocalDate> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * カレンダー画面のViewModel
 *
 * 月単位のカレンダー表示と、選択日のタスクログ表示を管理します。
 */
class CalendarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadMonthData()
    }

    /**
     * 現在の月のデータを読み込み
     */
    fun loadMonthData() {
        val month = _uiState.value.currentMonth
        loadLogsForMonth(month)
    }

    /**
     * 指定月のログを取得
     *
     * @param month 対象月
     */
    private fun loadLogsForMonth(month: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // 月の開始日と終了日
                val fromDate = month.atDay(1)
                val toDate = month.atEndOfMonth()

                val response = RetrofitClient.api.getLogsByDateRange(
                    from = fromDate.format(dateFormatter),
                    to = toDate.format(dateFormatter)
                )

                if (response.isSuccessful && response.body() != null) {
                    val logs = response.body()!!.logs
                    android.util.Log.d(TAG, "API成功: ${logs.size}件のタスクを取得")
                    logs.forEach { log ->
                        android.util.Log.d(TAG, "タスク: ${log.task_title}, scheduled_date: ${log.scheduled_date}")
                    }
                    processLogs(logs)
                } else {
                    android.util.Log.e(TAG, "APIエラー: ${response.code()}")
                    _uiState.update { it.copy(errorMessage = "データ取得に失敗しました") }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "例外発生: ${e.message}", e)
                _uiState.update { it.copy(errorMessage = "サーバー接続エラー: ${e.message}") }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * タスクデータを処理してUIStateに反映
     *
     * @param logs タスク一覧
     */
    private fun processLogs(logs: List<CalendarLogResponse>) {
        val logsMap = mutableMapOf<LocalDate, MutableList<CalendarLogResponse>>()
        val datesWithLogs = mutableSetOf<LocalDate>()

        for (log in logs) {
            log.scheduled_date?.let { scheduledDate ->
                try {
                    val date = LocalDate.parse(scheduledDate.substring(0, 10))
                    logsMap.getOrPut(date) { mutableListOf() }.add(log)
                    datesWithLogs.add(date)
                } catch (e: Exception) {
                    // パースエラーは無視
                }
            }
        }

        _uiState.update {
            it.copy(
                logsMap = logsMap.mapValues { entry -> entry.value.toList() },
                datesWithLogs = datesWithLogs
            )
        }
    }

    /**
     * 日付を選択
     *
     * @param date 選択する日付
     */
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    /**
     * 前月に移動
     */
    fun navigateToPreviousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadLogsForMonth(newMonth)
    }

    /**
     * 次月に移動
     */
    fun navigateToNextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadLogsForMonth(newMonth)
    }

    /**
     * 今日に移動
     */
    fun navigateToToday() {
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)
        _uiState.update {
            it.copy(
                currentMonth = currentMonth,
                selectedDate = today
            )
        }
        loadLogsForMonth(currentMonth)
    }

    /**
     * エラーをクリア
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 選択日のログを取得
     *
     * @return 選択日のログ一覧
     */
    fun getLogsForSelectedDate(): List<CalendarLogResponse> {
        return _uiState.value.logsMap[_uiState.value.selectedDate] ?: emptyList()
    }

    companion object {
        private const val TAG = "CalendarViewModel"
    }
}
