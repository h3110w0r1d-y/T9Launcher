package com.h3110w0r1d.t9launcher.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.h3110w0r1d.t9launcher.App
import com.h3110w0r1d.t9launcher.BuildConfig
import com.h3110w0r1d.t9launcher.R
import androidx.core.net.toUri

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference?>("hide_app_list")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { preference: Preference? ->
                    startActivity(Intent(activity, HideAppActivity::class.java))
                    false
                }
            findPreference<Preference?>("hide_system_app")!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any ->
                    (requireActivity().application as App).appViewModel.setHideSystemApp(
                        (newValue as Boolean)
                    )
                    true
                }
            findPreference<Preference?>("github")!!.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { preference: Preference? ->
                    val uri = "https://github.com/h3110w0r1d-y/T9Launcher".toUri()
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    true
                }
            findPreference<Preference?>("version")!!.setSummary(BuildConfig.VERSION_NAME)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.finish() // back button
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}