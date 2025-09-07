package com.h3110w0r1d.t9launcher.di

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.database.sqlite.transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.h3110w0r1d.t9launcher.utils.DBHelper
import com.h3110w0r1d.t9launcher.utils.IconManager
import com.h3110w0r1d.t9launcher.utils.ImageUtil
import com.h3110w0r1d.t9launcher.utils.PinyinUtil
import com.h3110w0r1d.t9launcher.vo.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
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

        private fun deleteApp(className: String) {
            val db = dbHelper.writableDatabase
            db.delete(
                table,
                "className = ?",
                arrayOf(className),
            )
            iconManager.deleteIcon(className)
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
                    "ON CONFLICT(className) DO UPDATE SET " +
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

        fun getAllApps(): ArrayList<AppInfo> {
            iconManager.loadAllIcons()
            val result = ArrayList<AppInfo>()
            val cursor = queryAllApps()

            while (cursor.moveToNext()) {
                val className = cursor.getString(0)
                val packageName = cursor.getString(1)
                val appName = cursor.getString(2)
                val startCount = cursor.getInt(3)
                val isSystemApp = cursor.getInt(4) == 1
                val searchDataJson = cursor.getString(5)
                val iconBitmap = iconManager.getIcon(className)
                if (iconBitmap == null) {
                    continue
                }
                val appIcon = iconBitmap.asImageBitmap()

                val gson = Gson()
                val searchData =
                    gson.fromJson<ArrayList<ArrayList<String>>>(
                        searchDataJson,
                        object : TypeToken<ArrayList<ArrayList<String>>>() {}.type,
                    )
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
            return result
        }

        fun updateAppInfo(updateIcon: Boolean): ArrayList<AppInfo> {
            val classNames: ArrayList<String> = ArrayList()
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
                classNames.add(resolveInfo.activityInfo.name)
            }
            val cursor = queryAllApps()
            val needRemove: ArrayList<String> = ArrayList()
            val startCounts: HashMap<String, Int> = HashMap()
            while (cursor.moveToNext()) {
                val className = cursor.getString(0)
                startCounts[className] = cursor.getInt(3)
                if (!classNames.contains(className)) {
                    needRemove.add(className)
                }
            }
            cursor.close()

            // 删除已卸载的包
            for (className in needRemove) {
                deleteApp(className)
            }

            val result = ArrayList<AppInfo>()
            val insertOrUpdateList = ArrayList<Array<Any>>()
            for (resolveInfo in resolveInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                val className = resolveInfo.activityInfo.name
                val packageInfo: PackageInfo
                try {
                    packageInfo = packageManager.getPackageInfo(packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    continue
                }
                val appName = resolveInfo.loadLabel(packageManager).toString()
                var appIcon = iconManager.getIcon(className)?.asImageBitmap()
                if (updateIcon || appIcon == null) {
                    val iconDrawable = resolveInfo.loadIcon(packageManager)
                    val iconBitmap = ImageUtil.drawable2IconBitmap(iconDrawable)
                    iconManager.addIcon(className, iconBitmap)
                    appIcon = iconBitmap.asImageBitmap()
                }

                val searchData = pinyinUtil.getPinYin(appName)
                val applicationInfo = packageInfo.applicationInfo
                val isSystemApp = (applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val searchDataJson = Gson().toJson(searchData)
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
                        startCounts[className] ?: 0,
                        appIcon,
                        isSystemApp,
                        searchData,
                    ),
                )
            }
            insertOrUpdate(insertOrUpdateList)
            iconManager.saveIconsToFile()
            return result
        }

        fun updateStartCount(app: AppInfo) {
            val className = app.className
            val startCount = app.startCount
            val db = dbHelper.writableDatabase
            try {
                db.execSQL(
                    "UPDATE $table SET startCount = ? WHERE className = ?",
                    arrayOf<Any>(
                        startCount,
                        className,
                    ),
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
