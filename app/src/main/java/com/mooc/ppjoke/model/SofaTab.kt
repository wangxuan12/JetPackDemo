package com.mooc.ppjoke.model

data class SofaTab(
    val activeColor: String,
    val activeSize: Int,
    val normalColor: String,
    val normalSize: Int,
    val select: Int,
    val tabGravity: Int,
    val tabs: List<Tab> = mutableListOf()
)

data class Tab(
    val enable: Boolean,
    val index: Int,
    val tag: String,
    val title: String
)