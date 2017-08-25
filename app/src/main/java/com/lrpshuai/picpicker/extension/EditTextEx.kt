package com.vodjk.yst.extension

import android.widget.EditText

/**
 * Created by LRP1989 on 2017/7/28.
 */
/**
 * 获取编辑框上的去除空格后的文字
 */
fun EditText.getEtTrimTxt(): String {
    val txt = text.toString().trim()
    if (txt.isEmpty()) {
        return ""
    } else {
        return txt
    }
}