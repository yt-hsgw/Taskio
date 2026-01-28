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
 * HomeScreen UIテスト
 *
 * ホーム画面の表示と操作をテストします。
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // スプラッシュ画面完了を待機
        composeTestRule.waitForSplashToComplete()
    }

    @Test
    fun homeScreen_displaysWeekCalendar() {
        // 曜日が表示されていることを確認
        composeTestRule.onNodeWithText(TaskioStrings.MONDAY_SHORT).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.TUESDAY_SHORT).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.WEDNESDAY_SHORT).assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysTodayHeader() {
        // 「今日」ヘッダーが表示されていることを確認
        // アプリ起動時は今日が選択されているため、「今日」が表示される
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(TaskioStrings.TODAY).assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysFab() {
        // FABが表示されていることを確認
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).assertIsDisplayed()
    }

    @Test
    fun homeScreen_fabOpensCreateDialog() {
        // FABをクリック
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).performClick()

        // ダイアログが表示されることを確認
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(TaskioStrings.TASK_NEW).assertIsDisplayed()
    }

    @Test
    fun homeScreen_createDialogShowsRequiredFields() {
        // FABをクリックしてダイアログを開く
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).performClick()
        composeTestRule.waitForIdle()

        // 必須フィールドが表示されていることを確認
        composeTestRule.onNodeWithText(TaskioStrings.TASK_TITLE_REQUIRED).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.TASK_DESCRIPTION_OPTIONAL).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.TASK_RECURRING).assertIsDisplayed()
    }

    @Test
    fun homeScreen_createDialogCanBeCancelled() {
        // FABをクリックしてダイアログを開く
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).performClick()
        composeTestRule.waitForIdle()

        // キャンセルボタンをクリック
        composeTestRule.onNodeWithText(TaskioStrings.BUTTON_CANCEL).performClick()
        composeTestRule.waitForIdle()

        // ダイアログが閉じることを確認（新しいタスクテキストが非表示）
        composeTestRule.onNodeWithText(TaskioStrings.TASK_NEW).assertDoesNotExist()
    }

    @Test
    fun homeScreen_emptyStateDisplaysMessage() {
        // タスクがない場合、空状態メッセージが表示されることを確認
        // API通信完了を待つ
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // 空状態メッセージが表示されていることを確認
        // Note: タスクが存在する場合はこのテストは失敗する可能性があるため、
        // 実際のテスト環境ではテストデータの初期化が必要
        composeTestRule.onNodeWithText(TaskioStrings.EMPTY_TASKS_MESSAGE).assertExists()
    }
}
