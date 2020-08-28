package com.mooc.ppjoke.utils

import android.content.ComponentName
import androidx.fragment.app.FragmentActivity
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import com.mooc.ppjoke.FixFragmentNavigator
import com.mooc.ppjoke.model.Destination
import com.mooc.libcommon.AppGlobals

object NavGraphBuilder {

    fun build(controller : NavController, activity: FragmentActivity, containerId : Int) {
        val provider = controller.navigatorProvider
//        val fragmentNavigator = provider.getNavigator(FragmentNavigator::class.java)
        val fragmentNavigator = FixFragmentNavigator(activity, activity.supportFragmentManager, containerId)
        provider.addNavigator(fragmentNavigator)
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