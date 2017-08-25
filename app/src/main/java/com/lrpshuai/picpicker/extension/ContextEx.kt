package com.vodjk.yst.extension

import android.content.Context
import android.view.View
import com.lrpshuai.picpicker.utils.ToastUtils

/**
 * Created by LRP1989 on 2017/7/28.
 */

fun Context.showToast(id: Int) {
    ToastUtils.show(this, id)
}

fun Context.showToast(str: String) {
    ToastUtils.show(this, str)
}

/**
 * 获取资源文件中的字符串
 */
fun Context.getResString(strId: Int): String = resources.getString(strId)

/**
 * 获取资源文件中的color
 */
fun Context.getResColor(colorId: Int): Int = resources.getColor(colorId)

fun Context.setViewClick(listener: View.OnClickListener, vararg views: View) {
    for (it in views) {
        it.setOnClickListener(listener)
    }
}