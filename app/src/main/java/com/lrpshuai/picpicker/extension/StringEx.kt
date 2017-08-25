package com.vodjk.yst.extension

import java.util.regex.Pattern

/**
 * Created by LRP1989 on 2017/7/28.
 */

/**
 *是否为手机号
 */
fun String.isMoblie(): Boolean {
    if (isNullOrEmpty()) {
        return false
    } else {
        val oneNum = substring(0, 1)
        val pattern = Pattern.compile("[0-9]*")
        val isNum = pattern.matcher(this)
        return isNum.matches() && "1" == oneNum
    }
}

fun String.isNotNull(): Boolean {
    if (isNullOrEmpty()) {
        return false
    }
    return isNotEmpty()
}