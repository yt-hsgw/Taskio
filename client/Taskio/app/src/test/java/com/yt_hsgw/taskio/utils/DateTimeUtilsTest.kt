package com.yt_hsgw.taskio.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * 日付・時刻ユーティリティのテスト
 *
 * アプリ全体で使用される日付・時刻関連の処理をテストします。
 */
class DateTimeUtilsTest {

    // ─────────────────────────────
    // ISO日時パーステスト
    // ─────────────────────────────

    @Test
    fun `ISO-8601形式の日時文字列をパースできる`() {
        val dateTimeString = "2025-01-15T10:30:00Z"
        val dateTime = LocalDateTime.parse(
            dateTimeString.removeSuffix("Z"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )

        assertEquals(2025, dateTime.year)
        assertEquals(1, dateTime.monthValue)
        assertEquals(15, dateTime.dayOfMonth)
        assertEquals(10, dateTime.hour)
        assertEquals(30, dateTime.minute)
        assertEquals(0, dateTime.second)
    }

    @Test
    fun `日付部分のみを抽出してパースできる`() {
        val dateTimeString = "2025-01-15T10:30:00Z"
        val date = LocalDate.parse(dateTimeString.substringBefore("T"))

        assertEquals(LocalDate.of(2025, 1, 15), date)
    }

    @Test
    fun `時刻部分のみを抽出してパースできる`() {
        val dateTimeString = "2025-01-15T10:30:00Z"
        val timePart = dateTimeString.substringAfter("T").removeSuffix("Z")
        val time = LocalTime.parse(timePart)

        assertEquals(LocalTime.of(10, 30, 0), time)
    }

    @Test
    fun `不正な日付形式はパースエラーになる`() {
        val invalidDateString = "invalid-date"

        val exception = runCatching {
            LocalDate.parse(invalidDateString)
        }.exceptionOrNull()

        assertNotNull(exception)
        assertTrue(exception is DateTimeParseException)
    }

    @Test
    fun `空文字列はパースエラーになる`() {
        val emptyString = ""

        val exception = runCatching {
            LocalDate.parse(emptyString)
        }.exceptionOrNull()

        assertNotNull(exception)
    }

    // ─────────────────────────────
    // 日付フォーマットテスト
    // ─────────────────────────────

    @Test
    fun `日付をISO形式でフォーマットできる`() {
        val date = LocalDate.of(2025, 1, 15)
        val formatted = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        assertEquals("2025-01-15", formatted)
    }

    @Test
    fun `日時をISO形式でフォーマットできる`() {
        val dateTime = LocalDateTime.of(2025, 1, 15, 10, 30, 0)
        val formatted = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        assertEquals("2025-01-15T10:30:00", formatted)
    }

    @Test
    fun `日付を日本語形式でフォーマットできる`() {
        val date = LocalDate.of(2025, 1, 15)
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        val formatted = date.format(formatter)

        assertEquals("2025年01月15日", formatted)
    }

    @Test
    fun `時刻を日本語形式でフォーマットできる`() {
        val time = LocalTime.of(14, 30)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formatted = time.format(formatter)

        assertEquals("14:30", formatted)
    }

    // ─────────────────────────────
    // 継続時間計算テスト
    // ─────────────────────────────

    @Test
    fun `2つの時刻から継続時間(分)を計算できる`() {
        val start = LocalDateTime.of(2025, 1, 15, 10, 0, 0)
        val end = LocalDateTime.of(2025, 1, 15, 11, 30, 0)

        val duration = Duration.between(start, end)
        val minutes = duration.toMinutes()

        assertEquals(90, minutes)
    }

    @Test
    fun `日をまたぐ継続時間を計算できる`() {
        val start = LocalDateTime.of(2025, 1, 15, 23, 0, 0)
        val end = LocalDateTime.of(2025, 1, 16, 1, 0, 0)

        val duration = Duration.between(start, end)
        val minutes = duration.toMinutes()

        assertEquals(120, minutes) // 2時間
    }

    @Test
    fun `継続時間を時間と分に変換できる`() {
        val totalMinutes = 150L // 2時間30分

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        assertEquals(2, hours)
        assertEquals(30, minutes)
    }

    @Test
    fun `継続時間をフォーマット済み文字列に変換できる`() {
        val totalMinutes = 90L

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val formatted = "${hours}時間${minutes}分"

        assertEquals("1時間30分", formatted)
    }

    // ─────────────────────────────
    // 曜日変換テスト
    // ─────────────────────────────

    @Test
    fun `DayOfWeekをカレンダーインデックスに変換できる`() {
        // 日曜日=0, 月曜日=1, ..., 土曜日=6
        fun DayOfWeek.toCalendarIndex(): Int = when (this) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }

        assertEquals(0, DayOfWeek.SUNDAY.toCalendarIndex())
        assertEquals(1, DayOfWeek.MONDAY.toCalendarIndex())
        assertEquals(5, DayOfWeek.FRIDAY.toCalendarIndex())
        assertEquals(6, DayOfWeek.SATURDAY.toCalendarIndex())
    }

