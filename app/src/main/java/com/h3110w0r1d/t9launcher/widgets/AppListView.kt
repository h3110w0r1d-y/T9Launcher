package com.h3110w0r1d.t9launcher.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.vo.AppInfo

class AppListView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridView(context, attrs, defStyleAttr), OnTouchListener {
    private val adapter: AppListAdapter

    private var listener: OnItemClickListener

    private var appInfo: ArrayList<AppInfo>

    init {
        appInfo = ArrayList<AppInfo>()
        adapter = AppListAdapter()
        setAdapter(adapter)
        listener = object : OnItemClickListener {
            override fun onItemClick(v: View?, app: AppInfo?) {}
            override fun onItemLongClick(v: View?, app: AppInfo?) {}
        }
    }

    fun updateAppInfo(appInfo: ArrayList<AppInfo>) {
        this.appInfo = appInfo
        adapter.notifyDataSetChanged()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start()
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        }
        return false
    }

    private inner class AppListAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return appInfo.size
        }

        override fun getItem(position: Int): Any? {
            return appInfo.get(position)
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView =
                    LayoutInflater.from(context).inflate(R.layout.app_item, parent, false)
            }

            val app = appInfo[position]
            val courseTV = convertView.findViewById<TextView>(R.id.idTVApp)
            val courseIV = convertView.findViewById<ImageView>(R.id.idIVApp)
            courseTV.text = app.appName

            courseIV.setImageDrawable(app.appIcon)

            convertView.setOnTouchListener(this@AppListView)
            convertView.setOnClickListener { v: View? ->
                listener.onItemClick(
                    v,
                    appInfo[position]
                )
            }
            convertView.setOnLongClickListener { v: View? ->
                v!!.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
                listener.onItemLongClick(v, appInfo[position])
                true
            }

            return convertView
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(v: View?, app: AppInfo?)

        fun onItemLongClick(v: View?, app: AppInfo?)
    }
}
