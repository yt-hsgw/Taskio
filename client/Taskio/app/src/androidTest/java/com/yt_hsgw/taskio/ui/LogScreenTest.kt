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
 * LogScreen UIテスト
 *
 * ログ画面の表示と操作をテストします。
 */
@RunWith(AndroidJUnit4::class)
class LogScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // スプラッシュ画面完了を待機
        composeTestRule.waitForSplashToComplete()
        // ログ画面に遷移
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_LOG).performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun logScreen_isDisplayed() {
        // ログ画面が表示されていることを確認
        // タブが選択されていることで確認
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_LOG).assertIsDisplayed()
    }

    @Test
    fun logScreen_displaysEmptyOrTaskList() {
        // ローディング完了を待つ
        composeTestRule.waitForIdle()
        Thread.sleep(2000) // API呼び出し完了を待つ

        // 空状態メッセージが表示されていることを確認
        // Note: タスクが存在する場合はこのテストは失敗する可能性があるため、
        // 実際のテスト環境ではテストデータの初期化が必要
        composeTestRule.onNodeWithText(TaskioStrings.EMPTY_LOG_MESSAGE).assertExists()
    }

    @Test
    fun logScreen_bottomNavigationStillVisible() {
        // ボトムナビゲーションが表示されていることを確認
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_HOME).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_LOG).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_CALENDAR).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_PROFILE).assertIsDisplayed()
    }

    @Test
    fun logScreen_canNavigateBack() {
        // 他のタブに遷移できることを確認
        composeTestRule.onNodeWithText(TaskioStrings.SCREEN_HOME).performClick()
        composeTestRule.waitForIdle()

        // Home画面のFABが表示されることを確認
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).assertIsDisplayed()
    }
}
