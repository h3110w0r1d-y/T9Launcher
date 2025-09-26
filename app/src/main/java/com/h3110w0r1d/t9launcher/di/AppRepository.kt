package com.h3110w0r1d.t9launcher.di

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.database.sqlite.transaction
import com.h3110w0r1d.t9launcher.utils.DBHelper
import com.h3110w0r1d.t9launcher.utils.IconManager
import com.h3110w0r1d.t9launcher.utils.ImageUtil
import com.h3110w0r1d.t9launcher.utils.PinyinUtil
import com.h3110w0r1d.t9launcher.vo.AppInfo
import com.h3110w0r1d.t9launcher.vo.AppInfo.SortByStartCount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.text.Collator
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dbHelper: DBHelper,
        private val pinyinUtil: PinyinUtil,
        private val iconManager: IconManager,
    ) {
        private val table = "T_AppInfo"

        private val _appList = MutableStateFlow<ArrayList<AppInfo>>(ArrayList())
        val appList: StateFlow<ArrayList<AppInfo>> = _appList.asStateFlow()

        private val _appMap = MutableStateFlow<HashMap<String, AppInfo>>(HashMap())
        val appMap: StateFlow<HashMap<String, AppInfo>> = _appMap.asStateFlow()

        // 排序相关
        private val collator: Collator = Collator.getInstance(Locale.CHINA)

        /**
         * 对应用列表进行排序
         */
        private fun sortAppList(appList: ArrayList<AppInfo>): ArrayList<AppInfo> {
            val sortedList = appList.toMutableList()
            sortedList.sortWith(compareBy(collator) { it.appName })
            sortedList.sortWith(SortByStartCount())
            return ArrayList(sortedList)
        }

        private fun queryAllApps(): Cursor {
            val db = dbHelper.readableDatabase

            return db.query(
                table,
                arrayOf(
                    "className",
                    "packageName",
                    "appName",
                    "startCount",
                    "isSystemApp",
                    "searchData",
                ),
                null,
                null,
                null,
                null,
                "startCount DESC",
            )
        }

        private fun deleteApp(
            packageName: String,
            className: String,
        ) {
            val db = dbHelper.writableDatabase
            db.delete(
                table,
                "packageName = ? AND className = ?",
                arrayOf(packageName, className),
            )
            iconManager.deleteIcon(packageName, className)
        }

        private fun insertOrUpdate(insertOrUpdateList: ArrayList<Array<Any>>) {
            val db = dbHelper.writableDatabase
            val upsertSql = (
                "INSERT INTO $table (" +
                    "className," +
                    "packageName," +
                    "appName," +
                    "isSystemApp," +
                    "searchData" +
                    ") VALUES (?,?,?,?,?) " +
                    "ON CONFLICT(className, packageName) DO UPDATE SET " +
                    "packageName=EXCLUDED.packageName," +
                    "appName=EXCLUDED.appName," +
                    "isSystemApp=EXCLUDED.isSystemApp," +
                    "searchData=EXCLUDED.searchData;"
            )
            val statement = db.compileStatement(upsertSql)
            db.transaction {
                try {
                    for (row in insertOrUpdateList) {
                        statement.clearBindings()
                        statement.bindString(1, row[0] as String)
                        statement.bindString(2, row[1] as String)
                        statement.bindString(3, row[2] as String)
                        statement.bindLong(4, if (row[3] as Boolean) 1 else 0)
                        statement.bindString(5, row[4] as String)
                        statement.executeInsert()
                    }
                } finally {
                }
            }
            statement.close()
        }

        /**
         * 处理单个resolveInfo的通用函数
         * @param resolveInfo 要处理的resolveInfo
         * @param packageManager PackageManager实例
         * @param startCounts 启动次数映射，用于获取现有应用的启动次数
         * @param updateIcon 是否更新图标
         * @param defaultStartCount 默认启动次数（用于新应用或更新应用）
         * @param skipExisting 是否跳过已存在的应用（用于addApp场景）
         * @param currentAppMap 当前应用映射，用于检查应用是否已存在
         * @return 处理结果，包含AppInfo和数据库插入数据，如果跳过则返回null
         */
        private fun processResolveInfo(
            resolveInfo: ResolveInfo,
            packageManager: PackageManager,
            startCounts: HashMap<String, Int>? = null,
            updateIcon: Boolean = false,
            defaultStartCount: Int = 0,
            skipExisting: Boolean = false,
            currentAppMap: HashMap<String, AppInfo>? = null,
        ): ProcessResult? {
            val activityInfo = resolveInfo.activityInfo
            val packageName = activityInfo.packageName
            val className = activityInfo.name
            val componentId = "$packageName/$className"

            // 如果设置了跳过已存在的应用，则检查并跳过
            if (skipExisting && currentAppMap?.containsKey(componentId) == true) {
                return null
            }

            val packageInfo: PackageInfo
            try {
                packageInfo = packageManager.getPackageInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                return null
            }

            val appName = resolveInfo.loadLabel(packageManager).toString()
            var appIcon = iconManager.getIcon(componentId)?.asImageBitmap()
            if (updateIcon || appIcon == null) {
                val iconDrawable = resolveInfo.loadIcon(packageManager)
                val iconBitmap = ImageUtil.drawable2IconBitmap(iconDrawable)
                iconManager.addIcon(componentId, iconBitmap)
                appIcon = iconBitmap.asImageBitmap()
            }

            val searchData = pinyinUtil.getPinYin(appName)
            val applicationInfo = packageInfo.applicationInfo
            val isSystemApp = (applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val searchDataJson = Json.encodeToString<ArrayList<ArrayList<String>>>(searchData)

            // 确定启动次数
            val startCount = startCounts?.get(componentId) ?: defaultStartCount

            val appInfo =
                AppInfo(
                    className,
                    packageName,
                    appName,
                    startCount,
                    appIcon,
                    isSystemApp,
                    searchData,
                )

            val insertData: Array<Any> =
                arrayOf(
                    className,
                    packageName,
                    appName,
                    isSystemApp,
                    searchDataJson,
                )

            return ProcessResult(appInfo, insertData)
        }

        /**
         * 处理resolveInfo的结果数据类
         */
        private data class ProcessResult(
            val appInfo: AppInfo,
            val insertData: Array<Any>,
        )

        suspend fun loadAllApps() {
            if (_appList.value.isNotEmpty()) {
                return
            }

            return withContext(Dispatchers.IO) {
                iconManager.loadAllIcons()
                val result = ArrayList<AppInfo>()
                val resultMap = HashMap<String, AppInfo>()
                val cursor = queryAllApps()

                while (cursor.moveToNext()) {
                    val className = cursor.getString(0)
                    val packageName = cursor.getString(1)
                    val componentId = "$packageName/$className"
                    val appName = cursor.getString(2)
                    val startCount = cursor.getInt(3)
                    val isSystemApp = cursor.getInt(4) == 1
                    val searchDataJson = cursor.getString(5)
                    val iconBitmap = iconManager.getIcon(componentId)
                    if (iconBitmap == null) {
                        continue
                    }
                    val appIcon = iconBitmap.asImageBitmap()

                    val searchData =
                        Json.decodeFromString<ArrayList<ArrayList<String>>>(searchDataJson)
                    val appInfo =
                        AppInfo(
                            className,
                            packageName,
                            appName,
                            startCount,
                            appIcon,
                            isSystemApp,
                            searchData,
                        )
                    resultMap[componentId] = appInfo
                    result.add(appInfo)
                }
                cursor.close()

                // 对应用列表进行排序
                val sortedResult = sortAppList(result)

                // 更新缓存和StateFlow
                _appList.value = sortedResult
                _appMap.value = resultMap
            }
        }

        suspend fun updateAppInfo(updateIcon: Boolean = false) =
            withContext(Dispatchers.IO) {
                val componentIds: ArrayList<String> = ArrayList()
                val packageManager = context.packageManager
                val resolveInfoList =
                    packageManager.queryIntentActivities(
                        Intent(
                            Intent.ACTION_MAIN,
                            null,
                        ).addCategory(Intent.CATEGORY_LAUNCHER),
                        0,
                    )
                for (resolveInfo in resolveInfoList) {
                    val activityInfo = resolveInfo.activityInfo
                    val componentId = "${activityInfo.packageName}/${activityInfo.name}"
                    componentIds.add(componentId)
                }
                val cursor = queryAllApps()
                val needRemove: ArrayList<Array<String>> = ArrayList()
                val startCounts: HashMap<String, Int> = HashMap()
                while (cursor.moveToNext()) {
                    val className = cursor.getString(0)
                    val packageName = cursor.getString(1)
                    val componentId = "$packageName/$className"
                    startCounts[componentId] = cursor.getInt(3)
                    if (!componentIds.contains(componentId)) {
                        needRemove.add(arrayOf(packageName, className))
                        continue
                    }
                }
                cursor.close()

                // 删除已卸载的包
                for (name in needRemove) {
                    deleteApp(name[0], name[1])
                }

                val result = ArrayList<AppInfo>()
                val resultMap = HashMap<String, AppInfo>()
                val insertOrUpdateList = ArrayList<Array<Any>>()
                for (resolveInfo in resolveInfoList) {
                    val processResult =
                        processResolveInfo(
                            resolveInfo = resolveInfo,
                            packageManager = packageManager,
                            startCounts = startCounts,
                            updateIcon = updateIcon,
                            defaultStartCount = 0,
                        )

                    if (processResult != null) {
                        val componentId = "${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}"
                        insertOrUpdateList.add(processResult.insertData)
                        resultMap[componentId] = processResult.appInfo
                        result.add(processResult.appInfo)
                    }
                }
                // 对应用列表进行排序
                val sortedResult = sortAppList(result)

                // 更新StateFlow
                _appList.value = sortedResult
                _appMap.value = resultMap

                insertOrUpdate(insertOrUpdateList)
                iconManager.saveIconsToFile()
            }

        fun updateStartCount(app: AppInfo) {
            val packageName = app.packageName
            val className = app.className
            val startCount = app.startCount
            val db = dbHelper.writableDatabase
            try {
                db.execSQL(
                    "UPDATE $table SET startCount = ? WHERE packageName = ? AND className = ?",
                    arrayOf<Any>(
                        startCount,
                        packageName,
                        className,
                    ),
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 添加新安装的应用
         */
        suspend fun addApp(packageName: String?) {
            if (packageName.isNullOrEmpty()) return

            withContext(Dispatchers.IO) {
                try {
                    val packageManager = context.packageManager

                    // 查询该包的所有启动器活动
                    val intent =
                        Intent(Intent.ACTION_MAIN, null).apply {
                            addCategory(Intent.CATEGORY_LAUNCHER)
                            setPackage(packageName)
                        }
                    val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

                    val insertOrUpdateList = ArrayList<Array<Any>>()
                    val newApps = ArrayList<AppInfo>()
                    val currentAppMap = _appMap.value.toMutableMap()

                    for (resolveInfo in resolveInfoList) {
                        val processResult =
                            processResolveInfo(
                                resolveInfo = resolveInfo,
                                packageManager = packageManager,
                                updateIcon = true,
                                defaultStartCount = 0,
                                skipExisting = true,
                                currentAppMap = HashMap(currentAppMap),
                            )

                        if (processResult != null) {
                            val componentId = "${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}"
                            insertOrUpdateList.add(processResult.insertData)
                            currentAppMap[componentId] = processResult.appInfo
                            newApps.add(processResult.appInfo)
                        }
                    }

                    if (insertOrUpdateList.isNotEmpty()) {
                        // 更新应用列表
                        val currentAppList = _appList.value.toMutableList()
                        currentAppList.addAll(newApps)
                        val sortedResult = sortAppList(ArrayList(currentAppList))

                        _appList.value = sortedResult
                        _appMap.value = HashMap(currentAppMap)

                        insertOrUpdate(insertOrUpdateList)
                        iconManager.saveIconsToFile()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * 移除已卸载的应用
         */
        suspend fun removeApp(packageName: String?) {
            if (packageName.isNullOrEmpty()) return

            withContext(Dispatchers.IO) {
                try {
                    val currentAppList = _appList.value.toMutableList()
                    val currentAppMap = _appMap.value.toMutableMap()
                    val appsToRemove = ArrayList<AppInfo>()

                    // 查找需要移除的应用
                    for (app in currentAppList) {
                        if (app.packageName == packageName) {
                            appsToRemove.add(app)
                            currentAppMap.remove(app.componentId())
                            deleteApp(app.packageName, app.className)
                        }
                    }

                    // 从列表中移除
                    currentAppList.removeAll(appsToRemove)

                    if (appsToRemove.isNotEmpty()) {
                        _appList.value = ArrayList(currentAppList)
                        _appMap.value = HashMap(currentAppMap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * 更新被替换的应用
         */
        suspend fun updateApp(packageName: String?) {
            if (packageName.isNullOrEmpty()) return

            withContext(Dispatchers.IO) {
                try {
                    val packageManager = context.packageManager

                    // 查询该包的所有启动器活动
                    val intent =
                        Intent(Intent.ACTION_MAIN, null).apply {
                            addCategory(Intent.CATEGORY_LAUNCHER)
                            setPackage(packageName)
                        }
                    val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

                    val insertOrUpdateList = ArrayList<Array<Any>>()
                    val updatedApps = ArrayList<AppInfo>()
                    val currentAppMap = _appMap.value.toMutableMap()
                    val currentAppList = _appList.value.toMutableList()

                    // 先移除旧的应用信息
                    val appsToRemove = currentAppList.filter { it.packageName == packageName }
                    currentAppList.removeAll(appsToRemove)
                    for (app in appsToRemove) {
                        currentAppMap.remove("${app.packageName}/${app.className}")
                    }

                    // 添加更新后的应用信息
                    for (resolveInfo in resolveInfoList) {
                        val processResult =
                            processResolveInfo(
                                resolveInfo = resolveInfo,
                                packageManager = packageManager,
                                updateIcon = true,
                                defaultStartCount = 0,
                            )

                        if (processResult != null) {
                            val componentId = "${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}"
                            insertOrUpdateList.add(processResult.insertData)
                            currentAppMap[componentId] = processResult.appInfo
                            updatedApps.add(processResult.appInfo)
                        }
                    }

                    if (insertOrUpdateList.isNotEmpty()) {
                        insertOrUpdate(insertOrUpdateList)
                        iconManager.saveIconsToFile()

                        // 更新应用列表
                        currentAppList.addAll(updatedApps)
                        val sortedResult = sortAppList(ArrayList(currentAppList))

                        _appList.value = sortedResult
                        _appMap.value = HashMap(currentAppMap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
