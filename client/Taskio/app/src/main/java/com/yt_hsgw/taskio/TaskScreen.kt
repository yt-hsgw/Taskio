@file:OptIn(ExperimentalMaterial3Api::class)
package com.yt_hsgw.taskio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.model.TaskResponse
import com.yt_hsgw.taskio.utils.DateTimeUtils
import com.yt_hsgw.taskio.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@Composable
fun TaskScreen(viewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // エラーが発生したらSnackbarを表示
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Task Creator", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            // ----------------------
            // 入力欄
            // ----------------------
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.loading
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                enabled = !uiState.loading
            )

            Spacer(Modifier.height(12.dp))

            // ----------------------
            // 登録ボタン
            // ----------------------
            Button(
                onClick = {
                    scope.launch {
                        viewModel.createTask()
                    }
                },
                enabled = !uiState.loading && uiState.title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Create Task")
            }

            Spacer(Modifier.height(20.dp))

            // ----------------------
            // タスクリスト
            // ----------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Task List", style = MaterialTheme.typography.titleMedium)
                if (!uiState.loading && uiState.tasks.isNotEmpty()) {
                    Text(
                        "${uiState.tasks.size} tasks",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            if (uiState.loading && uiState.tasks.isEmpty()) {
                // 初回ロード中
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.tasks.isEmpty()) {
                // タスクがない場合
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "タスクがありません\n上の入力欄から作成できます",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // タスク一覧
//                LazyColumn {
//                    items(uiState.tasks, key = { it.id }) { task ->
//                        TaskCard(
//                            task = task,
//                            onDelete = { viewModel.deleteTask(task.id) }
//                        )
//                    }
//                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskResponse,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    task.description?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                
                // 削除ボタン
                IconButton(onClick = { showDeleteDialog = true }) {
                    Text("🗑️")
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            // 作成日時
            Text(
                "作成: ${DateTimeUtils.formatIsoDateTime(task.created_at)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // 削除確認ダイアログ
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("タスクを削除") },
            text = { Text("「${task.title}」を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}