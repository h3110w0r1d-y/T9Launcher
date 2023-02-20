package com.h3110w0r1d.t9launcher.vo;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.util.Comparator;
import java.util.List;

public class AppInfo {
    private String appName;
    private final String packageName;
    private boolean isSystemApp;

    private int startCount = 0;
    private float matchRate = 0;
    private Drawable appIcon;
    public List<List<String>> searchData;

    public static class SortByMatchRate implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo p1, AppInfo p2) {
            final float rate = p1.getMatchRate() - p2.getMatchRate();
            if (rate == 0) {
                return 0;
            }
            return rate>0 ? -1 : 1;
        }
    }

    public static class SortByStartCount implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo p1, AppInfo p2) {
            return p2.getStartCount() - p1.getStartCount() ;
        }
    }


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

    public void setAppName(String appName){
        this.appName = appName;
    }

    public int getStartCount() {
        return startCount;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon){
        this.appIcon = appIcon;
    }

    public boolean Start(Context ctx) {
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        if(intent != null){
            this.startCount += 1;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            return true;
        }
        return false;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setIsSystemApp(boolean isSystemApp){
        this.isSystemApp = isSystemApp;
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

    public void setSearchData(List<List<String>> searchData){
        this.searchData = searchData;
    }
}
