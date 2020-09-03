package com.mooc.ppjoke.utils

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation

object BindingAdapters {

    @SuppressLint("CheckResult")
    @BindingAdapter("image_url", "isCircle", requireAll = true)
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

    fun setImageUrl(view : ImageView, imageUrl : String?) = setImageUrl(view, imageUrl, false)

    @SuppressLint("CheckResult")
    fun setBlurImageUrl(view: ImageView, coverUrl: String?, radius: Int) {
        Glide.with(view).load(coverUrl)
            .override(radius)
            .transform(BlurTransformation())
            .dontAnimate()
            .into(object : CustomTarget<Drawable>(){
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    view.background = resource
                }
            })
    }
}