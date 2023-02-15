package com.h3110w0r1d.t9launcher.widgets;

import static com.h3110w0r1d.t9launcher.utils.Image.DrawableToBitmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.h3110w0r1d.t9launcher.R;
import com.h3110w0r1d.t9launcher.vo.AppInfo;

import java.util.ArrayList;

public class AppListView extends GridView implements View.OnTouchListener{
	
	private final AppListAdapter adapter;
	
	private OnItemClickListener listener;
	
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
		listener = new OnItemClickListener(){
			@Override
			public void onItemClick(View v, AppInfo app){ }
			@Override
			public void onItemLongClick(View v, AppInfo app){ }
		};
	}
	
	public void updateAppInfo(ArrayList<AppInfo> appInfo){
		this.appInfo = appInfo;
		adapter.notifyDataSetChanged();
	}
	
	public AppInfo getItem(int position){
		return appInfo.get(position);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event){
		int action = event.getAction();
		if(action == MotionEvent.ACTION_DOWN){
			v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start();
		}else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL){
			v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
		}
		return false;
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

			convertView.setOnTouchListener(AppListView.this);
			convertView.setOnClickListener(v -> listener.onItemClick(v, appInfo.get(position)));
			convertView.setOnLongClickListener(v -> {
				v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
				listener.onItemLongClick(v, appInfo.get(position));
				return true;
			});
			
			return convertView;
		}
	}
	
	public void setOnItemClickListener(OnItemClickListener listener){
		this.listener = listener;
	}

	public interface OnItemClickListener{
		void onItemClick(View v, AppInfo app);
		
		void onItemLongClick(View v, AppInfo app);
	}
}
