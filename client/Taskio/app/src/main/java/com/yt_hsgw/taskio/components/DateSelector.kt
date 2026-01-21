package com.yt_hsgw.taskio.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioColors
import com.yt_hsgw.taskio.ui.theme.TaskioDimens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 日付選択コンポーネント
 *
 * Material3のDatePickerを使用した日付選択UI
 * 選択された日付の表示とクリア機能を提供
 *
 * @param label 未選択時に表示するラベルテキスト
 * @param selectedDate 選択された日付（nullable）
 * @param onDateSelected 日付が選択された時のコールバック（nullでクリア）
 * @param modifier オプションのModifier
 * @param enabled 有効/無効状態
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        DateSelectorButton(
            selectedDate = selectedDate,
            label = label,
            enabled = enabled,
            onButtonClick = { showDatePicker = true },
            onClearClick = { onDateSelected(null) }
        )
    }

    if (showDatePicker) {
        DatePickerDialogContent(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * 日付選択ボタンと クリアボタン
 */
@Composable
private fun DateSelectorButton(
    selectedDate: LocalDate?,
    label: String,
    enabled: Boolean,
    onButtonClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onButtonClick,
            modifier = Modifier.weight(1f),
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = TaskioStrings.CD_SELECT_DATE,
                tint = if (enabled) TaskioColors.Primary else TaskioColors.TextDisabled
            )
            Spacer(modifier = Modifier.width(TaskioDimens.PaddingSmall))
            Text(
                text = selectedDate?.format(DATE_FORMATTER) ?: label,
                color = if (selectedDate != null) {
                    TaskioColors.TextPrimary
                } else {
                    TaskioColors.TextSecondary
                }
            )
        }

        if (selectedDate != null && enabled) {
            IconButton(onClick = onClearClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = TaskioStrings.CD_CLEAR_DATE,
                    tint = TaskioColors.TextSecondary
                )
            }
        }
    }
}

/**
 * 日付選択ダイアログ
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogContent(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                    onDismiss()
                }
            ) {
                Text(TaskioStrings.BUTTON_OK)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(TaskioStrings.BUTTON_CANCEL)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Constants
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd (E)")
