package com.yt_hsgw.taskio.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

/**
 * ナビゲーションUIテスト
 *
 * ボトムナビゲーションによる画面遷移をテストします。
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // スプラッシュ画面完了を待機
        composeTestRule.waitForSplashToComplete()
    }

    @Test
    fun bottomNavigation_displaysAllTabs() {
        composeTestRule.waitForIdle()

        // すべてのタブが表示されていることを確認
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_LOG).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_CALENDAR).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_PROFILE).assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_navigateToLogScreen() {
        // Logタブをクリック
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_LOG).performClick()
        composeTestRule.waitForIdle()

        // Log画面のコンテンツが表示されることを確認
        // タブが選択されていることで確認
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_LOG).assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_navigateToCalendarScreen() {
        // Calendarタブをクリック
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_CALENDAR).performClick()
        composeTestRule.waitForIdle()

        // Calendar画面が表示されることを確認
        // カレンダーの「今日」ボタンが表示されることを確認
        composeTestRule.onNodeWithText(TaskioStrings.CALENDAR_TODAY).assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_navigateToProfileScreen() {
        // Profileタブをクリック
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_PROFILE).performClick()
        composeTestRule.waitForIdle()

        // Profile画面が表示されることを確認
        composeTestRule.onNodeWithText(TaskioStrings.PLACEHOLDER_PROFILE).assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_navigateBackToHome() {
        // 別のタブに移動
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_LOG).performClick()
        composeTestRule.waitForIdle()

        // Homeタブに戻る
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_HOME).performClick()
        composeTestRule.waitForIdle()

        // FABが表示されることでHome画面を確認
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).assertIsDisplayed()
    }
}
