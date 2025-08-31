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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.h3110w0r1d.t9launcher.App;
import com.h3110w0r1d.t9launcher.R;
import com.h3110w0r1d.t9launcher.utils.DBHelper;
import com.h3110w0r1d.t9launcher.utils.Pinyin4jUtil;
import com.h3110w0r1d.t9launcher.vo.AppInfo;

import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppViewModel extends AndroidViewModel{

	private boolean isHideSystemApp;
	private final Set<String> hideAppList;

	public boolean isLoadingAppList = false;
	public boolean isShowingHideApp = false;

	private final SharedPreferences.Editor spEditor;
	private final Set<String> localPackageName = new HashSet<>();
	private final ArrayList<AppInfo> appList = new ArrayList<>();
	private final MutableLiveData<Boolean> Loading = new MutableLiveData<>(true);
	private final MutableLiveData<ArrayList<AppInfo>> searchResultLiveData = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<ArrayList<AppInfo>> hideAppListLiveData = new MutableLiveData<>(new ArrayList<>());
	public String SearchText = "";
	public SQLiteDatabase AppListDB;
	
	public AppViewModel(@NonNull Application application){
		super(application);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
		spEditor = sharedPreferences.edit();
		hideAppList = sharedPreferences.getStringSet("hideAppList", new HashSet<>());
		isHideSystemApp =  sharedPreferences.getBoolean("hide_system_app", false);

		new Thread(()-> {
			Pinyin4jUtil.defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			Pinyin4jUtil.defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			try {
				((App) getApplication()).appViewModel.AppListDB = new DBHelper((App) getApplication(), "AppList2.db", null, 1).getWritableDatabase();
			} catch (Exception e) {
				Toast.makeText(application, R.string.failed_init_database, Toast.LENGTH_LONG).show();
			}
			((App) getApplication()).appViewModel.loadAppList(getApplication());
		}).start();
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
		if (isLoadingAppList) {
			return;
		}
		isLoadingAppList = true;
		appList.clear();
		Cursor cursor = AppListDB.query(
				"T_AppInfo",
				new String[]{
						"packageName", "appName", "startCount", "isSystemApp", "searchData"
				}, null, null, null, null, null);

		File data_dir = context.getDataDir();
		while (cursor.moveToNext()) {
			String packageName = cursor.getString(0);
			String appName = cursor.getString(1);
			int startCount = cursor.getInt(2);
			File app_icon = new File(data_dir, packageName + ".icon");
			if (!app_icon.exists()) {
				continue;
			}
			RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), app_icon.getAbsolutePath());
			roundedDrawable.getPaint().setAntiAlias(true);
			float cornerRadius = roundedDrawable.getIntrinsicHeight() * 0.26f;
			roundedDrawable.setCornerRadius(cornerRadius);

			boolean isSystemApp = cursor.getInt(3) == 1;
			Gson gson = new Gson();
			List<List<String>> searchData = gson.fromJson(cursor.getString(4),new TypeToken<List<List<String>>>(){}.getType());
			appList.add(new AppInfo(
					packageName, appName, startCount, roundedDrawable, isSystemApp, searchData
			));
		}
		cursor.close();
		if (!appList.isEmpty()) {
			setLoadingStatus(false);
		}
		RefreshAppInfo(context);
	}

	public void showDefaultAppList(){
		ArrayList<AppInfo> appInfo = new ArrayList<>();
		for (AppInfo app : appList) {
			if (isHideSystemApp && app.isSystemApp()){
				continue;
			}
			if (hideAppList.contains(app.getPackageName())) {
				continue;
			}
			appInfo.add(app);
		}
		appList.sort(new AppInfo.SortByStartCount());
		searchResultLiveData.postValue(appInfo);
	}
	
	/**
	 * 搜索应用
	 * @param key 关键词
	 */
	public void searchApp(@Nullable String key){
		if (key == null) {
			key = SearchText;
		} else {
			SearchText = key;
		}
		if (isShowingHideApp) {
			ShowHideApp();
			return;
		}
		if(TextUtils.isEmpty(key)){
			showDefaultAppList();
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
		key = key.toLowerCase();
		ArrayList<AppInfo> appInfo = new ArrayList<>();
		for(AppInfo app : appList){
			if (hideAppList.contains(app.getPackageName()) && (app.getPackageName().contains(key) || app.getAppName().toLowerCase().contains(key))) {
				appInfo.add(app);
			}
		}
		for(AppInfo app : appList){
			if (!hideAppList.contains(app.getPackageName()) && (app.getPackageName().contains(key) || app.getAppName().toLowerCase().contains(key))) {
				appInfo.add(app);
			}
		}
		hideAppListLiveData.postValue(appInfo);
	}

	public boolean isAppHide(String packageName){
		return hideAppList.contains(packageName);
	}


	private void RefreshAppInfo(Context context){
		localPackageName.clear();
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
		for (ResolveInfo resolveInfo : resolveInfoList) {
			localPackageName.add(resolveInfo.activityInfo.packageName);
		}

		Cursor cursor = AppListDB.query(
				"T_AppInfo",
				new String[]{
						"packageName", "appName", "startCount", "isSystemApp", "searchData"
				}, null, null, null, null, null);
		List<String> needRemove = new ArrayList<>();
		while (cursor.moveToNext()) {
			String packageName = cursor.getString(0);
			if (!localPackageName.contains(packageName)) {
				needRemove.add(packageName);
			}
		}
		cursor.close();
		for (int i=appList.size()-1; i>=0; i--){
			AppInfo app = appList.get(i);
			if (needRemove.contains(app.getPackageName())){
				AppListDB.delete("T_AppInfo", "packageName = ?", new String[]{app.getPackageName()});
				appList.remove(app);
			}
		}
		setLoadingStatus(false);

		searchApp(null);

		new Thread(() -> {
			File data_dir = context.getDataDir();
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

					File app_icon = new File(data_dir, packageName + ".icon");
					try (FileOutputStream out = new FileOutputStream(app_icon)) {
						assert roundedDrawable.getBitmap() != null;
						roundedDrawable.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
					} catch (IOException e) {
						e.printStackTrace();
					}
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

			searchApp(null);

			isLoadingAppList = false;
		}).start();

	}

	public void InsertOrUpdate(AppInfo app) {
		String packageName = app.getPackageName();
		String appName = app.getAppName();
		Bitmap appIcon = ((RoundedBitmapDrawable)app.getAppIcon()).getBitmap();
		boolean isSystemApp = app.isSystemApp();
		List<List<String>> searchData = app.getSearchData();
		try {
			assert appIcon != null;
			AppListDB.execSQL("INSERT INTO T_AppInfo VALUES(?,?,?,?,?)",
					new Object[]{
							packageName, appName, 0, isSystemApp ? 1 : 0, new Gson().toJson(searchData)
					});
		} catch (Exception e) {
			AppListDB.execSQL("UPDATE T_AppInfo SET appName = ?, isSystemApp = ?, searchData = ? WHERE packageName = ?",
					new Object[]{
							appName, isSystemApp ? 1 : 0, new Gson().toJson(searchData), packageName
					});
		}
	}

	public void UpdateStartCount(AppInfo app) {
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
		spEditor.remove("hideAppList").commit();
		spEditor.putStringSet("hideAppList", hideAppList).commit();
	}

	public void ShowHideApp() {
		isShowingHideApp = true;
		ArrayList<AppInfo> appInfo = new ArrayList<>();
		for (AppInfo app : appList){
			if (hideAppList.contains(app.getPackageName())){
				appInfo.add(app);
			}
		}
		if (isHideSystemApp){
			for (AppInfo app : appList){
				if (!hideAppList.contains(app.getPackageName()) && app.isSystemApp()){
					appInfo.add(app);
				}
			}
		}
		searchResultLiveData.postValue(appInfo);
	}

	public void setHideSystemApp(boolean newValue) {
		isHideSystemApp = newValue;
	}

}
