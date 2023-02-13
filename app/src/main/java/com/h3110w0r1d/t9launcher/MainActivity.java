package com.h3110w0r1d.t9launcher;

import android.content.Intent;
import android.graphics.Color;
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

public class MainActivity extends AppCompatActivity {
    public static GridView appGV;
    public static AppInfoAdapter adapter;
    View.OnClickListener t9btnClick = view -> {
        Button btn = (Button) view;
        if ("1234567890".contains((String) btn.getText())) {
            Data.SearchText += btn.getText();
        }
        ((TextView)findViewById(R.id.TVSearch)).setText(Data.SearchText);
        Data.Search();
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
        Data.getPackageList(this);
        Data.mainActivity = this;
    }

    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        if (nonRoot) {
            Data.SearchText = "";
            ((TextView)findViewById(R.id.TVSearch)).setText("");
            Data.Search();
        }
        return super.moveTaskToBack(nonRoot);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void addListener() {
        RelativeLayout relativeLayout =(RelativeLayout)findViewById(R.id.RLMain);
        relativeLayout.setOnClickListener(v -> this.onBackPressed());
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
            view.getContext().startActivity(intent);
            return true;
        });

    }

}