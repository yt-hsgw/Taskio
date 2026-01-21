package com.yt_hsgw.taskio.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 日時関連のユーティリティオブジェクト
 *
 * ISO-8601形式の日時パース、フォーマット、継続時間計算などを提供します。
 */
object DateTimeUtils {

    // ─────────────────────────────
    // Formatters
    // ─────────────────────────────

    /** 日付フォーマッター (yyyy/MM/dd) */
    private val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)

    /** 時刻フォーマッター (HH:mm) */
    private val timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT)

    /** 日時フォーマッター (yyyy/MM/dd HH:mm) */
    private val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)

    // ─────────────────────────────
    // Parsing
    // ─────────────────────────────

    /**
     * ISO-8601形式の文字列をInstantに変換
     *
     * @param isoString "2025-01-15T10:30:00Z" 形式の文字列
     * @return パース成功時はInstant、失敗時はnull
     */
    fun parseIsoDateTime(isoString: String): Instant? {
        return runCatching { Instant.parse(isoString) }.getOrNull()
    }

    // ─────────────────────────────
    // Current Time
    // ─────────────────────────────

    /**
     * 現在時刻をISO-8601形式で取得
     *
     * @return "2025-01-15T10:30:00Z" 形式の文字列
     */
    fun getCurrentIsoDateTime(): String = Instant.now().toString()

    // ─────────────────────────────
    // Formatting
    // ─────────────────────────────

    /**
     * Instantを日付形式にフォーマット
     *
     * @param instant フォーマット対象のInstant
     * @return "yyyy/MM/dd" 形式の文字列
     */
    fun formatDate(instant: Instant): String {
        return instant.atZone(ZoneId.systemDefault()).format(dateFormatter)
    }

    /**
     * Instantを時刻形式にフォーマット
     *
     * @param instant フォーマット対象のInstant
     * @return "HH:mm" 形式の文字列
     */
    fun formatTime(instant: Instant): String {
        return instant.atZone(ZoneId.systemDefault()).format(timeFormatter)
    }

    /**
     * Instantを日時形式にフォーマット
     *
     * @param instant フォーマット対象のInstant
     * @return "yyyy/MM/dd HH:mm" 形式の文字列
     */
    fun formatDateTime(instant: Instant): String {
        return instant.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
    }

    /**
     * ISO-8601文字列を日時形式にフォーマット
     *
     * @param isoString ISO-8601形式の文字列
     * @return "yyyy/MM/dd HH:mm" 形式の文字列、パース失敗時は "Invalid date"
     */
    fun formatIsoDateTime(isoString: String): String {
        return parseIsoDateTime(isoString)?.let { formatDateTime(it) } ?: INVALID_DATE_MESSAGE
    }

    // ─────────────────────────────
    // Duration
    // ─────────────────────────────

    /**
     * 継続時間（分）を読みやすい形式にフォーマット
     *
     * @param minutes 継続時間（分）
     * @return "2h30m" のような文字列、nullまたは0の場合は "0m"
     */
    fun formatDuration(minutes: Long?): String {
        if (minutes == null || minutes == 0L) return ZERO_DURATION

        val hours = minutes / MINUTES_PER_HOUR
        val mins = minutes % MINUTES_PER_HOUR

        return buildString {
            if (hours > 0) append("${hours}h")
            if (mins > 0) {
                if (hours > 0) append(" ")
                append("${mins}m")
            }
        }
    }

    /**
     * 2つのInstant間の継続時間（分）を計算
     *
     * @param start 開始時刻
     * @param end 終了時刻
     * @return 継続時間（分）
     */
    fun calculateDurationMinutes(start: Instant, end: Instant): Long {
        return ChronoUnit.MINUTES.between(start, end)
    }

    // ─────────────────────────────
    // Relative Time
    // ─────────────────────────────

    /**
     * 相対時刻を表示
     *
     * @param instant 対象時刻
     * @return "2時間前", "昨日" などの相対表現
     */
    fun formatRelativeTime(instant: Instant): String {
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)

        return when {
            minutes < 1 -> JUST_NOW
            minutes < MINUTES_PER_HOUR -> "${minutes}分前"
            minutes < MINUTES_PER_DAY -> "${minutes / MINUTES_PER_HOUR}時間前"
            minutes < MINUTES_PER_DAY * 2 -> YESTERDAY
            else -> "${minutes / MINUTES_PER_DAY}日前"
        }
    }

    // ─────────────────────────────
    // Constants
    // ─────────────────────────────

    private const val DATE_FORMAT = "yyyy/MM/dd"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm"
    private const val INVALID_DATE_MESSAGE = "Invalid date"
    private const val ZERO_DURATION = "0m"
    private const val JUST_NOW = "たった今"
    private const val YESTERDAY = "昨日"
    private const val MINUTES_PER_HOUR = 60
    private const val MINUTES_PER_DAY = 1440
}
