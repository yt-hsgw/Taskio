package com.yt_hsgw.taskio.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * TaskLogResponse, TaskLogRequest シリアライゼーションテスト
 */
class TaskLogSerializationTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    // TaskLogResponse tests

    @Test
    fun `deserialize complete task log response`() {
        val json = """
            {
                "id": "log-id-123",
                "task_id": "task-id-456",
                "start_at": "2025-01-15T09:00:00Z",
                "end_at": "2025-01-15T10:30:00Z",
                "duration_minutes": 90,
                "memo": "Completed the task",
                "created_at": "2025-01-15T09:00:00Z",
                "updated_at": "2025-01-15T10:30:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("log-id-123", result!!.id)
        assertEquals("task-id-456", result.task_id)
        assertEquals("2025-01-15T09:00:00Z", result.start_at)
        assertEquals("2025-01-15T10:30:00Z", result.end_at)
        assertEquals(90, result.duration_minutes)
        assertEquals("Completed the task", result.memo)
    }

    @Test
    fun `deserialize task log response with null optional fields`() {
        val json = """
            {
                "id": "log-id",
                "task_id": "task-id",
                "start_at": null,
                "end_at": null,
                "duration_minutes": null,
                "memo": null,
                "created_at": "2025-01-15T09:00:00Z",
                "updated_at": "2025-01-15T09:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertNull(result!!.start_at)
        assertNull(result.end_at)
        assertNull(result.duration_minutes)
        assertNull(result.memo)
    }

    @Test
    fun `deserialize task log response with missing optional fields`() {
        val json = """
            {
                "id": "log-id",
                "task_id": "task-id",
                "created_at": "2025-01-15T09:00:00Z",
                "updated_at": "2025-01-15T09:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskLogResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertNull(result!!.start_at)
        assertNull(result.end_at)
        assertNull(result.duration_minutes)
        assertNull(result.memo)
    }

    @Test
    fun `serialize task log response`() {
        val log = TaskLogResponse(
            id = "log-123",
            task_id = "task-456",
            start_at = "2025-01-15T09:00:00Z",
            end_at = "2025-01-15T10:00:00Z",
            duration_minutes = 60,
            memo = "Test memo",
            created_at = "2025-01-15T09:00:00Z",
            updated_at = "2025-01-15T10:00:00Z"
        )

        val adapter = moshi.adapter(TaskLogResponse::class.java)
        val json = adapter.toJson(log)

        assertTrue(json.contains("\"id\":\"log-123\""))
        assertTrue(json.contains("\"task_id\":\"task-456\""))
        assertTrue(json.contains("\"duration_minutes\":60"))
    }

    // TaskLogRequest tests

    @Test
    fun `serialize complete task log request`() {
        val request = TaskLogRequest(
            start_at = "2025-01-15T09:00:00Z",
            end_at = "2025-01-15T10:30:00Z",
            memo = "Work session",
            target_date = "2025-01-15T00:00:00Z",
            is_completed = true
        )

        val adapter = moshi.adapter(TaskLogRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"start_at\":\"2025-01-15T09:00:00Z\""))
        assertTrue(json.contains("\"end_at\":\"2025-01-15T10:30:00Z\""))
        assertTrue(json.contains("\"memo\":\"Work session\""))
        assertTrue(json.contains("\"target_date\":\"2025-01-15T00:00:00Z\""))
        assertTrue(json.contains("\"is_completed\":true"))
    }

    @Test
    fun `serialize minimal task log request`() {
        val request = TaskLogRequest(
            start_at = null,
            end_at = null,
            memo = null,
            target_date = null,
            is_completed = null
        )

        val adapter = moshi.adapter(TaskLogRequest::class.java)
        val json = adapter.toJson(request)

        assertNotNull(json)
    }

    @Test
    fun `serialize task log request with start time only`() {
        val request = TaskLogRequest(
            start_at = "2025-01-15T09:00:00Z",
            end_at = null,
            memo = null,
            target_date = "2025-01-15T00:00:00Z",
            is_completed = false
        )

        val adapter = moshi.adapter(TaskLogRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"start_at\":\"2025-01-15T09:00:00Z\""))
        assertTrue(json.contains("\"is_completed\":false"))
    }

    @Test
    fun `deserialize task log request`() {
        val json = """
            {
                "start_at": "2025-01-15T09:00:00Z",
                "end_at": "2025-01-15T11:00:00Z",
                "memo": "Morning session",
                "target_date": "2025-01-15T00:00:00Z",
                "is_completed": true
            }
        """.trimIndent()

        val adapter = moshi.adapter(TaskLogRequest::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("2025-01-15T09:00:00Z", result!!.start_at)
        assertEquals("2025-01-15T11:00:00Z", result.end_at)
        assertEquals("Morning session", result.memo)
        assertEquals("2025-01-15T00:00:00Z", result.target_date)
        assertTrue(result.is_completed!!)
    }
}
