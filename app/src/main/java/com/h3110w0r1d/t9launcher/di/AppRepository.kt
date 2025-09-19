package com.h3110w0r1d.t9launcher.di

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.database.sqlite.transaction
import com.h3110w0r1d.t9launcher.utils.DBHelper
import com.h3110w0r1d.t9launcher.utils.IconManager
import com.h3110w0r1d.t9launcher.utils.ImageUtil
import com.h3110w0r1d.t9launcher.utils.PinyinUtil
import com.h3110w0r1d.t9launcher.vo.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
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
        private var appList = ArrayList<AppInfo>()

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

        fun loadAllApps(): ArrayList<AppInfo> {
            if (appList.isNotEmpty()) {
                return appList
            }
            iconManager.loadAllIcons()
            val result = ArrayList<AppInfo>()
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
                result.add(
                    AppInfo(
                        className,
                        packageName,
                        appName,
                        startCount,
                        appIcon,
                        isSystemApp,
                        searchData,
                    ),
                )
            }
            cursor.close()
            appList = result
            return result
        }

        fun updateAppInfo(updateIcon: Boolean): ArrayList<AppInfo> {
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
            val insertOrUpdateList = ArrayList<Array<Any>>()
            for (resolveInfo in resolveInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                val className = resolveInfo.activityInfo.name
                val componentId = "$packageName/$className"

                val packageInfo: PackageInfo
                try {
                    packageInfo = packageManager.getPackageInfo(packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    continue
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
                insertOrUpdateList.add(
                    arrayOf(
                        className,
                        packageName,
                        appName,
                        isSystemApp,
                        searchDataJson,
                    ),
                )

                result.add(
                    AppInfo(
                        className,
                        packageName,
                        appName,
                        startCounts[componentId] ?: 0,
                        appIcon,
                        isSystemApp,
                        searchData,
                    ),
                )
            }
            insertOrUpdate(insertOrUpdateList)
            iconManager.saveIconsToFile()
            appList = result
            return result
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
    }
