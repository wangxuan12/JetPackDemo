package com.wx.jetpackdemo.utils

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

object BindingAdapters {

    @SuppressLint("CheckResult")
    @BindingAdapter("app:image_url", "app:isCircle", requireAll = true)
    @JvmStatic fun setImageUrlBinding(view : ImageView, imageUrl : String?, isCircle : Boolean) {
        val builder = Glide.with(view).load(imageUrl)
        if (isCircle) builder.transform(CircleCrop())
        view.layoutParams?.takeIf { it.width > 0 && it.height > 0 }
            ?.also { builder.override(it.width, it.height) }
        builder.into(view)
    }

    fun setImageUrl(view : ImageView, imageUrl : String?, isCircle : Boolean) {
        val builder = Glide.with(view).load(imageUrl)
        if (isCircle) builder.transform(CircleCrop())
        view.layoutParams?.takeIf { it.width > 0 && it.height > 0 }
            ?.also { builder.override(it.width, it.height) }
        builder.into(view)
    }
}