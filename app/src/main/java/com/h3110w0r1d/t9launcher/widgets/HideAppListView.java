package com.h3110w0r1d.t9launcher.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListAdapter;

import com.h3110w0r1d.t9launcher.R;
import com.h3110w0r1d.t9launcher.vo.AppInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
public class HideAppListView extends ListView {
    Context context;
    private final HideAppListAdapter adapter;
    private ArrayList<AppInfo> appInfo;
    private List<String> selectedPackages = new ArrayList<>();

    public HideAppListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        appInfo = new ArrayList<>();
        adapter = new HideAppListAdapter(context, R.layout.hide_app_list_item, appInfo);
        setAdapter(adapter);
    }

    public void updateAppInfo(ArrayList<AppInfo> appInfo) {
        this.appInfo = appInfo;
        adapter.notifyDataSetChanged();
    }

    private class HideAppListAdapter extends ArrayAdapter {
        public HideAppListAdapter(@NonNull Context context, int resource, @NonNull List objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View view, @NonNull ViewGroup parent){
            if(view == null){
                view = LayoutInflater.from(getContext()).inflate(R.layout.hide_app_list_item, parent, false);
            }
            AppInfo app = appInfo.get(position);

            ImageView icon = view.findViewById(R.id.appIcon);
            TextView text1 = view.findViewById(R.id.text1);
            TextView text2 = view.findViewById(R.id.text2);
            CheckBox checkBox = view.findViewById(R.id.checkbox);

            icon.setImageDrawable(app.getAppIcon());
            text1.setText(app.getAppName());
            text2.setText(app.getPackageName());
            checkBox.setChecked(selectedPackages.contains(app.getPackageName()));

            return view;
        }
    }

}