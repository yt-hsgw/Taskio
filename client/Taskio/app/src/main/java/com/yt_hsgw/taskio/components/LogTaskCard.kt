package com.yt_hsgw.taskio.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioTheme

/**
 * ログ画面用タスクカード
 *
 * タスク情報と週間進捗を表示するカードコンポーネントです。
 * 説明文は常に表示され、3行以上の場合のみ展開/折りたたみが可能です。
 *
 * @param task タスク情報
 * @param weeklyProgress 週間進捗マップ（曜日インデックス -> 実行済みフラグ）
 * @param onEditClick 編集ボタンクリック時のコールバック
 * @param onDeleteClick 削除ボタンクリック時のコールバック
 * @param modifier Modifier
 */
@Composable
fun LogTaskCard(
    task: TaskItem,
    weeklyProgress: Map<Int, Boolean>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 説明文の行数をチェック（3行以上なら展開可能）
    val descriptionLines = task.description?.count { it == '\n' }?.plus(1) ?: 0
    val isLongDescription = !task.description.isNullOrBlank() && 
        (descriptionLines >= ExpandableLineThreshold || (task.description?.length ?: 0) > LongDescriptionCharThreshold)
    
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
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
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = TaskioStrings.CD_EDIT_TASK,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = TaskioStrings.CD_DELETE_TASK,
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

            // 説明文（常に表示）
            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(DescriptionTopMargin))

                if (isLongDescription) {
                    // 長い説明文は展開/折りたたみ可能
                    Column {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isExpanded) Int.MAX_VALUE else CollapsedMaxLines,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(ExpandToggleTopMargin))

                        // 展開/折りたたみボタン
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isExpanded) TaskioStrings.COLLAPSE else TaskioStrings.EXPAND,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) TaskioStrings.COLLAPSE else TaskioStrings.EXPAND,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // 短い説明文はそのまま表示
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
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
private val DescriptionTopMargin = 12.dp
private val ExpandToggleTopMargin = 4.dp
private const val ExpandableLineThreshold = 3
private const val LongDescriptionCharThreshold = 100
private const val CollapsedMaxLines = 2

// ─────────────────────────────
// プレビュー
// ─────────────────────────────

@Preview(showBackground = true)
@Composable
private fun LogTaskCardShortDescPreview() {
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
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogTaskCardLongDescPreview() {
    TaskioTheme {
        LogTaskCard(
            task = TaskItem(
                id = "2",
                title = "資格勉強",
                description = "AWS認定資格の勉強。毎日1時間は確保する。\n" +
                    "ソリューションアーキテクトアソシエイトを目標に。\n" +
                    "公式ドキュメントとUdemyの講座を活用。\n" +
                    "模擬試験で80%以上取れるようになったら受験。",
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
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogTaskCardNoDescPreview() {
    TaskioTheme {
        LogTaskCard(
            task = TaskItem(
                id = "3",
                title = "ランニング",
                description = null,
                createdAt = "2025-01-24",
                repeatDays = listOf(2, 4, 6),
                isRecurring = true
            ),
            weeklyProgress = mapOf(
                0 to false,
                1 to false,
                2 to true,
                3 to false,
                4 to true,
                5 to false,
                6 to false
            ),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