    @Test
    fun `カレンダーインデックスから日本語曜日名を取得できる`() {
        val dayNames = listOf("日", "月", "火", "水", "木", "金", "土")

        assertEquals("日", dayNames[0])
        assertEquals("月", dayNames[1])
        assertEquals("土", dayNames[6])
    }

    // ─────────────────────────────
    // 週の計算テスト
    // ─────────────────────────────

    @Test
    fun `今週の日曜日を取得できる`() {
        val today = LocalDate.of(2025, 1, 15) // 水曜日
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() % 7)

        // 2025年1月15日(水)の週の日曜日は1月12日
        assertEquals(LocalDate.of(2025, 1, 12), startOfWeek)
    }

    @Test
    fun `今週の7日間を取得できる`() {
        val today = LocalDate.of(2025, 1, 15)
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() % 7)
        val weekDates = (0 until 7).map { startOfWeek.plusDays(it.toLong()) }

        assertEquals(7, weekDates.size)
        assertEquals(LocalDate.of(2025, 1, 12), weekDates.first()) // 日曜日
        assertEquals(LocalDate.of(2025, 1, 18), weekDates.last())  // 土曜日
    }

    // ─────────────────────────────
    // 月の計算テスト
    // ─────────────────────────────

    @Test
    fun `月の開始日と終了日を取得できる`() {
        val yearMonth = YearMonth.of(2025, 1)

        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()

        assertEquals(LocalDate.of(2025, 1, 1), firstDay)
        assertEquals(LocalDate.of(2025, 1, 31), lastDay)
    }

    @Test
    fun `閏年の2月の終了日を正しく取得できる`() {
        val yearMonth = YearMonth.of(2024, 2) // 2024年は閏年

        val lastDay = yearMonth.atEndOfMonth()

        assertEquals(LocalDate.of(2024, 2, 29), lastDay)
    }

    @Test
    fun `平年の2月の終了日を正しく取得できる`() {
        val yearMonth = YearMonth.of(2025, 2) // 2025年は平年

        val lastDay = yearMonth.atEndOfMonth()

        assertEquals(LocalDate.of(2025, 2, 28), lastDay)
    }

    // ─────────────────────────────
    // 日付比較テスト
    // ─────────────────────────────

    @Test
    fun `日付の前後比較ができる`() {
        val date1 = LocalDate.of(2025, 1, 15)
        val date2 = LocalDate.of(2025, 1, 20)

        assertTrue(date1.isBefore(date2))
        assertTrue(date2.isAfter(date1))
        assertTrue(date1.isEqual(LocalDate.of(2025, 1, 15)))
    }

    @Test
    fun `日付が範囲内かどうかを判定できる`() {
        val targetDate = LocalDate.of(2025, 1, 15)
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)

        val isInRange = !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate)

        assertTrue(isInRange)
    }

    // ─────────────────────────────
    // nullセーフなパーステスト
    // ─────────────────────────────

    @Test
    fun `nullableな日付文字列を安全にパースできる`() {
        val nullDateString: String? = null

        val result = nullDateString?.let {
            runCatching {
                LocalDate.parse(it.substringBefore("T"))
            }.getOrNull()
        }

        assertNull(result)
    }

    @Test
    fun `有効な日付文字列を安全にパースできる`() {
        val dateString: String? = "2025-01-15T00:00:00Z"

        val result = dateString?.let {
            runCatching {
                LocalDate.parse(it.substringBefore("T"))
            }.getOrNull()
        }

        assertEquals(LocalDate.of(2025, 1, 15), result)
    }
}
