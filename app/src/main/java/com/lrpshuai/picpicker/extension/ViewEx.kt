package com.vodjk.yst.extension

import android.view.View

/**
 * Created by LRP1989 on 2017/7/28.
 */
/**
 * view的扩展方法，是否是显示状态
 */
fun View.isVisibility(): Boolean = visibility == View.VISIBLE

fun View.gone() {
    visibility = View.GONE
}

fun View.visiable() {
    visibility = View.VISIBLE
}

fun View.invisiable() {
    visibility = View.INVISIBLE
}