@file:SuppressLint("RestrictedApi")

package com.mooc.libcommon.utils

import android.annotation.SuppressLint
import android.os.Looper
import android.widget.Toast
import androidx.arch.core.executor.ArchTaskExecutor
import com.mooc.libcommon.global.AppGlobals

fun showToast(message: String?) {
    //showToast可能会出现在异步线程调用
    if (Looper.getMainLooper() == Looper.myLooper()) {
        Toast.makeText(AppGlobals.getApplication(), message, Toast.LENGTH_SHORT).show()
    } else {
        ArchTaskExecutor.getMainThreadExecutor().execute { Toast.makeText(AppGlobals.getApplication(), message, Toast.LENGTH_SHORT).show() }
    }
}

fun postIO(block: () -> Unit) {
    ArchTaskExecutor.getIOThreadExecutor().execute { block() }
}

fun postMain(block: () -> Unit) {
    ArchTaskExecutor.getMainThreadExecutor().execute { block() }
}