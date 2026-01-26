package com.yt_hsgw.taskio.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.ui.theme.TaskioTheme
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * カレンダーの日付セル
 *
 * 各日付を表示し、選択状態・今日・ログの有無を視覚的に表現します。
 *
 * @param date 表示する日付
 * @param isSelected 選択中かどうか
 * @param isToday 今日かどうか
 * @param hasLogs ログが存在するかどうか
 * @param isCurrentMonth 現在表示中の月の日付かどうか
 * @param onClick クリック時のコールバック
 * @param modifier Modifier
 */
@Composable
fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasLogs: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfWeek = date.dayOfWeek

    // 日付の色を決定
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
        dayOfWeek == DayOfWeek.SATURDAY -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }

    // 背景色を決定
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    // 枠線（今日の場合）
    val borderModifier = if (isToday && !isSelected) {
        Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .padding(horizontal = 1.dp, vertical = 1.dp)  // セル間の間隔を縮小
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(borderModifier)
            .background(backgroundColor, CircleShape)
            .clickable(enabled = isCurrentMonth) { onClick() }
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 日付テキスト
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
        )

        // ログインジケーター（ドット）
        if (hasLogs && isCurrentMonth) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(6.dp)
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun CalendarDayCellNormalPreview() {
    TaskioTheme {
        CalendarDayCell(
            date = LocalDate.of(2026, 1, 15),
            isSelected = false,
            isToday = false,
            hasLogs = false,
            isCurrentMonth = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarDayCellSelectedPreview() {
    TaskioTheme {
        CalendarDayCell(
            date = LocalDate.of(2026, 1, 15),
            isSelected = true,
            isToday = false,
            hasLogs = true,
            isCurrentMonth = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarDayCellTodayPreview() {
    TaskioTheme {
        CalendarDayCell(
            date = LocalDate.now(),
            isSelected = false,
            isToday = true,
            hasLogs = true,
            isCurrentMonth = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarDayCellOtherMonthPreview() {
    TaskioTheme {
        CalendarDayCell(
            date = LocalDate.of(2026, 1, 31),
            isSelected = false,
            isToday = false,
            hasLogs = false,
            isCurrentMonth = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarDayCellSundayPreview() {
    TaskioTheme {
        CalendarDayCell(
            date = LocalDate.of(2026, 1, 26), // Sunday
            isSelected = false,
            isToday = false,
            hasLogs = true,
            isCurrentMonth = true,
            onClick = {}
        )
    }
}