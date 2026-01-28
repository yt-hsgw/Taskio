package com.yt_hsgw.taskio.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ApiErrorResponse シリアライゼーションテスト
 */
class ApiErrorSerializationTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun `deserialize not found error`() {
        val json = """
            {
                "error": "NotFound",
                "message": "Task"
            }
        """.trimIndent()

        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("NotFound", result!!.error)
        assertEquals("Task", result.message)
    }

    @Test
    fun `deserialize bad request error`() {
        val json = """
            {
                "error": "BadRequest",
                "message": "Title cannot be empty"
            }
        """.trimIndent()

        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("BadRequest", result!!.error)
        assertEquals("Title cannot be empty", result.message)
    }

    @Test
    fun `deserialize invalid uuid error`() {
        val json = """
            {
                "error": "InvalidUuid",
                "message": "Invalid UUID format"
            }
        """.trimIndent()

        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("InvalidUuid", result!!.error)
        assertEquals("Invalid UUID format", result.message)
    }

    @Test
    fun `deserialize internal error`() {
        val json = """
            {
                "error": "InternalError",
                "message": "Internal server error"
            }
        """.trimIndent()

        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("InternalError", result!!.error)
        assertEquals("Internal server error", result.message)
    }

    @Test
    fun `deserialize database error`() {
        val json = """
            {
                "error": "DatabaseError",
                "message": "Internal server error"
            }
        """.trimIndent()

        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        val result = adapter.fromJson(json)

        assertNotNull(result)
        assertEquals("DatabaseError", result!!.error)
    }

    @Test
    fun `serialize api error response`() {
        val error = ApiErrorResponse(
            error = "NotFound",
            message = "TaskLog"
        )

        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        val json = adapter.toJson(error)

        assertTrue(json.contains("\"error\":\"NotFound\""))
        assertTrue(json.contains("\"message\":\"TaskLog\""))
    }

    @Test
    fun `serialize and deserialize roundtrip`() {
        val original = ApiErrorResponse(
            error = "BadRequest",
            message = "Validation failed"
        )

        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        val json = adapter.toJson(original)
        val deserialized = adapter.fromJson(json)

        assertNotNull(deserialized)
        assertEquals(original.error, deserialized!!.error)
        assertEquals(original.message, deserialized.message)
    }
}
