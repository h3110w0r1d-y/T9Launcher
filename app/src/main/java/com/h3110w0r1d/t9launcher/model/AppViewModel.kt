package com.h3110w0r1d.t9launcher.model

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.h3110w0r1d.t9launcher.data.app.AppInfo
import com.h3110w0r1d.t9launcher.data.app.AppInfo.SortByMatchRate
import com.h3110w0r1d.t9launcher.data.app.AppRepository
import com.h3110w0r1d.t9launcher.data.config.AppConfig
import com.h3110w0r1d.t9launcher.data.config.AppConfigManager
import com.h3110w0r1d.t9launcher.data.config.AppListStyleConfig
import com.h3110w0r1d.t9launcher.data.config.KeyboardStyleConfig
import com.h3110w0r1d.t9launcher.data.config.SearchConfig
import com.h3110w0r1d.t9launcher.data.config.ThemeConfig
import com.h3110w0r1d.t9launcher.utils.PinyinUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        private val appRepository: AppRepository,
        private val configManager: AppConfigManager,
        private val pinyinUtil: PinyinUtil,
    ) : ViewModel() {
        private var searchText: String = ""
        private var isLoadingAppList: Boolean = false
        private var isAppListListenerStarted: Boolean = false

        val appConfig: StateFlow<AppConfig> = configManager.appConfig

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
                val newHiddenIds =
                    config.search.hiddenComponentIds.toMutableSet().apply {
                        remove(app.componentId())
                    }
                viewModelScope.launch {
                    configManager.updateSearchConfig(
                        config.search.copy(hiddenComponentIds = newHiddenIds),
                    )
                }
            } else {
                val newHiddenIds =
                    config.search.hiddenComponentIds.toMutableSet().apply {
                        add(app.componentId())
                    }
                viewModelScope.launch {
                    configManager.updateSearchConfig(
                        config.search.copy(hiddenComponentIds = newHiddenIds),
                    )
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

        // 简化的配置更新方法
        fun updateAppListStyle(config: AppListStyleConfig) {
            viewModelScope.launch {
                configManager.updateAppListStyle(config)
            }
        }

        fun updateKeyboardStyle(config: KeyboardStyleConfig) {
            viewModelScope.launch {
                configManager.updateKeyboardStyle(config)
            }
        }

        fun updateThemeConfig(config: ThemeConfig) {
            viewModelScope.launch {
                configManager.updateThemeConfig(config)
            }
        }

        fun updateSearchConfig(config: SearchConfig) {
            viewModelScope.launch {
                configManager.updateSearchConfig(config)
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
                configManager.updateShortcutConfig(ArrayList(newQuickStartConfig))
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
                if (config.search.hideSystemAppEnabled && app.isSystemApp) {
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
                if (config.search.hideSystemAppEnabled && app.isSystemApp) {
                    continue
                }
                if (config.search.hiddenComponentIds.contains(app.componentId())) {
                    continue
                }
                val matchRate = pinyinUtil.search(app, key, config.search.englishFuzzyMatchEnabled) // 匹配度
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

        fun setShowedOnboarding() {
            viewModelScope.launch {
                configManager.setShowedOnboarding()
            }
        }

        fun searchHideApp(key: String) {
            var key = key
            key = key.lowercase(Locale.getDefault())
            val appInfo = ArrayList<AppInfo>()
            val currentAppList = appRepository.appList.value
            val config = appConfig.value

            for (app in currentAppList) {
                if (config.search.hideSystemAppEnabled && app.isSystemApp) {
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
            return config.search.hiddenComponentIds.contains(app.componentId())
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
                if (isAppHide(app) || (config.search.hideSystemAppEnabled && app.isSystemApp)) {
                    appInfo.add(app)
                    app.setMatchRange(-1, -1)
                }
            }
            _searchResultAppList.value = appInfo
        }
    }

val LocalGlobalViewModel =
    staticCompositionLocalOf<AppViewModel> {
        error("AppViewModel not provided!")
    }
