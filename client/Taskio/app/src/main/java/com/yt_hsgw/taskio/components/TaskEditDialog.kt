package com.yt_hsgw.taskio.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioTheme
import java.time.LocalDate

// カラーパレット（HomeScreenと共通）
private val TextSecondary = Color(0xFF757575)

/**
 * タスク編集ダイアログ
 *
 * HomeScreenのタスク追加ダイアログと同じUI/UXを提供します。
 * タスクのタイトル、説明、予定日、繰り返し設定を編集できます。
 *
 * @param title 現在のタイトル
 * @param description 現在の説明
 * @param scheduledDate 予定日（null可）
 * @param isRecurring 繰り返しフラグ
 * @param selectedDays 選択された曜日のセット（0=日曜日）
 * @param loading 保存中フラグ
 * @param onTitleChange タイトル変更コールバック
 * @param onDescriptionChange 説明変更コールバック
 * @param onScheduledDateChange 予定日変更コールバック
 * @param onRecurringChange 繰り返しフラグ変更コールバック
 * @param onDayToggle 曜日トグルコールバック
 * @param onSave 保存コールバック
 * @param onDismiss 閉じるコールバック
 */
@Composable
fun TaskEditDialog(
    title: String,
    description: String,
    scheduledDate: LocalDate?,
    isRecurring: Boolean,
    selectedDays: Set<Int>,
    loading: Boolean = false,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onScheduledDateChange: (LocalDate?) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
    onDayToggle: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val isSaveEnabled = title.isNotBlank() && 
        (!isRecurring || selectedDays.isNotEmpty()) && 
        !loading

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = {
            Text(
                text = TaskioStrings.TASK_EDIT,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
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
                    singleLine = true,
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(FieldSpacing))

                // 説明入力
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(TaskioStrings.TASK_DESCRIPTION_OPTIONAL) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = MinDescriptionLines,
                    maxLines = MaxDescriptionLines,
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(FieldSpacing))

                // 予定日選択（繰り返しでない場合のみ有効）
                DateSelector(
                    label = TaskioStrings.TASK_SCHEDULED_DATE,
                    selectedDate = scheduledDate,
                    onDateSelected = onScheduledDateChange,
                    enabled = !loading && !isRecurring
                )

                Spacer(modifier = Modifier.height(FieldSpacing))

                // 繰り返しスイッチ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = TaskioStrings.TASK_RECURRING,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = onRecurringChange,
                        enabled = !loading
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

                    // 円形の曜日セレクター（HomeScreenと同じ）
                    DayOfWeekSelector(
                        selectedDays = selectedDays,
                        onDayToggle = onDayToggle,
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = isSaveEnabled
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonLoadingSize),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = TaskioStrings.BUTTON_SAVE,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !loading
            ) {
                Text(
                    text = TaskioStrings.BUTTON_CANCEL,
                    color = TextSecondary
                )
            }
        },
        shape = RoundedCornerShape(DialogCornerRadius)
    )
}

/**
 * 後方互換性のためのオーバーロード
 *
 * scheduledDateを使用しない場合のシンプルなインターフェース
 */
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
    TaskEditDialog(
        title = title,
        description = description,
        scheduledDate = null,
        isRecurring = isRecurring,
        selectedDays = selectedDays,
        loading = false,
        onTitleChange = onTitleChange,
        onDescriptionChange = onDescriptionChange,
        onScheduledDateChange = {},
        onRecurringChange = onRecurringChange,
        onDayToggle = onDayToggle,
        onSave = onSave,
        onDismiss = onDismiss
    )
}

// ─────────────────────────────
// 定数
// ─────────────────────────────

private const val MinDescriptionLines = 2
private const val MaxDescriptionLines = 4
private val FieldSpacing = 16.dp
private val DaySelectorTopMargin = 8.dp
private val DayChipTopMargin = 8.dp
private val DialogCornerRadius = 20.dp
private val ButtonLoadingSize = 16.dp

// ─────────────────────────────
// プレビュー
// ─────────────────────────────

@Preview(showBackground = true)
@Composable
private fun TaskEditDialogRecurringPreview() {
    TaskioTheme {
        TaskEditDialog(
            title = "ジム",
            description = "24hジムでのトレーニング",
            scheduledDate = null,
            isRecurring = true,
            selectedDays = setOf(1, 3, 5),
            loading = false,
            onTitleChange = {},
            onDescriptionChange = {},
            onScheduledDateChange = {},
            onRecurringChange = {},
            onDayToggle = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskEditDialogScheduledPreview() {
    TaskioTheme {
        TaskEditDialog(
            title = "歯医者",
            description = "定期検診",
            scheduledDate = LocalDate.now().plusDays(7),
            isRecurring = false,
            selectedDays = emptySet(),
            loading = false,
            onTitleChange = {},
            onDescriptionChange = {},
            onScheduledDateChange = {},
            onRecurringChange = {},
            onDayToggle = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskEditDialogLoadingPreview() {
    TaskioTheme {
        TaskEditDialog(
            title = "ジム",
            description = "24hジムでのトレーニング",
            scheduledDate = null,
            isRecurring = true,
            selectedDays = setOf(1, 3, 5),
            loading = true,
            onTitleChange = {},
            onDescriptionChange = {},
            onScheduledDateChange = {},
            onRecurringChange = {},
            onDayToggle = {},
            onSave = {},
            onDismiss = {}
        )
    }
}
