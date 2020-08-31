package com.mooc.ppjoke.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.mooc.ppjoke.R
import com.mooc.ppjoke.databinding.LayoutRefreshViewBinding
import java.lang.reflect.ParameterizedType

abstract class AbsListFragment<T, VM : AbsViewModel<T>> : Fragment(), OnRefreshListener, OnLoadMoreListener {
    private lateinit var binding: LayoutRefreshViewBinding
    protected var adapter : PagedListAdapter<T, RecyclerView.ViewHolder>? = null
    protected var viewModel : VM? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutRefreshViewBinding.inflate(inflater, container, false)

        binding.refreshLayout.setEnableRefresh(true)
        binding.refreshLayout.setEnableLoadMore(true)
        binding.refreshLayout.setOnRefreshListener(this)
        binding.refreshLayout.setOnLoadMoreListener(this)

        adapter = createAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.itemAnimator = null
        val decoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        context?.also {context -> ContextCompat.getDrawable(context, R.drawable.list_divider)?.also { decoration.setDrawable(it) } }
        binding.recyclerView.addItemDecoration(decoration)

        genericViewModel()
        return binding.root
    }


    fun submitList(pagedList: PagedList<T>) {
        pagedList.takeIf { it.size > 0 }?.also { adapter?.submitList(it) }
        finishRefresh(pagedList.size > 0)
    }

    fun genericViewModel() {
        //利用 子类传递的 泛型参数实例化出absViewModel 对象。
        val type: ParameterizedType = javaClass.genericSuperclass as ParameterizedType
        val arguments = type.actualTypeArguments
        arguments.takeIf { it.size > 1 }?.also {
            val argument = it[1]
            val modelClazz = (argument as Class<*>).asSubclass(AbsViewModel::class.java)
            viewModel = ViewModelProvider(this).get(modelClazz) as  VM?

            //触发页面初始化数据加载的逻辑
            viewModel?.getPageData()?.observe(viewLifecycleOwner,
                Observer { pagedList -> submitList(pagedList) })

            //监听分页时有无更多数据,以决定是否关闭上拉加载的动画
            viewModel?.getBoundaryPageData()?.observe(viewLifecycleOwner, Observer { hasData -> finishRefresh(hasData) })
        }
    }

    fun finishRefresh(hasData: Boolean) {
        val state = binding.refreshLayout.state
        if (state.isFooter && state.isOpening) {
            binding.refreshLayout.finishLoadMore()
        } else if (state.isHeader && state.isOpening) {
            binding.refreshLayout.finishRefresh()
        }

        val currentList = adapter?.currentList
        if (hasData || currentList != null && currentList.size > 0) {
            binding.emptyView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.VISIBLE
        }
    }

    abstract fun createAdapter() : PagedListAdapter<T, RecyclerView.ViewHolder>
}