package com.yt_hsgw.taskio.model

import org.junit.Assert.*
import org.junit.Test

/**
 * TaskItem ユニットテスト
 *
 * TaskItem データクラスと TaskResponse からの変換テスト
 */
class TaskItemTest {

    @Test
    fun `create task item with all fields`() {
        val item = TaskItem(
            id = "test-id",
            title = "Test Task",
            description = "Description",
            createdAt = "2025-01-15T00:00:00Z",
            isStarted = true,
            isFinished = false,
            dueDate = "2025-01-20T12:00:00Z",
            scheduledDate = "2025-01-15T09:00:00Z",
            repeatDays = listOf(1, 3, 5),
            isRecurring = true
        )

        assertEquals("test-id", item.id)
        assertEquals("Test Task", item.title)
        assertEquals("Description", item.description)
        assertEquals("2025-01-15T00:00:00Z", item.createdAt)
        assertTrue(item.isStarted)
        assertFalse(item.isFinished)
        assertEquals("2025-01-20T12:00:00Z", item.dueDate)
        assertEquals("2025-01-15T09:00:00Z", item.scheduledDate)
        assertEquals(listOf(1, 3, 5), item.repeatDays)
        assertTrue(item.isRecurring)
    }

    @Test
    fun `create task item with default values`() {
        val item = TaskItem(
            id = "test-id",
            title = "Test Task",
            description = null,
            createdAt = "2025-01-15T00:00:00Z"
        )

        assertFalse(item.isStarted)
        assertFalse(item.isFinished)
        assertNull(item.dueDate)
        assertNull(item.scheduledDate)
        assertNull(item.repeatDays)
        assertFalse(item.isRecurring)
    }

    @Test
    fun `convert task response to task item`() {
        val response = TaskResponse(
            id = "response-id",
            title = "Response Task",
            description = "Response Description",
            is_active = true,
            due_date = "2025-01-20T12:00:00Z",
            scheduled_date = "2025-01-15T09:00:00Z",
            repeat_days = listOf(0, 6),
            is_recurring = true,
            created_at = "2025-01-01T00:00:00Z",
            updated_at = "2025-01-10T00:00:00Z"
        )

        val item = response.toTaskItem()

        assertEquals("response-id", item.id)
        assertEquals("Response Task", item.title)
        assertEquals("Response Description", item.description)
        assertEquals("2025-01-01T00:00:00Z", item.createdAt)
        assertEquals("2025-01-20T12:00:00Z", item.dueDate)
        assertEquals("2025-01-15T09:00:00Z", item.scheduledDate)
        assertEquals(listOf(0, 6), item.repeatDays)
        assertTrue(item.isRecurring)
    }

    @Test
    fun `convert task response with null fields to task item`() {
        val response = TaskResponse(
            id = "response-id",
            title = "Minimal Task",
            description = null,
            is_active = true,
            due_date = null,
            scheduled_date = null,
            repeat_days = null,
            is_recurring = false,
            created_at = "2025-01-01T00:00:00Z",
            updated_at = "2025-01-01T00:00:00Z"
        )

        val item = response.toTaskItem()

        assertEquals("response-id", item.id)
        assertEquals("Minimal Task", item.title)
        assertNull(item.description)
        assertNull(item.dueDate)
        assertNull(item.scheduledDate)
        assertNull(item.repeatDays)
        assertFalse(item.isRecurring)
    }

    @Test
    fun `task item default isStarted is false`() {
        val item = TaskItem(
            id = "id",
            title = "Task",
            description = null,
            createdAt = "2025-01-15T00:00:00Z"
        )

        assertFalse(item.isStarted)
    }

    @Test
    fun `task item default isFinished is false`() {
        val item = TaskItem(
            id = "id",
            title = "Task",
            description = null,
            createdAt = "2025-01-15T00:00:00Z"
        )

        assertFalse(item.isFinished)
    }

    @Test
    fun `task item copy with modified fields`() {
        val original = TaskItem(
            id = "id",
            title = "Original",
            description = null,
            createdAt = "2025-01-15T00:00:00Z",
            isStarted = false,
            isFinished = false
        )

        val modified = original.copy(isStarted = true)

        assertEquals(original.id, modified.id)
        assertEquals(original.title, modified.title)
        assertFalse(original.isStarted)
        assertTrue(modified.isStarted)
    }

    @Test
    fun `task item equality`() {
        val item1 = TaskItem(
            id = "same-id",
            title = "Task",
            description = null,
            createdAt = "2025-01-15T00:00:00Z"
        )

        val item2 = TaskItem(
            id = "same-id",
            title = "Task",
            description = null,
            createdAt = "2025-01-15T00:00:00Z"
        )

        assertEquals(item1, item2)
    }

    @Test
    fun `task item inequality with different id`() {
        val item1 = TaskItem(
            id = "id-1",
            title = "Task",
            description = null,
            createdAt = "2025-01-15T00:00:00Z"
        )

        val item2 = TaskItem(
            id = "id-2",
            title = "Task",
            description = null,
            createdAt = "2025-01-15T00:00:00Z"
        )

        assertNotEquals(item1, item2)
    }

    @Test
    fun `convert recurring task response correctly`() {
        val response = TaskResponse(
            id = "recurring-id",
            title = "Daily Task",
            description = null,
            is_active = true,
            due_date = null,
            scheduled_date = "2025-01-15T09:00:00Z",
            repeat_days = listOf(1, 2, 3, 4, 5), // 平日のみ
            is_recurring = true,
            created_at = "2025-01-01T00:00:00Z",
            updated_at = "2025-01-01T00:00:00Z"
        )

        val item = response.toTaskItem()

        assertTrue(item.isRecurring)
        assertEquals(listOf(1, 2, 3, 4, 5), item.repeatDays)
        assertEquals("2025-01-15T09:00:00Z", item.scheduledDate)
    }
}
