package com.h3110w0r1d.t9launcher.widgets

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.vo.AppInfo
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri

@SuppressLint("InflateParams")
class AppPopMenu(private val context: Context) : PopupWindow(context) {
    private val popHeight: Int

    private var currentApp: AppInfo? = null

    init {
        isFocusable = true
        isOutsideTouchable = true
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val contentView = LayoutInflater.from(context).inflate(R.layout.layout_app_pop, null)
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        popHeight = contentView.measuredHeight

        contentView.findViewById<View>(R.id.pop_app_info)
            .setOnClickListener { v: View? ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = ("package:" + currentApp!!.packageName).toUri()
                this.context.startActivity(intent)
                dismiss()
            }

        contentView.findViewById<View>(R.id.pop_copy_package_name)
            .setOnClickListener { v: View? ->
                val clipboard =
                    this.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("text", currentApp!!.packageName)
                clipboard.setPrimaryClip(clip)
                dismiss()
            }

        contentView.findViewById<View>(R.id.pop_uninstall_app)
            .setOnClickListener { v: View? ->
                val uri = Uri.fromParts("package", currentApp!!.packageName, null)
                val intent = Intent(Intent.ACTION_DELETE, uri)
                this.context.startActivity(intent)
                dismiss()
            }

        setContentView(contentView)
    }

    /**
     * 在指定位置展示长按菜单
     */
    fun show(v: View?, currentApp: AppInfo?) {
        this.currentApp = currentApp

        val location = IntArray(2)
        v?.getLocationOnScreen(location)
        showAtLocation(v, Gravity.START or Gravity.TOP, location[0], location[1] - popHeight)
    }
}