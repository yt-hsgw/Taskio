package com.yt_hsgw.taskio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioTheme

/**
 * 週間進捗インジケーター
 *
 * 日曜日から土曜日までの7日間の実行状態を円形インジケーターで表示します。
 *
 * @param weeklyProgress 曜日インデックス（0=日曜日）をキーとした実行済みフラグマップ
 * @param repeatDays 繰り返し曜日のリスト（0=日曜日）、nullの場合は全曜日が対象
 * @param modifier Modifier
 */
@Composable
fun WeeklyProgressIndicator(
    weeklyProgress: Map<Int, Boolean>,
    repeatDays: List<Int>?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = TaskioStrings.CD_WEEKLY_PROGRESS
        },
        horizontalArrangement = Arrangement.spacedBy(DayIndicatorSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 日曜日(0)から土曜日(6)まで
        for (dayIndex in 0 until DAYS_IN_WEEK) {
            val isScheduled = repeatDays?.contains(dayIndex) ?: false
            val isCompleted = weeklyProgress[dayIndex] ?: false
            val dayLabel = TaskioStrings.getDayOfWeekByIndex(dayIndex)

            DayIndicator(
                dayLabel = dayLabel,
                isScheduled = isScheduled,
                isCompleted = isCompleted
            )
        }
    }
}

/**
 * 個別の曜日インジケーター
 *
 * @param dayLabel 曜日ラベル（例: "日", "月"）
 * @param isScheduled その曜日が予定されているかどうか
 * @param isCompleted その曜日が実行済みかどうか
 */
@Composable
private fun DayIndicator(
    dayLabel: String,
    isScheduled: Boolean,
    isCompleted: Boolean
) {
    val backgroundColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isScheduled -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }

    val borderColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isScheduled -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val textColor = when {
        isCompleted -> MaterialTheme.colorScheme.onPrimary
        isScheduled -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = Modifier
            .size(DayIndicatorSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = DayIndicatorBorderWidth,
                color = borderColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayLabel,
            fontSize = DayIndicatorFontSize,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

// ─────────────────────────────
// 定数
// ─────────────────────────────

private const val DAYS_IN_WEEK = 7
private val DayIndicatorSize = 32.dp
private val DayIndicatorSpacing = 4.dp
private val DayIndicatorBorderWidth = 1.dp
private val DayIndicatorFontSize = 10.sp

// ─────────────────────────────
// プレビュー
// ─────────────────────────────

@Preview(showBackground = true)
@Composable
private fun WeeklyProgressIndicatorPreview() {
    TaskioTheme {
        WeeklyProgressIndicator(
            weeklyProgress = mapOf(
                0 to false, // 日
                1 to true,  // 月 - 実行済み
                2 to false, // 火
                3 to true,  // 水 - 実行済み
                4 to false, // 木
                5 to true,  // 金 - 実行済み
                6 to false  // 土
            ),
            repeatDays = listOf(1, 3, 5) // 月・水・金
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyProgressIndicatorAllDaysPreview() {
    TaskioTheme {
        WeeklyProgressIndicator(
            weeklyProgress = mapOf(
                0 to true,
                1 to true,
                2 to false,
                3 to true,
                4 to false,
                5 to true,
                6 to true
            ),
            repeatDays = listOf(0, 1, 2, 3, 4, 5, 6) // 毎日
        )
    }
}
