package com.mooc.ppjoke.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mooc.ppjoke.utils.BindingAdapters
import com.mooc.libcommon.utils.PixUtils
import jp.wasabeef.glide.transformations.BlurTransformation

class CustomImageView : AppCompatImageView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun bindData(
        width: Int,
        height: Int,
        marginLeft: Int,
        maxWidth: Int = PixUtils.getScreenWidth(),
        maxHeight: Int = PixUtils.getScreenWidth(),
        imageUrl: String?
    ) {
        if (width <= 0 || height <= 0) {
            Glide.with(this).load(imageUrl).into(object : CustomTarget<Drawable>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    setSize(
                        resource.intrinsicWidth,
                        resource.intrinsicHeight,
                        marginLeft,
                        maxWidth,
                        maxHeight
                    )
                    setImageDrawable(resource)
                }

            })
            return
        }
        setSize(width, height, marginLeft, maxWidth, maxHeight)
        BindingAdapters.setImageUrl(this, imageUrl, false)
    }

    private fun setSize(width: Int, height: Int, marginLeft: Int, maxWidth: Int, maxHeight: Int) {
        val finalWidth: Int
        val finalHight: Int
        if (width > height) {
            finalWidth = maxWidth
            finalHight = (height * (finalWidth * 1.0f / width)).toInt()
        } else {
            finalHight = maxHeight
            finalWidth = (width * (finalHight * 1.0f / height)).toInt()
        }
        val layoutParams = ViewGroup.MarginLayoutParams(finalWidth, finalHight)
        layoutParams.marginStart = if (height > width) marginLeft else 0
        setLayoutParams(layoutParams)
    }

    @SuppressLint("CheckResult")
    fun setBlurImageUrl(coverUrl: String?, radius: Int) {
        Glide.with(this).load(coverUrl)
            .transform(BlurTransformation())
            .override(50)
            .dontAnimate()
            .into(object : CustomTarget<Drawable>(){
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    background = resource
                }
            })

    }
}