package com.h3110w0r1d.t9launcher.model

import android.text.TextUtils
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.h3110w0r1d.t9launcher.di.AppRepository
import com.h3110w0r1d.t9launcher.utils.Pinyin4jUtil
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
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

data class AppConfig(
    val isHideSystemApp: Boolean = false,
    val hiddenClassNames: Set<String> = emptySet(),
)

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        private val appRepository: AppRepository,
        private val dataStore: DataStore<Preferences>,
    ) : ViewModel() {
        private val collator: Collator = Collator.getInstance(Locale.CHINA)
        private var appList: ArrayList<AppInfo> = arrayListOf()
        private var searchText: String = ""
        private var isLoadingAppList: Boolean = false

        private val isHideSystemAppKey = booleanPreferencesKey("is_hide_system_app")
        private val hiddenClassNamesKey = stringSetPreferencesKey("hidden_class_names")
        val appConfig: StateFlow<AppConfig> =
            dataStore.data
                .map { preferences ->
                    AppConfig(
                        isHideSystemApp = preferences[isHideSystemAppKey] ?: false,
                        hiddenClassNames = preferences[hiddenClassNamesKey] ?: emptySet(),
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

        init {
            Pinyin4jUtil.defaultFormat.caseType = HanyuPinyinCaseType.LOWERCASE
            Pinyin4jUtil.defaultFormat.toneType = HanyuPinyinToneType.WITHOUT_TONE
            Log.d("ViewModelLifecycle", "ViewModel created")
        }

        override fun onCleared() {
            super.onCleared()
            // ViewModel 销毁时调用
            Log.d("MyViewModelLifecycle", "ViewModel cleared")
        }

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
            if (isLoadingAppList) {
                return
            }
            viewModelScope.launch {
                isLoadingAppList = true
                withContext(Dispatchers.IO) {
                    appList = appRepository.getAllApps()
                    appList.sortWith(compareBy(collator) { it.appName })
                    appList.sortWith(SortByStartCount())
                }
                if (!appList.isEmpty()) {
                    setLoadingStatus(false)
                    searchApp(null)
                }

                withContext(Dispatchers.IO) {
                    appList = appRepository.updateAppInfo()
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
                    appList = appRepository.updateAppInfo()
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

        fun searchApp(key: String?) {
            var key = key
            if (key == null) {
                key = searchText
            } else {
                searchText = key
            }
//            if (isShowingHideApp) {
//                showHideApp()
//                return
//            }
            if (TextUtils.isEmpty(key)) {
                showDefaultAppList()
                return
            }
            val appInfo = ArrayList<AppInfo>()
            for (app in appList) {
                if (appConfig.value.isHideSystemApp && app.isSystemApp) {
                    continue
                }
                if (appConfig.value.hiddenClassNames.contains(app.className)) {
                    continue
                }
                val matchRate = Pinyin4jUtil.Search(app, key) // 匹配度
                if (matchRate > 0) {
                    app.matchRate = matchRate
                    appInfo.add(app)
                }
            }
            appInfo.sortWith(SortByMatchRate())
            _searchResultAppList.value = appInfo
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

//    fun saveHideList() {
//        spEditor.remove("hideAppList").commit()
//        spEditor.putStringSet("hideAppList", hideAppList).commit()
//    }

        fun showHideApp() {
//            isShowingHideApp = true
            val appInfo = ArrayList<AppInfo>()
            for (app in appList) {
                if (isAppHide(app) || (appConfig.value.isHideSystemApp && app.isSystemApp)) {
                    appInfo.add(app)
                }
            }
            _searchResultAppList.value = appInfo
        }
    }
