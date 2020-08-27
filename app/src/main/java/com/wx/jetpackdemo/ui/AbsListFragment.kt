package com.wx.jetpackdemo.ui

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
import com.wx.jetpackdemo.R
import com.wx.jetpackdemo.databinding.LayoutRefreshViewBinding
import java.lang.reflect.ParameterizedType

abstract class AbsListFragment<T, VM : AbsViewModel<T>> : Fragment(), OnRefreshListener, OnLoadMoreListener {
    private lateinit var binding: LayoutRefreshViewBinding
    private var adapter : PagedListAdapter<T, RecyclerView.ViewHolder>? = null
    private var viewModel : VM? = null

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

        adapter = getAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.itemAnimator = null
        val decoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        context?.also {context -> ContextCompat.getDrawable(context, R.drawable.list_divider)?.also { decoration.setDrawable(it) } }
        binding.recyclerView.addItemDecoration(decoration)

        afterCreateView()
        return binding.root
    }

    abstract fun afterCreateView()

    fun submit(pagedList: PagedList<T>) {
        pagedList.takeIf { it.size > 0 }?.also { adapter?.submitList(it) }
        finishRefresh(pagedList.size > 0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val type: ParameterizedType = javaClass.genericSuperclass as ParameterizedType
        val arguments = type.actualTypeArguments
        arguments.takeIf { it.size > 1 }?.also {
            val argument = it[1]
            val modelClazz = (argument as Class<*>).asSubclass(AbsViewModel::class.java)
            viewModel = ViewModelProvider(this).get(modelClazz) as  VM?
            viewModel?.getPageData()?.observe(viewLifecycleOwner,
                Observer { pagedList -> submit(pagedList) })
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

    abstract fun getAdapter() : PagedListAdapter<T, RecyclerView.ViewHolder>
}