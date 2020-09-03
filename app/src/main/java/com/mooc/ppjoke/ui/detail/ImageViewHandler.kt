package com.mooc.ppjoke.ui.detail

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mooc.ppjoke.R
import com.mooc.ppjoke.databinding.ActivityFeedDetailTypeImageBinding
import com.mooc.ppjoke.databinding.LayoutFeedDetailTypeImageHeaderBinding
import com.mooc.ppjoke.model.Feed

class ImageViewHandler(activity: FragmentActivity) : ViewHandler(activity) {
    protected val imageBinding: ActivityFeedDetailTypeImageBinding =
        DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image)
    protected lateinit var headerBinding: LayoutFeedDetailTypeImageHeaderBinding

    init {
        interactionBinding = imageBinding.interactionLayout
        recyclerView = imageBinding.recyclerView
        imageBinding.actionClose.setOnClickListener {
            activity.finish()
        }
    }

    override fun bindData(feed: Feed) {
        super.bindData(feed)
        imageBinding.feed = feed
        imageBinding.authorInfoLayout.owner = activity

        headerBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(
            LayoutInflater.from(activity),
            recyclerView,
            false
        )
        headerBinding.authorInfoLayout.owner = activity
        headerBinding.feed = feed

        headerBinding.headerImage.bindData(
            feed.width,
            feed.height,
            if (feed.width > feed.height) 0 else 16,
            imageUrl = feed.cover
        )
        listAdapter.addHeaderView(headerBinding.root)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visible = headerBinding.root.top <= -imageBinding.titleLayout.measuredHeight
                imageBinding.authorInfoLayout.root.visibility = if (visible) View.VISIBLE else View.GONE
                imageBinding.title.visibility = if (visible) View.GONE else View.VISIBLE
            }
        })
    }
}