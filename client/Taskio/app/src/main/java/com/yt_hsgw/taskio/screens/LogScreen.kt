package com.yt_hsgw.taskio.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioDimens

/**
 * ログ画面
 *
 * タスクの実行ログを一覧表示する画面です。
 * 各タスクの開始時刻、終了時刻、継続時間などを確認できます。
 *
 * TODO: ログ一覧の実装
 * TODO: ログのフィルタリング機能
 */
@Composable
fun LogScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TaskioDimens.PaddingLarge)
    ) {
        Text(
            text = TaskioStrings.SCREEN_LOG,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(TaskioDimens.PaddingLarge))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = TaskioStrings.PLACEHOLDER_LOG,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
