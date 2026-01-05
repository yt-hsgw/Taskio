package com.yt_hsgw.taskio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val DarkGray = Color(0xFF444444)
private val MintGreen = Color(0xFFA8E6CF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Taskio", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.Add, "Add") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color.White)) {
            HorizontalCalendar(
                dates = uiState.calendarDates,
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.onDateSelected(it) }
            )

            Text("Today Tasks", Modifier.padding(16.dp), fontWeight = FontWeight.Bold, fontSize = 20.sp)

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskActionCard(
                        task = task,
                        onStartClick = { viewModel.toggleStart(task.id) },
                        onFinishClick = { viewModel.toggleFinish(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalCalendar(dates: List<LocalDate>, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        dates.forEach { date ->
            val isToday = date == today
            val isSelected = date == selectedDate
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color.Black else Color.Transparent)
                    .border(width = if (isToday) 1.dp else 0.dp, color = if (isToday) Color.Black else Color.Transparent, shape = RoundedCornerShape(12.dp))
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 8.dp)
            ) {
                Text(date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.ENGLISH), color = if (isSelected) Color.White else Color.Black, fontSize = 11.sp)
                Text(date.dayOfMonth.toString(), color = if (isSelected) Color.White else Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TaskActionCard(task: TaskItem, onStartClick: () -> Unit, onFinishClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkGray)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = task.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Column(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusToggleButton(label = "開始", isDone = task.isStarted, onClick = onStartClick)
            StatusToggleButton(label = "終了", isDone = task.isFinished, onClick = onFinishClick)
        }
    }
}

@Composable
fun StatusToggleButton(label: String, isDone: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(100.dp) // ボタンの幅を一定に揃える（"開始"と"終了"で幅がズレないように）
            .clip(RoundedCornerShape(12.dp))
            .background(DarkGray)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier.padding(end = 4.dp)
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isDone) MintGreen else Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = DarkGray
                )
            }
        }
    }
}