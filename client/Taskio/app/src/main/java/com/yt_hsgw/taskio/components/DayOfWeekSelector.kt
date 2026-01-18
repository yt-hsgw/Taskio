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
import androidx.compose.ui.unit.sp

/**
 * 曜日選択コンポーネント（円形デザイン）
 * @param selectedDays 選択された曜日のインデックスセット（0=日曜日, 1=月曜日, ..., 6=土曜日）
 * @param onDayToggle 曜日がタップされた時のコールバック
 * @param modifier Modifier
 * @param circleSize 各円のサイズ
 * @param enabled 有効/無効状態
 */
@Composable
fun DayOfWeekSelector(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    circleSize: Dp = 40.dp,
    enabled: Boolean = true
) {
    val daysOfWeek = listOf("日", "月", "火", "水", "木", "金", "土")

    // カラー定義
    val selectedColor = Color(0xFF4CAF50)  // グリーン
    val unselectedColor = Color(0xFFE8E8E8)  // ライトグレー
    val selectedTextColor = Color.White
    val unselectedTextColor = Color(0xFF666666)
    val disabledColor = Color(0xFFF5F5F5)
    val disabledTextColor = Color(0xFFBDBDBD)

    // 日曜・土曜のカラー
    val sundayColor = Color(0xFFE57373)  // 薄い赤
    val saturdayColor = Color(0xFF64B5F6)  // 薄い青

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(index)

            // 曜日に応じた選択時の色
            val activeColor = when {
                !isSelected -> unselectedColor
                index == 0 -> sundayColor  // 日曜日
                index == 6 -> saturdayColor  // 土曜日
                else -> selectedColor
            }

            val backgroundColor = if (enabled) activeColor else disabledColor
            val textColor = when {
                !enabled -> disabledTextColor
                isSelected -> selectedTextColor
                else -> unselectedTextColor
            }

            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .then(
                        if (!isSelected && enabled) {
                            Modifier.border(1.dp, Color(0xFFDDDDDD), CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable(enabled = enabled) { onDayToggle(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}
