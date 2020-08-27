package com.wx.jetpackdemo.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.wx.jetpackdemo.R
import com.wx.jetpackdemo.utils.BindingAdapters
import com.wx.libcommon.PixUtils
import kotlinx.android.synthetic.main.layout_player_view.view.*

class ListPlayerView : FrameLayout {
    private lateinit var category: String
    private var videoUrl: String? = null
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true)
    }

    fun bindData(category: String, width: Int, height: Int, coverUrl: String?, videoUrl: String?) {
        this.category = category
        this.videoUrl = videoUrl

        BindingAdapters.setImageUrl(cover, coverUrl, false)
        if (width < height) {
            blur_backgroud.setBlurImageUrl(coverUrl, 10)
            blur_backgroud.visibility = View.VISIBLE
        } else {
            blur_backgroud.visibility = View.INVISIBLE
        }
        setSize(width, height)
    }

    private fun setSize(width: Int, height: Int) {
        val maxWidth = PixUtils.getScreenWidth()
        val maxHight = maxWidth

        val layoutWidth = maxWidth
        val layoutHeight: Int

        val coverWidht : Int
        val coverHeight : Int

        if (width >= height) {
            coverWidht = maxWidth
            layoutHeight = (height * (maxWidth * 1.0f / width)).toInt()
            coverHeight = layoutHeight
        } else {
            layoutHeight = maxHight
            coverHeight = maxHight
            coverWidht = (width * (maxHight * 1.0f / height)).toInt()
        }
        val params = layoutParams
        params.width = layoutWidth
        params.height = layoutHeight
        layoutParams = params

        val blurParams = blur_backgroud.layoutParams
        blurParams.width = layoutWidth
        blurParams.height = height
        blur_backgroud.layoutParams = blurParams

        val coverParams = cover.layoutParams as FrameLayout.LayoutParams
        coverParams.width = coverWidht
        coverParams.height = coverHeight
        coverParams.gravity = Gravity.CENTER
        cover.layoutParams = coverParams

        val playBtnParams = play_btn.layoutParams as FrameLayout.LayoutParams
        playBtnParams.gravity = Gravity.CENTER
        play_btn.layoutParams = playBtnParams

    }
}