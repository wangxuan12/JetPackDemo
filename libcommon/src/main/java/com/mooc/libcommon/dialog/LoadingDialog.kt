package com.mooc.libcommon.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.mooc.libcommon.R
import com.mooc.libcommon.utils.PixUtils
import com.mooc.libcommon.view.ViewHelper
import kotlinx.android.synthetic.main.layout_loading_view.*

class LoadingDialog : AlertDialog {

    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    fun setLoadingText(loadingText: String) {
        loading_text?.text = loadingText
    }

    override fun show() {
        super.show()
        setContentView(R.layout.layout_loading_view)
        val attributes = window?.attributes
        attributes?.width = WindowManager.LayoutParams.WRAP_CONTENT
        attributes?.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes?.gravity = Gravity.CENTER
        attributes?.dimAmount = 0.5f
        //这个背景必须设置，否则 会出现对话框 宽度很宽
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //给转loading对话框来一个圆角
        ViewHelper.setViewOutline(loading_layout, PixUtils.dp2px(10), ViewHelper.RADIUS_ALL)
        window?.attributes = attributes
    }
}