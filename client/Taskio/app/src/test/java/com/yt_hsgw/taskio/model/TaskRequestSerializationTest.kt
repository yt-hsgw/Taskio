package com.yt_hsgw.taskio.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * TaskRequest シリアライゼーションテスト
 */
class TaskRequestSerializationTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun `serialize complete task request`() {
        val request = TaskRequest(
            title = "New Task",
            description = "Task description",
            due_date = "2025-01-20T12:00:00Z",
            scheduled_date = "2025-01-15T09:00:00Z",
            repeat_days = listOf(1, 3, 5)
        )

        val adapter = moshi.adapter(TaskRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"title\":\"New Task\""))
        assertTrue(json.contains("\"description\":\"Task description\""))
        assertTrue(json.contains("\"due_date\":\"2025-01-20T12:00:00Z\""))
        assertTrue(json.contains("\"scheduled_date\":\"2025-01-15T09:00:00Z\""))
        assertTrue(json.contains("\"repeat_days\":[1,3,5]"))
    }

    @Test
    fun `serialize minimal task request`() {
        val request = TaskRequest(
            title = "Simple Task",
            description = null,
            due_date = null,
            scheduled_date = null,
            repeat_days = null
        )

        val adapter = moshi.adapter(TaskRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"title\":\"Simple Task\""))
    }

    @Test
    fun `serialize recurring task request`() {
        val request = TaskRequest(
            title = "Daily Standup",
            description = null,
            due_date = null,
            scheduled_date = "2025-01-15T09:00:00Z",
            repeat_days = listOf(1, 2, 3, 4, 5) // 平日
        )

        val adapter = moshi.adapter(TaskRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"repeat_days\":[1,2,3,4,5]"))
    }

    @Test
    fun `deserialize task request`() {
        val json = """
            {
                "title": "Parsed Task",
                "description": "From JSON",
                "due_date": "2025-02-01T00:00:00Z",
                "scheduled_date": null,
                "repeat_days": [0, 6]
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskRequest::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("Parsed Task", result!!.title)
        assertEquals("From JSON", result.description)
        assertEquals("2025-02-01T00:00:00Z", result.due_date)
        assertNull(result.scheduled_date)
        assertEquals(listOf(0, 6), result.repeat_days)
    }

    @Test
    fun `serialize task with empty repeat_days`() {
        val request = TaskRequest(
            title = "Non-recurring Task",
            description = null,
            due_date = null,
            scheduled_date = null,
            repeat_days = emptyList()
        )

        val adapter = moshi.adapter(TaskRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"repeat_days\":[]"))
    }

    @Test
    fun `serialize task with weekend repeat_days`() {
        val request = TaskRequest(
            title = "Weekend Task",
            description = "Only on weekends",
            due_date = null,
            scheduled_date = "2025-01-18T10:00:00Z",
            repeat_days = listOf(0, 6) // 日曜日と土曜日
        )

        val adapter = moshi.adapter(TaskRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"repeat_days\":[0,6]"))
    }
}
