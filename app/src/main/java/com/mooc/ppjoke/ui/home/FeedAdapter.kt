package com.mooc.ppjoke.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mooc.ppjoke.BR
import com.mooc.ppjoke.R
import com.mooc.ppjoke.databinding.LayoutFeedTypeImageBinding
import com.mooc.ppjoke.databinding.LayoutFeedTypeVideoBinding
import com.mooc.ppjoke.model.Feed
import com.mooc.ppjoke.ui.detail.FeedDetailActivity
import com.mooc.ppjoke.view.ListPlayerView

open class FeedAdapter(val context: Context?, var category : String) :
    PagedListAdapter<Feed, FeedAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }
    }) {
    private var inflater : LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        val feed = getItem(position)
        feed?.itemType?.also {
            if (it == Feed.TYPE_IMAGE) return R.layout.layout_feed_type_image
            else if (it == Feed.TYPE_VIDEO) return R.layout.layout_feed_type_video
        }
        return 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = if (viewType == Feed.TYPE_IMAGE) {
            DataBindingUtil.inflate<LayoutFeedTypeImageBinding>(inflater, viewType, parent, false)
        } else {
            DataBindingUtil.inflate<LayoutFeedTypeVideoBinding>(inflater, viewType, parent, false)
        }
        return ViewHolder(binding.root, binding, category)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.also {feed ->
            context?.also { context ->
                holder.bindData(feed, context)
                holder.itemView.setOnClickListener {
                    FeedDetailActivity.start(context, feed, category)
                }
            }
        }
    }

    class ViewHolder(itemView : View, var binding : ViewDataBinding, var category: String) : RecyclerView.ViewHolder(itemView) {
        private var listPlayerView: ListPlayerView? = null

        fun bindData(item: Feed, context: Context) {
            //这里之所以手动绑定数据的原因是 图片 和视频区域都是需要计算的
            //而dataBinding的执行默认是延迟一帧的。
            //当列表上下滑动的时候 ，会明显的看到宽高尺寸不对称的问题

            binding.setVariable(BR.feed, item)
            binding.lifecycleOwner = context as LifecycleOwner
            if (binding is LayoutFeedTypeImageBinding) {
                val imageBinding = binding as LayoutFeedTypeImageBinding
                imageBinding.feedImage.bindData(item.width, item.height, 16, imageUrl = item.cover)
            } else if (binding is LayoutFeedTypeVideoBinding){
                val videoBinding = binding as LayoutFeedTypeVideoBinding
                videoBinding.listPlayerView.bindData(category, item.width, item.height, item.cover, item.url ?: "")
                listPlayerView = videoBinding.listPlayerView
            }
        }

        fun isVideoItem(): Boolean = binding is LayoutFeedTypeVideoBinding

        fun getListPlayerView() : ListPlayerView? = listPlayerView

    }
}