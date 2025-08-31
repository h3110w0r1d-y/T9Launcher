package com.h3110w0r1d.t9launcher.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.h3110w0r1d.t9launcher.App;
import com.h3110w0r1d.t9launcher.R;
import com.h3110w0r1d.t9launcher.model.AppViewModel;
import com.h3110w0r1d.t9launcher.vo.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class HideAppActivity extends AppCompatActivity {

    private AppViewModel appViewModel;

    private HideAppListAdapter adapter;

    private final List<AppInfo> hideAppList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_hide_app);
        appViewModel = ((App)getApplication()).appViewModel;

        adapter = new HideAppListAdapter(this, R.layout.hide_app_list_item, hideAppList);

        ListView listView = findViewById(R.id.hideAppListView);
        listView.setAdapter(adapter);

        appViewModel.getHideAppListLiveData().observe(this, searchResult -> {
            adapter.clear();
            adapter.addAll(searchResult);
        });
        appViewModel.searchHideApp("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        appViewModel.SaveHideList();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        appViewModel.SaveHideList();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hide_app_menu, menu);

        SearchView sv = (SearchView) menu.findItem(R.id.search).getActionView();
        //设置监听
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                appViewModel.searchHideApp(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                appViewModel.searchHideApp(query);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private class HideAppListAdapter extends ArrayAdapter<AppInfo> {
        Context context;
        List<AppInfo> array;
        public HideAppListAdapter(@NonNull Context context, int resource, @NonNull List<AppInfo> objects) {
            super(context, resource, objects);
            this.context = context;
            this.array = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View view, @NonNull ViewGroup parent){
            if(view == null){
                view = LayoutInflater.from(getContext()).inflate(R.layout.hide_app_list_item, parent, false);
            }
            AppInfo app = array.get(position);

            ImageView icon = view.findViewById(R.id.appIcon);
            TextView text1 = view.findViewById(R.id.text1);
            TextView text2 = view.findViewById(R.id.text2);
            CheckBox checkBox = view.findViewById(R.id.checkbox);

            icon.setImageDrawable(app.getAppIcon());
            text1.setText(app.getAppName());
            text2.setText(app.getPackageName());
            checkBox.setChecked(appViewModel.isAppHide(app.getPackageName()));

            view.setOnClickListener(v -> {
                checkBox.setChecked(!checkBox.isChecked());
                appViewModel.setAppHide(app.getPackageName(), checkBox.isChecked());
            });

            return view;
        }
    }
}