package com.h3110w0r1d.t9launcher.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class GSwipeRefreshLayout extends SwipeRefreshLayout {
    private boolean mHasScrollingChild = false;
    private GridView mScrollingChild = null;
    private boolean mDragUp = false;
    private float mDragPosition = 0;

    public GSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public GSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount() > 1 && getChildAt(1) instanceof GridView) {
            mHasScrollingChild = true;
            mScrollingChild = (GridView) getChildAt(1);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mHasScrollingChild) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setEnabled(true);
                    mDragPosition = ev.getY();
                    if(mScrollingChild.getChildAt(0) != null &&  mScrollingChild.getChildAt(0).getTop() < 45) {
                        setEnabled(false);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mDragUp) {
                        break;
                    }
                    if (ev.getY() - mDragPosition < 0) {
                        mDragUp = true;
                        setEnabled(false);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mDragUp = false;
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}