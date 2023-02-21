package com.h3110w0r1d.t9launcher;

import android.app.Application;

import com.h3110w0r1d.t9launcher.model.AppViewModel;

public class App extends Application{
	
	public AppViewModel appViewModel;
	
	@Override
	public void onCreate(){
		super.onCreate();
		appViewModel = new AppViewModel(this);
	}
}
