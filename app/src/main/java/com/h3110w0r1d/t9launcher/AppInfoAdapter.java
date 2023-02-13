package com.h3110w0r1d.t9launcher;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class AppInfoAdapter extends ArrayAdapter<AppInfo> {
    public AppInfoAdapter(@NonNull Context context, ArrayList<AppInfo> appInfoArrayList) {
        super(context, 0, appInfoArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listitemView = convertView;
        if (listitemView == null) {
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.app_item, parent, false);
        }

        AppInfo appInfo = getItem(position);
        TextView courseTV = listitemView.findViewById(R.id.idTVApp);
        ImageView courseIV = listitemView.findViewById(R.id.idIVApp);

        courseTV.setText(appInfo.getAppName());
        courseIV.setImageDrawable(appInfo.getAppIcon());
        listitemView.setOnClickListener(v -> {
            Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(appInfo.getPackageName());
            if (intent != null) {
                intent.setPackage(null);
                getContext().startActivity(intent);
                Data.mainActivity.moveTaskToBack(true);
            }
        });
        return listitemView;
    }
}
