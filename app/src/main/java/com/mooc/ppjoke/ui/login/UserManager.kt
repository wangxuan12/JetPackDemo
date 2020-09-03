package com.mooc.ppjoke.ui.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mooc.ppjoke.model.User
import com.mooc.libnetwork.cache.CacheManager

object UserManager {
    private const val KEY_CACHE_USER = "key_cache_user"
    private val userLiveData: MutableLiveData<User> = MutableLiveData()
    private var mUser : User? = null

    init {
        val user = CacheManager.getCache(KEY_CACHE_USER) as User?
        user?.takeIf { it.expires_time > System.currentTimeMillis() }?.let { mUser = it }
    }

    fun save(user : User) {
        mUser = user
        CacheManager.save(KEY_CACHE_USER, user)
        userLiveData.takeIf { it.hasObservers() }?.also { it.postValue(user) }
    }

    fun login(context: Context) : LiveData<User> {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return userLiveData
    }

    fun isLogin(): Boolean {
        return mUser?.let { it.expires_time > System.currentTimeMillis() } ?: false
    }

    fun getUser() : User? {
        return if (isLogin()) mUser else null
    }

    fun getUserId() : Long {
        return if (isLogin()) mUser?.userId ?: 0 else 0
    }
}