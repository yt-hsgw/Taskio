package com.yt_hsgw.taskio.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.model.CalendarLogResponse
import com.yt_hsgw.taskio.model.TaskStatus
import com.yt_hsgw.taskio.ui.theme.TaskioColors
import com.yt_hsgw.taskio.ui.theme.TaskioTheme

// ステータスに応じた色を取得
private val TaskStatus.cardColor: Color
    @Composable
    get() = when (this) {
        TaskStatus.NOT_STARTED -> TaskioColors.StatusDefault
        TaskStatus.IN_PROGRESS -> TaskioColors.StatusInProgress  // 薄い青
        TaskStatus.COMPLETED -> TaskioColors.StatusCompleted     // 薄い緑
    }

private val TaskStatus.borderColor: Color
    @Composable
    get() = when (this) {
        TaskStatus.NOT_STARTED -> MaterialTheme.colorScheme.outlineVariant
        TaskStatus.IN_PROGRESS -> TaskioColors.Secondary  // 青
        TaskStatus.COMPLETED -> TaskioColors.Primary      // 緑
    }

/**
 * 日別タスクログカード
 *
 * 選択された日のタスクログを表示するカードです。
 *
 * @param log ログデータ
 * @param modifier Modifier
 */
@Composable
fun DailyTaskCard(
    log: CalendarLogResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = log.status.cardColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = log.status.borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // タスクタイトル
            Text(
                text = log.task_title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // タスク詳細（あれば）
            log.task_description?.let { description ->
                if (description.isNotBlank()) {
                    DescriptionRow(description = description)
                }
            }

            // メモ（あれば）
            log.memo?.let { memo ->
                if (memo.isNotBlank()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    MemoRow(memo = memo)
                }
            }
        }
    }
}

/**
 * 詳細行
 *
 * @param description 詳細テキスト
 */
@Composable
private fun DescriptionRow(description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 2.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * メモ行
 *
 * @param memo メモテキスト
 */
@Composable
private fun MemoRow(memo: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Notes,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 2.dp)
        )

        Text(
            text = memo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "未開始")
@Composable
private fun DailyTaskCardNotStartedPreview() {
    TaskioTheme {
        DailyTaskCard(
            log = CalendarLogResponse(
                id = "1",
                task_id = "task-1",
                task_title = "ジム",
                task_description = "24hジムでのトレーニング",
                memo = null,
                created_at = "2026-01-25T18:00:00",
                status = TaskStatus.NOT_STARTED
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "進行中")
@Composable
private fun DailyTaskCardInProgressPreview() {
    TaskioTheme {
        DailyTaskCard(
            log = CalendarLogResponse(
                id = "2",
                task_id = "task-2",
                task_title = "読書",
                task_description = "技術書を読む",
                memo = "第3章まで完了",
                created_at = "2026-01-25T07:00:00",
                status = TaskStatus.IN_PROGRESS
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "完了")
@Composable
private fun DailyTaskCardCompletedPreview() {
    TaskioTheme {
        DailyTaskCard(
            log = CalendarLogResponse(
                id = "3",
                task_id = "task-3",
                task_title = "買い物",
                task_description = "スーパーで食材を買う",
                memo = "野菜、肉、調味料を購入",
                created_at = "2026-01-25T10:00:00",
                status = TaskStatus.COMPLETED
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
