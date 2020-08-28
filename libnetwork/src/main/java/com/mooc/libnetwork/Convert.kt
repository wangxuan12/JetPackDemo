package com.mooc.libnetwork

import java.lang.reflect.Type

interface Convert<T> {
    fun convert(response: String, type: Type): T?

    fun convert(response: String, clazz : Class<*>) : T?
}