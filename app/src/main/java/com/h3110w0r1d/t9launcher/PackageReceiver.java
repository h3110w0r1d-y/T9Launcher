package com.h3110w0r1d.t9launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class PackageReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent){
//		if(Objects.equals(intent.getAction(), "android.intent.action.PACKAGE_ADDED")){
//			Objects.requireNonNull(((App) context.getApplicationContext()).getAppViewModel()).loadAppList(context);
//		}else if(Objects.equals(intent.getAction(), "android.intent.action.PACKAGE_REMOVED")){
//			Objects.requireNonNull(((App) context.getApplicationContext()).getAppViewModel()).loadAppList(context);
//		} else if (Objects.equals(intent.getAction(), "android.intent.action.PACKAGE_REPLACED")) {
//			Objects.requireNonNull(((App) context.getApplicationContext()).getAppViewModel()).loadAppList(context);
//		}
	}
}