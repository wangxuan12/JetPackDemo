package com.mooc.ppjoke.ui.sofa

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mooc.libcommon.utils.PixUtils
import com.mooc.libnavannotation.FragmentDestination
import com.mooc.ppjoke.databinding.FragmentSofaBinding
import com.mooc.ppjoke.model.SofaTab
import com.mooc.ppjoke.model.Tab
import com.mooc.ppjoke.ui.home.HomeFragment
import com.mooc.ppjoke.utils.AppConfig
import kotlinx.android.synthetic.main.fragment_sofa.*

@FragmentDestination(pageUrl = "main/tabs/sofa")
class SofaFragment : Fragment() {

    private lateinit var mediator: TabLayoutMediator
    private lateinit var binding: FragmentSofaBinding
    private var tabConfig: SofaTab? = null
        get() {
            if (field == null) {
                field = AppConfig.getSofaTab()
            }
            return field
        }
    private val tabs: MutableList<Tab> = mutableListOf()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSofaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabConfig?.tabs?.forEach {
            if (it.enable) tabs.add(it)
        }
        //限制页面预加载
        binding.viewPager.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT

        //viewPager2默认只有一种类型的Adapter。FragmentStateAdapter
        //并且在页面切换的时候 不会调用子Fragment的setUserVisibleHint ，取而代之的是onPause(),onResume()、
        binding.viewPager.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle){
            override fun getItemCount(): Int {
                return tabs.size
            }

            override fun createFragment(position: Int): Fragment {
                //这里不需要自己保管了,FragmentStateAdapter内部自己会管理已实例化的fragment对象。
                return getTabFragment(position)
            }
        }
        binding.tabLayout.tabGravity = tabConfig?.tabGravity ?: TabLayout.GRAVITY_START
        //viewPager2 就不能用TabLayout.setUpWithViewPager()了
        //取而代之的是TabLayoutMediator。我们可以在onConfigureTab()方法的回调里面 做tab标签的配置

        //其中autoRefresh的意思是:如果viewPager2 中child的数量发生了变化，也即调用了adapter#notifyItemChanged()前后getItemCount不同。
        //要不要 重新刷野tabLayout的tab标签。视情况而定,像sofaFragment的tab数量一旦固定了是不会变的，传true/false  都问题不大
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager, false,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.customView = makeTabView(position)
            })
        mediator.attach()

        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
        //切换到默认选择项,要等待初始化完成之后才有效
        binding.viewPager.post { binding.viewPager.setCurrentItem(tabConfig?.select ?: 0, false) }
    }

    private val pageChangeCallback =  object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val tabCount = binding.tabLayout.tabCount
            for (i: Int in 0 until tabCount) {
                val tab = binding.tabLayout.getTabAt(i)
                tab?.also {
                    val customView = it.customView as TextView?
                    if (it.position == position) {
                        customView?.textSize = tabConfig?.activeSize?.toFloat() ?: PixUtils.dp2px(16).toFloat()
                        customView?.typeface = Typeface.DEFAULT_BOLD
                    } else {
                        customView?.textSize = tabConfig?.normalSize?.toFloat() ?: PixUtils.dp2px(14).toFloat()
                        customView?.typeface = Typeface.DEFAULT
                    }
                }
            }
        }
    }

    private fun makeTabView(position: Int): View {
        val tabView = TextView(context)
        val states = Array(2) { IntArray(1) }
        states[0] = intArrayOf(android.R.attr.state_selected)
        val colors = tabConfig?.let { intArrayOf(Color.parseColor(it.activeColor), Color.parseColor(it.normalColor)) }
        val colorStateList = ColorStateList(states, colors)
        tabView.setTextColor(colorStateList)
        tabView.text = tabs[position].title
        tabView.textSize = tabConfig?.normalSize?.toFloat() ?: PixUtils.dp2px(14).toFloat()
        return tabView
    }

    private fun getTabFragment(position: Int): Fragment {
        return HomeFragment.newInstance(tabs[position].tag)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        for (fragment: Fragment in childFragmentManager.fragments) {
            if (fragment.isAdded && fragment.isVisible) {
                fragment.onHiddenChanged(hidden)
                break
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("SofaFragment", "onResume: " )
    }

    override fun onPause() {
        super.onPause()
        Log.e("SofaFragment", "onPause: " )
    }

    override fun onDestroy() {
        mediator.detach()
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroy()
    }
}