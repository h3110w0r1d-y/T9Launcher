package com.h3110w0r1d.t9launcher.model

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.h3110w0r1d.t9launcher.App
import com.h3110w0r1d.t9launcher.utils.DBHelper
import com.h3110w0r1d.t9launcher.utils.ImageUtil
import com.h3110w0r1d.t9launcher.utils.Pinyin4jUtil
import com.h3110w0r1d.t9launcher.vo.AppInfo
import com.h3110w0r1d.t9launcher.vo.AppInfo.SortByMatchRate
import com.h3110w0r1d.t9launcher.vo.AppInfo.SortByStartCount
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private var isHideSystemApp: Boolean
    private val hideAppList: MutableSet<String>

    var isLoadingAppList: Boolean = false
	var isShowingHideApp: Boolean = false

    private val spEditor: SharedPreferences.Editor
    private val localPackageName: MutableSet<String> = HashSet()
    private val appList = ArrayList<AppInfo>()
    val loadingStatus: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
	val searchResultLiveData: MutableLiveData<ArrayList<AppInfo>> =
        MutableLiveData<ArrayList<AppInfo>>(ArrayList())
	val hideAppListLiveData: MutableLiveData<ArrayList<AppInfo>> =
        MutableLiveData<ArrayList<AppInfo>>(ArrayList())
    var searchText: String = ""
    val appListDB: SQLiteDatabase by lazy {
        DBHelper(
            getApplication() as App,
            "AppList2.db",
            null,
            1
        ).writableDatabase
    }

    init {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication())
        spEditor = sharedPreferences.edit()
        hideAppList = sharedPreferences.getStringSet("hideAppList", HashSet<String>())!!
        isHideSystemApp = sharedPreferences.getBoolean("hide_system_app", false)

        Thread {
            Pinyin4jUtil.defaultFormat.caseType = HanyuPinyinCaseType.LOWERCASE
            Pinyin4jUtil.defaultFormat.toneType = HanyuPinyinToneType.WITHOUT_TONE

            (getApplication() as App).appViewModel.loadAppList(getApplication())
        }.start()
    }

    fun setAppHide(packageName: String, isHide: Boolean) {
        if (isHide) {
            hideAppList.add(packageName)
        } else {
            hideAppList.remove(packageName)
        }
    }

    fun setLoadingStatus(loading: Boolean) {
        loadingStatus.postValue(loading)
    }

    /**
     * 加载并更新应用列表
     */
    @SuppressLint("Recycle")
    fun loadAppList(context: Context) {
        if (isLoadingAppList) {
            return
        }
        isLoadingAppList = true
        appList.clear()
        val cursor = appListDB.query(
            "T_AppInfo",
            arrayOf(
                "packageName", "appName", "startCount", "isSystemApp", "searchData"
            ), null, null, null, null, "startCount DESC"
        )

        val dataDir = context.dataDir
        while (cursor.moveToNext()) {
            val packageName = cursor.getString(0)
            val appName = cursor.getString(1)
            val startCount = cursor.getInt(2)
            val appIcon = File(dataDir, "$packageName.icon")
            if (!appIcon.exists()) {
                continue
            }
            val roundedDrawable = RoundedBitmapDrawableFactory.create(
                context.resources,
                appIcon.absolutePath
            )
            roundedDrawable.paint.isAntiAlias = true
            val cornerRadius = roundedDrawable.intrinsicHeight * 0.26f
            roundedDrawable.setCornerRadius(cornerRadius)

            val isSystemApp = cursor.getInt(3) == 1
            val gson = Gson()
            val searchData = gson.fromJson<MutableList<MutableList<String>>>(
                cursor.getString(4),
                object : TypeToken<MutableList<MutableList<String>>>() {}.type
            )
            appList.add(
                AppInfo(
                    packageName, appName, startCount, roundedDrawable, isSystemApp, searchData
                )
            )
        }
        cursor.close()
        if (!appList.isEmpty()) {
            setLoadingStatus(false)
        }
        refreshAppInfo(context)
    }

    fun showDefaultAppList() {
        val appInfo = ArrayList<AppInfo>()
        for (app in appList) {
            if (isHideSystemApp && app.isSystemApp) {
                continue
            }
            if (hideAppList.contains(app.packageName)) {
                continue
            }
            appInfo.add(app)
        }
        appList.sortWith(SortByStartCount())
        Log.d("showDefaultAppList", appList.toString())
        searchResultLiveData.postValue(appInfo)
    }

    /**
     * 搜索应用
     * @param key 关键词
     */
    fun searchApp(key: String?) {
        var key = key
        if (key == null) {
            key = searchText
        } else {
            searchText = key
        }
        if (isShowingHideApp) {
            showHideApp()
            return
        }
        if (TextUtils.isEmpty(key)) {
            showDefaultAppList()
            return
        }
        val appInfo = ArrayList<AppInfo>()
        for (app in appList) {
            if (isHideSystemApp && app.isSystemApp) {
                continue
            }
            if (hideAppList.contains(app.packageName)) {
                continue
            }
            val matchRate = Pinyin4jUtil.Search(app, key) // 匹配度
            if (matchRate > 0) {
                app.matchRate = matchRate
                appInfo.add(app)
            }
        }
        appInfo.sortWith(SortByStartCount())
        appInfo.sortWith(SortByMatchRate())
        Log.i("appList", appList.toString())
        searchResultLiveData.postValue(appInfo)
    }

    fun searchHideApp(key: String) {
        var key = key
        key = key.lowercase(Locale.getDefault())
        val appInfo = ArrayList<AppInfo>()
        for (app in appList) {
            if (hideAppList.contains(app.packageName) && (app.packageName
                    .contains(key) || app.appName.lowercase(
                    Locale.getDefault()
                ).contains(key))
            ) {
                appInfo.add(app)
            }
        }
        for (app in appList) {
            if (!hideAppList.contains(app.packageName) && (app.packageName
                    .contains(key) || app.appName.lowercase(
                    Locale.getDefault()
                ).contains(key))
            ) {
                appInfo.add(app)
            }
        }
        hideAppListLiveData.postValue(appInfo)
    }

    fun isAppHide(packageName: String?): Boolean {
        return hideAppList.contains(packageName)
    }


    private fun refreshAppInfo(context: Context) {
        localPackageName.clear()
        val packageManager = context.packageManager
        val resolveInfoList = packageManager.queryIntentActivities(
            Intent(
                Intent.ACTION_MAIN,
                null
            ).addCategory(Intent.CATEGORY_LAUNCHER), 0
        )
        for (resolveInfo in resolveInfoList) {
            localPackageName.add(resolveInfo.activityInfo.packageName)
        }

        val cursor = appListDB.query(
            "T_AppInfo",
            arrayOf(
                "packageName", "appName", "startCount", "isSystemApp", "searchData"
            ), null, null, null, null, null
        )
        val needRemove: MutableList<String> = ArrayList()
        while (cursor.moveToNext()) {
            val packageName = cursor.getString(0)
            if (!localPackageName.contains(packageName)) {
                needRemove.add(packageName)
            }
        }
        cursor.close()
        for (i in appList.indices.reversed()) {
            val app = appList[i]
            if (needRemove.contains(app.packageName)) {
                appListDB.delete(
                    "T_AppInfo",
                    "packageName = ?",
                    arrayOf(app.packageName)
                )
                appList.remove(app)
            }
        }
        setLoadingStatus(false)

        searchApp(null)

        Thread {
            val dataDir = context.dataDir
            for (resolveInfo in resolveInfoList) {
                try {
                    val packageName = resolveInfo.activityInfo.packageName
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)
                    val appName = resolveInfo.loadLabel(packageManager).toString()
                    val icon = resolveInfo.loadIcon(packageManager)
                    val roundedDrawable = RoundedBitmapDrawableFactory.create(
                        context.resources,
                        ImageUtil.drawable2IconBitmap(icon)
                    )
                    roundedDrawable.paint.isAntiAlias = true
                    val cornerRadius = roundedDrawable.intrinsicHeight * 0.26f
                    roundedDrawable.setCornerRadius(cornerRadius)

                    val appIcon = File(dataDir, "$packageName.icon")
                    try {
                        FileOutputStream(appIcon).use { out ->
                            checkNotNull(roundedDrawable.bitmap)
                            roundedDrawable.bitmap!!
                                .compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val searchData = Pinyin4jUtil.getPinYin(appName)
                    val applicationInfo = packageInfo.applicationInfo
                    val isSystemApp = (applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    var findInAppList = false
                    for (app in appList) {
                        if (app.packageName == packageName) {
                            findInAppList = true
                            app.appName = appName
                            app.appIcon = roundedDrawable
                            app.isSystemApp = isSystemApp
                            app.searchData = searchData
                            insertOrUpdate(app)
                            break
                        }
                    }
                    if (findInAppList) {
                        continue
                    }
                    val app =
                        AppInfo(packageName, appName, 0, roundedDrawable, isSystemApp, searchData)
                    appList.add(app)
                    insertOrUpdate(app)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }

            searchApp(null)
            isLoadingAppList = false
        }.start()
    }

    fun insertOrUpdate(app: AppInfo) {
        val packageName = app.packageName
        val appName = app.appName
        val appIcon = (app.appIcon as RoundedBitmapDrawable).bitmap
        val isSystemApp = app.isSystemApp
        val searchData = app.searchData
        try {
            checkNotNull(appIcon)
            appListDB.execSQL(
                "INSERT INTO T_AppInfo VALUES(?,?,?,?,?)",
                arrayOf<Any>(
                    packageName, appName, 0, if (isSystemApp) 1 else 0, Gson().toJson(searchData)
                )
            )
        } catch (_: Exception) {
            appListDB.execSQL(
                "UPDATE T_AppInfo SET appName = ?, isSystemApp = ?, searchData = ? WHERE packageName = ?",
                arrayOf<Any>(
                    appName, if (isSystemApp) 1 else 0, Gson().toJson(searchData), packageName
                )
            )
        }
    }

    fun updateStartCount(app: AppInfo) {
        val packageName = app.packageName
        val startCount = app.startCount
        try {
            appListDB.execSQL(
                "UPDATE T_AppInfo SET startCount = ? WHERE packageName = ?",
                arrayOf<Any>(
                    startCount, packageName
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveHideList() {
        spEditor.remove("hideAppList").commit()
        spEditor.putStringSet("hideAppList", hideAppList).commit()
    }

    fun showHideApp() {
        isShowingHideApp = true
        val appInfo = ArrayList<AppInfo>()
        for (app in appList) {
            if (hideAppList.contains(app.packageName)) {
                appInfo.add(app)
            }
        }
        if (isHideSystemApp) {
            for (app in appList) {
                if (!hideAppList.contains(app.packageName) && app.isSystemApp) {
                    appInfo.add(app)
                }
            }
        }
        searchResultLiveData.postValue(appInfo)
    }

    fun setHideSystemApp(newValue: Boolean) {
        isHideSystemApp = newValue
    }
}
