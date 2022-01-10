package com.coworkerteam.coworker.utils

import android.app.Activity
import android.util.DisplayMetrics
import android.view.View

class ScreenSizeUtils {
    fun getScreenWidthSize(activity: Activity): Int {
        val outMetrics = DisplayMetrics()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = activity.display
            display?.getRealMetrics(outMetrics)
            return outMetrics.widthPixels
        } else {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
            return outMetrics.widthPixels
        }
    }

    fun getScreenHeightSize(activity: Activity): Int {
        val outMetrics = DisplayMetrics()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = activity.display
            display?.getRealMetrics(outMetrics)
            return outMetrics.heightPixels
        } else {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
            return outMetrics.heightPixels
        }
    }

    fun setViewWidthSize(view: View, width: Int) {
        val layoutParams = view?.layoutParams
        layoutParams?.height = width
        view?.layoutParams = layoutParams
    }

    fun setViewHeightSize(view: View, height: Int) {
        val layoutParams = view?.layoutParams
        layoutParams?.width = height
        view?.layoutParams = layoutParams
    }
}