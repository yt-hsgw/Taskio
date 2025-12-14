@file:OptIn(ExperimentalMaterial3Api::class)
package com.yt_hsgw.taskio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun TaskScreen(viewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text("Task Creator", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // ----------------------
        // 入力欄
        // ----------------------
        OutlinedTextField(
            value = uiState.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
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
            enabled = !uiState.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Task")
        }

        Spacer(Modifier.height(20.dp))

        // ----------------------
        // ローディング
        // ----------------------
        if (uiState.loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }

        // ----------------------
        // エラー表示
        // ----------------------
        uiState.errorMessage?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
        }

        // ----------------------
        // タスクリスト
        // ----------------------
        Text("Task List", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(uiState.tasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(task.title, style = MaterialTheme.typography.titleMedium)
                        task.description?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("ID: ${task.id}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
