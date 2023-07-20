package com.h3110w0r1d.t9launcher.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(T_AppInfo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
    private static final String T_AppInfo =
            "create table T_AppInfo(" +
                    "packageName VARCHAR(256) NOT NULL UNIQUE," +
                    "appName NTEXT NOT NULL," +
                    "startCount INTEGER NOT NULL DEFAULT 0," +
                    "isSystemApp BOOLEAN NOT NULL DEFAULT 0," +
                    "searchData TEXT NOT NULL)";
}
