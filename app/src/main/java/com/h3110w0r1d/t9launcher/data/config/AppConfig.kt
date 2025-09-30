package com.h3110w0r1d.t9launcher.data.config

/**
 * 应用列表样式配置
 */
data class AppListStyleConfig(
    val iconSize: Float = 50f,
    val iconHorizonPadding: Float = 10f,
    val iconVerticalPadding: Float = 10f,
    val iconCornerRadius: Int = 26,
    val rowSpacing: Float = 10f,
    val gridColumns: Int = 5,
    val appListHeight: Float = 210f,
    val appNameSize: Float = 12f,
)

/**
 * 键盘样式配置
 */
data class KeyboardStyleConfig(
    val keyboardButtonHeight: Float = 60f,
    val keyboardWidth: Float = .8f,
    val keyboardBottomPadding: Float = 10f,
    val keyboardQSIconSize: Float = 48f,
    val keyboardQSIconAlpha: Float = 0.5f,
)

/**
 * 主题配置
 */
data class ThemeConfig(
    val isUseSystemColor: Boolean = true,
    val themeColor: String = "blue",
    val nightModeFollowSystem: Boolean = true,
    val nightModeEnabled: Boolean = false,
    val highContrastEnabled: Boolean = false,
)

/**
 * 搜索配置
 */
data class SearchConfig(
    val hideSystemAppEnabled: Boolean = false,
    val hiddenComponentIds: Set<String> = emptySet(),
    val englishFuzzyMatchEnabled: Boolean = false,
    val highlightSearchResultEnabled: Boolean = false,
)

/**
 * 主配置类
 */
data class AppConfig(
    val appListStyle: AppListStyleConfig = AppListStyleConfig(),
    val keyboardStyle: KeyboardStyleConfig = KeyboardStyleConfig(),
    val theme: ThemeConfig = ThemeConfig(),
    val search: SearchConfig = SearchConfig(),
    val isShowedOnboarding: Boolean = false,
    val shortcutConfig: ArrayList<String> = arrayListOf("", "", "", "", "", "", "", "", ""),
    val isConfigInitialized: Boolean = false,
)
