package com.mooc.libcommon.extention

import androidx.lifecycle.*
import java.util.concurrent.ConcurrentHashMap

object LiveDataBus {
    private val conHashMap = ConcurrentHashMap<String, StickyLiveData<*>>()

    fun <T> with(eventName: String): StickyLiveData<T> {
        val liveData: StickyLiveData<T> = conHashMap.getOrDefault(eventName, StickyLiveData<T>(eventName)) as StickyLiveData<T>
        conHashMap.putIfAbsent(eventName, liveData)
        return liveData
    }

    class StickyLiveData<T>(private var eventName: String) : LiveData<T>() {
        private var stickyValue: T? = null
        private var version = 0

        public override fun setValue(value: T) {
            version++
            super.setValue(value)
        }

        public override fun postValue(value: T) {
            version++
            super.postValue(value)
        }

        fun setStickyValue(value: T) {
            this.stickyValue = value
            setValue(value)
        }

        fun postStickyValue(value: T) {
            this.stickyValue = value
            postValue(value)
        }

        override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
            observerSticky(owner, observer, false)
        }

        private fun observerSticky(
            owner: LifecycleOwner,
            observer: Observer<in T>,
            sticky: Boolean = true
        ) {
            super.observe(owner, WrapperObserver(this, observer, sticky))
            owner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) conHashMap.remove(eventName)
                }
            })
        }

        private class WrapperObserver<T>(
            var liveData: StickyLiveData<T>,
            var observer: Observer<in T>,
            var sticky: Boolean
        ) : Observer<T> {
            //标记该liveData已经发射几次数据了，用以过滤老数据重复接收
            private var lastVersion = 0

            init {
                //比如先使用StickyLiveData发送了一条数据。StickyLiveData#version=1
                //当创建WrapperObserver注册进去的时候，就至少需要把它的version和 StickyLiveData的version保持一致
                //用以过滤老数据，否则 岂不是会收到老的数据？
                lastVersion = liveData.version
            }

            override fun onChanged(t: T) {
                //如果当前observer收到数据的次数已经大于等于了StickyLiveData发送数据的个数了则return

                /**
                 * observer.mLastVersion >= mLiveData.mVersion
                 * 这种情况 只会出现在，我们先行创建一个liveData发射了一条数据。此时liveData的mversion=1.
                 *
                 * 而后注册一个observer进去。由于我们代理了传递进来的observer,进而包装成wrapperObserver，此时wrapperObserver的lastVersion 就会跟liveData的mversion 对齐。保持一样。把wrapperObserver注册到liveData中。
                 *
                 * 根据liveData的原理，一旦一个新的observer 注册进去,也是会尝试把数据派发给他的。这就是黏性事件(先发送,后接收)。
                 *
                 * 但此时wrapperObserver的lastVersion 已经和 liveData的version 一样了。由此来控制黏性事件的分发与否
                 */
                if (lastVersion >= liveData.version) {
                    //但如果当前observer它是关心 黏性事件的，则给他。
                    liveData.stickyValue?.takeIf { sticky }?.also { observer.onChanged(it) }
                } else {
                    lastVersion = liveData.version
                    observer.onChanged(t)
                }
            }

        }
    }
}
