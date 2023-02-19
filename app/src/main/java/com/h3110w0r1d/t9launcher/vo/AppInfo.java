package com.h3110w0r1d.t9launcher.vo;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.Comparator;
import java.util.List;

public class AppInfo {
    private final String appName;
    private final String packageName;
    private final boolean isSystemApp;

    private int startCount = 0;
    private float matchRate = 0;
    private final Drawable appIcon;
    public List<List<String>> searchData;

    public AppInfo(String packageName, String appName, int startCount, Drawable appIcon, boolean isSystemApp, List<List<String>> searchData) {
        this.packageName = packageName;
        this.appName = appName;
        this.startCount = startCount;
        this.appIcon = appIcon;
        this.isSystemApp = isSystemApp;
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

    public float getMatchRate() {
        return matchRate;
    }

    public void setMatchRate(float matchRate) {
        this.matchRate = matchRate;
    }

    public List<List<String>> getSearchData() {
        return searchData;
    }

}
