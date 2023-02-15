package com.h3110w0r1d.t9launcher.widgets;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.h3110w0r1d.t9launcher.R;
import com.h3110w0r1d.t9launcher.vo.AppInfo;

public class AppPopMenu extends PopupWindow{
	
	private Context context;
	
	private int popWidth, popHeight;
	
	private AppInfo currentApp;
	
	public AppPopMenu(Context context){
		super(context);
		this.context = context;
		
		setFocusable(true);
		setOutsideTouchable(true);
		setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		View contentView = LayoutInflater.from(context).inflate(R.layout.layout_app_pop, null);
		contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		
		popWidth = contentView.getMeasuredWidth();
		popHeight = contentView.getMeasuredHeight();

		contentView.findViewById(R.id.pop_app_info).setOnClickListener(v -> {
			Intent intent = new Intent();
			intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + currentApp.getPackageName()));
			this.context.startActivity(intent);
			dismiss();
		});

		contentView.findViewById(R.id.pop_copy_package_name).setOnClickListener(v -> {
			ClipboardManager clipboard = (ClipboardManager)this.context.getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("text", currentApp.getPackageName());
			clipboard.setPrimaryClip(clip);
			dismiss();
		});

		setContentView(contentView);
	}
	
	/**
	 * 在指定位置展示长按菜单
	 */
	public void show(View v, AppInfo currentApp){
		this.currentApp = currentApp;

		int[] location = new int[2];
		v.getLocationOnScreen(location);
		
		showAtLocation(v, Gravity.START | Gravity.TOP, location[0], location[1] - popHeight);
	}
}