package com.mooc.ppjoke.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mooc.ppjoke.databinding.LayoutFeedTypeImageBinding
import com.mooc.ppjoke.databinding.LayoutFeedTypeVideoBinding
import com.mooc.ppjoke.model.Feed

class FeedAdapter(val context: Context?, var category : String) :
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
        return getItem(position)?.itemType ?: 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = if (viewType == Feed.TYPE_IMAGE) {
            LayoutFeedTypeImageBinding.inflate(inflater)
        } else {
            LayoutFeedTypeVideoBinding.inflate(inflater)
        }
        return ViewHolder(binding.root, binding, category)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.also {
            context?.also { context -> holder.bindData(it, context) }
        }
    }

    class ViewHolder(itemView : View, var binding : ViewDataBinding, var category: String) : RecyclerView.ViewHolder(itemView) {
        fun bindData(item: Feed, context: Context) {
            if (binding is LayoutFeedTypeImageBinding) {
                val imageBinding = binding as LayoutFeedTypeImageBinding
                imageBinding.feed = item
                imageBinding.feedImage.bindData(item.width, item.height, 16, imageUrl = item.cover)
            } else {
                val videoBinding = binding as LayoutFeedTypeVideoBinding
                videoBinding.feed = item
                videoBinding.listPlayerView.bindData(category, item.width, item.height, item.cover, item.url)
            }
            binding.lifecycleOwner = context as LifecycleOwner
        }

    }
}