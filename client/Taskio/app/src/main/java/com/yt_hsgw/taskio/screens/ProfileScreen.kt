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
 * プロフィール画面
 *
 * ユーザー設定やアプリ設定を管理する画面です。
 *
 * TODO: ユーザー設定の実装
 * TODO: アプリ設定の実装
 * TODO: データエクスポート機能
 */
@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TaskioDimens.PaddingLarge)
    ) {
        Text(
            text = TaskioStrings.SCREEN_PROFILE,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(TaskioDimens.PaddingLarge))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = TaskioStrings.PLACEHOLDER_PROFILE,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
