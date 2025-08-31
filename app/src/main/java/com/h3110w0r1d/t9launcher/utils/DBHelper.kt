package com.h3110w0r1d.t9launcher.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(T_AppInfo)
    }

    override fun onUpgrade(db: SQLiteDatabase?, i: Int, i1: Int) {
    }

    companion object {
        private val T_AppInfo = "create table T_AppInfo(" +
                "packageName VARCHAR(256) NOT NULL UNIQUE," +
                "appName NTEXT NOT NULL," +
                "startCount INTEGER NOT NULL DEFAULT 0," +
                "isSystemApp BOOLEAN NOT NULL DEFAULT 0," +
                "searchData TEXT NOT NULL)"
    }
}
