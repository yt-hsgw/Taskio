package com.yt_hsgw.taskio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yt_hsgw.taskio.MainActivity
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.util.TestUtils.waitForSplashToComplete
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * CalendarScreen UIテスト
 *
 * カレンダー画面の表示と操作をテストします。
 */
@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // スプラッシュ画面完了を待機
        composeTestRule.waitForSplashToComplete()
        // カレンダー画面に遷移
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_CALENDAR).performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun calendarScreen_displaysHeader() {
        // 年月が表示されていることを確認
        val currentMonth = YearMonth.now()
        // 実装のフォーマットに合わせる（年と月の間にスペースあり）
        val formatter = DateTimeFormatter.ofPattern("yyyy年 M月", Locale.JAPANESE)
        val expectedText = currentMonth.format(formatter)

        composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()
    }

    @Test
    fun calendarScreen_displaysNavigationButtons() {
        // 前月/次月ボタンが表示されていることを確認
        composeTestRule.onNodeWithContentDescription(TaskioStrings.CALENDAR_PREVIOUS_MONTH).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TaskioStrings.CALENDAR_NEXT_MONTH).assertIsDisplayed()
    }

    @Test
    fun calendarScreen_displaysTodayButton() {
        // 「今日」ボタンが表示されていることを確認
        composeTestRule.onNodeWithText(TaskioStrings.CALENDAR_TODAY).assertIsDisplayed()
    }

    @Test
    fun calendarScreen_displaysLegend() {
        // 凡例が表示されていることを確認
        composeTestRule.onNodeWithText(TaskioStrings.CALENDAR_LEGEND_HAS_LOG).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.CALENDAR_LEGEND_TODAY).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.CALENDAR_LEGEND_SELECTED).assertIsDisplayed()
    }

    @Test
    fun calendarScreen_displaysWeekdayHeaders() {
        // 曜日ヘッダーが表示されていることを確認
        composeTestRule.onNodeWithText(TaskioStrings.SUNDAY_SHORT).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.MONDAY_SHORT).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.SATURDAY_SHORT).assertIsDisplayed()
    }

    @Test
    fun calendarScreen_navigateToNextMonth() {
        val currentMonth = YearMonth.now()
        val nextMonth = currentMonth.plusMonths(1)
        val formatter = DateTimeFormatter.ofPattern("yyyy年 M月", Locale.JAPANESE)

        // 次月ボタンをクリック
        composeTestRule.onNodeWithContentDescription(TaskioStrings.CALENDAR_NEXT_MONTH).performClick()
        composeTestRule.waitForIdle()

        // 次月が表示されることを確認
        composeTestRule.onNodeWithText(nextMonth.format(formatter)).assertIsDisplayed()
    }

    @Test
    fun calendarScreen_navigateToPreviousMonth() {
        val currentMonth = YearMonth.now()
        val prevMonth = currentMonth.minusMonths(1)
        val formatter = DateTimeFormatter.ofPattern("yyyy年 M月", Locale.JAPANESE)

        // 前月ボタンをクリック
        composeTestRule.onNodeWithContentDescription(TaskioStrings.CALENDAR_PREVIOUS_MONTH).performClick()
        composeTestRule.waitForIdle()

        // 前月が表示されることを確認
        composeTestRule.onNodeWithText(prevMonth.format(formatter)).assertIsDisplayed()
    }

    @Test
    fun calendarScreen_todayButtonReturnsToCurrentMonth() {
        val currentMonth = YearMonth.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy年 M月", Locale.JAPANESE)

        // 別の月に移動
        composeTestRule.onNodeWithContentDescription(TaskioStrings.CALENDAR_NEXT_MONTH).performClick()
        composeTestRule.onNodeWithContentDescription(TaskioStrings.CALENDAR_NEXT_MONTH).performClick()
        composeTestRule.waitForIdle()

        // 「今日」ボタンをクリック（複数の「今日」テキストがあるため、最初のノード=ボタンを選択）
        composeTestRule.onAllNodesWithText(TaskioStrings.CALENDAR_TODAY)[0].performClick()
        composeTestRule.waitForIdle()

        // 現在の月に戻ることを確認
        composeTestRule.onNodeWithText(currentMonth.format(formatter)).assertIsDisplayed()
    }
}
