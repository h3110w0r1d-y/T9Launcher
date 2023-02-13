package com.h3110w0r1d.t9launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            Data.getPackageList(context);
        }

        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            Data.getPackageList(context);
        }
    }
}