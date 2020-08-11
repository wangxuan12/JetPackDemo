package com.wx.jetpackdemo.ui.utils

import android.content.ComponentName
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.fragment.FragmentNavigator
import com.wx.jetpackdemo.MainApplication
import com.wx.jetpackdemo.model.Destination

object NavGraphBuilder {

    fun build(controller : NavController) {
        val provider = controller.navigatorProvider
        val fragmentNavigator = provider.getNavigator(FragmentNavigator::class.java)
        val activityNavigator = provider.getNavigator(ActivityNavigator::class.java)

        val navGraph = NavGraph(NavGraphNavigator(provider))
        val destConfig = AppConfig.getDestConfig()
        for (value : Destination in destConfig.values) {
            if (value.isFragment) {
                val destination = fragmentNavigator.createDestination()
                destination.className = value.clazzName
                destination.id = value.id
                destination.addDeepLink(value.pageUrl)
                navGraph.addDestination(destination)
            } else {
                val destination = activityNavigator.createDestination()
                destination.setComponentName(ComponentName(AppGlobals.getApplication().packageName, value.clazzName))
                destination.id = value.id
                destination.addDeepLink(value.pageUrl)
                navGraph.addDestination(destination)
            }

            if (value.asStarter) navGraph.startDestination = value.id
        }

        controller.graph = navGraph
    }
}