package com.yt_hsgw.taskio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioTheme

/**
 * ログ画面用タスクカード
 *
 * タスク情報と週間進捗を表示するカードコンポーネントです。
 * タップで詳細セクションを展開/折りたたみできます。
 *
 * @param task タスク情報
 * @param weeklyProgress 週間進捗マップ（曜日インデックス -> 実行済みフラグ）
 * @param onEditClick 編集ボタンクリック時のコールバック
 * @param modifier Modifier
 */
@Composable
fun LogTaskCard(
    task: TaskItem,
    weeklyProgress: Map<Int, Boolean>,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = CardElevation
        )
    ) {
        Column(
            modifier = Modifier.padding(CardPadding)
        ) {
            // ヘッダー行：タイトル + 編集ボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = TaskioStrings.CD_EDIT_TASK,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(ProgressIndicatorTopMargin))

            // 週間進捗インジケーター
            WeeklyProgressIndicator(
                weeklyProgress = weeklyProgress,
                repeatDays = task.repeatDays
            )

            Spacer(modifier = Modifier.height(DetailSectionTopMargin))

            // 詳細セクション（展開可能）
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TaskioStrings.TASK_DETAIL,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 展開時の詳細内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = ExpandedContentTopPadding)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Spacer(modifier = Modifier.height(ExpandedContentTopPadding))

                    // 説明
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(DetailItemSpacing))
                    }

                    // 繰り返し曜日
                    Text(
                        text = "${TaskioStrings.TASK_RECURRING}: ${TaskioStrings.formatRepeatDays(task.repeatDays)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────
// 定数
// ─────────────────────────────

private val CardPadding = 16.dp
private val CardElevation = 2.dp
private val ProgressIndicatorTopMargin = 12.dp
private val DetailSectionTopMargin = 8.dp
private val ExpandedContentTopPadding = 8.dp
private val DetailItemSpacing = 8.dp

// ─────────────────────────────
// プレビュー
// ─────────────────────────────

@Preview(showBackground = true)
@Composable
private fun LogTaskCardPreview() {
    TaskioTheme {
        LogTaskCard(
            task = TaskItem(
                id = "1",
                title = "ジム",
                description = "24hジムでのトレーニング",
                createdAt = "2025-01-24",
                repeatDays = listOf(1, 3, 5),
                isRecurring = true
            ),
            weeklyProgress = mapOf(
                0 to false,
                1 to true,
                2 to false,
                3 to true,
                4 to false,
                5 to false,
                6 to false
            ),
            onEditClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogTaskCardExpandedPreview() {
    TaskioTheme {
        LogTaskCard(
            task = TaskItem(
                id = "2",
                title = "資格勉強",
                description = "AWS認定資格の勉強。毎日1時間は確保する。",
                createdAt = "2025-01-24",
                repeatDays = listOf(0, 1, 2, 3, 4, 5, 6),
                isRecurring = true
            ),
            weeklyProgress = mapOf(
                0 to true,
                1 to true,
                2 to true,
                3 to false,
                4 to true,
                5 to true,
                6 to false
            ),
            onEditClick = {}
        )
    }
}
