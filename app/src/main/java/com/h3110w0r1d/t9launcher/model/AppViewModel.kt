package com.h3110w0r1d.t9launcher.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.h3110w0r1d.t9launcher.di.AppRepository
import com.h3110w0r1d.t9launcher.utils.PinyinUtil
import com.h3110w0r1d.t9launcher.vo.AppInfo
import com.h3110w0r1d.t9launcher.vo.AppInfo.SortByMatchRate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Locale
import javax.inject.Inject

data class AppConfig(
    val hideSystemAppEnabled: Boolean = false,
    val hiddenComponentIds: Set<String> = emptySet(),
    val englishFuzzyMatchEnabled: Boolean = false,
    val nightModeFollowSystem: Boolean = true,
    val nightModeEnabled: Boolean = false,
    val highlightSearchResultEnabled: Boolean = false,
    val isUseSystemColor: Boolean = true,
    val themeColor: String = "blue",
    // 应用列表样式配置
    val iconSize: Float = 50f,
    val iconHorizonPadding: Float = 10f,
    val iconVerticalPadding: Float = 10f,
    val iconCornerRadius: Int = 26,
    val rowSpacing: Float = 10f,
    val gridColumns: Int = 5,
    val appListHeight: Float = 210f,
    val appNameSize: Float = 12f,
    // 键盘样式配置
    val keyboardButtonHeight: Float = 60f,
    val keyboardWidth: Float = .8f,
    val keyboardBottomPadding: Float = 10f,
    val keyboardQSIconSize: Float = 48f,
    val keyboardQSIconAlpha: Float = 0.5f,
    // 引导界面是否展示过
    val isShowedOnboarding: Boolean = false,
    val shortcutConfig: ArrayList<String> = arrayListOf("", "", "", "", "", "", "", "", ""),
    // 配置是否初始化完成
    // 只有读取配置后，isConfigInitialized才会是true
    val isConfigInitialized: Boolean = false,
)

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        private val appRepository: AppRepository,
        private val dataStore: DataStore<Preferences>,
        private val pinyinUtil: PinyinUtil,
    ) : ViewModel() {
        private var searchText: String = ""
        private var isLoadingAppList: Boolean = false
        private var isAppListListenerStarted: Boolean = false
        private val hideSystemAppEnabledKey = booleanPreferencesKey("is_hide_system_app")
        private val nightModeFollowSystemKey = booleanPreferencesKey("night_mode_follow_system")
        private val nightModeEnabledKey = booleanPreferencesKey("night_mode_enabled")
        private val isHighlightSearchResultKey = booleanPreferencesKey("is_highlight_search_result")
        private val isUseSystemColorKey = booleanPreferencesKey("is_use_system_color")
        private val themeColorKey = stringPreferencesKey("theme_color")
        private val hiddenComponentIdKey = stringSetPreferencesKey("hidden_class_names")
        private val iconSizeKey = floatPreferencesKey("icon_size")
        private val iconHorizonPaddingKey = floatPreferencesKey("icon_horizon_padding")
        private val iconVerticalPaddingKey = floatPreferencesKey("icon_vertical_padding")
        private val iconCornerRadiusKey = intPreferencesKey("icon_corner_radius")
        private val rowSpacingKey = floatPreferencesKey("row_spacing")
        private val gridColumnsKey = intPreferencesKey("grid_columns")
        private val appListHeightKey = floatPreferencesKey("app_list_height")
        private val keyboardButtonHeightKey = floatPreferencesKey("keyboard_button_height")
        private val keyboardWidthKey = floatPreferencesKey("keyboard_width")
        private val keyboardBottomPaddingKey = floatPreferencesKey("keyboard_bottom_padding")
        private val keyboardQSIconSizeKey = floatPreferencesKey("keyboard_qs_icon_size")
        private val keyboardQSIconAlphaKey = floatPreferencesKey("keyboard_qs_icon_alpha")
        private val appNameSizeKey = floatPreferencesKey("app_name_size")
        private val isShowedOnboardingKey = booleanPreferencesKey("is_showed_onboarding")
        private val shortcutConfigKey = stringPreferencesKey("shortcut_config")
        private val isConfigInitializedKey = booleanPreferencesKey("is_config_initialized")
        private val englishFuzzyMatchKey = booleanPreferencesKey("english_fuzzy_match")

        val appConfig: StateFlow<AppConfig> =
            dataStore.data
                .map { preferences ->
                    AppConfig(
                        hideSystemAppEnabled = preferences[hideSystemAppEnabledKey] ?: false,
                        hiddenComponentIds = preferences[hiddenComponentIdKey] ?: emptySet(),
                        nightModeFollowSystem = preferences[nightModeFollowSystemKey] ?: true,
                        nightModeEnabled = preferences[nightModeEnabledKey] ?: false,
                        highlightSearchResultEnabled = preferences[isHighlightSearchResultKey] ?: false,
                        englishFuzzyMatchEnabled = preferences[englishFuzzyMatchKey] ?: false,
                        isUseSystemColor = preferences[isUseSystemColorKey] ?: true,
                        themeColor = preferences[themeColorKey] ?: "blue",
                        // 应用列表样式配置
                        iconSize = preferences[iconSizeKey] ?: 50f,
                        iconHorizonPadding = preferences[iconHorizonPaddingKey] ?: 10f,
                        iconVerticalPadding = preferences[iconVerticalPaddingKey] ?: 10f,
                        rowSpacing = preferences[rowSpacingKey] ?: 10f,
                        gridColumns = preferences[gridColumnsKey] ?: 5,
                        appListHeight = preferences[appListHeightKey] ?: 210f,
                        appNameSize = preferences[appNameSizeKey] ?: 12f,
                        iconCornerRadius = preferences[iconCornerRadiusKey] ?: 26,
                        // 键盘样式配置
                        keyboardButtonHeight = preferences[keyboardButtonHeightKey] ?: 60f,
                        keyboardWidth = preferences[keyboardWidthKey] ?: .8f,
                        keyboardBottomPadding = preferences[keyboardBottomPaddingKey] ?: 10f,
                        keyboardQSIconSize = preferences[keyboardQSIconSizeKey] ?: 48f,
                        keyboardQSIconAlpha = preferences[keyboardQSIconAlphaKey] ?: 0.5f,
                        // 引导界面是否展示过
                        isShowedOnboarding = preferences[isShowedOnboardingKey] ?: false,
                        shortcutConfig =
                            Json.decodeFromString<ArrayList<String>>(
                                preferences[shortcutConfigKey]
                                    ?: "[\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"]",
                            ),
                        isConfigInitialized = preferences[isConfigInitializedKey] ?: true,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = AppConfig(),
                )

        val appMap: StateFlow<HashMap<String, AppInfo>> = appRepository.appMap
        private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
        val isLoading: StateFlow<Boolean> = _isLoading
        private val _isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing
        private val _searchResultAppList: MutableStateFlow<ArrayList<AppInfo>> = MutableStateFlow(ArrayList())
        val searchResultAppList: StateFlow<ArrayList<AppInfo>> = _searchResultAppList

        private val _hideAppList: MutableStateFlow<ArrayList<AppInfo>> =
            MutableStateFlow(ArrayList())
        val hideAppList: StateFlow<ArrayList<AppInfo>> = _hideAppList

        fun switchAppHide(app: AppInfo) {
            val config = appConfig.value
            if (isAppHide(app)) {
                config.hiddenComponentIds.toMutableSet().apply {
                    remove(app.componentId())
                    viewModelScope.launch {
                        dataStore.edit { preferences ->
                            preferences[hiddenComponentIdKey] = this@apply
                        }
                    }
                }
            } else {
                config.hiddenComponentIds.toMutableSet().apply {
                    add(app.componentId())
                    viewModelScope.launch {
                        dataStore.edit { preferences ->
                            preferences[hiddenComponentIdKey] = this@apply
                        }
                    }
                }
            }
        }

        fun setLoadingStatus(loading: Boolean) {
            _isLoading.value = loading
        }

        /**
         * 启动应用列表监听器，当应用列表更新时自动刷新搜索结果
         */
        private fun startAppListListener() {
            if (isAppListListenerStarted) return

            isAppListListenerStarted = true
            viewModelScope.launch {
                appRepository.appList.collect { appList ->
                    if (appList.isNotEmpty()) {
                        setLoadingStatus(false)
                        searchApp(null)
                    }
                }
            }
        }

        fun setIsHideSystemApp(isHide: Boolean) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[hideSystemAppEnabledKey] = isHide
                }
            }
        }

        fun setIsHighlightSearchResult(isHighlight: Boolean) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[isHighlightSearchResultKey] = isHighlight
                }
            }
        }

        fun setNightModeFollowSystem(follow: Boolean) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[nightModeFollowSystemKey] = follow
                }
            }
        }

        fun setNightModeEnabled(enabled: Boolean) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[nightModeEnabledKey] = enabled
                }
            }
        }

        fun setIsUseSystemColor(isUse: Boolean) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[isUseSystemColorKey] = isUse
                }
            }
        }

        fun setThemeColor(color: String) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[themeColorKey] = color
                }
            }
        }

        fun setEnglishFuzzyMatch(englishFuzzyMatch: Boolean) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[englishFuzzyMatchKey] = englishFuzzyMatch
                }
            }
        }

        fun setQuickStartApp(
            index: Int,
            componentId: String,
        ) {
            val config = appConfig.value
            val newQuickStartConfig = config.shortcutConfig.toMutableList()
            newQuickStartConfig[index] = componentId
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[shortcutConfigKey] = Json.encodeToString(newQuickStartConfig)
                }
            }
        }

        /**
         * 加载并更新应用列表
         */
        fun loadAppList() {
            if (isLoadingAppList || !isLoading.value) {
                return
            }

            // 启动应用列表监听器
            startAppListListener()

            viewModelScope.launch {
                isLoadingAppList = true
                appRepository.loadAllApps()

                // 检查是否有数据
                val currentAppList = appRepository.appList.value
                if (currentAppList.isNotEmpty()) {
                    setLoadingStatus(false)
                    searchApp(null)
                }

                appRepository.updateAppInfo(false)
                setLoadingStatus(false)
                searchApp(null)
                isLoadingAppList = false
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                withContext(Dispatchers.IO) {
                    appRepository.updateAppInfo(true)
                }
                searchApp(null)
                _isRefreshing.value = false
            }
        }

        fun showDefaultAppList() {
            val appInfo = ArrayList<AppInfo>()
            val currentAppList = appRepository.appList.value
            val config = appConfig.value

            for (app in currentAppList) {
                if (config.hideSystemAppEnabled && app.isSystemApp) {
                    continue
                }
                if (isAppHide(app)) {
                    continue
                }
                app.setMatchRange(-1, -1)
                appInfo.add(app)
            }
            _searchResultAppList.value = appInfo
        }

        fun searchApp(key: String?): Boolean {
            var key = key
            if (key == null) {
                key = searchText
            }
            if (key.isEmpty()) {
                showDefaultAppList()
                searchText = key
                return true
            }
            val appInfo = ArrayList<AppInfo>()
            val currentAppList = appRepository.appList.value
            val config = appConfig.value

            for (app in currentAppList) {
                if (config.hideSystemAppEnabled && app.isSystemApp) {
                    continue
                }
                if (config.hiddenComponentIds.contains(app.componentId())) {
                    continue
                }
                val matchRate = pinyinUtil.search(app, key, config.englishFuzzyMatchEnabled) // 匹配度
                if (matchRate > 0) {
                    app.matchRate = matchRate
                    appInfo.add(app)
                }
            }
            if (appInfo.isEmpty()) {
                return false
            }
            appInfo.sortWith(SortByMatchRate())
            _searchResultAppList.value = appInfo
            searchText = key
            return true
        }

        fun updateAppListStyle(newAppConfig: AppConfig) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[iconSizeKey] = newAppConfig.iconSize
                    preferences[iconHorizonPaddingKey] = newAppConfig.iconHorizonPadding
                    preferences[iconVerticalPaddingKey] = newAppConfig.iconVerticalPadding
                    preferences[rowSpacingKey] = newAppConfig.rowSpacing
                    preferences[gridColumnsKey] = newAppConfig.gridColumns
                    preferences[appListHeightKey] = newAppConfig.appListHeight
                    preferences[appNameSizeKey] = newAppConfig.appNameSize
                    preferences[iconCornerRadiusKey] = newAppConfig.iconCornerRadius
                }
            }
        }

        fun updateKeyboardStyle(newAppConfig: AppConfig) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[keyboardButtonHeightKey] = newAppConfig.keyboardButtonHeight
                    preferences[keyboardWidthKey] = newAppConfig.keyboardWidth
                    preferences[keyboardBottomPaddingKey] = newAppConfig.keyboardBottomPadding
                    preferences[keyboardQSIconSizeKey] = newAppConfig.keyboardQSIconSize
                    preferences[keyboardQSIconAlphaKey] = newAppConfig.keyboardQSIconAlpha
                }
            }
        }

        fun setShowedOnboarding() {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[isShowedOnboardingKey] = true
                }
            }
        }

        fun searchHideApp(key: String) {
            var key = key
            key = key.lowercase(Locale.getDefault())
            val appInfo = ArrayList<AppInfo>()
            val currentAppList = appRepository.appList.value
            val config = appConfig.value

            for (app in currentAppList) {
                if (config.hideSystemAppEnabled && app.isSystemApp) {
                    continue
                }
                if (app.packageName
                        .contains(key) ||
                    app.appName
                        .lowercase(
                            Locale.getDefault(),
                        ).contains(key)
                ) {
                    appInfo.add(app)
                }
            }
            appInfo.sortWith { o1, o2 ->
                (if (isAppHide(o2)) 1 else 0) - (if (isAppHide(o1)) 1 else 0)
            }
            _hideAppList.value = appInfo
        }

        private fun isAppHide(app: AppInfo): Boolean {
            val config = appConfig.value
            return config.hiddenComponentIds.contains(app.componentId())
        }

        fun updateStartCount(app: AppInfo) {
            app.startCount += 1
            appRepository.updateStartCount(app)
        }

        fun showHideApp() {
            val appInfo = ArrayList<AppInfo>()
            val currentAppList = appRepository.appList.value
            val config = appConfig.value

            for (app in currentAppList) {
                if (isAppHide(app) || (config.hideSystemAppEnabled && app.isSystemApp)) {
                    appInfo.add(app)
                    app.setMatchRange(-1, -1)
                }
            }
            _searchResultAppList.value = appInfo
        }
    }
