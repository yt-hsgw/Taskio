package com.yt_hsgw.taskio.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yt_hsgw.taskio.model.TaskItem
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioTheme
import com.yt_hsgw.taskio.viewmodel.TaskDayState
import com.yt_hsgw.taskio.viewmodel.TaskWithDayState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TaskCard コンポーネントテスト
 */
@RunWith(AndroidJUnit4::class)
class TaskCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTask(
        id: String = "test-id",
        title: String = "テストタスク",
        description: String? = "説明文",
        isStarted: Boolean = false,
        isFinished: Boolean = false,
        isRecurring: Boolean = false
    ) = TaskWithDayState(
        task = TaskItem(
            id = id,
            title = title,
            description = description,
            createdAt = "2025-01-01T00:00:00Z",
            isRecurring = isRecurring
        ),
        dayState = TaskDayState(
            isStarted = isStarted,
            isFinished = isFinished
        )
    )

    @Test
    fun taskCard_displaysTitle() {
        val task = createTask(title = "買い物に行く")

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = {},
                    onFinishClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("買い物に行く").assertIsDisplayed()
    }

    @Test
    fun taskCard_displaysDescription() {
        val task = createTask(description = "スーパーで野菜を買う")

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = {},
                    onFinishClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("スーパーで野菜を買う").assertIsDisplayed()
    }

    @Test
    fun taskCard_displaysRecurringIcon_whenRecurring() {
        val task = createTask(isRecurring = true)

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = {},
                    onFinishClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(TaskioStrings.CD_RECURRING).assertIsDisplayed()
    }

    @Test
    fun taskCard_hidesRecurringIcon_whenNotRecurring() {
        val task = createTask(isRecurring = false)

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = {},
                    onFinishClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(TaskioStrings.CD_RECURRING).assertDoesNotExist()
    }

    @Test
    fun taskCard_displaysStartAndEndButtons() {
        val task = createTask()

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = {},
                    onFinishClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(TaskioStrings.ACTION_START).assertIsDisplayed()
        composeTestRule.onNodeWithText(TaskioStrings.ACTION_END).assertIsDisplayed()
    }

    @Test
    fun taskCard_startButtonClickTriggersCallback() {
        var clicked = false
        val task = createTask()

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = { clicked = true },
                    onFinishClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(TaskioStrings.ACTION_START).performClick()
        assertTrue(clicked)
    }

    @Test
    fun taskCard_endButtonDisabled_whenNotStarted() {
        var endClicked = false
        val task = createTask(isStarted = false)

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = {},
                    onFinishClick = { endClicked = true }
                )
            }
        }

        // 終了ボタンをクリック
        composeTestRule.onNodeWithText(TaskioStrings.ACTION_END).performClick()
        // 開始前は終了ボタンが無効なのでコールバックは呼ばれない
        assertTrue("End button should be disabled when not started", !endClicked)
    }

    @Test
    fun taskCard_endButtonClickTriggersCallback_whenStarted() {
        var clicked = false
        val task = createTask(isStarted = true)

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = {},
                    onFinishClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText(TaskioStrings.ACTION_END).performClick()
        assertTrue(clicked)
    }

    @Test
    fun taskCard_startButtonDisabled_whenFinished() {
        var startClicked = false
        val task = createTask(isStarted = true, isFinished = true)

        composeTestRule.setContent {
            TaskioTheme {
                TaskCard(
                    task = task,
                    onStartClick = { startClicked = true },
                    onFinishClick = {}
                )
            }
        }

        // 開始ボタンをクリック
        composeTestRule.onNodeWithText(TaskioStrings.ACTION_START).performClick()
        // 完了後は開始ボタンが無効なのでコールバックは呼ばれない
        assertTrue("Start button should be disabled when finished", !startClicked)
    }
}
