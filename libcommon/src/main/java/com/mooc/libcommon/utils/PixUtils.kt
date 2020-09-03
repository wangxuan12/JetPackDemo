package com.mooc.libcommon.utils

import com.mooc.libcommon.global.AppGlobals

object PixUtils {
    @JvmStatic
    fun dp2px(dpValue : Int) : Int {
        val metrics = AppGlobals.getApplication().resources.displayMetrics
        return (metrics.density * dpValue + 0.5f).toInt()
    }

    fun getScreenWidth(): Int {
        return AppGlobals.getApplication().resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(): Int {
        return AppGlobals.getApplication().resources.displayMetrics.heightPixels
    }
}