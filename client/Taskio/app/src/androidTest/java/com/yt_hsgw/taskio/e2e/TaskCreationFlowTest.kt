package com.yt_hsgw.taskio.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yt_hsgw.taskio.MainActivity
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.util.TestUtils.waitForSplashToComplete
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * タスク作成フローのE2Eテスト
 *
 * ユーザーがタスクを作成する一連の操作をテストします。
 */
@RunWith(AndroidJUnit4::class)
class TaskCreationFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // スプラッシュ画面完了を待機
        composeTestRule.waitForSplashToComplete()
    }

    @Test
    fun createTask_basicFlow() {
        // 1. FABをクリックしてダイアログを開く
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).performClick()
        composeTestRule.waitForIdle()

        // 2. ダイアログが表示されることを確認
        composeTestRule.onNodeWithText(TaskioStrings.TASK_NEW).assertIsDisplayed()

        // 3. タイトルを入力
        composeTestRule.onNodeWithText(TaskioStrings.TASK_TITLE_REQUIRED).performTextInput("E2Eテストタスク")
        composeTestRule.waitForIdle()

        // 4. 説明を入力
        composeTestRule.onNodeWithText(TaskioStrings.TASK_DESCRIPTION_OPTIONAL).performTextInput("これはE2Eテスト用のタスクです")
        composeTestRule.waitForIdle()

        // 5. 作成ボタンが表示されていることを確認
        composeTestRule.onNodeWithText(TaskioStrings.BUTTON_CREATE).assertIsDisplayed()

        // Note: 実際の作成はAPIが必要なため、ここではUIの確認のみ
    }

    @Test
    fun createTask_recurringTaskFlow() {
        // 1. FABをクリック
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).performClick()
        composeTestRule.waitForIdle()

        // 2. タイトルを入力
        composeTestRule.onNodeWithText(TaskioStrings.TASK_TITLE_REQUIRED).performTextInput("毎日のジョギング")

        // 3. 繰り返しスイッチをオン（Switchを直接クリック）
        composeTestRule.onNode(isToggleable()).performClick()
        composeTestRule.waitForIdle()

        // 4. 曜日選択が表示されることを確認
        composeTestRule.onNodeWithText(TaskioStrings.TASK_SELECT_RECURRING_DAYS).assertIsDisplayed()

        // 5. 曜日を選択（月曜日）
        // 週カレンダーにも「月」があるため、ダイアログ内の曜日選択ボタン（2番目のノード）を選択
        composeTestRule.onAllNodesWithText(TaskioStrings.MONDAY_SHORT)[1].performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun createTask_cancelFlow() {
        // 1. FABをクリック
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).performClick()
        composeTestRule.waitForIdle()

        // 2. ダイアログが表示されることを確認
        composeTestRule.onNodeWithText(TaskioStrings.TASK_NEW).assertIsDisplayed()

        // 3. タイトルを入力
        composeTestRule.onNodeWithText(TaskioStrings.TASK_TITLE_REQUIRED).performTextInput("キャンセルテスト")

        // 4. キャンセルボタンをクリック
        composeTestRule.onNodeWithText(TaskioStrings.BUTTON_CANCEL).performClick()
        composeTestRule.waitForIdle()

        // 5. ダイアログが閉じることを確認
        composeTestRule.onNodeWithText(TaskioStrings.TASK_NEW).assertDoesNotExist()
    }

    @Test
    fun createTask_validationCheck() {
        // 1. FABをクリック
        composeTestRule.onNodeWithContentDescription(TaskioStrings.TASK_ADD).performClick()
        composeTestRule.waitForIdle()

        // 2. タイトルを入力せずに作成ボタンをクリック
        composeTestRule.onNodeWithText(TaskioStrings.BUTTON_CREATE).performClick()
        composeTestRule.waitForIdle()

        // 3. ダイアログがまだ表示されていることを確認（バリデーションエラーで閉じない）
        composeTestRule.onNodeWithText(TaskioStrings.TASK_NEW).assertIsDisplayed()
    }
}
