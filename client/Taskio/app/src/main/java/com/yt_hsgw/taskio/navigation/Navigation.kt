package com.yt_hsgw.taskio.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * アプリの画面定義
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // スプラッシュ画面（ナビゲーション非表示）
    object Splash : Screen("splash", "Splash")

    object Home : Screen("home", "Home", Icons.Default.Task)
    object Log : Screen("log", "Log", Icons.Default.List)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

/**
 * Bottom Navigation に表示する画面のリスト
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Log,
    Screen.Calendar,
    Screen.Profile
)