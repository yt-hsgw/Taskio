package com.yt_hsgw.taskio.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.model.CalendarLogResponse
import com.yt_hsgw.taskio.ui.theme.TaskioTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 日別タスク一覧
 *
 * 選択された日のタスクログを一覧表示します。
 *
 * @param selectedDate 選択中の日付
 * @param logs 表示するログ一覧
 * @param modifier Modifier
 */
@Composable
fun DailyTaskList(
    selectedDate: LocalDate,
    logs: List<CalendarLogResponse>,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("M月d日（E）", Locale.JAPANESE)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 日付ヘッダー
        Text(
            text = selectedDate.format(dateFormatter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (logs.isEmpty()) {
            // ログがない場合
            EmptyState()
        } else {
            // ログ一覧
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = logs,
                    key = { it.id }
                ) { log ->
                    DailyTaskCard(log = log)
                }
            }
        }
    }
}

/**
 * ログがない場合の空状態表示
 */
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📝",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = "この日のログはありません",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun DailyTaskListPreview() {
    TaskioTheme {
        DailyTaskList(
            selectedDate = LocalDate.of(2026, 1, 25),
            logs = listOf(
                CalendarLogResponse(
                    id = "1",
                    task_id = "task-1",
                    task_title = "ジム",
                    task_description = "24hジムでのトレーニング",
                    memo = "脚トレ中心",
                    created_at = "2026-01-25T18:00:00"
                ),
                CalendarLogResponse(
                    id = "2",
                    task_id = "task-2",
                    task_title = "読書",
                    task_description = null,
                    memo = "サピエンス全史",
                    created_at = "2026-01-25T07:00:00"
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyTaskListEmptyPreview() {
    TaskioTheme {
        DailyTaskList(
            selectedDate = LocalDate.of(2026, 1, 25),
            logs = emptyList()
        )
    }
}
