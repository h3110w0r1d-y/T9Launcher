package com.h3110w0r1d.t9launcher.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(
    context: Context,
) : SQLiteOpenHelper(context, "AppList4.db", null, 1) {
    private val tAppInfo =
        "create table T_AppInfo(" +
            "className VARCHAR(256) NOT NULL," +
            "packageName VARCHAR(256) NOT NULL," +
            "appName TEXT NOT NULL," +
            "startCount INTEGER NOT NULL DEFAULT 0," +
            "isSystemApp BOOLEAN NOT NULL DEFAULT 0," +
            "searchData TEXT NOT NULL," +
            "UNIQUE(className, packageName))"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(tAppInfo)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        i: Int,
        i1: Int,
    ) {}
}
