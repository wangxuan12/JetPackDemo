package com.mooc.ppjoke.exoplayer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

class PageListPlayDetector(owner: LifecycleOwner, val recyclerView: RecyclerView) {
    private val targets = mutableListOf<IPlayTarget>()
    fun addTarget(target: IPlayTarget) = targets.add(target)
    fun removeTarget(target: IPlayTarget) = targets.remove(target)

    private var playingTarget: IPlayTarget? = null

    private val dataObserver = object : RecyclerView.AdapterDataObserver(){
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            postAutoPlay()
        }
    }

    val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) autoPlay()
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dx == 0 && dy == 0) {
                //时序问题。当执行了AdapterDataObserver#onItemRangeInserted  可能还没有被布局到RecyclerView上。
                //所以此时 recyclerView.getChildCount()还是等于0的。
                //等childView 被布局到RecyclerView上之后，会执行onScrolled（）方法
                //并且此时 dx,dy都等于0
                postAutoPlay()
            } else {
                //如果有正在播放的,且滑动时被划出了屏幕 则停止
                if (playingTarget?.let { it.isPlaying() && !isTargetInBounds(it) } == true) playingTarget?.inActive()
            }
        }
    }

    init {
        owner.lifecycle.addObserver(object : LifecycleEventObserver{
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    playingTarget = null
                    targets.clear()
                    recyclerView.adapter?.unregisterAdapterDataObserver(dataObserver)
                    recyclerView.removeCallbacks(delayAutoPlay)
                    recyclerView.removeOnScrollListener(scrollListener)
                    owner.lifecycle.removeObserver(this)
                }
            }
        })
        recyclerView.adapter?.registerAdapterDataObserver(dataObserver)
        recyclerView.addOnScrollListener(scrollListener)
    }

    private fun postAutoPlay() {
        recyclerView.post(delayAutoPlay)
        // TODO: 2020/8/31
        //使用闭包这个方法不走就很奇怪
//        recyclerView.post { delayAutoPlay }
    }

    val delayAutoPlay = Runnable { autoPlay() }

    private fun autoPlay() {
        if (targets.size <= 0 || recyclerView.childCount <= 0) return
        if (playingTarget?.let { it.isPlaying() && isTargetInBounds(it) } == true) return

        var activeTarget: IPlayTarget? = null
        for (target: IPlayTarget in targets) {
            if (isTargetInBounds(target)) {
                activeTarget = target
                break
            }
        }
        activeTarget?.also {
            playingTarget?.inActive()
            playingTarget = it
            playingTarget?.onActive()
        }
    }

    /**
     * 检测 IPlayTarget 所在的 viewGroup 是否至少还有一半的大小在屏幕内
     */
    fun isTargetInBounds(target: IPlayTarget) : Boolean {
        val owner = target.getOwner()
        if (!owner.isShown || !owner.isAttachedToWindow) return false

        val location = IntArray(2)
        owner.getLocationOnScreen(location)

        val center = location[1] + owner.height / 2
        //承载视频播放画面的ViewGroup它需要至少一半的大小 在RecyclerView上下范围内
        return rvLocation?.let { center >= it.first && center <= it.second } ?: false
    }

    private var rvLocation: Pair<Int, Int>? = null
        get() {
            if (field == null) {
                val location = IntArray(2)
                recyclerView.getLocationOnScreen(location)

                val top = location[1]
                val bottom = top + recyclerView.height

                field = Pair(top, bottom)
            }
            return field
        }

    fun onResume() = playingTarget?.onActive()
    fun onPause() = playingTarget?.inActive()
}