package com.mooc.libcommon.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

/**
 * dispatchKeyEventPreIme 复写这个方案 可以在对话框弹框中 ，监听backPress事件。以销毁对话框
 */
class CustomEditText: AppCompatEditText {
    private var keyEvent: () -> Boolean = { false }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            if (keyEvent.invoke()) return true
        }
        return super.dispatchKeyEventPreIme(event)
    }

    fun onKeyEvent(block: () -> Boolean) {
        keyEvent = block
    }
}