package com.yt_hsgw.taskio.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateTimeUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

    /**
     * ISO-8601形式の文字列をInstantに変換
     * @param isoString "2025-01-15T10:30:00Z" 形式
     */
    fun parseIsoDateTime(isoString: String): Instant? {
        return try {
            Instant.parse(isoString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 現在時刻をISO-8601形式で取得
     */
    fun getCurrentIsoDateTime(): String {
        return Instant.now().toString()
    }

    /**
     * Instantを "yyyy/MM/dd" 形式にフォーマット
     */
    fun formatDate(instant: Instant): String {
        return instant
            .atZone(ZoneId.systemDefault())
            .format(dateFormatter)
    }

    /**
     * Instantを "HH:mm" 形式にフォーマット
     */
    fun formatTime(instant: Instant): String {
        return instant
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter)
    }

    /**
     * Instantを "yyyy/MM/dd HH:mm" 形式にフォーマット
     */
    fun formatDateTime(instant: Instant): String {
        return instant
            .atZone(ZoneId.systemDefault())
            .format(dateTimeFormatter)
    }

    /**
     * ISO-8601文字列を "yyyy/MM/dd HH:mm" 形式にフォーマット
     */
    fun formatIsoDateTime(isoString: String): String {
        return parseIsoDateTime(isoString)?.let {
            formatDateTime(it)
        } ?: "Invalid date"
    }

    /**
     * 継続時間（分）を "XhYm" 形式にフォーマット
     * @param minutes 継続時間（分）
     * @return "2h30m" のような文字列
     */
    fun formatDuration(minutes: Long?): String {
        if (minutes == null || minutes == 0L) return "0m"

        val hours = minutes / 60
        val mins = minutes % 60

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
     */
    fun calculateDurationMinutes(start: Instant, end: Instant): Long {
        return ChronoUnit.MINUTES.between(start, end)
    }

    /**
     * 相対時刻を表示 (例: "2時間前", "昨日")
     */
    fun formatRelativeTime(instant: Instant): String {
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)

        return when {
            minutes < 1 -> "たった今"
            minutes < 60 -> "${minutes}分前"
            minutes < 1440 -> "${minutes / 60}時間前"
            minutes < 2880 -> "昨日"
            else -> "${minutes / 1440}日前"
        }
    }
}