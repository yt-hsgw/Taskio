package com.yt_hsgw.taskio.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.ui.theme.TaskioTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * カレンダーヘッダー
 *
 * 現在表示中の月と前後月へのナビゲーションボタンを表示します。
 *
 * @param yearMonth 表示する年月
 * @param onPreviousMonth 前月ボタンクリック時のコールバック
 * @param onNextMonth 次月ボタンクリック時のコールバック
 * @param onTodayClick 「今日」ボタンクリック時のコールバック
 * @param modifier Modifier
 */
@Composable
fun CalendarHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy年 M月", Locale.JAPANESE)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 前月ボタン
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "前月",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // 年月表示
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = yearMonth.format(formatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 今日ボタン（現在月でない場合のみ表示）
            if (yearMonth != YearMonth.now()) {
                TextButton(onClick = onTodayClick) {
                    Text(
                        text = "今日",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 次月ボタン
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "次月",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun CalendarHeaderPreview() {
    TaskioTheme {
        CalendarHeader(
            yearMonth = YearMonth.of(2026, 1),
            onPreviousMonth = {},
            onNextMonth = {},
            onTodayClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarHeaderCurrentMonthPreview() {
    TaskioTheme {
        CalendarHeader(
            yearMonth = YearMonth.now(),
            onPreviousMonth = {},
            onNextMonth = {},
            onTodayClick = {}
        )
    }
}