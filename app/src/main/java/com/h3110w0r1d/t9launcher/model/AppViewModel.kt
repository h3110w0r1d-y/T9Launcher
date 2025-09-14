package com.h3110w0r1d.t9launcher.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.h3110w0r1d.t9launcher.di.AppRepository
import com.h3110w0r1d.t9launcher.utils.PinyinUtil
import com.h3110w0r1d.t9launcher.vo.AppInfo
import com.h3110w0r1d.t9launcher.vo.AppInfo.SortByMatchRate
import com.h3110w0r1d.t9launcher.vo.AppInfo.SortByStartCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

data class AppConfig(
    val isHideSystemApp: Boolean = false,
    val hiddenClassNames: Set<String> = emptySet(),
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
)

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        private val appRepository: AppRepository,
        private val dataStore: DataStore<Preferences>,
        private val pinyinUtil: PinyinUtil,
    ) : ViewModel() {
        private val collator: Collator = Collator.getInstance(Locale.CHINA)
        private var appList: ArrayList<AppInfo> = arrayListOf()
        private var searchText: String = ""
        private var isLoadingAppList: Boolean = false

        private val isHideSystemAppKey = booleanPreferencesKey("is_hide_system_app")
        private val hiddenClassNamesKey = stringSetPreferencesKey("hidden_class_names")
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
        private val appNameSizeKey = floatPreferencesKey("app_name_size")

        val appConfig: StateFlow<AppConfig> =
            dataStore.data
                .map { preferences ->
                    AppConfig(
                        isHideSystemApp = preferences[isHideSystemAppKey] ?: false,
                        hiddenClassNames = preferences[hiddenClassNamesKey] ?: emptySet(),
                        iconSize = preferences[iconSizeKey] ?: 50f,
                        iconHorizonPadding = preferences[iconHorizonPaddingKey] ?: 10f,
                        iconVerticalPadding = preferences[iconVerticalPaddingKey] ?: 10f,
                        rowSpacing = preferences[rowSpacingKey] ?: 10f,
                        gridColumns = preferences[gridColumnsKey] ?: 5,
                        appListHeight = preferences[appListHeightKey] ?: 210f,
                        keyboardButtonHeight = preferences[keyboardButtonHeightKey] ?: 60f,
                        keyboardWidth = preferences[keyboardWidthKey] ?: .8f,
                        keyboardBottomPadding = preferences[keyboardBottomPaddingKey] ?: 10f,
                        appNameSize = preferences[appNameSizeKey] ?: 12f,
                        iconCornerRadius = preferences[iconCornerRadiusKey] ?: 26,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = AppConfig(),
                )

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
            if (isAppHide(app)) {
                appConfig.value.hiddenClassNames.toMutableSet().apply {
                    remove(app.className)
                    viewModelScope.launch {
                        dataStore.edit { preferences ->
                            preferences[hiddenClassNamesKey] = this@apply
                        }
                    }
                }
            } else {
                appConfig.value.hiddenClassNames.toMutableSet().apply {
                    add(app.className)
                    viewModelScope.launch {
                        dataStore.edit { preferences ->
                            preferences[hiddenClassNamesKey] = this@apply
                        }
                    }
                }
            }
        }

        fun setLoadingStatus(loading: Boolean) {
            _isLoading.value = loading
        }

        fun setIsHideSystemApp(isHide: Boolean) {
            viewModelScope.launch {
                dataStore.edit { preferences ->
                    preferences[isHideSystemAppKey] = isHide
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
            viewModelScope.launch {
                isLoadingAppList = true
                withContext(Dispatchers.IO) {
                    appList = appRepository.loadAllApps()
                    appList.sortWith(compareBy(collator) { it.appName })
                    appList.sortWith(SortByStartCount())
                }
                if (!appList.isEmpty()) {
                    setLoadingStatus(false)
                    searchApp(null)
                }

                withContext(Dispatchers.IO) {
                    appList = appRepository.updateAppInfo(false)
                    appList.sortWith(compareBy(collator) { it.appName })
                    appList.sortWith(SortByStartCount())
                }
                setLoadingStatus(false)
                searchApp(null)
                isLoadingAppList = false
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                withContext(Dispatchers.IO) {
                    appList = appRepository.updateAppInfo(true)
                    appList.sortWith(compareBy(collator) { it.appName })
                    appList.sortWith(SortByStartCount())
                }
                searchApp(null)
                _isRefreshing.value = false
            }
        }

        fun showDefaultAppList() {
            val appInfo = ArrayList<AppInfo>()
            for (app in appList) {
                if (appConfig.value.isHideSystemApp && app.isSystemApp) {
                    continue
                }
                if (isAppHide(app)) {
                    continue
                }
                appInfo.add(app)
            }
            appList.sortWith(SortByStartCount())
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
            for (app in appList) {
                if (appConfig.value.isHideSystemApp && app.isSystemApp) {
                    continue
                }
                if (appConfig.value.hiddenClassNames.contains(app.className)) {
                    continue
                }
                val matchRate = pinyinUtil.Search(app, key) // 匹配度
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
                }
            }
        }

        fun searchHideApp(key: String) {
            var key = key
            key = key.lowercase(Locale.getDefault())
            val appInfo = ArrayList<AppInfo>()
            for (app in appList) {
                if (appConfig.value.isHideSystemApp && app.isSystemApp) {
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

        private fun isAppHide(app: AppInfo): Boolean = appConfig.value.hiddenClassNames.contains(app.className)

        fun updateStartCount(app: AppInfo) {
            app.startCount += 1
            appRepository.updateStartCount(app)
        }

        fun showHideApp() {
            val appInfo = ArrayList<AppInfo>()
            for (app in appList) {
                if (isAppHide(app) || (appConfig.value.isHideSystemApp && app.isSystemApp)) {
                    appInfo.add(app)
                }
            }
            _searchResultAppList.value = appInfo
        }
    }
