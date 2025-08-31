package com.h3110w0r1d.t9launcher.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.h3110w0r1d.t9launcher.App
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.model.AppViewModel
import com.h3110w0r1d.t9launcher.vo.AppInfo

class HideAppActivity : AppCompatActivity() {
    private val appViewModel: AppViewModel by lazy { (application as App).appViewModel }

    private val hideAppList: MutableList<AppInfo> = ArrayList()

    private val adapter: HideAppListAdapter by lazy {
        HideAppListAdapter(
            this,
            R.layout.hide_app_list_item,
            hideAppList
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_hide_app)

        findViewById<ListView>(R.id.hideAppListView).adapter = adapter

        appViewModel.hideAppListLiveData.observe(
            this,
            Observer { searchResult: ArrayList<AppInfo> ->
                adapter.clear()
                adapter.addAll(searchResult)
            })
        appViewModel.searchHideApp("")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        appViewModel.saveHideList()
        super.finish()
    }

    override fun onBackPressed() {
        appViewModel.saveHideList()
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.hide_app_menu, menu)

        val sv = menu.findItem(R.id.search).actionView as SearchView
        //设置监听
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                appViewModel.searchHideApp(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                appViewModel.searchHideApp(query)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    inner class HideAppListAdapter(
        context: Context,
        resource: Int,
        objects: MutableList<AppInfo>
    ) : ArrayAdapter<AppInfo?>(context, resource, objects) {
        var array: MutableList<AppInfo> = objects

        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var view = view
            if (view == null) {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.hide_app_list_item, parent, false)
            }
            val app = array[position]

            val icon = view.findViewById<ImageView>(R.id.appIcon)
            val text1 = view.findViewById<TextView>(R.id.text1)
            val text2 = view.findViewById<TextView>(R.id.text2)
            val checkBox = view.findViewById<CheckBox>(R.id.checkbox)

            icon.setImageDrawable(app.appIcon)
            text1.text = app.appName
            text2.text = app.packageName
            checkBox.isChecked = appViewModel.isAppHide(app.packageName)

            view.setOnClickListener { v: View? ->
                checkBox.isChecked = !checkBox.isChecked
                appViewModel.setAppHide(app.packageName, checkBox.isChecked)
            }

            return view
        }
    }
}