package com.h3110w0r1d.t9launcher.model;

import static com.h3110w0r1d.t9launcher.utils.Image.DrawableToBitmap;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.h3110w0r1d.t9launcher.utils.Pinyin4jUtil;
import com.h3110w0r1d.t9launcher.vo.AppInfo;
import com.h3110w0r1d.t9launcher.vo.AppInfoComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListViewModel extends AndroidViewModel{
	
	private final MutableLiveData<ArrayList<AppInfo>> appListLiveData = new MutableLiveData<>(new ArrayList<>());

	private final MutableLiveData<Boolean> Loading = new MutableLiveData<>(true);
	
	private final MutableLiveData<ArrayList<AppInfo>> searchResultLiveData = new MutableLiveData<>(new ArrayList<>());
	
	public AppListViewModel(@NonNull Application application){
		super(application);
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
	public void loadAppList(Context context){
		appListLiveData.postValue(getAppList(context));
		Loading.postValue(false);
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
	
	@NonNull
	private ArrayList<AppInfo> getAppList(@NonNull Context context){
		ArrayList<AppInfo> appInfo = new ArrayList<>(64);
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> apps = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
		for(ResolveInfo app : apps){
			try{
				String packageName = app.activityInfo.packageName;
				PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
				
				String label = app.loadLabel(packageManager).toString();
				Drawable icon = app.loadIcon(packageManager);
				RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), DrawableToBitmap(icon));
				roundedDrawable.getPaint().setAntiAlias(true);

				float cornerRadius = roundedDrawable.getIntrinsicHeight() * 0.26f;
				roundedDrawable.setCornerRadius(cornerRadius);
				List<List<String>> searchData = Pinyin4jUtil.getPinYin(label);
				
				ApplicationInfo applicationInfo = packageInfo.applicationInfo;
				boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
				
				appInfo.add(new AppInfo(label, packageName, roundedDrawable, isSystemApp, searchData));
			}catch(PackageManager.NameNotFoundException e){
				e.printStackTrace();
			}
		}
		return appInfo;
	}
}
