package com.h3110w0r1d.t9launcher.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.h3110w0r1d.t9launcher.App;
import com.h3110w0r1d.t9launcher.R;
import com.h3110w0r1d.t9launcher.model.AppListViewModel;
import com.h3110w0r1d.t9launcher.vo.AppInfo;
import com.h3110w0r1d.t9launcher.widgets.AppListView;
import com.h3110w0r1d.t9launcher.widgets.AppPopMenu;

public class MainActivity extends AppCompatActivity{
	
	private EditText searchText;
	
	private AppListView appListView;
	
	private AppPopMenu appPopMenu;
	
	private AppListViewModel appListViewModel;
	
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
				Intent intent = getPackageManager().getLaunchIntentForPackage(app.getPackageName());
				if(intent != null){
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					clearSearchAndBack();
				}
			}
			
			@Override
			public void onItemLongClick(View v, AppInfo app){
				appPopMenu.show(v, app);
			}
		});
		
		appPopMenu = new AppPopMenu(this);
		
		appListViewModel = ((App)getApplication()).appListViewModel;
		appListViewModel.getAppListLiveData().observe(this, appInfo -> {
		
		});
		
		appListViewModel.getSearchResultLiveData().observe(this, searchResult -> {
			appListView.updateAppInfo(searchResult);
		});
	}
	
	@Override
	public void onBackPressed(){
		clearSearchAndBack();
	}
	
	public void clearSearchAndBack(){
		moveTaskToBack(true);
		searchText.setText("");
		appListViewModel.searchApp("");
	}
	
	private void initStatusBar(){
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		getWindow().setNavigationBarColor(Color.TRANSPARENT);
	}
	
	private void addListener(){
		findViewById(R.id.RLMain).setOnClickListener(v -> clearSearchAndBack());
		
		View.OnClickListener t9btnClick = view -> {
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
			appListViewModel.searchApp(searchText.getText().toString());
		};
		
		findViewById(R.id.t9btn_0).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_1).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_2).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_3).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_4).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_5).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_6).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_7).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_8).setOnClickListener(t9btnClick);
		findViewById(R.id.t9btn_9).setOnClickListener(t9btnClick);
		
		Button clear = findViewById(R.id.t9btn_clear);
		clear.setOnClickListener(view -> {
			int len = searchText.length();
			if(len > 0){
				searchText.setText(searchText.getText().delete(len - 1, len));
			}
			appListViewModel.searchApp(searchText.getText().toString());
		});
		clear.setOnLongClickListener(view -> {
			searchText.setText("");
			appListViewModel.searchApp("");
			return true;
		});
		
		findViewById(R.id.t9btn_setting).setOnLongClickListener(view -> {
			MainActivity.this.startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			return true;
		});
		
	}
}