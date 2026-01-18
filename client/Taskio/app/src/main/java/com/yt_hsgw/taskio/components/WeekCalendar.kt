package com.yt_hsgw.taskio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.yt_hsgw.taskio.ui.theme.TaskioColors
import com.yt_hsgw.taskio.ui.theme.TaskioDimens
import com.yt_hsgw.taskio.ui.theme.TaskioTypography
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 週間カレンダーコンポーネント
 *
 * 7日分の日付を横並びで表示し、選択・今日のハイライトをサポート
 *
 * @param dates 表示する日付のリスト（通常7日分）
 * @param selectedDate 現在選択されている日付
 * @param onDateSelected 日付選択時のコールバック
 * @param modifier オプションのModifier
 */
@Composable
fun WeekCalendar(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(TaskioDimens.PaddingLarge),
        shape = RoundedCornerShape(TaskioDimens.CornerRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = TaskioColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = TaskioDimens.ElevationMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = TaskioDimens.PaddingLarge,
                    horizontal = TaskioDimens.PaddingSmall
                ),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dates.forEach { date ->
                CalendarDayItem(
                    date = date,
                    isToday = date == today,
                    isSelected = date == selectedDate,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

/**
 * カレンダーの1日分の表示コンポーネント
 *
 * @param date 表示する日付
 * @param isToday 今日かどうか
 * @param isSelected 選択されているかどうか
 * @param onClick クリック時のコールバック
 */
@Composable
private fun CalendarDayItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> TaskioColors.Primary
        isToday -> TaskioColors.Primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val dayTextColor = when {
        isSelected -> Color.White.copy(alpha = 0.8f)
        else -> TaskioColors.TextSecondary
    }

    val dateTextColor = when {
        isSelected -> Color.White
        else -> TaskioColors.TextPrimary
    }

    val fontWeight = when {
        isSelected || isToday -> FontWeight.Bold
        else -> FontWeight.Medium
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(TaskioDimens.CornerRadiusMedium))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = TaskioDimens.CalendarDayPaddingHorizontal,
                vertical = TaskioDimens.CalendarDayPaddingVertical
            )
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.JAPANESE),
            fontSize = TaskioTypography.FontSizeSmall,
            color = dayTextColor,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(TaskioDimens.PaddingXSmall))
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = TaskioTypography.FontSizeTitle,
            color = dateTextColor,
            fontWeight = fontWeight
        )
    }
}
