package com.h3110w0r1d.t9launcher;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import com.h3110w0r1d.t9launcher.model.AppViewModel;

public class App extends Application{
	
	public AppViewModel appViewModel;
	
	@Override
	public void onCreate(){
		super.onCreate();
		register();
		appViewModel = new AppViewModel(this);
	}

	private void register() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		intentFilter.addDataScheme("package");
		registerReceiver(new PackageReceiver(), intentFilter);
	}
}
