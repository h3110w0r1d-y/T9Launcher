package com.h3110w0r1d.t9launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent){
		if(intent.getAction().equals("android.intent.action.PACKAGE_ADDED")){
			((App)context.getApplicationContext()).appViewModel.loadAppList(context);
		}else if(intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")){
			((App)context.getApplicationContext()).appViewModel.loadAppList(context);
		}
	}
}