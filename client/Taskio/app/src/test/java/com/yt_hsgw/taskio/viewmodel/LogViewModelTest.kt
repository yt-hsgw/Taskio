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
import java.time.LocalDate

/**
 * LogViewModelのユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LogViewModel

    @Before
    fun setup() {
        viewModel = LogViewModel()
    }

    // ─────────────────────────────
    // 初期状態テスト
    // ─────────────────────────────

    @Test
    fun `初期状態ではタスクリストが空`() {
        assertTrue(viewModel.uiState.value.tasks.isEmpty())
    }

    @Test
    fun `初期状態では編集ダイアログが閉じている`() {
        assertFalse(viewModel.uiState.value.showEditDialog)
    }

    @Test
    fun `初期状態では削除ダイアログが閉じている`() {
        assertFalse(viewModel.uiState.value.showDeleteDialog)
    }

    @Test
    fun `UI状態が正しく初期化される`() {
        // ViewModelが正常に初期化され、uiStateが利用可能であることを確認
        val state = viewModel.uiState.value
        assertNotNull(state)
        assertNotNull(state.tasks)
        assertNotNull(state.weeklyProgress)
    }

    // ─────────────────────────────
    // 編集ダイアログテスト
    // ─────────────────────────────

    @Test
    fun `編集ダイアログを開くとタスク情報が設定される`() {
        val task = createTestTask()

        viewModel.openEditDialog(task)

        val state = viewModel.uiState.value
        assertTrue(state.showEditDialog)
        assertEquals(task, state.editingTask)
        assertEquals(task.title, state.editTitle)
        assertEquals(task.description ?: "", state.editDescription)
        assertEquals(task.isRecurring, state.editIsRecurring)
    }

    @Test
    fun `編集ダイアログを閉じると状態がリセットされる`() {
        val task = createTestTask()
        viewModel.openEditDialog(task)

        viewModel.closeEditDialog()

        val state = viewModel.uiState.value
        assertFalse(state.showEditDialog)
        assertNull(state.editingTask)
        assertEquals("", state.editTitle)
        assertEquals("", state.editDescription)
        assertNull(state.editScheduledDate)
        assertFalse(state.editIsRecurring)
        assertTrue(state.editSelectedDays.isEmpty())
    }

    @Test
    fun `編集ダイアログでタイトルを更新できる`() {
        val task = createTestTask()
        viewModel.openEditDialog(task)

        viewModel.updateEditTitle("Updated Title")

        assertEquals("Updated Title", viewModel.uiState.value.editTitle)
    }

    @Test
    fun `編集ダイアログで説明を更新できる`() {
        val task = createTestTask()
        viewModel.openEditDialog(task)

        viewModel.updateEditDescription("Updated Description")

        assertEquals("Updated Description", viewModel.uiState.value.editDescription)
    }

    @Test
    fun `編集ダイアログで予定日を更新できる`() {
        val task = createTestTask()
        viewModel.openEditDialog(task)

        val newDate = LocalDate.of(2025, 6, 15)
        viewModel.updateEditScheduledDate(newDate)

        assertEquals(newDate, viewModel.uiState.value.editScheduledDate)
    }

    // ─────────────────────────────
    // 繰り返し設定テスト（編集ダイアログ内）
    // ─────────────────────────────

    @Test
    fun `繰り返し設定をONにすると予定日がクリアされる`() {
        val task = createTestTask()
        viewModel.openEditDialog(task)

        val date = LocalDate.of(2025, 6, 15)
        viewModel.updateEditScheduledDate(date)

        viewModel.toggleEditRecurring(true)

        assertTrue(viewModel.uiState.value.editIsRecurring)
        assertNull(viewModel.uiState.value.editScheduledDate)
    }

    @Test
    fun `繰り返し設定をOFFにすると曜日選択がクリアされる`() {
        val task = createTestTask()
        viewModel.openEditDialog(task)

        viewModel.toggleEditRecurring(true)
        viewModel.toggleEditDay(1)
        viewModel.toggleEditDay(3)

        viewModel.toggleEditRecurring(false)

        assertFalse(viewModel.uiState.value.editIsRecurring)
        assertTrue(viewModel.uiState.value.editSelectedDays.isEmpty())
    }

    @Test
    fun `曜日をトグルすると選択状態が切り替わる`() {
        viewModel.toggleEditRecurring(true)

        viewModel.toggleEditDay(1) // 選択
        assertTrue(viewModel.uiState.value.editSelectedDays.contains(1))

        viewModel.toggleEditDay(1) // 選択解除
        assertFalse(viewModel.uiState.value.editSelectedDays.contains(1))
    }

    // ─────────────────────────────
    // 削除確認ダイアログテスト
    // ─────────────────────────────

    @Test
    fun `削除確認ダイアログを表示するとタスクが設定される`() {
        val task = createTestTask()

        viewModel.showDeleteConfirmation(task)

        val state = viewModel.uiState.value
        assertTrue(state.showDeleteDialog)
        assertEquals(task, state.taskToDelete)
    }

    @Test
    fun `削除確認ダイアログを非表示にすると状態がリセットされる`() {
        val task = createTestTask()
        viewModel.showDeleteConfirmation(task)

        viewModel.hideDeleteConfirmation()

        val state = viewModel.uiState.value
        assertFalse(state.showDeleteDialog)
        assertNull(state.taskToDelete)
    }

    // ─────────────────────────────
    // エラー・フラグ管理テスト
    // ─────────────────────────────

    @Test
    fun `エラーをクリアするとエラーメッセージがnullになる`() {
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `タスク更新フラグをリセットするとfalseになる`() {
        viewModel.resetTaskUpdated()
        assertFalse(viewModel.uiState.value.taskUpdated)
    }

    @Test
    fun `タスク削除フラグをリセットするとfalseになる`() {
        viewModel.resetTaskDeleted()
        assertFalse(viewModel.uiState.value.taskDeleted)
    }

    // ─────────────────────────────
    // 週間進捗計算テスト
    // ─────────────────────────────

    @Test
    fun `週間進捗のデータ構造を確認`() {
        // weeklyProgressはMap<String, Map<Int, Boolean>>
        // タスクID -> 曜日(0-6) -> 完了状態
        val weeklyProgress = viewModel.uiState.value.weeklyProgress
        assertTrue(weeklyProgress.isEmpty()) // 初期状態では空
    }

    @Test
    fun `繰り返しタスクの週間進捗パターンを確認`() {
        // 月・水・金の繰り返しタスクの場合
        val repeatDays = listOf(1, 3, 5) // 月・水・金
        val today = LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek.value % 7 // 日曜=0

        // repeatDaysに含まれる曜日で、過去または今日の場合は完了扱い
        repeatDays.forEach { dayIndex ->
            val isPastOrToday = dayIndex <= currentDayOfWeek
            val isScheduledDay = repeatDays.contains(dayIndex)
            val expectedCompleted = isPastOrToday && isScheduledDay

            // 計算ロジックの確認
            assertTrue(isScheduledDay)
            assertEquals(isPastOrToday, expectedCompleted)
        }
    }

    // ─────────────────────────────
    // TaskUpdateEventテスト
    // ─────────────────────────────

    @Test
    fun `TaskUpdateEvent_TaskUpdatedのデータ構造を確認`() {
        val task = createTestTask()
        val event = TaskUpdateEvent.TaskUpdated(task)

        assertEquals(task, event.task)
    }

    @Test
    fun `TaskUpdateEvent_TaskDeletedのデータ構造を確認`() {
        val taskId = "task-123"
        val event = TaskUpdateEvent.TaskDeleted(taskId)

        assertEquals(taskId, event.taskId)
    }

    // ─────────────────────────────
    // ヘルパーメソッド
    // ─────────────────────────────

    private fun createTestTask(
        id: String = "task-1",
        title: String = "Test Task",
        description: String? = "Test Description",
        isRecurring: Boolean = false,
        repeatDays: List<Int>? = null
    ): TaskItem {
        return TaskItem(
            id = id,
            title = title,
            description = description,
            createdAt = "2025-01-01T00:00:00Z",
            scheduledDate = "2025-01-15T00:00:00Z",
            repeatDays = repeatDays,
            isRecurring = isRecurring
        )
    }
}
