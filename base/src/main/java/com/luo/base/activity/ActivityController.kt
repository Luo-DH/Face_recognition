package com.luo.base.activity

import android.app.Activity

/**
 * @author: Luo-DH
 * @date: 2021/6/18
 *  activity管理类，当创建或者销毁activity时，从这个单例中维护
 *  的list中添加或者删除。
 */
object ActivityController {

    private val activities = ArrayList<Activity>()

    /**
     * 往list中添加一个activity
     * @param activity Activity
     */
    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    /**
     * 往list中删除指定activity
     * @param activity Activity
     */
    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    /**
     * 销毁所有的activity
     */
    fun finishAll() {
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
        // 杀死当前进程
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}