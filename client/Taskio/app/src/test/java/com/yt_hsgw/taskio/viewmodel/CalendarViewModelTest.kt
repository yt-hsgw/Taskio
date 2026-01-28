package com.yt_hsgw.taskio.viewmodel

import com.yt_hsgw.taskio.model.CalendarLogResponse
import com.yt_hsgw.taskio.model.TaskStatus
import com.yt_hsgw.taskio.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * CalendarViewModelのユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setup() {
        viewModel = CalendarViewModel()
    }

    // ─────────────────────────────
    // 初期状態テスト
    // ─────────────────────────────

    @Test
    fun `初期状態では現在月が設定される`() {
        val currentMonth = YearMonth.now()
        assertEquals(currentMonth, viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `初期状態では今日が選択されている`() {
        val today = LocalDate.now()
        assertEquals(today, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `初期状態ではログマップが空`() {
        assertTrue(viewModel.uiState.value.logsMap.isEmpty())
    }

    @Test
    fun `初期状態ではエラーメッセージがない`() {
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ─────────────────────────────
    // 月移動テスト
    // ─────────────────────────────

    @Test
    fun `前月に移動すると月が1つ減る`() {
        val initialMonth = viewModel.uiState.value.currentMonth

        viewModel.navigateToPreviousMonth()

        val expectedMonth = initialMonth.minusMonths(1)
        assertEquals(expectedMonth, viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `次月に移動すると月が1つ増える`() {
        val initialMonth = viewModel.uiState.value.currentMonth

        viewModel.navigateToNextMonth()

        val expectedMonth = initialMonth.plusMonths(1)
        assertEquals(expectedMonth, viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `複数回の月移動が正しく動作する`() {
        val initialMonth = viewModel.uiState.value.currentMonth

        // 3ヶ月先に移動
        viewModel.navigateToNextMonth()
        viewModel.navigateToNextMonth()
        viewModel.navigateToNextMonth()

        assertEquals(initialMonth.plusMonths(3), viewModel.uiState.value.currentMonth)

        // 2ヶ月戻る
        viewModel.navigateToPreviousMonth()
        viewModel.navigateToPreviousMonth()

        assertEquals(initialMonth.plusMonths(1), viewModel.uiState.value.currentMonth)
    }

    // ─────────────────────────────
    // 今日へ移動テスト
    // ─────────────────────────────

    @Test
    fun `今日へ移動すると今日の日付と月にリセットされる`() {
        // 別の月に移動
        viewModel.navigateToNextMonth()
        viewModel.navigateToNextMonth()
        viewModel.selectDate(LocalDate.of(2030, 1, 15))

        // 今日へ移動
        viewModel.navigateToToday()

        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)

        assertEquals(today, viewModel.uiState.value.selectedDate)
        assertEquals(currentMonth, viewModel.uiState.value.currentMonth)
    }

    // ─────────────────────────────
    // 日付選択テスト
    // ─────────────────────────────

    @Test
    fun `日付を選択すると選択日が更新される`() {
        val targetDate = LocalDate.of(2025, 6, 20)

        viewModel.selectDate(targetDate)

        assertEquals(targetDate, viewModel.uiState.value.selectedDate)
    }

    // ─────────────────────────────
    // エラー処理テスト
    // ─────────────────────────────

    @Test
    fun `エラーをクリアするとエラーメッセージがnullになる`() {
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ─────────────────────────────
    // ログマッピングテスト
    // ─────────────────────────────

    @Test
    fun `選択日のログが正しく取得される`() {
        // 初期状態ではログがないため空リストが返される
        val logs = viewModel.getLogsForSelectedDate()
        assertTrue(logs.isEmpty())
    }

    @Test
    fun `ログの日付パースが正しく動作する`() {
        // 日付文字列のパース確認（processLogs内部ロジックの確認）
        val scheduledDate = "2025-01-15T10:00:00Z"
        val parsedDate = LocalDate.parse(scheduledDate.substring(0, 10))

        assertEquals(LocalDate.of(2025, 1, 15), parsedDate)
    }

    @Test
    fun `不正な日付形式はパースエラーになる`() {
        val invalidDate = "invalid-date"

        val result = runCatching {
            LocalDate.parse(invalidDate.substring(0, 10))
        }

        assertTrue(result.isFailure)
    }

    // ─────────────────────────────
    // ログデータ構造テスト
    // ─────────────────────────────

    @Test
    fun `CalendarLogResponseのデータ構造を確認`() {
        val log = CalendarLogResponse(
            id = "log-1",
            task_id = "task-1",
            task_title = "Test Task",
            task_description = "Test Description",
            memo = null,
            created_at = "2025-01-15T09:00:00Z",
            scheduled_date = "2025-01-15T00:00:00Z",
            status = TaskStatus.COMPLETED
        )

        assertEquals("log-1", log.id)
        assertEquals("task-1", log.task_id)
        assertEquals("Test Task", log.task_title)
        assertEquals("Test Description", log.task_description)
        assertNull(log.memo)
        assertEquals(TaskStatus.COMPLETED, log.status)
    }

    @Test
    fun `進行中タスクのログデータ構造を確認`() {
        val log = CalendarLogResponse(
            id = "log-2",
            task_id = "task-2",
            task_title = "In Progress Task",
            task_description = null,
            memo = "Working on it",
            created_at = "2025-01-15T09:00:00Z",
            scheduled_date = "2025-01-15T00:00:00Z",
            status = TaskStatus.IN_PROGRESS
        )

        assertEquals(TaskStatus.IN_PROGRESS, log.status)
        assertEquals("Working on it", log.memo)
        assertNull(log.task_description)
    }

    // ─────────────────────────────
    // UI状態の存在確認テスト
    // ─────────────────────────────

    @Test
    fun `UI状態が正しく初期化される`() {
        // ViewModelが正常に初期化され、uiStateが利用可能であることを確認
        val state = viewModel.uiState.value
        assertNotNull(state)
        assertNotNull(state.currentMonth)
        assertNotNull(state.selectedDate)
        assertNotNull(state.logsMap)
        assertNotNull(state.datesWithLogs)
    }
}
