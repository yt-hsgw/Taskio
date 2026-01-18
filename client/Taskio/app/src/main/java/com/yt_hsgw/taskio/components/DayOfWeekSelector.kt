package com.yt_hsgw.taskio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioColors
import com.yt_hsgw.taskio.ui.theme.TaskioDimens
import com.yt_hsgw.taskio.ui.theme.TaskioTypography

/**
 * 曜日選択コンポーネント（円形デザイン）
 *
 * 7つの曜日を円形ボタンとして横並びに表示し、複数選択をサポート
 * 曜日ごとに異なる色を使用（日曜=赤、土曜=青、平日=緑）
 *
 * @param selectedDays 選択された曜日のインデックスセット（0=日曜日, 1=月曜日, ..., 6=土曜日）
 * @param onDayToggle 曜日がタップされた時のコールバック
 * @param modifier オプションのModifier
 * @param circleSize 各円のサイズ（デフォルト: 40dp）
 * @param enabled 有効/無効状態
 */
@Composable
fun DayOfWeekSelector(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    circleSize: Dp = TaskioDimens.DayOfWeekCircleSize,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DAYS_OF_WEEK.forEachIndexed { index, day ->
            DayCircle(
                day = day,
                dayIndex = index,
                isSelected = selectedDays.contains(index),
                enabled = enabled,
                circleSize = circleSize,
                onClick = { onDayToggle(index) }
            )
        }
    }
}

/**
 * 個別の曜日円ボタン
 *
 * @param day 曜日の表示文字
 * @param dayIndex 曜日インデックス（0=日曜日, 6=土曜日）
 * @param isSelected 選択されているかどうか
 * @param enabled 有効かどうか
 * @param circleSize 円のサイズ
 * @param onClick クリック時のコールバック
 */
@Composable
private fun DayCircle(
    day: String,
    dayIndex: Int,
    isSelected: Boolean,
    enabled: Boolean,
    circleSize: Dp,
    onClick: () -> Unit
) {
    val backgroundColor = resolveBackgroundColor(
        dayIndex = dayIndex,
        isSelected = isSelected,
        enabled = enabled
    )

    val textColor = resolveTextColor(
        isSelected = isSelected,
        enabled = enabled
    )

    val borderModifier = if (!isSelected && enabled) {
        Modifier.border(BORDER_WIDTH, BORDER_COLOR, CircleShape)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .size(circleSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(borderModifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = TaskioTypography.FontSizeDefault
        )
    }
}

/**
 * 背景色を決定
 */
private fun resolveBackgroundColor(
    dayIndex: Int,
    isSelected: Boolean,
    enabled: Boolean
): Color = when {
    !enabled -> DISABLED_BACKGROUND
    !isSelected -> TaskioColors.DayUnselected
    dayIndex == SUNDAY_INDEX -> TaskioColors.Sunday
    dayIndex == SATURDAY_INDEX -> TaskioColors.Saturday
    else -> TaskioColors.Weekday
}

/**
 * テキスト色を決定
 */
private fun resolveTextColor(
    isSelected: Boolean,
    enabled: Boolean
): Color = when {
    !enabled -> TaskioColors.TextDisabled
    isSelected -> Color.White
    else -> UNSELECTED_TEXT_COLOR
}

// Constants
private val DAYS_OF_WEEK = listOf(
    TaskioStrings.SUNDAY_SHORT,
    TaskioStrings.MONDAY_SHORT,
    TaskioStrings.TUESDAY_SHORT,
    TaskioStrings.WEDNESDAY_SHORT,
    TaskioStrings.THURSDAY_SHORT,
    TaskioStrings.FRIDAY_SHORT,
    TaskioStrings.SATURDAY_SHORT
)

private const val SUNDAY_INDEX = 0
private const val SATURDAY_INDEX = 6

private val BORDER_WIDTH = 1.dp
private val BORDER_COLOR = Color(0xFFDDDDDD)
private val DISABLED_BACKGROUND = Color(0xFFF5F5F5)
private val UNSELECTED_TEXT_COLOR = Color(0xFF666666)
