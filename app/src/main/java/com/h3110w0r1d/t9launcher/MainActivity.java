package com.h3110w0r1d.t9launcher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    GridView appGV;
    AppInfoAdapter adapter;
    View.OnClickListener t9btnClick = view -> {
        Button btn = (Button) view;
        if ("1234567890".contains((String) btn.getText())) {
            Data.SearchText += btn.getText();
        }
        ((TextView)findViewById(R.id.TVSearch)).setText(Data.SearchText);
        Data.Search();
        adapter.notifyDataSetChanged();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        addListener();

        appGV = findViewById(R.id.idGVApp);
        adapter = new AppInfoAdapter(this, Data.SearchResult);
        appGV.setAdapter(adapter);

        Pinyin4jUtil.defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        Pinyin4jUtil.defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        getPackageList();
        Data.mainActivity = this;
    }

    @Override
    public void onBackPressed() {
        Data.SearchText = "";
        ((TextView)findViewById(R.id.TVSearch)).setText("");
        Data.Search();
        moveTaskToBack(true);
    }

    private void addListener() {
        RelativeLayout relativeclic1 =(RelativeLayout)findViewById(R.id.RLMain);
        relativeclic1.setOnClickListener(v -> this.onBackPressed());
        ((Button) findViewById(R.id.t9btn_0)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_1)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_2)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_3)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_4)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_5)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_6)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_7)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_8)).setOnClickListener(t9btnClick);
        ((Button) findViewById(R.id.t9btn_9)).setOnClickListener(t9btnClick);

        ((Button) findViewById(R.id.t9btn_clear)).setOnClickListener(view -> {
            if (Data.SearchText.length() > 0) {
                Data.SearchText = Data.SearchText.substring(0, Data.SearchText.length() - 1);
            }
            t9btnClick.onClick(view);
        });
        ((Button) findViewById(R.id.t9btn_clear)).setOnLongClickListener(view -> {
            Data.SearchText = "";
            t9btnClick.onClick(view);
            return true;
        });

        ((Button) findViewById(R.id.t9btn_setting)).setOnLongClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            Data.mainActivity.startActivity(intent);
            return true;
        });

    }

    private void getPackageList() {
        Data.map.clear();
        Data.AppArrayList.clear();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < mApps.size(); i++) {
            ResolveInfo info = mApps.get(i);
            String packageName = info.activityInfo.packageName;
            Drawable icon = info.loadIcon(getPackageManager());
            String label = info.loadLabel(getPackageManager()).toString();
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, 0);
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                int flags = applicationInfo.flags;
                List<List<String>> searchData = Pinyin4jUtil.getPinYin(label);
                boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) != 0;

                if (Data.map.containsKey(packageName)) {
                    continue;
                }
                Data.map.put(packageName, packageName);
                Data.AppArrayList.add(new AppInfo(label, packageName, icon, isSystemApp, searchData));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}