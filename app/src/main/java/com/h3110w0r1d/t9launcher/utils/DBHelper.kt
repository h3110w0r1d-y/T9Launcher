package com.h3110w0r1d.t9launcher.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DBHelper
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : SQLiteOpenHelper(context, "AppList3.db", null, 1) {
        companion object {
            private const val T_APP_INFO =
                "create table T_AppInfo(" +
                    "className VARCHAR(256) NOT NULL UNIQUE," +
                    "packageName VARCHAR(256) NOT NULL," +
                    "appName TEXT NOT NULL," +
                    "startCount INTEGER NOT NULL DEFAULT 0," +
                    "isSystemApp BOOLEAN NOT NULL DEFAULT 0," +
                    "searchData TEXT NOT NULL)"
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(T_APP_INFO)
        }

        override fun onUpgrade(
            db: SQLiteDatabase?,
            i: Int,
            i1: Int,
        ) {
        }
    }
