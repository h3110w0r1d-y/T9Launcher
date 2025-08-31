package com.h3110w0r1d.t9launcher.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.vo.AppInfo

@SuppressLint("ViewConstructor")
class HideAppListView(context: Context, attrs: AttributeSet?) : ListView(context, attrs) {
    private val adapter: HideAppListAdapter
    private var appInfo: ArrayList<AppInfo>
    private val selectedPackages: MutableList<String> = ArrayList()

    init {
        appInfo = ArrayList<AppInfo>()
        adapter = HideAppListAdapter(context, R.layout.hide_app_list_item, appInfo)
    }

    fun updateAppInfo(appInfo: ArrayList<AppInfo>) {
        this.appInfo = appInfo
        adapter.notifyDataSetChanged()
    }

    private inner class HideAppListAdapter(
        context: Context,
        resource: Int,
        objects: MutableList<*>
    ) : ArrayAdapter<Any?>(context, resource, objects) {
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var view = view
            if (view == null) {
                view = LayoutInflater.from(getContext())
                    .inflate(R.layout.hide_app_list_item, parent, false)
            }
            val app = appInfo[position]

            val icon = view.findViewById<ImageView>(R.id.appIcon)
            val text1 = view.findViewById<TextView>(R.id.text1)
            val text2 = view.findViewById<TextView>(R.id.text2)
            val checkBox = view.findViewById<CheckBox>(R.id.checkbox)

            icon.setImageDrawable(app.appIcon)
            text1.text = app.appName
            text2.text = app.packageName
            checkBox.isChecked = selectedPackages.contains(app.packageName)

            return view
        }
    }
}