package com.mooc.libcommon.extention

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * 一个能够添加HeaderView,FooterView的PagedListAdapter。
 * 解决了添加HeaderView和FooterView时 RecyclerView定位不准确的问题
 *
 * @param <T>  data class
 * @param <VH>
 */
abstract class AbsPagedListAdapter<T, VH : RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>) :
    PagedListAdapter<T, VH>(diffCallback) {
    private val headers = SparseArray<View>()
    private val footers = SparseArray<View>()
    fun getHeaderCount() = headers.size()
    fun getFooterCount() = footers.size()

    private var BASE_ITEM_TYPE_HEADER = 100000
    private var BASE_ITEM_TYPE_FOOTER = 200000

    fun addHeaderView(view: View) {
        //判断给View对象是否还没有处在mHeaders数组里面
        if (headers.indexOfValue(view) < 0) {
            headers.put(BASE_ITEM_TYPE_HEADER++, view)
            notifyDataSetChanged()
        }
    }

    fun addFooterView(view: View) {
        //判断给View对象是否还没有处在mFooters数组里面
        if (footers.indexOfValue(view) < 0) {
            footers.put(BASE_ITEM_TYPE_FOOTER++, view)
            notifyDataSetChanged()
        }
    }

    // 移除头部
    fun removeHeaderView(view: View) {
        headers.indexOfValue(view).takeIf { it >= 0 }?.let {
            headers.remove(it)
            notifyDataSetChanged()
        }
    }

    // 移除底部
    fun removeFooterView(view: View) {
        footers.indexOfValue(view).takeIf { it >= 0 }?.let {
            footers.remove(it)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        val itemCount = super.getItemCount()
        return itemCount + headers.size() + footers.size()
    }

    fun getOriginalItemCount(): Int = itemCount - headers.size() - footers.size()

    override fun getItemViewType(position: Int): Int {
        //返回该position对应的headerview的  viewType
        if (isHeaderPosition(position)) return headers.keyAt(position)
        //footer类型的，需要计算一下它的position实际大小
        if (isFooterPosition(position)) return footers.keyAt(position - getOriginalItemCount() - headers.size())
        return getItemViewType2(position - headers.size())
    }

    open protected fun getItemViewType2(i: Int): Int = 0

    private fun isFooterPosition(position: Int): Boolean {
        return position >= getOriginalItemCount() + headers.size()
    }

    private fun isHeaderPosition(position: Int): Boolean {
        return position < headers.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        if (headers.indexOfKey(viewType) >= 0) return object :
            RecyclerView.ViewHolder(headers.get(viewType)) {} as VH
        if (footers.indexOfKey(viewType) >= 0) return object :
            RecyclerView.ViewHolder(footers.get(viewType)) {} as VH
        return onCreateViewHolder2(parent, viewType)
    }

    abstract fun onCreateViewHolder2(parent: ViewGroup, viewType: Int): VH

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (isHeaderPosition(position) || isFooterPosition(position)) return
        //列表中正常类型的itemView的 position 咱们需要减去添加headerView的个数
        onBindViewHolder2(holder, position - headers.size())
    }

    abstract fun onBindViewHolder2(holder: VH, i: Int)

    override fun onViewAttachedToWindow(holder: VH) {
        if (!isHeaderPosition(holder.adapterPosition) && !isFooterPosition(holder.adapterPosition)) {
            onViewAttachedToWindow2(holder)
        }
    }

    open fun onViewAttachedToWindow2(holder: VH){}

    override fun onViewDetachedFromWindow(holder: VH) {
        if (!isHeaderPosition(holder.adapterPosition) && !isFooterPosition(holder.adapterPosition)) {
            onViewDetachedFromWindow2(holder)
        }
    }

    open fun onViewDetachedFromWindow2(holder: VH){}

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.registerAdapterDataObserver(AdapterDataObserverProxy(observer))
    }

    //如果先添加了headerView,而后网络数据回来了再更新到列表上
    //由于Paging在计算列表上item的位置时 并不会顾及我们有没有添加headerView，就会出现列表定位的问题
    //实际上 RecyclerView#setAdapter方法，它会给Adapter注册了一个AdapterDataObserver
    //那么可以代理registerAdapterDataObserver()传递进来的observer。在各个方法的实现中，把headerView的个数算上，再中转出去即可
    private inner class AdapterDataObserverProxy(var observer: RecyclerView.AdapterDataObserver): RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            observer.onChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            observer.onItemRangeChanged(positionStart + headers.size(), itemCount)
        }

        override fun onItemRangeChanged(
            positionStart: Int,
            itemCount: Int,
            payload: Any?
        ) {
            observer.onItemRangeChanged(positionStart + headers.size(), itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            observer.onItemRangeInserted(positionStart + headers.size(), itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            observer.onItemRangeRemoved(positionStart + headers.size(), itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            observer.onItemRangeMoved(fromPosition + headers.size(), toPosition + headers.size(), itemCount)
        }
    }
}