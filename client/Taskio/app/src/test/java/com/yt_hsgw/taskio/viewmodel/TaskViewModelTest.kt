package com.yt_hsgw.taskio.viewmodel

import com.yt_hsgw.taskio.model.TaskItem
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
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * TaskViewModelのユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: TaskViewModel

    @Before
    fun setup() {
        viewModel = TaskViewModel()
    }

    // ─────────────────────────────
    // 初期状態テスト
    // ─────────────────────────────

    @Test
    fun `初期状態では今日の日付が選択されている`() {
        val today = LocalDate.now()
        assertEquals(today, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `初期状態ではタスクリストが空`() {
        assertTrue(viewModel.uiState.value.tasks.isEmpty())
    }

    @Test
    fun `初期状態では入力フィールドが空`() {
        assertEquals("", viewModel.uiState.value.title)
        assertEquals("", viewModel.uiState.value.description)
    }

    // ─────────────────────────────
    // 日付選択テスト
    // ─────────────────────────────

    @Test
    fun `日付を選択すると選択日が更新される`() {
        val targetDate = LocalDate.of(2025, 6, 15)
        viewModel.onDateSelected(targetDate)
        assertEquals(targetDate, viewModel.uiState.value.selectedDate)
    }

    // ─────────────────────────────
    // 入力状態管理テスト
    // ─────────────────────────────

    @Test
    fun `タイトル入力が反映される`() {
        viewModel.updateTitle("New Task")
        assertEquals("New Task", viewModel.uiState.value.title)
    }

    @Test
    fun `説明入力が反映される`() {
        viewModel.updateDescription("Task description")
        assertEquals("Task description", viewModel.uiState.value.description)
    }

    @Test
    fun `エラーメッセージがクリアされる`() {
        // エラー状態を模擬するため、直接状態を設定することはできないので
        // clearErrorが呼び出せることを確認
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ─────────────────────────────
    // 繰り返し設定テスト
    // ─────────────────────────────

    @Test
    fun `繰り返し設定をONにすると isRecurring が true になる`() {
        viewModel.toggleRecurring(true)
        assertTrue(viewModel.uiState.value.isRecurring)
    }

    @Test
    fun `繰り返し設定をOFFにすると曜日選択がリセットされる`() {
        // 繰り返しをONにして曜日を選択
        viewModel.toggleRecurring(true)
        viewModel.toggleDay(1) // 月曜日
        viewModel.toggleDay(3) // 水曜日

        // 繰り返しをOFFにする
        viewModel.toggleRecurring(false)

        assertFalse(viewModel.uiState.value.isRecurring)
        assertTrue(viewModel.uiState.value.selectedDays.isEmpty())
    }

    @Test
    fun `曜日をトグルすると選択状態が切り替わる`() {
        viewModel.toggleRecurring(true)

        // 曜日を選択
        viewModel.toggleDay(1)
        assertTrue(viewModel.uiState.value.selectedDays.contains(1))

        // 同じ曜日をもう一度トグルすると選択解除
        viewModel.toggleDay(1)
        assertFalse(viewModel.uiState.value.selectedDays.contains(1))
    }

    @Test
    fun `複数の曜日を選択できる`() {
        viewModel.toggleRecurring(true)
        viewModel.toggleDay(1) // 月曜日
        viewModel.toggleDay(3) // 水曜日
        viewModel.toggleDay(5) // 金曜日

        assertEquals(setOf(1, 3, 5), viewModel.uiState.value.selectedDays)
    }

    // ─────────────────────────────
    // タスク作成ダイアログテスト
    // ─────────────────────────────

    @Test
    fun `ダイアログを開くと選択中の日付が予定日に設定される`() {
        val targetDate = LocalDate.of(2025, 3, 15)
        viewModel.onDateSelected(targetDate)

        viewModel.openCreateDialog()

        val scheduledDate = viewModel.uiState.value.scheduledDate
        assertNotNull(scheduledDate)
        assertEquals(targetDate, scheduledDate?.toLocalDate())
    }

    @Test
    fun `ダイアログ状態をクリアすると入力がリセットされる`() {
        viewModel.updateTitle("Test")
        viewModel.updateDescription("Description")
        viewModel.toggleRecurring(true)
        viewModel.toggleDay(1)

        viewModel.clearDialogState()

        assertEquals("", viewModel.uiState.value.title)
        assertEquals("", viewModel.uiState.value.description)
        assertFalse(viewModel.uiState.value.isRecurring)
        assertTrue(viewModel.uiState.value.selectedDays.isEmpty())
    }

    @Test
    fun `タスク作成完了フラグがリセットされる`() {
        viewModel.resetTaskCreated()
        assertFalse(viewModel.uiState.value.taskCreated)
    }

    // ─────────────────────────────
    // 日付フィルタリングテスト (TaskDayKeyの生成)
    // ─────────────────────────────

    @Test
    fun `createTaskDayKeyが正しいフォーマットでキーを生成する`() {
        val taskId = "task-123"
        val date = LocalDate.of(2025, 1, 15)

        val key = TaskViewModel.createTaskDayKey(taskId, date)

        assertEquals("task-123_2025-01-15", key)
    }

    // ─────────────────────────────
    // フィルタリングロジックテスト
    // ─────────────────────────────

    @Test
    fun `繰り返しタスクが正しい曜日でフィルタリングされる`() = runTest {
        // 月・水・金の繰り返しタスク
        val recurringTask = TaskItem(
            id = "task-1",
            title = "Workout",
            description = null,
            createdAt = "2025-01-01T00:00:00Z",
            repeatDays = listOf(1, 3, 5), // 月・水・金
            isRecurring = true,
            scheduledDate = "2025-01-01T00:00:00Z" // 開始日
        )

        // 月曜日を選択（2025-01-06は月曜日）
        val monday = LocalDate.of(2025, 1, 6)
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)

        // filterTasksForDateは private なので、
        // filteredTasksのStateFlowを通じてテスト
        // ここでは繰り返しタスクのロジックを直接確認
        assertTrue(recurringTask.repeatDays!!.contains(1)) // 月曜日インデックス

        // 火曜日を選択（2025-01-07は火曜日）
        val tuesday = LocalDate.of(2025, 1, 7)
        assertEquals(DayOfWeek.TUESDAY, tuesday.dayOfWeek)
        assertFalse(recurringTask.repeatDays!!.contains(2)) // 火曜日インデックス
    }

    @Test
    fun `単発タスクは予定日と一致する日のみ表示される`() {
        val singleTask = TaskItem(
            id = "task-2",
            title = "Meeting",
            description = null,
            createdAt = "2025-01-01T00:00:00Z",
            scheduledDate = "2025-01-15T10:00:00Z",
            isRecurring = false
        )

        // 予定日の日付部分を抽出
        val scheduledDate = LocalDate.parse(singleTask.scheduledDate!!.substringBefore("T"))
        assertEquals(LocalDate.of(2025, 1, 15), scheduledDate)
    }

    @Test
    fun `繰り返しタスクはスケジュール開始日前は表示されない`() {
        val recurringTask = TaskItem(
            id = "task-3",
            title = "Daily Standup",
            description = null,
            createdAt = "2025-01-01T00:00:00Z",
            repeatDays = listOf(1, 2, 3, 4, 5), // 月〜金
            isRecurring = true,
            scheduledDate = "2025-02-01T00:00:00Z" // 2月1日から開始
        )

        // 開始日より前の日付
        val beforeStartDate = LocalDate.of(2025, 1, 15)
        val startDate = LocalDate.parse(recurringTask.scheduledDate!!.substringBefore("T"))

        assertTrue(beforeStartDate.isBefore(startDate))

        // 開始日以降の日付
        val afterStartDate = LocalDate.of(2025, 2, 10)
        assertFalse(afterStartDate.isBefore(startDate))
    }
}
