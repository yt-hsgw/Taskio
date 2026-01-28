package com.yt_hsgw.taskio.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * TaskResponse シリアライゼーションテスト
 */
class TaskResponseSerializationTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun `deserialize complete task response`() {
        val json = """
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "title": "Test Task",
                "description": "Task description",
                "is_active": true,
                "due_date": "2025-01-20T12:00:00Z",
                "scheduled_date": "2025-01-15T09:00:00Z",
                "repeat_days": [1, 3, 5],
                "is_recurring": true,
                "created_at": "2025-01-01T00:00:00Z",
                "updated_at": "2025-01-10T00:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", result!!.id)
        assertEquals("Test Task", result.title)
        assertEquals("Task description", result.description)
        assertTrue(result.is_active)
        assertEquals("2025-01-20T12:00:00Z", result.due_date)
        assertEquals("2025-01-15T09:00:00Z", result.scheduled_date)
        assertEquals(listOf(1, 3, 5), result.repeat_days)
        assertTrue(result.is_recurring)
        assertEquals("2025-01-01T00:00:00Z", result.created_at)
        assertEquals("2025-01-10T00:00:00Z", result.updated_at)
    }

    @Test
    fun `deserialize task response with null optional fields`() {
        val json = """
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "title": "Simple Task",
                "description": null,
                "is_active": true,
                "due_date": null,
                "scheduled_date": null,
                "repeat_days": null,
                "is_recurring": false,
                "created_at": "2025-01-01T00:00:00Z",
                "updated_at": "2025-01-01T00:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("Simple Task", result!!.title)
        assertNull(result.description)
        assertNull(result.due_date)
        assertNull(result.scheduled_date)
        assertNull(result.repeat_days)
        assertFalse(result.is_recurring)
    }

    @Test
    fun `deserialize task response with missing optional fields`() {
        val json = """
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "title": "Minimal Task",
                "is_active": true,
                "is_recurring": false,
                "created_at": "2025-01-01T00:00:00Z",
                "updated_at": "2025-01-01T00:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("Minimal Task", result!!.title)
        assertNull(result.description)
    }

    @Test
    fun `serialize task response`() {
        val task = TaskResponse(
            id = "test-id",
            title = "Test Task",
            description = "Description",
            is_active = true,
            due_date = "2025-01-20T12:00:00Z",
            scheduled_date = null,
            repeat_days = listOf(0, 6),
            is_recurring = true,
            created_at = "2025-01-01T00:00:00Z",
            updated_at = "2025-01-10T00:00:00Z"
        )

        val adapter = moshi.adapter(TaskResponse::class.java)
        val json = adapter.toJson(task)

        assertTrue(json.contains("\"id\":\"test-id\""))
        assertTrue(json.contains("\"title\":\"Test Task\""))
        assertTrue(json.contains("\"is_recurring\":true"))
        assertTrue(json.contains("\"repeat_days\":[0,6]"))
    }

    @Test
    fun `deserialize inactive task`() {
        val json = """
            {
                "id": "deleted-task",
                "title": "Deleted Task",
                "is_active": false,
                "is_recurring": false,
                "created_at": "2025-01-01T00:00:00Z",
                "updated_at": "2025-01-15T00:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertFalse(result!!.is_active)
    }
}
