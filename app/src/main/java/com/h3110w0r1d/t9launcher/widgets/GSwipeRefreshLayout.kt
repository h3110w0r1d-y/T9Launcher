package com.h3110w0r1d.t9launcher.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.GridView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class GSwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SwipeRefreshLayout(context, attrs) {
    private var mHasScrollingChild = false
    private var mScrollingChild: GridView? = null
    private var mDragUp = false
    private var mDragPosition = 0f

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1 && getChildAt(1) is GridView) {
            mHasScrollingChild = true
            mScrollingChild = getChildAt(1) as GridView?
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (mHasScrollingChild) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    isEnabled = true
                    mDragPosition = ev.y
                    if (mScrollingChild?.getChildAt(0) != null && mScrollingChild?.getChildAt(0)?.top!! < 45) {
                        isEnabled = false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mDragUp) {
                        return super.dispatchTouchEvent(ev)
                    }
                    if (ev.y - mDragPosition < 0) {
                        mDragUp = true
                        isEnabled = false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mDragUp = false
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}