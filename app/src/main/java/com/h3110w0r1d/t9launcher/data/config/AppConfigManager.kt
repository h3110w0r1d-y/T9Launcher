package com.h3110w0r1d.t9launcher.data.config

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json

/**
 * 配置键管理
 */
private object ConfigKeys {
    // 应用列表样式键
    val iconSize = floatPreferencesKey("icon_size")
    val iconHorizonPadding = floatPreferencesKey("icon_horizon_padding")
    val iconVerticalPadding = floatPreferencesKey("icon_vertical_padding")
    val iconCornerRadius = intPreferencesKey("icon_corner_radius")
    val rowSpacing = floatPreferencesKey("row_spacing")
    val gridColumns = intPreferencesKey("grid_columns")
    val appListHeight = floatPreferencesKey("app_list_height")
    val appNameSize = floatPreferencesKey("app_name_size")

    // 键盘样式键
    val keyboardButtonHeight = floatPreferencesKey("keyboard_button_height")
    val keyboardWidth = floatPreferencesKey("keyboard_width")
    val keyboardBottomPadding = floatPreferencesKey("keyboard_bottom_padding")
    val keyboardQSIconSize = floatPreferencesKey("keyboard_qs_icon_size")
    val keyboardQSIconAlpha = floatPreferencesKey("keyboard_qs_icon_alpha")

    // 主题键
    val isUseSystemColor = booleanPreferencesKey("is_use_system_color")
    val themeColor = stringPreferencesKey("theme_color")
    val nightModeFollowSystem = booleanPreferencesKey("night_mode_follow_system")
    val nightModeEnabled = booleanPreferencesKey("night_mode_enabled")
    val highContrastEnabled = booleanPreferencesKey("high_contrast_enabled")

    // 搜索键
    val hideSystemAppEnabled = booleanPreferencesKey("is_hide_system_app")
    val hiddenComponentIds = stringSetPreferencesKey("hidden_class_names")
    val englishFuzzyMatchEnabled = booleanPreferencesKey("english_fuzzy_match")
    val highlightSearchResultEnabled = booleanPreferencesKey("is_highlight_search_result")

    // 其他键
    val isShowedOnboarding = booleanPreferencesKey("is_showed_onboarding")
    val shortcutConfig = stringPreferencesKey("shortcut_config")
    val isConfigInitialized = booleanPreferencesKey("is_config_initialized")
}

