package com.yt_hsgw.taskio.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yt_hsgw.taskio.ui.theme.TaskioTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * 曜日ラベル（日曜始まり）
 */
private val WEEKDAY_LABELS = listOf("日", "月", "火", "水", "木", "金", "土")

/**
 * カレンダーグリッド
 *
 * 月のカレンダーを曜日ヘッダーと日付グリッドで表示します。
 *
 * @param yearMonth 表示する年月
 * @param selectedDate 選択中の日付
 * @param datesWithLogs ログが存在する日付のセット
 * @param onDateSelected 日付選択時のコールバック
 * @param modifier Modifier
 */
@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    datesWithLogs: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()

    // カレンダーの日付リストを生成
    val calendarDates = remember(yearMonth) {
        generateCalendarDates(yearMonth)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // 曜日ヘッダー
        WeekdayHeader()

        // 日付グリッド
        calendarDates.chunked(7).forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),  // 行間の間隔を削除
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    CalendarDayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        hasLogs = datesWithLogs.contains(date),
                        isCurrentMonth = YearMonth.from(date) == yearMonth,
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 曜日ヘッダー行
 */
@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WEEKDAY_LABELS.forEachIndexed { index, label ->
            val color = when (index) {
                0 -> MaterialTheme.colorScheme.error // 日曜
                6 -> MaterialTheme.colorScheme.tertiary // 土曜
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * カレンダー用の日付リストを生成
 *
 * 表示月の前後の日付も含めて、6週間分（42日）の日付リストを返します。
 * 週は日曜日から始まります。
 *
 * @param yearMonth 対象の年月
 * @return 日付のリスト
 */
private fun generateCalendarDates(yearMonth: YearMonth): List<LocalDate> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    // 月の最初の日曜日を取得（前月の日付になることがある）
    val firstSunday = firstDayOfMonth.with(
        TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
    )

    // 月の最後の土曜日を取得（次月の日付になることがある）
    val lastSaturday = lastDayOfMonth.with(
        TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)
    )

    // 日付リストを生成
    val dates = mutableListOf<LocalDate>()
    var currentDate = firstSunday

    while (!currentDate.isAfter(lastSaturday)) {
        dates.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }

    return dates
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun CalendarGridPreview() {
    TaskioTheme {
        CalendarGrid(
            yearMonth = YearMonth.of(2026, 1),
            selectedDate = LocalDate.of(2026, 1, 25),
            datesWithLogs = setOf(
                LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 1, 21),
                LocalDate.of(2026, 1, 23),
                LocalDate.of(2026, 1, 25)
            ),
            onDateSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarGridCurrentMonthPreview() {
    TaskioTheme {
        CalendarGrid(
            yearMonth = YearMonth.now(),
            selectedDate = LocalDate.now(),
            datesWithLogs = setOf(
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(3)
            ),
            onDateSelected = {}
        )
    }
}