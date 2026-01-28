package com.yt_hsgw.taskio.util

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.yt_hsgw.taskio.MainActivity

/**
 * UIテスト用のユーティリティ関数
 */
object TestUtils {

    /**
     * スプラッシュ画面の完了を待機
     *
     * スプラッシュ画面が終了してメインUIが表示されるまで待機します。
     * スプラッシュは約2.5秒（アニメーション500ms + 表示2000ms）かかります。
     *
     * 注意: "Taskio"テキストはスプラッシュ画面とメイン画面の両方に存在するため、
     * ボトムナビゲーションの"Home"テキストを待機することでメイン画面の表示を確認します。
     */
    fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.waitForSplashToComplete() {
        // ボトムナビゲーションの「Home」が表示されるまで待機（最大10秒）
        // スプラッシュ画面には「Home」がないため、これで確実にメイン画面を検出できる
        waitUntil(timeoutMillis = 10000) {
            try {
                onNodeWithText("Home").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        // 追加の安定化待機
        waitForIdle()
        // UIが完全にレンダリングされるまで少し待機
        Thread.sleep(500)
    }

    /**
     * 指定した要素が表示されるまで待機
     */
    fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.waitForNode(
        text: String,
        timeoutMillis: Long = 5000
    ) {
        waitUntil(timeoutMillis = timeoutMillis) {
            try {
                onNodeWithText(text).assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
