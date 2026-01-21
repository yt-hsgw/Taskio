package com.yt_hsgw.taskio.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector
import com.yt_hsgw.taskio.ui.TaskioStrings

/**
 * アプリの画面定義
 *
 * Navigation Composeで使用する画面ルートを定義します。
 *
 * @property route ナビゲーションルート
 * @property title 画面タイトル
 * @property icon アイコン（Bottom Navigationで使用）
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    /**
     * スプラッシュ画面
     *
     * アプリ起動時に表示されるブランディング画面。
     * ナビゲーションバーには表示されない。
     */
    data object Splash : Screen(
        route = ROUTE_SPLASH,
        title = TITLE_SPLASH
    )

    /**
     * ホーム画面
     *
     * タスク一覧と週間カレンダーを表示するメイン画面。
     */
    data object Home : Screen(
        route = ROUTE_HOME,
        title = TaskioStrings.SCREEN_HOME,
        icon = Icons.Default.Task
    )

    /**
     * ログ画面
     *
     * タスクの実行ログを一覧表示する画面。
     */
    data object Log : Screen(
        route = ROUTE_LOG,
        title = TaskioStrings.SCREEN_LOG,
        icon = Icons.Default.List
    )

    /**
     * カレンダー画面
     *
     * タスク実行履歴をカレンダー形式で表示する画面。
     */
    data object Calendar : Screen(
        route = ROUTE_CALENDAR,
        title = TaskioStrings.SCREEN_CALENDAR,
        icon = Icons.Default.CalendarMonth
    )

    /**
     * プロフィール画面
     *
     * ユーザー設定とアプリ設定を管理する画面。
     */
    data object Profile : Screen(
        route = ROUTE_PROFILE,
        title = TaskioStrings.SCREEN_PROFILE,
        icon = Icons.Default.Person
    )

    companion object {
        // Route定数
        private const val ROUTE_SPLASH = "splash"
        private const val ROUTE_HOME = "home"
        private const val ROUTE_LOG = "log"
        private const val ROUTE_CALENDAR = "calendar"
        private const val ROUTE_PROFILE = "profile"

        // Title定数
        private const val TITLE_SPLASH = "Splash"
    }
}

/**
 * Bottom Navigationに表示する画面のリスト
 *
 * スプラッシュ画面は含まれない。
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Log,
    Screen.Calendar,
    Screen.Profile
)
