package com.yt_hsgw.taskio.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yt_hsgw.taskio.ui.TaskioStrings
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 日付と時刻を選択するためのコンポーネント
 *
 * Material3のDatePickerとTimePickerを組み合わせて使用します。
 * ユーザーはまず日付を選択し、次に時刻を選択します。
 *
 * @param label ボタンに表示するラベル
 * @param selectedDateTime 現在選択されている日時（nullable）
 * @param onDateTimeSelected 日時が選択された時のコールバック（nullでクリア）
 * @param modifier オプションのModifier
 * @param enabled コンポーネントの有効/無効状態
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    label: String,
    selectedDateTime: LocalDateTime?,
    onDateTimeSelected: (LocalDateTime?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDate by remember(selectedDateTime) { mutableStateOf(selectedDateTime?.toLocalDate()) }
    var tempTime by remember(selectedDateTime) { mutableStateOf(selectedDateTime?.toLocalTime()) }

    Column(modifier = modifier) {
        DateTimeSelectButton(
            label = label,
            selectedDateTime = selectedDateTime,
            enabled = enabled,
            onClick = { showDatePicker = true }
        )

        if (selectedDateTime != null) {
            ClearButton(
                enabled = enabled,
                onClick = {
                    tempDate = null
                    tempTime = null
                    onDateTimeSelected(null)
                }
            )
        }
    }

    // 日付選択ダイアログ
    if (showDatePicker) {
        DatePickerDialogContent(
            initialDate = tempDate,
            onDateSelected = { date ->
                tempDate = date
                showDatePicker = false
                showTimePicker = true
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // 時刻選択ダイアログ
    if (showTimePicker) {
        TimePickerDialogContent(
            initialTime = tempTime,
            onTimeSelected = { time ->
                tempTime = time
                showTimePicker = false
                if (tempDate != null && tempTime != null) {
                    onDateTimeSelected(LocalDateTime.of(tempDate, tempTime))
                }
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

/**
 * 日時選択ボタン
 */
@Composable
private fun DateTimeSelectButton(
    label: String,
    selectedDateTime: LocalDateTime?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text(
            text = selectedDateTime?.let {
                "$label: ${it.format(DATE_TIME_FORMATTER)}"
            } ?: TaskioStrings.formatDateTimeSetLabel(label)
        )
    }
}

/**
 * クリアボタン
 */
@Composable
private fun ClearButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(TaskioStrings.BUTTON_CLEAR)
    }
}

/**
 * 日付選択ダイアログの内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogContent(
    initialDate: java.time.LocalDate?,
    onDateSelected: (java.time.LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
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

/**
 * 時刻選択ダイアログの内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogContent(
    initialTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = initialTime ?: LocalTime.now()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(TaskioStrings.DATETIME_SELECT_TIME) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
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
    )
}

// Constants
private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(TaskioStrings.DATE_FORMAT_WITH_TIME)
