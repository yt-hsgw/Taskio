package com.yt_hsgw.taskio.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioColors
import com.yt_hsgw.taskio.ui.theme.TaskioDimens
import com.yt_hsgw.taskio.ui.theme.TaskioTypography
import java.time.LocalDate

/**
 * タスク作成ダイアログコンポーネント
 *
 * タスクの新規作成に必要な入力フォームを提供
 *
 * @param title タスクタイトル
 * @param description タスク説明
 * @param scheduledDate 予定日（nullable）
 * @param isRecurring 繰り返しタスクかどうか
 * @param selectedDays 選択された曜日のセット
 * @param loading ローディング状態
 * @param onTitleChange タイトル変更時のコールバック
 * @param onDescriptionChange 説明変更時のコールバック
 * @param onScheduledDateChange 予定日変更時のコールバック
 * @param onRecurringToggle 繰り返しトグル時のコールバック
 * @param onDayToggle 曜日トグル時のコールバック
 * @param onDismiss ダイアログ閉じる時のコールバック
 * @param onCreateTask タスク作成時のコールバック
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    title: String,
    description: String,
    scheduledDate: LocalDate?,
    isRecurring: Boolean,
    selectedDays: Set<Int>,
    loading: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onScheduledDateChange: (LocalDate?) -> Unit,
    onRecurringToggle: (Boolean) -> Unit,
    onDayToggle: (Int) -> Unit,
    onDismiss: () -> Unit,
    onCreateTask: () -> Unit
) {
    val isFormValid = title.isNotBlank() && (!isRecurring || selectedDays.isNotEmpty())

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = {
            Text(
                text = TaskioStrings.TASK_NEW,
                fontWeight = FontWeight.Bold,
                fontSize = TaskioTypography.FontSizeHeadline
            )
        },
        text = {
            DialogContent(
                title = title,
                description = description,
                scheduledDate = scheduledDate,
                isRecurring = isRecurring,
                selectedDays = selectedDays,
                loading = loading,
                onTitleChange = onTitleChange,
                onDescriptionChange = onDescriptionChange,
                onScheduledDateChange = onScheduledDateChange,
                onRecurringToggle = onRecurringToggle,
                onDayToggle = onDayToggle
            )
        },
        confirmButton = {
            CreateButton(
                loading = loading,
                enabled = !loading && isFormValid,
                onClick = onCreateTask
            )
        },
        dismissButton = {
            CancelButton(
                enabled = !loading,
                onClick = onDismiss
            )
        },
        shape = RoundedCornerShape(TaskioDimens.CornerRadiusXLarge)
    )
}

/**
 * ダイアログのコンテンツ部分
 */
@Composable
private fun DialogContent(
    title: String,
    description: String,
    scheduledDate: LocalDate?,
    isRecurring: Boolean,
    selectedDays: Set<Int>,
    loading: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onScheduledDateChange: (LocalDate?) -> Unit,
    onRecurringToggle: (Boolean) -> Unit,
    onDayToggle: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TaskioDimens.PaddingLarge)
    ) {
        TitleInput(
            value = title,
            onValueChange = onTitleChange,
            enabled = !loading
        )

        DescriptionInput(
            value = description,
            onValueChange = onDescriptionChange,
            enabled = !loading
        )

        ScheduledDateSection(
            scheduledDate = scheduledDate,
            onDateChange = onScheduledDateChange,
            enabled = !loading && !isRecurring
        )

        RecurringSection(
            isRecurring = isRecurring,
            selectedDays = selectedDays,
            onRecurringToggle = { enabled ->
                onRecurringToggle(enabled)
                if (enabled) onScheduledDateChange(null)
            },
            onDayToggle = onDayToggle,
            enabled = !loading
        )
    }
}

/**
 * タイトル入力フィールド
 */
@Composable
private fun TitleInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(TaskioStrings.TASK_TITLE_REQUIRED) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(TaskioDimens.CornerRadiusMedium)
    )
}

/**
 * 説明入力フィールド
 */
@Composable
private fun DescriptionInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(TaskioStrings.TASK_DESCRIPTION_OPTIONAL) },
        modifier = Modifier.fillMaxWidth(),
        maxLines = MAX_DESCRIPTION_LINES,
        enabled = enabled,
        shape = RoundedCornerShape(TaskioDimens.CornerRadiusMedium)
    )
}

/**
 * 予定日選択セクション
 */
@Composable
private fun ScheduledDateSection(
    scheduledDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = TaskioStrings.TASK_SCHEDULED_DATE,
            fontSize = TaskioTypography.FontSizeDefault,
            fontWeight = FontWeight.Medium,
            color = TaskioColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(TaskioDimens.PaddingSmall))
        DateSelector(
            label = TaskioStrings.TASK_SELECT_DATE,
            selectedDate = scheduledDate,
            onDateSelected = onDateChange,
            enabled = enabled
        )
    }
}

/**
 * 繰り返し設定セクション
 */
@Composable
private fun RecurringSection(
    isRecurring: Boolean,
    selectedDays: Set<Int>,
    onRecurringToggle: (Boolean) -> Unit,
    onDayToggle: (Int) -> Unit,
    enabled: Boolean
) {
    Column {
        RecurringToggleRow(
            isRecurring = isRecurring,
            onToggle = onRecurringToggle,
            enabled = enabled
        )

        if (isRecurring) {
            Spacer(modifier = Modifier.height(TaskioDimens.PaddingMedium))
            Text(
                text = TaskioStrings.TASK_SELECT_RECURRING_DAYS,
                fontSize = TaskioTypography.FontSizeSmall,
                color = TaskioColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(TaskioDimens.PaddingSmall))
            DayOfWeekSelector(
                selectedDays = selectedDays,
                onDayToggle = onDayToggle,
                enabled = enabled,
                circleSize = TaskioDimens.DayOfWeekCircleSizeSmall
            )
        }
    }
}

/**
 * 繰り返しトグル行
 */
@Composable
private fun RecurringToggleRow(
    isRecurring: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                modifier = Modifier.size(TaskioDimens.IconSizeLarge),
                tint = if (isRecurring) TaskioColors.Secondary else TaskioColors.TextSecondary
            )
            Spacer(modifier = Modifier.width(TaskioDimens.PaddingSmall))
            Text(
                text = TaskioStrings.TASK_RECURRING,
                fontSize = TaskioTypography.FontSizeDefault,
                fontWeight = FontWeight.Medium,
                color = TaskioColors.TextPrimary
            )
        }
        Switch(
            checked = isRecurring,
            onCheckedChange = onToggle,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TaskioColors.Secondary
            )
        )
    }
}

/**
 * 作成ボタン
 */
@Composable
private fun CreateButton(
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(TaskioDimens.CornerRadiusMedium),
        colors = ButtonDefaults.buttonColors(containerColor = TaskioColors.Primary)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(TaskioDimens.IconSizeMedium),
                color = Color.White,
                strokeWidth = PROGRESS_STROKE_WIDTH
            )
            Spacer(Modifier.width(TaskioDimens.PaddingSmall))
        }
        Text(
            text = TaskioStrings.BUTTON_CREATE,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * キャンセルボタン
 */
@Composable
private fun CancelButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text = TaskioStrings.BUTTON_CANCEL,
            color = TaskioColors.TextSecondary
        )
    }
}

// Constants
private const val MAX_DESCRIPTION_LINES = 2
private val PROGRESS_STROKE_WIDTH = TaskioDimens.PaddingXSmall / 2