class AppConfigManager(
    private val dataStore: DataStore<Preferences>,
) {
    val appConfig: StateFlow<AppConfig> =
        dataStore.data
            .map { preferences ->
                AppConfig(
                    appListStyle =
                        AppListStyleConfig(
                            iconSize = preferences[ConfigKeys.iconSize] ?: 50f,
                            iconHorizonPadding = preferences[ConfigKeys.iconHorizonPadding] ?: 10f,
                            iconVerticalPadding = preferences[ConfigKeys.iconVerticalPadding] ?: 10f,
                            iconCornerRadius = preferences[ConfigKeys.iconCornerRadius] ?: 26,
                            rowSpacing = preferences[ConfigKeys.rowSpacing] ?: 10f,
                            gridColumns = preferences[ConfigKeys.gridColumns] ?: 5,
                            appListHeight = preferences[ConfigKeys.appListHeight] ?: 210f,
                            appNameSize = preferences[ConfigKeys.appNameSize] ?: 12f,
                        ),
                    keyboardStyle =
                        KeyboardStyleConfig(
                            keyboardButtonHeight = preferences[ConfigKeys.keyboardButtonHeight] ?: 60f,
                            keyboardWidth = preferences[ConfigKeys.keyboardWidth] ?: .8f,
                            keyboardBottomPadding = preferences[ConfigKeys.keyboardBottomPadding] ?: 10f,
                            keyboardQSIconSize = preferences[ConfigKeys.keyboardQSIconSize] ?: 48f,
                            keyboardQSIconAlpha = preferences[ConfigKeys.keyboardQSIconAlpha] ?: 0.5f,
                        ),
                    theme =
                        ThemeConfig(
                            isUseSystemColor = preferences[ConfigKeys.isUseSystemColor] ?: true,
                            themeColor = preferences[ConfigKeys.themeColor] ?: "blue",
                            nightModeFollowSystem = preferences[ConfigKeys.nightModeFollowSystem] ?: true,
                            nightModeEnabled = preferences[ConfigKeys.nightModeEnabled] ?: false,
                            highContrastEnabled = preferences[ConfigKeys.highContrastEnabled] ?: false,
                        ),
                    search =
                        SearchConfig(
                            hideSystemAppEnabled = preferences[ConfigKeys.hideSystemAppEnabled] ?: false,
                            hiddenComponentIds = preferences[ConfigKeys.hiddenComponentIds] ?: emptySet(),
                            englishFuzzyMatchEnabled = preferences[ConfigKeys.englishFuzzyMatchEnabled] ?: false,
                            highlightSearchResultEnabled = preferences[ConfigKeys.highlightSearchResultEnabled] ?: false,
                        ),
                    isShowedOnboarding = preferences[ConfigKeys.isShowedOnboarding] ?: false,
                    shortcutConfig =
                        Json.decodeFromString<ArrayList<String>>(
                            preferences[ConfigKeys.shortcutConfig] ?: "[\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"]",
                        ),
                    isConfigInitialized = preferences[ConfigKeys.isConfigInitialized] ?: true,
                )
            }.stateIn(
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                started = SharingStarted.Eagerly,
                initialValue = AppConfig(),
            )

    suspend fun updateAppListStyle(config: AppListStyleConfig) {
        dataStore.edit { preferences ->
            preferences[ConfigKeys.iconSize] = config.iconSize
            preferences[ConfigKeys.iconHorizonPadding] = config.iconHorizonPadding
            preferences[ConfigKeys.iconVerticalPadding] = config.iconVerticalPadding
            preferences[ConfigKeys.iconCornerRadius] = config.iconCornerRadius
            preferences[ConfigKeys.rowSpacing] = config.rowSpacing
            preferences[ConfigKeys.gridColumns] = config.gridColumns
            preferences[ConfigKeys.appListHeight] = config.appListHeight
            preferences[ConfigKeys.appNameSize] = config.appNameSize
        }
    }

    suspend fun updateKeyboardStyle(config: KeyboardStyleConfig) {
        dataStore.edit { preferences ->
            preferences[ConfigKeys.keyboardButtonHeight] = config.keyboardButtonHeight
            preferences[ConfigKeys.keyboardWidth] = config.keyboardWidth
            preferences[ConfigKeys.keyboardBottomPadding] = config.keyboardBottomPadding
            preferences[ConfigKeys.keyboardQSIconSize] = config.keyboardQSIconSize
            preferences[ConfigKeys.keyboardQSIconAlpha] = config.keyboardQSIconAlpha
        }
    }

    suspend fun updateThemeConfig(config: ThemeConfig) {
        dataStore.edit { preferences ->
            preferences[ConfigKeys.isUseSystemColor] = config.isUseSystemColor
            preferences[ConfigKeys.themeColor] = config.themeColor
            preferences[ConfigKeys.nightModeFollowSystem] = config.nightModeFollowSystem
            preferences[ConfigKeys.nightModeEnabled] = config.nightModeEnabled
            preferences[ConfigKeys.highContrastEnabled] = config.highContrastEnabled
        }
    }

    suspend fun updateSearchConfig(config: SearchConfig) {
        dataStore.edit { preferences ->
            preferences[ConfigKeys.hideSystemAppEnabled] = config.hideSystemAppEnabled
            preferences[ConfigKeys.hiddenComponentIds] = config.hiddenComponentIds
            preferences[ConfigKeys.englishFuzzyMatchEnabled] = config.englishFuzzyMatchEnabled
            preferences[ConfigKeys.highlightSearchResultEnabled] = config.highlightSearchResultEnabled
        }
    }

    suspend fun updateShortcutConfig(shortcutConfig: ArrayList<String>) {
        dataStore.edit { preferences ->
            preferences[ConfigKeys.shortcutConfig] = Json.encodeToString(shortcutConfig)
        }
    }

    suspend fun setShowedOnboarding() {
        dataStore.edit { preferences ->
            preferences[ConfigKeys.isShowedOnboarding] = true
        }
    }
}

val LocalAppConfig =
    staticCompositionLocalOf<AppConfig> {
        error("AppConfig not provided!")
    }
