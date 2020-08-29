package com.mooc.libcommon.view

import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import com.mooc.libcommon.R

object ViewHelper {
    const val RADIUS_ALL = 0
    const val RADIUS_LEFT = 1
    const val RADIUS_TOP = 2
    const val RADIUS_RIGHT = 3
    const val RADIUS_BOTTOM = 4

    fun setViewOutline(
        view: View,
        attributeSet: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val typeArray = view.context.obtainStyledAttributes(attributeSet, R.styleable.viewOutLineStrategy, defStyleAttr, defStyleRes)
        val radius = typeArray.getDimensionPixelSize(R.styleable.viewOutLineStrategy_clip_radius, 0)
        val side = typeArray.getInt(R.styleable.viewOutLineStrategy_clip_side, 0)
        typeArray.recycle()
        setViewOutline(view, radius, side)
    }

    fun setViewOutline(owner: View, radius: Int, radiusSide: Int) {
        owner.outlineProvider = object : ViewOutlineProvider(){
            override fun getOutline(view: View?, outline: Outline?) {
                val w = view?.width ?: 0
                val h = view?.height ?: 0
                if (w == 0 || h == 0) return

                var left = 0; var top = 0; var right = w; var bottom = h;
                when (radiusSide) {
                    RADIUS_LEFT -> right += radius
                    RADIUS_TOP -> bottom += radius
                    RADIUS_RIGHT -> left -= radius
                    RADIUS_BOTTOM -> top -= radius
                    RADIUS_ALL -> if (radius <= 0) outline?.setRect(left, top, right, bottom)
                }
                outline?.setRoundRect(left, top, right, bottom, radius.toFloat())
            }
        }
        owner.clipToOutline = radius > 0
        owner.invalidate()
    }
}