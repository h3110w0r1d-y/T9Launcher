package com.h3110w0r1d.t9launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Data {

    public static HashMap<String, String> map = new HashMap<>();
    public static ArrayList<AppInfo> AppArrayList = new ArrayList<AppInfo>();

    public static MainActivity mainActivity;
    public static ArrayList<AppInfo> SearchResult = new ArrayList<AppInfo>();
    public static String SearchText = "";

    public static void Search() {
        Data.SearchResult.clear();
        if (Objects.equals(SearchText, "")){
            MainActivity.adapter.notifyDataSetChanged();
            return;
        }
        for (AppInfo app : AppArrayList) {
            if (Pinyin4jUtil.Search(app, SearchText)) {
                Data.SearchResult.add(app);
            }
        }
        MainActivity.adapter.notifyDataSetChanged();
    }

    public static void getPackageList(Context ctx) {
        Data.map.clear();
        Data.AppArrayList.clear();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> mApps = ctx.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < mApps.size(); i++) {
            ResolveInfo info = mApps.get(i);
            String packageName = info.activityInfo.packageName;
            Drawable icon = info.loadIcon(ctx.getPackageManager());
            String label = info.loadLabel(ctx.getPackageManager()).toString();
            try {
                PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(packageName, 0);
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                int flags = applicationInfo.flags;
                List<List<String>> searchData = Pinyin4jUtil.getPinYin(label);
                boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                Data.map.put(packageName, packageName);
                Data.AppArrayList.add(new AppInfo(label, packageName, icon, isSystemApp, searchData));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
