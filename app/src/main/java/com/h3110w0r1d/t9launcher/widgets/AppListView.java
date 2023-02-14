package com.h3110w0r1d.t9launcher.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.h3110w0r1d.t9launcher.R;
import com.h3110w0r1d.t9launcher.ui.MainActivity;
import com.h3110w0r1d.t9launcher.vo.AppInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class AppListView extends GridView{
	
	private final AppListAdapter adapter;
	
	private ArrayList<AppInfo> appInfo;
	
	public AppListView(Context context){
		this(context, null);
	}
	
	public AppListView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	
	public AppListView(Context context, AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		appInfo = new ArrayList<>();
		adapter = new AppListAdapter();
		setAdapter(adapter);
	}
	
	public void updateAppInfo(ArrayList<AppInfo> appInfo){
		this.appInfo = appInfo;
		adapter.notifyDataSetChanged();
	}
	
	public AppInfo getItem(int position){
		return appInfo.get(position);
	}
	
	private class AppListAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){
			return appInfo.size();
		}
		
		@Override
		public Object getItem(int position){
			return appInfo.get(position);
		}
		
		@Override
		public long getItemId(int position){
			return 0;
		}
		
		@SuppressLint("ClickableViewAccessibility")
		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_item, parent, false);
			}
			
			AppInfo app = appInfo.get(position);
			TextView courseTV = convertView.findViewById(R.id.idTVApp);
			ImageView courseIV = convertView.findViewById(R.id.idIVApp);
			
			courseTV.setText(app.getAppName());
			courseIV.setImageDrawable(app.getAppIcon());

			convertView.setOnTouchListener((view, event) -> {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start();
				} else if (event.getAction() == MotionEvent.ACTION_UP){
					view.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					view.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
				}
				return false;
			});

			convertView.setOnLongClickListener(view -> {
				view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
				Toast.makeText(getContext(), app.getAppName(), Toast.LENGTH_SHORT).show();
				return true;
			});

			convertView.setOnClickListener(v -> {
				Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName());
				if(intent != null){
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(intent);
					MainActivity.instance.clearSearchAndBack();
				}
			});

			return convertView;
		}
	}
}
