package com.h3110w0r1d.t9launcher;

import android.app.Application;

import com.h3110w0r1d.t9launcher.model.AppListViewModel;
import com.h3110w0r1d.t9launcher.utils.Pinyin4jUtil;

import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

public class App extends Application{
	
	public AppListViewModel appListViewModel;
	
	@Override
	public void onCreate(){
		super.onCreate();
		appListViewModel = new AppListViewModel(this);
	}
}
