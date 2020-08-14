package com.wx.libnetwork

data class ApiResponse<T>(var success: Boolean = false, var status: Int = 0, var message: String = "", var body: T? = null) {
    constructor() : this (false,0,"",null)
}

