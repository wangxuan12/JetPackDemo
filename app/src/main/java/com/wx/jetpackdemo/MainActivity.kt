package com.wx.jetpackdemo

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wx.jetpackdemo.ui.login.UserManager
import com.wx.jetpackdemo.utils.AppConfig
import com.wx.jetpackdemo.utils.NavGraphBuilder

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var navView: BottomNavigationView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        //由于 启动时设置了 R.style.launcher 的windowBackground属性
        //势必要在进入主页后,把窗口背景清理掉
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)

        NavGraphBuilder.build(navController, this, R.id.nav_host_fragment)

        navView.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val destConfig = AppConfig.getDestConfig()
        //遍历 target destination 是否需要登录拦截
        for ((k, v) in destConfig) {
            if (v.id == item.itemId && v.needLogin && !UserManager.isLogin()) {
                UserManager.login(this).observe(this, Observer {
                    navView.selectedItemId = item.itemId
                })
                return false
            }
        }
        navController.navigate(item.itemId)
        return !TextUtils.isEmpty(item.title)
    }
}