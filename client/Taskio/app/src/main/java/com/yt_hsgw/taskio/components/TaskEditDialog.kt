package com.yt_hsgw.taskio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioTheme

/**
 * タスク編集ダイアログ
 *
 * タスクのタイトル、説明、繰り返し設定を編集するダイアログです。
 *
 * @param title 現在のタイトル
 * @param description 現在の説明
 * @param isRecurring 繰り返しフラグ
 * @param selectedDays 選択された曜日のセット（0=日曜日）
 * @param onTitleChange タイトル変更コールバック
 * @param onDescriptionChange 説明変更コールバック
 * @param onRecurringChange 繰り返しフラグ変更コールバック
 * @param onDayToggle 曜日トグルコールバック
 * @param onSave 保存コールバック
 * @param onDismiss 閉じるコールバック
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskEditDialog(
    title: String,
    description: String,
    isRecurring: Boolean,
    selectedDays: Set<Int>,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
    onDayToggle: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = TaskioStrings.TASK_EDIT,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // タイトル入力
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(TaskioStrings.TASK_TITLE_REQUIRED) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(FieldSpacing))

                // 説明入力
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(TaskioStrings.TASK_DESCRIPTION_OPTIONAL) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = MinDescriptionLines,
                    maxLines = MaxDescriptionLines
                )

                Spacer(modifier = Modifier.height(FieldSpacing))

                // 繰り返しチェックボックス
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = onRecurringChange
                    )
                    Text(
                        text = TaskioStrings.TASK_RECURRING,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // 曜日選択（繰り返しが有効な場合のみ表示）
                if (isRecurring) {
                    Spacer(modifier = Modifier.height(DaySelectorTopMargin))

                    Text(
                        text = TaskioStrings.TASK_SELECT_RECURRING_DAYS,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(DayChipTopMargin))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DayChipSpacing),
                        verticalArrangement = Arrangement.spacedBy(DayChipSpacing)
                    ) {
                        // 日曜日(0)から土曜日(6)まで
                        for (dayIndex in 0 until DAYS_IN_WEEK) {
                            val dayLabel = TaskioStrings.getDayOfWeekByIndex(dayIndex)
                            val isSelected = selectedDays.contains(dayIndex)

                            FilterChip(
                                selected = isSelected,
                                onClick = { onDayToggle(dayIndex) },
                                label = { Text(dayLabel) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = title.isNotBlank() && (!isRecurring || selectedDays.isNotEmpty())
            ) {
                Text(TaskioStrings.BUTTON_SAVE)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(TaskioStrings.BUTTON_CANCEL)
            }
        }
    )
}

// ─────────────────────────────
// 定数
// ─────────────────────────────

private const val DAYS_IN_WEEK = 7
private const val MinDescriptionLines = 2
private const val MaxDescriptionLines = 4
private val FieldSpacing = 16.dp
private val DaySelectorTopMargin = 8.dp
private val DayChipTopMargin = 8.dp
private val DayChipSpacing = 8.dp

// ─────────────────────────────
// プレビュー
// ─────────────────────────────

@Preview(showBackground = true)
@Composable
private fun TaskEditDialogPreview() {
    TaskioTheme {
        TaskEditDialog(
            title = "ジム",
            description = "24hジムでのトレーニング",
            isRecurring = true,
            selectedDays = setOf(1, 3, 5),
            onTitleChange = {},
            onDescriptionChange = {},
            onRecurringChange = {},
            onDayToggle = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskEditDialogNonRecurringPreview() {
    TaskioTheme {
        TaskEditDialog(
            title = "歯医者",
            description = "定期検診",
            isRecurring = false,
            selectedDays = emptySet(),
            onTitleChange = {},
            onDescriptionChange = {},
            onRecurringChange = {},
            onDayToggle = {},
            onSave = {},
            onDismiss = {}
        )
    }
}
