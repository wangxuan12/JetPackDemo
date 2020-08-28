package com.mooc.libcommon

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import kotlinx.android.synthetic.main.layout_empty_view.view.*

class EmptyView : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_empty_view, this, true)
        orientation = VERTICAL
        gravity = Gravity.CENTER
    }

    fun setEmptyIcon(@DrawableRes iconRes : Int) {
        empty_icon.setImageResource(iconRes)
    }

    fun setTitle(text : String) {
        if (TextUtils.isEmpty(text)) {
            empty_text.visibility = View.GONE
        } else {
            empty_text.text = text
            empty_text.visibility = View.VISIBLE
        }
    }

    fun setButton(text : String, listener : OnClickListener) {
        if (TextUtils.isEmpty(text)) {
            empty_action.visibility = View.GONE
        } else {
            empty_action.text = text
            empty_action.visibility = View.VISIBLE
            empty_action.setOnClickListener(listener)
        }
    }
}