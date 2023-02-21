package com.h3110w0r1d.t9launcher.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.h3110w0r1d.t9launcher.App;
import com.h3110w0r1d.t9launcher.R;
import com.h3110w0r1d.t9launcher.model.AppViewModel;
import com.h3110w0r1d.t9launcher.model.DBHelper;
import com.h3110w0r1d.t9launcher.utils.Pinyin4jUtil;
import com.h3110w0r1d.t9launcher.vo.AppInfo;
import com.h3110w0r1d.t9launcher.widgets.AppListView;
import com.h3110w0r1d.t9launcher.widgets.AppPopMenu;

import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity{
	
	private EditText searchText;
	
	private AppListView appListView;
	
	private AppPopMenu appPopMenu;
	
	private AppViewModel appViewModel;

	private boolean keyLongClick = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initStatusBar();
		addListener();

		searchText = findViewById(R.id.TVSearch);
		appListView = findViewById(R.id.appListView);
		appListView.setOnItemClickListener(new AppListView.OnItemClickListener(){
			@Override
			public void onItemClick(View v, AppInfo app){
				if (app.Start(getApplicationContext())){
					appViewModel.UpdateStartCount(app);
					clearSearchAndBack();
				}
			}
			
			@Override
			public void onItemLongClick(View v, AppInfo app){
				appPopMenu.show(v, app);
			}
		});
		
		appPopMenu = new AppPopMenu(this);
		appViewModel = ((App)getApplication()).appViewModel;

		appViewModel.getLoadingStatus().observe(this, loading -> {
			findViewById(R.id.loading).setVisibility(loading ? View.VISIBLE : View.GONE);
			findViewById(R.id.swipeRefreshLayout).setVisibility(loading ? View.GONE : View.VISIBLE);
			appViewModel.searchApp(searchText.getText().toString());
		});
		appViewModel.getSearchResultLiveData().observe(this, searchResult -> appListView.updateAppInfo(searchResult));

		ignoreBatteryOptimization(this);
	}
	
	@Override
	public void onBackPressed(){
		clearSearchAndBack();
	}
	
	public void clearSearchAndBack(){
		searchText.setText("");
		appViewModel.searchApp("");
		moveTaskToBack(true);
	}
	
	private void initStatusBar(){
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		getWindow().setNavigationBarColor(Color.TRANSPARENT);
	}
	
	private void addListener(){
		findViewById(R.id.RLMain).setOnClickListener(v -> clearSearchAndBack());

		@SuppressLint("ClickableViewAccessibility")
		View.OnTouchListener t9btnTouch = (view, motionEvent) -> {
			if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
				if (keyLongClick){
					keyLongClick = false;
					return false;
				}
				int id = view.getId();
				if(id == R.id.t9btn_0) searchText.append("0");
				else if(id == R.id.t9btn_1) searchText.append("1");
				else if(id == R.id.t9btn_2) searchText.append("2");
				else if(id == R.id.t9btn_3) searchText.append("3");
				else if(id == R.id.t9btn_4) searchText.append("4");
				else if(id == R.id.t9btn_5) searchText.append("5");
				else if(id == R.id.t9btn_6) searchText.append("6");
				else if(id == R.id.t9btn_7) searchText.append("7");
				else if(id == R.id.t9btn_8) searchText.append("8");
				else if(id == R.id.t9btn_9) searchText.append("9");
				appViewModel.searchApp(searchText.getText().toString());
			}
			return false;
		};

		View.OnLongClickListener t9btnLongClick = view -> {
			keyLongClick = true;
			appViewModel.ShowHideApps();
			return true;
		};

		findViewById(R.id.t9btn_0).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_1).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_2).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_3).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_4).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_5).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_6).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_7).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_8).setOnTouchListener(t9btnTouch);
		findViewById(R.id.t9btn_9).setOnTouchListener(t9btnTouch);

		findViewById(R.id.t9btn_0).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_1).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_2).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_3).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_4).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_5).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_6).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_7).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_8).setOnLongClickListener(t9btnLongClick);
		findViewById(R.id.t9btn_9).setOnLongClickListener(t9btnLongClick);

		
		Button clear = findViewById(R.id.t9btn_clear);
		clear.setOnClickListener(view -> {
			int len = searchText.length();
			if(len > 0){
				searchText.setText(searchText.getText().delete(len - 1, len));
			}
			appViewModel.searchApp(searchText.getText().toString());
		});
		clear.setOnLongClickListener(view -> {
			searchText.setText("");
			appViewModel.searchApp("");
			return true;
		});
		
		findViewById(R.id.t9btn_setting).setOnLongClickListener(view -> {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		});
		findViewById(R.id.t9btn_setting).setOnClickListener(view -> {
			Toast.makeText(MainActivity.this, R.string.long_press_open_settings, Toast.LENGTH_SHORT).show();
		});

		((SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout)).setOnRefreshListener(() -> {
			new Thread(()-> {
				((App) getApplication()).appViewModel.loadAppList(getApplication());
				runOnUiThread(() -> {
					((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout)).setRefreshing(false);
					appViewModel.searchApp(searchText.getText().toString());
				});
			}).start();
		});
	}

	public void ignoreBatteryOptimization(Activity activity) {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		boolean hasIgnored = false;
		hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
		//  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
		if (!hasIgnored) {
			try {//先调用系统显示 电池优化权限
				@SuppressLint("BatteryLife")
				Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
				intent.setData(Uri.parse("package:" + activity.getPackageName()));
				startActivity(intent);
			} catch (Exception e) {//如果失败了则引导用户到电池优化界面
				try {
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					ComponentName cn = ComponentName.unflattenFromString("com.android.settings/.Settings$HighPowerApplicationsActivity");
					intent.setComponent(cn);
					startActivity(intent);
				}catch (Exception ex) { //如果全部失败则说明没有电池优化功能
				}
			}
		}
	}
}