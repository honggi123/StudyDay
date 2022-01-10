package com.coworkerteam.coworker.ui.statistics

import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import android.content.Context;
import android.util.AttributeSet;

class ScrollingCalendarBehavior(context: Context?, attrs: AttributeSet?) :
    AppBarLayout.Behavior(context, attrs) {
    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        ev: MotionEvent
    ): Boolean {
        return false /*super.onInterceptTouchEvent(parent, child, ev);*/
    }
}