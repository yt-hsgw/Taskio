package com.yt_hsgw.taskio.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Taskioアプリ全体で使用するカラーパレット
 */
object TaskioColors {
    // Primary Colors
    val Primary = Color(0xFF4CAF50)
    val PrimaryVariant = Color(0xFF388E3C)
    val Secondary = Color(0xFF2196F3)
    val SecondaryVariant = Color(0xFF1976D2)
    
    // Background Colors
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val CardBackground = Color(0xFFFFFFFF)
    
    // Text Colors
    val TextPrimary = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF757575)
    val TextDisabled = Color(0xFFBDBDBD)
    
    // Status Colors
    val StatusInProgress = Color(0xFFE3F2FD)  // 薄い青
    val StatusCompleted = Color(0xFFE8F5E9)   // 薄い緑
    val StatusDefault = Surface
    
    // Day of Week Colors
    val Sunday = Color(0xFFE57373)      // 赤
    val Saturday = Color(0xFF64B5F6)    // 青
    val Weekday = Primary               // 緑
    val DayUnselected = Color(0xFFE8E8E8)
    
    // Button Colors
    val ButtonDisabledBackground = Color(0x14808080)  // Gray with 8% alpha
    val ButtonInactiveBackground = Color(0x1F808080)  // Gray with 12% alpha
    val ButtonDisabledContent = Color(0x59808080)     // Gray with 35% alpha
}

/**
 * Taskioアプリ全体で使用するサイズ定義
 */
object TaskioDimens {
    // Padding
    val PaddingXSmall = 4.dp
    val PaddingSmall = 8.dp
    val PaddingMedium = 12.dp
    val PaddingLarge = 16.dp
    val PaddingXLarge = 20.dp
    
    // Corner Radius
    val CornerRadiusSmall = 8.dp
    val CornerRadiusMedium = 12.dp
    val CornerRadiusLarge = 16.dp
    val CornerRadiusXLarge = 20.dp
    
    // Icon Sizes
    val IconSizeSmall = 14.dp
    val IconSizeMedium = 16.dp
    val IconSizeLarge = 20.dp
    
    // Component Sizes
    val StatusIndicatorSize = 10.dp
    val ActionButtonWidth = 72.dp
    val DayOfWeekCircleSize = 40.dp
    val DayOfWeekCircleSizeSmall = 36.dp
    val CalendarDayPaddingHorizontal = 12.dp
    val CalendarDayPaddingVertical = 8.dp
    
    // Elevation
    val ElevationNone = 0.dp
    val ElevationSmall = 1.dp
    val ElevationMedium = 2.dp
    val ElevationLarge = 4.dp
}

/**
 * Taskioアプリ全体で使用するフォントサイズ定義
 */
object TaskioTypography {
    val FontSizeXSmall = 10.sp
    val FontSizeSmall = 12.sp
    val FontSizeMedium = 13.sp
    val FontSizeDefault = 14.sp
    val FontSizeLarge = 15.sp
    val FontSizeXLarge = 16.sp
    val FontSizeTitle = 18.sp
    val FontSizeHeadline = 20.sp
    val FontSizeDisplay = 24.sp
}
