package com.yt_hsgw.taskio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yt_hsgw.taskio.components.DateSelector
import com.yt_hsgw.taskio.components.DayOfWeekSelector
import com.yt_hsgw.taskio.viewmodel.TaskViewModel
import com.yt_hsgw.taskio.viewmodel.TaskWithDayState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// カラーパレット
private val PrimaryDark = Color(0xFF1A1A2E)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentBlue = Color(0xFF2196F3)
private val SurfaceLight = Color(0xFFFAFAFA)
private val CardBackground = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(message = error, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.taskCreated) {
        if (uiState.taskCreated) {
            showCreateDialog = false
            viewModel.resetTaskCreated()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = AccentGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "タスクを追加")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(SurfaceLight)
        ) {
            // 週間カレンダー
            WeekCalendar(
                dates = uiState.calendarDates,
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.onDateSelected(it) }
            )

            // 日付ヘッダー
            DateHeader(selectedDate = uiState.selectedDate)

            // タスク一覧
            if (filteredTasks.isEmpty()) {
                EmptyTasksView()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onStartClick = { viewModel.toggleStart(task.id) },
                            onFinishClick = { viewModel.toggleFinish(task.id) }
                        )
                    }
                }
            }
        }

        // タスク作成ダイアログ
        if (showCreateDialog) {
            CreateTaskDialog(
                title = uiState.title,
                description = uiState.description,
                scheduledDate = uiState.scheduledDate?.toLocalDate(),
                isRecurring = uiState.isRecurring,
                selectedDays = uiState.selectedDays,
                loading = uiState.loading,
                onTitleChange = { viewModel.updateTitle(it) },
                onDescriptionChange = { viewModel.updateDescription(it) },
                onScheduledDateChange = { date ->
                    viewModel.updateScheduledDate(date?.atStartOfDay())
                },
                onRecurringToggle = { viewModel.toggleRecurring(it) },
                onDayToggle = { viewModel.toggleDay(it) },
                onDismiss = {
                    showCreateDialog = false
                    viewModel.updateTitle("")
                    viewModel.updateDescription("")
                    viewModel.updateScheduledDate(null)
                    viewModel.toggleRecurring(false)
                },
                onCreateTask = {
                    scope.launch { viewModel.createTask() }
                }
            )
        }
    }
}

@Composable
private fun WeekCalendar(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dates.forEach { date ->
                val isToday = date == today
                val isSelected = date == selectedDate

                CalendarDay(
                    date = date,
                    isToday = isToday,
                    isSelected = isSelected,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> AccentGreen
        isToday -> AccentGreen.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        else -> TextPrimary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.JAPANESE),
            fontSize = 12.sp,
            color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 18.sp,
            color = textColor,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun DateHeader(selectedDate: LocalDate) {
    val today = LocalDate.now()
    val headerText = when (selectedDate) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("M月d日"))
    }

    Text(
        text = headerText,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
}

@Composable
private fun EmptyTasksView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📋",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "タスクがありません",
                fontSize = 18.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "右下の＋ボタンから追加できます",
                fontSize = 14.sp,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskWithDayState,
    onStartClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val isInProgress = task.isStarted && !task.isFinished
    val isCompleted = task.isFinished

    // アニメーションなしで即時反映
    val cardColor = when {
        isCompleted -> Color(0xFFE8F5E9)  // 薄い緑
        isInProgress -> Color(0xFFE3F2FD)  // 薄い青
        else -> CardBackground
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        shadowElevation = if (isInProgress) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ステータスインジケーター
            StatusIndicator(
                isStarted = task.isStarted,
                isFinished = task.isFinished
            )

            Spacer(modifier = Modifier.width(10.dp))

            // タスク情報
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (task.isRecurring) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "繰り返し",
                            modifier = Modifier.size(14.dp),
                            tint = AccentBlue
                        )
                    }
                }
                if (task.description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description!!,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // アクションボタン
            ActionButtons(
                isStarted = task.isStarted,
                isFinished = task.isFinished,
                onStartClick = onStartClick,
                onFinishClick = onFinishClick
            )
        }
    }
}

@Composable
private fun StatusIndicator(isStarted: Boolean, isFinished: Boolean) {
    val color = when {
        isFinished -> AccentGreen
        isStarted -> AccentBlue
        else -> TextSecondary.copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun ActionButtons(
    isStarted: Boolean,
    isFinished: Boolean,
    onStartClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 開始ボタン
        ActionButton(
            icon = if (isStarted) Icons.Default.Check else Icons.Default.PlayArrow,
            label = "開始",
            isActive = isStarted,
            activeColor = AccentBlue,
            onClick = onStartClick,
            enabled = !isFinished
        )

        // 終了ボタン
        ActionButton(
            icon = if (isFinished) Icons.Default.Check else Icons.Default.Stop,
            label = "終了",
            isActive = isFinished,
            activeColor = AccentGreen,
            onClick = onFinishClick,
            enabled = isStarted
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    // アニメーションなしで即時反映
    val backgroundColor = when {
        !enabled -> Color.Gray.copy(alpha = 0.08f)
        isActive -> activeColor
        else -> Color.Gray.copy(alpha = 0.12f)
    }

    val contentColor = when {
        !enabled -> Color.Gray.copy(alpha = 0.35f)
        isActive -> Color.White
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .width(72.dp)  // 固定幅で統一
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTaskDialog(
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
    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = {
            Text(
                "新しいタスク",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // タイトル入力
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("タイトル *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !loading,
                    shape = RoundedCornerShape(12.dp)
                )

                // 説明入力
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("説明（任意）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    enabled = !loading,
                    shape = RoundedCornerShape(12.dp)
                )

                // 予定日選択
                Column {
                    Text(
                        text = "予定日",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DateSelector(
                        label = "日付を選択",
                        selectedDate = scheduledDate,
                        onDateSelected = onScheduledDateChange,
                        enabled = !loading && !isRecurring
                    )
                }

                // 繰り返し設定
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isRecurring) AccentBlue else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "繰り返し",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = {
                                onRecurringToggle(it)
                                if (it) onScheduledDateChange(null)
                            },
                            enabled = !loading,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentBlue
                            )
                        )
                    }

                    // 曜日選択（繰り返しONの時のみ表示）
                    if (isRecurring) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "繰り返す曜日を選択",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DayOfWeekSelector(
                            selectedDays = selectedDays,
                            onDayToggle = onDayToggle,
                            enabled = !loading,
                            circleSize = 36.dp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreateTask,
                enabled = !loading && title.isNotBlank() &&
                        (!isRecurring || selectedDays.isNotEmpty()),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("作成", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !loading
            ) {
                Text("キャンセル", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}