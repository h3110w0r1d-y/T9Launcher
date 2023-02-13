package com.h3110w0r1d.t9launcher.vo;

import android.graphics.drawable.Drawable;

import java.util.List;

public class AppInfo {
    private final String appName;
    private final String packageName;
    private final boolean isSystemApp;

    private final Drawable appIcon;

    public List<List<String>> searchData;

    public AppInfo(String appName, String packageName, Drawable appIcon, boolean isSystemApp, List<List<String>> searchData) {
        this.appName = appName;
        this.packageName = packageName;
        this.isSystemApp = isSystemApp;
        this.appIcon = appIcon;
        this.searchData = searchData;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public List<List<String>> getSearchData() {
        return searchData;
    }

}
