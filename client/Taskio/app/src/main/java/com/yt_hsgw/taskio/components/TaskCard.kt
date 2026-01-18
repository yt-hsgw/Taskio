package com.yt_hsgw.taskio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioColors
import com.yt_hsgw.taskio.ui.theme.TaskioDimens
import com.yt_hsgw.taskio.ui.theme.TaskioTypography
import com.yt_hsgw.taskio.viewmodel.TaskWithDayState

/**
 * タスクカードコンポーネント
 *
 * タスクの情報と開始/終了ボタンを表示するカード
 *
 * @param task タスクと状態情報
 * @param onStartClick 開始ボタンクリック時のコールバック
 * @param onFinishClick 終了ボタンクリック時のコールバック
 * @param modifier オプションのModifier
 */
@Composable
fun TaskCard(
    task: TaskWithDayState,
    onStartClick: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInProgress = task.isStarted && !task.isFinished
    val isCompleted = task.isFinished

    val cardColor = when {
        isCompleted -> TaskioColors.StatusCompleted
        isInProgress -> TaskioColors.StatusInProgress
        else -> TaskioColors.StatusDefault
    }

    val elevation = if (isInProgress) {
        TaskioDimens.ElevationLarge
    } else {
        TaskioDimens.ElevationSmall
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TaskioDimens.CornerRadiusLarge),
        color = cardColor,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TaskioDimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIndicator(
                isStarted = task.isStarted,
                isFinished = task.isFinished
            )

            Spacer(modifier = Modifier.width(TaskioDimens.PaddingSmall + TaskioDimens.PaddingXSmall))

            TaskInfo(
                title = task.title,
                description = task.description,
                isRecurring = task.isRecurring,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(TaskioDimens.PaddingSmall))

            ActionButtons(
                isStarted = task.isStarted,
                isFinished = task.isFinished,
                onStartClick = onStartClick,
                onFinishClick = onFinishClick
            )
        }
    }
}

/**
 * タスクの状態を示すインジケーター
 *
 * @param isStarted 開始済みかどうか
 * @param isFinished 終了済みかどうか
 */
@Composable
private fun StatusIndicator(
    isStarted: Boolean,
    isFinished: Boolean
) {
    val color = when {
        isFinished -> TaskioColors.Primary
        isStarted -> TaskioColors.Secondary
        else -> TaskioColors.TextSecondary.copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier
            .size(TaskioDimens.StatusIndicatorSize)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * タスク情報表示部分
 *
 * @param title タスクタイトル
 * @param description タスク説明（nullable）
 * @param isRecurring 繰り返しタスクかどうか
 * @param modifier オプションのModifier
 */
@Composable
private fun TaskInfo(
    title: String,
    description: String?,
    isRecurring: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = TaskioTypography.FontSizeLarge,
                fontWeight = FontWeight.SemiBold,
                color = TaskioColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isRecurring) {
                Spacer(modifier = Modifier.width(TaskioDimens.PaddingSmall - TaskioDimens.PaddingXSmall))
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = TaskioStrings.CD_RECURRING,
                    modifier = Modifier.size(TaskioDimens.IconSizeSmall),
                    tint = TaskioColors.Secondary
                )
            }
        }
        if (description != null) {
            Spacer(modifier = Modifier.height(TaskioDimens.PaddingXSmall / 2))
            Text(
                text = description,
                fontSize = TaskioTypography.FontSizeMedium,
                color = TaskioColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 開始/終了アクションボタン群
 *
 * @param isStarted 開始済みかどうか
 * @param isFinished 終了済みかどうか
 * @param onStartClick 開始ボタンクリック時のコールバック
 * @param onFinishClick 終了ボタンクリック時のコールバック
 */
@Composable
private fun ActionButtons(
    isStarted: Boolean,
    isFinished: Boolean,
    onStartClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(TaskioDimens.PaddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            icon = if (isStarted) Icons.Default.Check else Icons.Default.PlayArrow,
            label = TaskioStrings.ACTION_START,
            isActive = isStarted,
            activeColor = TaskioColors.Secondary,
            onClick = onStartClick,
            enabled = !isFinished
        )

        ActionButton(
            icon = if (isFinished) Icons.Default.Check else Icons.Default.Stop,
            label = TaskioStrings.ACTION_END,
            isActive = isFinished,
            activeColor = TaskioColors.Primary,
            onClick = onFinishClick,
            enabled = isStarted
        )
    }
}

/**
 * 個別のアクションボタン
 *
 * @param icon 表示するアイコン
 * @param label ボタンラベル
 * @param isActive アクティブ状態かどうか
 * @param activeColor アクティブ時の背景色
 * @param onClick クリック時のコールバック
 * @param enabled 有効かどうか
 */
@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val backgroundColor = when {
        !enabled -> TaskioColors.ButtonDisabledBackground
        isActive -> activeColor
        else -> TaskioColors.ButtonInactiveBackground
    }

    val contentColor = when {
        !enabled -> TaskioColors.ButtonDisabledContent
        isActive -> Color.White
        else -> TaskioColors.TextSecondary
    }

    Box(
        modifier = Modifier
            .width(TaskioDimens.ActionButtonWidth)
            .clip(RoundedCornerShape(TaskioDimens.CornerRadiusSmall + TaskioDimens.PaddingXSmall / 2))
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(TaskioDimens.PaddingSmall),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(TaskioDimens.IconSizeMedium),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(TaskioDimens.PaddingXSmall))
            Text(
                text = label,
                fontSize = TaskioTypography.FontSizeSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
