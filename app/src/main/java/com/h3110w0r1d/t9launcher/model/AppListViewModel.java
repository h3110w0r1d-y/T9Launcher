package com.h3110w0r1d.t9launcher.model;

import static com.h3110w0r1d.t9launcher.utils.ImageUtil.Drawable2IconBitmap;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.h3110w0r1d.t9launcher.utils.ImageUtil;
import com.h3110w0r1d.t9launcher.utils.Pinyin4jUtil;
import com.h3110w0r1d.t9launcher.vo.AppInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListViewModel extends AndroidViewModel{
	private final SharedPreferences.Editor sharedPreferencesEditor;
	private boolean isLoading = false;
	private boolean isHideSystemApp = false;

	private final boolean enableCache = true;

	private final Set<String> hideAppList;
	
	private final ArrayList<AppInfo> appList = new ArrayList<>();

	private final MutableLiveData<Boolean> Loading = new MutableLiveData<>(true);

	private final MutableLiveData<ArrayList<AppInfo>> searchResultLiveData = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<ArrayList<AppInfo>> hideAppListLiveData = new MutableLiveData<>(new ArrayList<>());

	public SQLiteDatabase AppListDB;
	
	public AppListViewModel(@NonNull Application application){
		super(application);
		SharedPreferences sharedPreferences = application.getSharedPreferences("T9Launcher", Context.MODE_PRIVATE);
		sharedPreferencesEditor = sharedPreferences.edit();
		hideAppList = sharedPreferences.getStringSet("hideAppList", new HashSet<>());
		isHideSystemApp =  PreferenceManager.getDefaultSharedPreferences(getApplication()).getBoolean("hide_system_app", false);
	}

	public MutableLiveData<ArrayList<AppInfo>> getSearchResultLiveData(){
		return searchResultLiveData;
	}

	public MutableLiveData<ArrayList<AppInfo>> getHideAppListLiveData(){
		return hideAppListLiveData;
	}

	public void setAppHide(String packageName, boolean isHide){
		if (isHide) {
			hideAppList.add(packageName);
		} else {
			hideAppList.remove(packageName);
		}
	}

	public MutableLiveData<Boolean> getLoadingStatus(){
		return Loading;
	}

	public void setLoadingStatus(boolean loading){
		Loading.postValue(loading);
	}

	/**
	 * 加载并更新应用列表
	 */
	@SuppressLint("Recycle")
	public void loadAppList(Context context){
		if (isLoading) {
			return;
		}
		isLoading = true;
		appList.clear();
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

				appList.add(new AppInfo(
						packageName, appName, startCount, roundedDrawable, isSystemApp, searchData
				));
			}
			if (appList.size() != 0) {
				setLoadingStatus(false);
			}
		}
		new Thread(()->{
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
					for (AppInfo app : appList) {
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
			isLoading = false;
		}).start();

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
		for(AppInfo app : appList){
			if (isHideSystemApp && app.isSystemApp()){
				continue;
			}
			if (hideAppList.contains(app.getPackageName())) {
				continue;
			}
			float matchRate = Pinyin4jUtil.Search(app, key); // 匹配度
			if (matchRate > 0) {
				app.setMatchRate(matchRate);
				appInfo.add(app);
			}
		}
		appInfo.sort(new AppInfo.SortByStartCount());
		appInfo.sort(new AppInfo.SortByMatchRate());

		searchResultLiveData.postValue(appInfo);
	}

	public void searchHideApp(String key){
		ArrayList<AppInfo> appInfo = new ArrayList<>();
		for(AppInfo app : appList){
			if (hideAppList.contains(app.getPackageName()) && (app.getPackageName().contains(key) || app.getAppName().contains(key))) {
				appInfo.add(app);
			}
		}
		for(AppInfo app : appList){
			if (!hideAppList.contains(app.getPackageName()) && (app.getPackageName().contains(key) || app.getAppName().contains(key))) {
				appInfo.add(app);
			}
		}
		hideAppListLiveData.postValue(appInfo);
	}

	public boolean isAppHide(String packageName){
		return hideAppList.contains(packageName);
	}


	private void RefreshAppInfo(Context context){
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
		for(ResolveInfo resolveInfo : resolveInfoList){
			try{
				String packageName = resolveInfo.activityInfo.packageName;
				PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
				String appName = resolveInfo.loadLabel(packageManager).toString();
				Drawable icon = resolveInfo.loadIcon(packageManager);
				RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), Drawable2IconBitmap(icon));
				roundedDrawable.getPaint().setAntiAlias(true);

				float cornerRadius = roundedDrawable.getIntrinsicHeight() * 0.26f;
				roundedDrawable.setCornerRadius(cornerRadius);
				List<List<String>> searchData = Pinyin4jUtil.getPinYin(appName);
				ApplicationInfo applicationInfo = packageInfo.applicationInfo;
				boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
				boolean findInAppList = false;
				for (AppInfo app : appList){
					if (app.getPackageName().equals(packageName)){
						findInAppList = true;
						app.setAppName(appName);
						app.setAppIcon(roundedDrawable);
						app.setIsSystemApp(isSystemApp);
						app.setSearchData(searchData);
						InsertOrUpdate(app);
						break;
					}
				}
				if (findInAppList){
					continue;
				}
				AppInfo app = new AppInfo(packageName, appName, 0, roundedDrawable, isSystemApp, searchData);
				appList.add(app);
				InsertOrUpdate(app);
			}catch(PackageManager.NameNotFoundException e){
				e.printStackTrace();
			}
		}
		setLoadingStatus(false);
	}

	public void InsertOrUpdate(AppInfo app) {
		if (!enableCache){
			return;
		}
		String packageName = app.getPackageName();
		String appName = app.getAppName();
		Bitmap appIcon = ((RoundedBitmapDrawable)app.getAppIcon()).getBitmap();
		boolean isSystemApp = app.isSystemApp();
		List<List<String>> searchData = app.getSearchData();
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

	public void UpdateStartCount(AppInfo app) {
		if (!enableCache){
			return;
		}
		String packageName = app.getPackageName();
		int startCount = app.getStartCount();
		try {
			AppListDB.execSQL("UPDATE T_AppInfo SET startCount = ? WHERE packageName = ?",
					new Object[]{
							startCount, packageName
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void SaveHideList() {
		sharedPreferencesEditor.putStringSet("hideAppList", hideAppList);
		sharedPreferencesEditor.commit();
	}
}
