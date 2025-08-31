package com.h3110w0r1d.t9launcher.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.h3110w0r1d.t9launcher.App
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.vo.AppInfo
import com.h3110w0r1d.t9launcher.widgets.AppListView
import com.h3110w0r1d.t9launcher.widgets.AppPopMenu
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {
    private val searchText: TextView by lazy { findViewById(R.id.TVSearch) }

    private val appListView: AppListView by lazy { findViewById(R.id.appListView) }

    private val appPopMenu: AppPopMenu by lazy { AppPopMenu(this) }

    private val appViewModel: AppViewModel by lazy { (application as App).appViewModel }

    private var longClickDown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initStatusBar()
        addListener()

        appListView.setOnItemClickListener(object : AppListView.OnItemClickListener {
            override fun onItemClick(v: View?, app: AppInfo?) {
                if (app?.start(applicationContext) == true) {
                    appViewModel.updateStartCount(app)
                    clearSearchAndBack()
                }
            }

            override fun onItemLongClick(v: View?, app: AppInfo?) {
                appPopMenu.show(v, app)
            }
        })

        appViewModel.loadingStatus.observe(this, Observer { loading: Boolean? ->
            findViewById<View>(R.id.loading).visibility = if (loading == true) View.VISIBLE else View.GONE
            findViewById<View>(R.id.swipeRefreshLayout).visibility = if (loading == true) View.GONE else View.VISIBLE
//            if (loading == false) {
//                appViewModel.searchApp(searchText.text.toString())
//            }
        })
        appViewModel.searchResultLiveData
            .observe(this, { searchResult ->
                Log.d("observe LiveData", searchResult.toString())
                appListView.updateAppInfo(searchResult)
            })

        ignoreBatteryOptimization(this)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        clearSearchAndBack()
    }

    fun clearSearchAndBack() {
        appViewModel.isShowingHideApp = false
        searchText.text = ""
        appViewModel.searchApp("")
        moveTaskToBack(true)
    }

    private fun initStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun addListener() {
        findViewById<View>(R.id.RLMain).setOnClickListener { v: View? -> clearSearchAndBack() }

        @SuppressLint("ClickableViewAccessibility") val t9btnTouch =
            OnTouchListener { view: View?, motionEvent: MotionEvent? ->
                if (motionEvent!!.action == MotionEvent.ACTION_UP) {
                    if (longClickDown) {
                        longClickDown = false
                        return@OnTouchListener false
                    }
                    appViewModel.isShowingHideApp = false
                    val id = view!!.id
                    when (id) {
                        R.id.t9btn_0 -> searchText.append("0")
                        R.id.t9btn_1 -> searchText.append("1")
                        R.id.t9btn_2 -> searchText.append("2")
                        R.id.t9btn_3 -> searchText.append("3")
                        R.id.t9btn_4 -> searchText.append("4")
                        R.id.t9btn_5 -> searchText.append("5")
                        R.id.t9btn_6 -> searchText.append("6")
                        R.id.t9btn_7 -> searchText.append("7")
                        R.id.t9btn_8 -> searchText.append("8")
                        R.id.t9btn_9 -> searchText.append("9")
                    }
                    appViewModel.searchApp(searchText.text.toString())
                }
                false
            }

        val t9btnLongClick = OnLongClickListener { view: View? ->
            longClickDown = true
            appViewModel.showHideApp()
            true
        }

        findViewById<View>(R.id.t9btn_0).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_1).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_2).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_3).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_4).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_5).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_6).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_7).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_8).setOnTouchListener(t9btnTouch)
        findViewById<View>(R.id.t9btn_9).setOnTouchListener(t9btnTouch)

        findViewById<View>(R.id.t9btn_0).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_1).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_2).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_3).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_4).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_5).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_6).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_7).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_8).setOnLongClickListener(t9btnLongClick)
        findViewById<View>(R.id.t9btn_9).setOnLongClickListener(t9btnLongClick)


        val clear = findViewById<Button>(R.id.t9btn_clear)
        clear.setOnClickListener { view: View? ->
            appViewModel.isShowingHideApp = false
            val len = searchText.text.length
            if (len > 0) {
                searchText.text = searchText.text.toString().substring(0, len - 1)
            }
            appViewModel.searchApp(searchText.text.toString())
        }
        clear.setOnLongClickListener { view: View? ->
            appViewModel.isShowingHideApp = false
            searchText.text = ""
            appViewModel.searchApp("")
            true
        }

        findViewById<View>(R.id.t9btn_setting).setOnLongClickListener { view: View? ->
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        findViewById<View>(R.id.t9btn_setting).setOnClickListener { view: View? ->
            Toast.makeText(
                this@MainActivity,
                R.string.long_press_open_settings,
                Toast.LENGTH_SHORT
            ).show()
        }

        (findViewById<View>(R.id.swipeRefreshLayout) as SwipeRefreshLayout).setOnRefreshListener {
            Thread {
                (application as App).appViewModel.loadAppList(application)
                runOnUiThread {
                    (findViewById<View>(R.id.swipeRefreshLayout) as SwipeRefreshLayout).isRefreshing =
                        false
                    appViewModel.searchApp(searchText.text.toString())
                }
            }.start()
        }
    }

    fun ignoreBatteryOptimization(activity: Activity) {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.packageName)
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            try { //先调用系统显示 电池优化权限
                @SuppressLint("BatteryLife") val intent =
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = ("package:" + activity.packageName).toUri()
                startActivity(intent)
            } catch (_: Exception) { //如果失败了则引导用户到电池优化界面
                try {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    val cn =
                        ComponentName.unflattenFromString("com.android.settings/.Settings\$HighPowerApplicationsActivity")
                    intent.component = cn
                    startActivity(intent)
                } catch (_: Exception) { //如果全部失败则说明没有电池优化功能
                }
            }
        }
    }
}