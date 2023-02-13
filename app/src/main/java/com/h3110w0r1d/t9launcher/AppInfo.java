package com.h3110w0r1d.t9launcher;

import android.graphics.drawable.Drawable;

import java.util.List;

public class AppInfo {
    private String appName;
    private String packageName;
    private boolean isSystemApp;

    private Drawable appIcon;

    public List<List<String>> seatchData;

    public AppInfo(String appName, String packageName, Drawable appIcon, boolean isSystemApp, List<List<String>> seatchData) {
        this.appName = appName;
        this.packageName = packageName;
        this.isSystemApp = isSystemApp;
        this.appIcon = appIcon;
        this.seatchData = seatchData;
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

    public List<List<String>> getSeatchData() {
        return seatchData;
    }

}
