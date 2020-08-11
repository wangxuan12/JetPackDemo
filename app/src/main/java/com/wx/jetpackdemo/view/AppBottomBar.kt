package com.wx.jetpackdemo.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.wx.jetpackdemo.R
import com.wx.jetpackdemo.utils.AppConfig

class AppBottomBar : BottomNavigationView {
    private val sIcons = intArrayOf(
        R.drawable.icon_tab_home,
        R.drawable.icon_tab_sofa,
        R.drawable.icon_tab_publish,
        R.drawable.icon_tab_find,
        R.drawable.icon_tab_mine
    )

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    @SuppressLint("RestrictedApi")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val bottomBar = AppConfig.getBottomBar()
        val tabs = bottomBar.tabs

        val states = Array(2) { IntArray(1)}
        states[0] = IntArray(android.R.attr.state_selected)
        val colors = intArrayOf(Color.parseColor(bottomBar.activeColor), Color.parseColor(bottomBar.inActiveColor)) as IntArray
        val colorStateList = ColorStateList(states, colors)
        itemIconTintList = colorStateList
        itemTextColor = colorStateList
        labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED

        for (tab in tabs) {
            if (!tab.enable) {
                break
            }
            val id = getId(tab.pageUrl)
            if (id < 0) break
            val item = menu.add(0, id, tab.index, tab.title)
            item.setIcon(sIcons[tab.index])
        }

        for (tab in tabs) {
            val iconSize = dp2px(tab.size)

            val menuView : BottomNavigationMenuView = getChildAt(0) as BottomNavigationMenuView
//            val itemView : BottomNavigationItemView = menuView.getChildAt(tab.index) as BottomNavigationItemView
            val itemView : BottomNavigationItemView = menuView.findViewById(getId(tab.pageUrl))
            itemView.setIconSize(iconSize)

            if (TextUtils.isEmpty(tab.title)) {
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.tintColor)))
                itemView.setShifting(false)
            }
        }
    }

    private fun dp2px(size : Int) : Int {
        return (context.resources.displayMetrics.density * size + 0.5).toInt()
    }

    private fun getId(pageUrl : String) : Int {
        val destination = AppConfig.getDestConfig()[pageUrl]
        return destination?.id ?: -1
    }
}