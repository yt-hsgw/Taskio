package com.yt_hsgw.taskio.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import com.yt_hsgw.taskio.ui.TaskioStrings
import com.yt_hsgw.taskio.ui.theme.TaskioDimens
import kotlinx.coroutines.delay

/**
 * スプラッシュ画面
 *
 * アプリ起動時に表示されるブランディング画面です。
 * ロゴのスケールアニメーションを表示した後、メイン画面へ遷移します。
 *
 * @param onTimeout スプラッシュ表示完了後に呼び出されるコールバック
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(INITIAL_SCALE) }

    LaunchedEffect(Unit) {
        // ロゴのスケールアニメーション
        scale.animateTo(
            targetValue = TARGET_SCALE,
            animationSpec = tween(
                durationMillis = ANIMATION_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
        // 指定時間表示後にメイン画面へ
        delay(SPLASH_DISPLAY_DURATION_MS)
        onTimeout()
    }

    SplashContent(scale = scale.value)
}

/**
 * スプラッシュ画面のコンテンツ
 *
 * @param scale 現在のスケール値
 */
@Composable
private fun SplashContent(scale: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TaskioDimens.PaddingLarge),
            modifier = Modifier.scale(scale)
        ) {
            // アプリ名
            Text(
                text = TaskioStrings.APP_NAME,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // タグライン
            Text(
                text = TaskioStrings.SPLASH_TAGLINE,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(TaskioDimens.PaddingLarge * 2))

            // ローディングインジケーター
            CircularProgressIndicator(
                modifier = Modifier.size(PROGRESS_INDICATOR_SIZE),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ─────────────────────────────
// Constants
// ─────────────────────────────

/** 初期スケール値 */
private const val INITIAL_SCALE = 0f

/** 最終スケール値 */
private const val TARGET_SCALE = 1f

/** アニメーション時間（ミリ秒） */
private const val ANIMATION_DURATION_MS = 500

/** スプラッシュ表示時間（ミリ秒） */
private const val SPLASH_DISPLAY_DURATION_MS = 2000L

/** プログレスインジケーターのサイズ */
private val PROGRESS_INDICATOR_SIZE = TaskioDimens.PaddingLarge * 2
