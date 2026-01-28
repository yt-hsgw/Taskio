package com.yt_hsgw.taskio.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * CalendarLogResponse, TaskStatus シリアライゼーションテスト
 */
class CalendarLogSerializationTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    // TaskStatus tests

    @Test
    fun `deserialize task status not_started`() {
        val json = """
            {
                "id": "log-id",
                "task_id": "task-id",
                "task_title": "Test Task",
                "created_at": "2025-01-15T00:00:00Z",
                "status": "not_started"
            }
        """.trimIndent()

        val adapter = moshi.adapter(CalendarLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(TaskStatus.NOT_STARTED, result!!.status)
    }

    @Test
    fun `deserialize task status in_progress`() {
        val json = """
            {
                "id": "log-id",
                "task_id": "task-id",
                "task_title": "Test Task",
                "created_at": "2025-01-15T00:00:00Z",
                "status": "in_progress"
            }
        """.trimIndent()

        val adapter = moshi.adapter(CalendarLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(TaskStatus.IN_PROGRESS, result!!.status)
    }

    @Test
    fun `deserialize task status completed`() {
        val json = """
            {
                "id": "log-id",
                "task_id": "task-id",
                "task_title": "Test Task",
                "created_at": "2025-01-15T00:00:00Z",
                "status": "completed"
            }
        """.trimIndent()

        val adapter = moshi.adapter(CalendarLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(TaskStatus.COMPLETED, result!!.status)
    }

    // CalendarLogResponse tests

    @Test
    fun `deserialize complete calendar log response`() {
        val json = """
            {
                "id": "log-id-123",
                "task_id": "task-id-456",
                "task_title": "Morning Meeting",
                "task_description": "Daily standup meeting",
                "memo": "Discussed sprint progress",
                "created_at": "2025-01-15T09:00:00Z",
                "scheduled_date": "2025-01-15T09:00:00Z",
                "status": "completed"
            }
        """.trimIndent()

        val adapter = moshi.adapter(CalendarLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("log-id-123", result!!.id)
        assertEquals("task-id-456", result.task_id)
        assertEquals("Morning Meeting", result.task_title)
        assertEquals("Daily standup meeting", result.task_description)
        assertEquals("Discussed sprint progress", result.memo)
        assertEquals("2025-01-15T09:00:00Z", result.created_at)
        assertEquals("2025-01-15T09:00:00Z", result.scheduled_date)
        assertEquals(TaskStatus.COMPLETED, result.status)
    }

    @Test
    fun `deserialize calendar log with null optional fields`() {
        val json = """
            {
                "id": "log-id",
                "task_id": "task-id",
                "task_title": "Simple Task",
                "task_description": null,
                "memo": null,
                "created_at": "2025-01-15T00:00:00Z",
                "scheduled_date": null,
                "status": "not_started"
            }
        """.trimIndent()

        val adapter = moshi.adapter(CalendarLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertNull(result!!.task_description)
        assertNull(result.memo)
        assertNull(result.scheduled_date)
    }

    @Test
    fun `deserialize calendar log with missing optional fields`() {
        val json = """
            {
                "id": "log-id",
                "task_id": "task-id",
                "task_title": "Minimal Task",
                "created_at": "2025-01-15T00:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(CalendarLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("Minimal Task", result!!.task_title)
        assertNull(result.task_description)
        assertNull(result.memo)
        assertNull(result.scheduled_date)
        assertEquals(TaskStatus.NOT_STARTED, result.status) // default value
    }

    @Test
    fun `serialize calendar log response`() {
        val log = CalendarLogResponse(
            id = "log-123",
            task_id = "task-456",
            task_title = "Test Task",
            task_description = "Description",
            memo = "Memo",
            created_at = "2025-01-15T00:00:00Z",
            scheduled_date = "2025-01-15T09:00:00Z",
            status = TaskStatus.IN_PROGRESS
        )

        val adapter = moshi.adapter(CalendarLogResponse::class.java)
        val json = adapter.toJson(log)

        assertTrue(json.contains("\"id\":\"log-123\""))
        assertTrue(json.contains("\"task_title\":\"Test Task\""))
        assertTrue(json.contains("\"status\":\"in_progress\""))
    }

    // CalendarLogsResponse tests

    @Test
    fun `deserialize calendar logs response with multiple logs`() {
        val json = """
            {
                "logs": [
                    {
                        "id": "log-1",
                        "task_id": "task-1",
                        "task_title": "Task 1",
                        "created_at": "2025-01-15T00:00:00Z",
                        "status": "completed"
                    },
                    {
                        "id": "log-2",
                        "task_id": "task-2",
                        "task_title": "Task 2",
                        "created_at": "2025-01-16T00:00:00Z",
                        "status": "in_progress"
                    }
                ]
            }
        """.trimIndent()

        val adapter = moshi.adapter(CalendarLogsResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals(2, result!!.logs.size)
        assertEquals("Task 1", result.logs[0].task_title)
        assertEquals(TaskStatus.COMPLETED, result.logs[0].status)
        assertEquals("Task 2", result.logs[1].task_title)
        assertEquals(TaskStatus.IN_PROGRESS, result.logs[1].status)
    }

    @Test
    fun `deserialize calendar logs response with empty list`() {
        val json = """
            {
                "logs": []
            }
        """.trimIndent()

        val adapter = moshi.adapter(CalendarLogsResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertTrue(result!!.logs.isEmpty())
    }

    @Test
    fun `serialize calendar logs response`() {
        val response = CalendarLogsResponse(
            logs = listOf(
                CalendarLogResponse(
                    id = "log-1",
                    task_id = "task-1",
                    task_title = "Task 1",
                    created_at = "2025-01-15T00:00:00Z",
                    status = TaskStatus.NOT_STARTED
                )
            )
        )

        val adapter = moshi.adapter(CalendarLogsResponse::class.java)
        val json = adapter.toJson(response)

        assertTrue(json.contains("\"logs\""))
        assertTrue(json.contains("\"task_title\":\"Task 1\""))
    }
}
