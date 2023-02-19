package com.h3110w0r1d.t9launcher.model;

import static com.h3110w0r1d.t9launcher.utils.ImageUtil.Drawable2IconBitmap;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.h3110w0r1d.t9launcher.utils.ImageUtil;
import com.h3110w0r1d.t9launcher.utils.Pinyin4jUtil;
import com.h3110w0r1d.t9launcher.vo.AppInfo;
import com.h3110w0r1d.t9launcher.vo.AppInfoComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppListViewModel extends AndroidViewModel{

	private final boolean enableCache = true;
	
	private final MutableLiveData<ArrayList<AppInfo>> appListLiveData = new MutableLiveData<>(new ArrayList<>());

	private final MutableLiveData<Boolean> Loading = new MutableLiveData<>(true);
	
	private final MutableLiveData<ArrayList<AppInfo>> searchResultLiveData = new MutableLiveData<>(new ArrayList<>());

	public static SQLiteDatabase AppListDB;
	
	public AppListViewModel(@NonNull Application application){
		super(application);
		AppListDB = new DBHelper(application, "AppList.db", null, 1).getWritableDatabase();
	}
	
	public MutableLiveData<ArrayList<AppInfo>> getAppListLiveData(){
		return appListLiveData;
	}
	
	public MutableLiveData<ArrayList<AppInfo>> getSearchResultLiveData(){
		return searchResultLiveData;
	}

	public MutableLiveData<Boolean> getLoadingStatus(){
		return Loading;
	}

	public void setLoadingStatus(boolean loading){
		Loading.postValue(loading);
	}
	
	/**
	 * 获取应用列表
	 */
	public ArrayList<AppInfo> getAppList(){
		return appListLiveData.getValue();
	}
	
	/**
	 * 获取搜索结果
	 */
	public ArrayList<AppInfo> getSearchResult(){
		return searchResultLiveData.getValue();
	}
	
	/**
	 * 更新应用列表
	 */
	@SuppressLint("Recycle")
	public void loadAppList(Context context){
		Objects.requireNonNull(appListLiveData.getValue()).clear();
		if (enableCache) {
			Cursor cursor = AppListDB.query(
					"T_AppInfo",
					new String[]{
							"packageName", "appName", "startCount", "appIcon", "isSystemApp", "searchData"
					}, null, null, null, null, null);
			while (cursor.moveToNext()) {
				String packageName = cursor.getString(0);
				String appName = cursor.getString(1);
				int startCount = cursor.getInt(2);
				Bitmap appIcon = ImageUtil.Bytes2Bitmap(cursor.getBlob(3));

				RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), appIcon);
				roundedDrawable.getPaint().setAntiAlias(true);
				float cornerRadius = roundedDrawable.getIntrinsicHeight() * 0.26f;
				roundedDrawable.setCornerRadius(cornerRadius);

				boolean isSystemApp = cursor.getInt(4) == 1;
				List<List<String>> searchData = JSON.parseObject(cursor.getString(5),new TypeReference<List<List<String>>>(){});

				Objects.requireNonNull(appListLiveData.getValue()).add(new AppInfo(
						packageName, appName, startCount, roundedDrawable, isSystemApp, searchData
				));
			}
			if (appListLiveData.getValue().size() != 0) {
				setLoadingStatus(false);
			}
		}
		RefreshAppInfo(context);
		if (enableCache) {
			Cursor cursor = AppListDB.query(
					"T_AppInfo",
					new String[]{
							"packageName", "appName", "startCount", "appIcon", "isSystemApp", "searchData"
					}, null, null, null, null, null);
			while (cursor.moveToNext()) {
				String packageName = cursor.getString(0);
				boolean inDB = false;
				for (AppInfo app : Objects.requireNonNull(appListLiveData.getValue())) {
					if (app.getPackageName().equals(packageName)) {
						inDB = true;
						break;
					}
				}
				if (!inDB) {
					AppListDB.execSQL("DELETE FROM T_AppInfo WHERE packageName = ?", new Object[]{packageName});
				}
			}
		}
		System.out.println("加载完成");
	}
	
	/**
	 * 搜索应用
	 * @param key 关键词
	 */
	public void searchApp(@Nullable String key){
		if(TextUtils.isEmpty(key)){
			searchResultLiveData.postValue(new ArrayList<>());
			return;
		}
		ArrayList<AppInfo> appInfo = new ArrayList<>();
		for(AppInfo app : getAppList()){
			float matchRate = Pinyin4jUtil.Search(app, key); // 匹配度
			if (matchRate > 0) {
				app.setMatchRate(matchRate);
				appInfo.add(app);
			}
		}
		appInfo.sort(new AppInfoComparator());

		searchResultLiveData.postValue(appInfo);
	}

	private void RefreshAppInfo(Context context){
		ArrayList<AppInfo> appInfo = new ArrayList<>(64);
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> apps = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
		for(ResolveInfo app : apps){
			try{
				String packageName = app.activityInfo.packageName;
				PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
				String appName = app.loadLabel(packageManager).toString();
				Drawable icon = app.loadIcon(packageManager);
				RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), Drawable2IconBitmap(icon));
				roundedDrawable.getPaint().setAntiAlias(true);

				float cornerRadius = roundedDrawable.getIntrinsicHeight() * 0.26f;
				roundedDrawable.setCornerRadius(cornerRadius);
				Bitmap appIcon = roundedDrawable.getBitmap();
				List<List<String>> searchData = Pinyin4jUtil.getPinYin(appName);
				ApplicationInfo applicationInfo = packageInfo.applicationInfo;
				boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

				appInfo.add(new AppInfo(packageName, appName, 0, roundedDrawable, isSystemApp, searchData));
				if (enableCache) {
					try {
						assert appIcon != null;
						AppListDB.execSQL("INSERT INTO T_AppInfo VALUES(?,?,?,?,?,?)",
								new Object[]{
										packageName, appName, 0, ImageUtil.Bitmap2Bytes(appIcon), isSystemApp ? 1 : 0, JSON.toJSONString(searchData)
								});
					} catch (Exception e) {
						AppListDB.execSQL("UPDATE T_AppInfo SET appName = ?, appIcon = ?, isSystemApp = ?, searchData = ? WHERE packageName = ?",
								new Object[]{
										appName, ImageUtil.Bitmap2Bytes(appIcon), isSystemApp ? 1 : 0, JSON.toJSONString(searchData), packageName
								});
					}
				}
			}catch(PackageManager.NameNotFoundException e){
				e.printStackTrace();
			}
		}

		appListLiveData.postValue(appInfo);
		setLoadingStatus(false);
	}
}
