package com.h3110w0r1d.t9launcher.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.h3110w0r1d.t9launcher.App;
import com.h3110w0r1d.t9launcher.BuildConfig;
import com.h3110w0r1d.t9launcher.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            findPreference("hide_app_list").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), HideAppActivity.class));
                return false;
            });
            findPreference("hide_system_app").setOnPreferenceChangeListener((preference, newValue) -> {
                ((App) requireActivity().getApplication()).appViewModel.setHideSystemApp((Boolean) newValue);
                return true;
            });
            findPreference("github").setOnPreferenceClickListener(preference -> {
                Uri uri = Uri.parse("https://github.com/h3110w0r1d-y/T9Launcher");
                startActivity(new Intent(Intent.ACTION_VIEW,uri));
                return true;
            });
            findPreference("version").setSummary(BuildConfig.VERSION_NAME);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish(); // back button
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}