package com.yt_hsgw.taskio.rules

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Coroutinesテスト用のDispatcherルール
 *
 * ViewModelのテストでMain Dispatcherを使用する場合に必要です。
 * このルールにより、viewModelScope内のコルーチンがテスト用のDispatcherで実行されます。
 *
 * 使用例:
 * ```
 * class MyViewModelTest {
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule()
 *
 *     @Test
 *     fun testSomething() = runTest {
 *         // テストコード
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
