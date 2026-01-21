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
 * カレンダー画面
 *
 * タスクの実行履歴をカレンダー形式で表示する画面です。
 * 将来的に統計情報やグラフも表示する予定です。
 *
 * TODO: カレンダービューの実装
 * TODO: 統計グラフの実装
 */
@Composable
fun CalendarScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TaskioDimens.PaddingLarge)
    ) {
        ScreenTitle(title = TaskioStrings.SCREEN_CALENDAR)

        Spacer(modifier = Modifier.height(TaskioDimens.PaddingLarge))

        PlaceholderContent(message = TaskioStrings.PLACEHOLDER_CALENDAR)
    }
}

/**
 * 画面タイトル
 *
 * @param title 表示するタイトル
 */
@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium
    )
}

/**
 * プレースホルダーコンテンツ
 *
 * 機能が未実装の場合に表示するプレースホルダーです。
 *
 * @param message 表示するメッセージ
 */
@Composable
private fun PlaceholderContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
