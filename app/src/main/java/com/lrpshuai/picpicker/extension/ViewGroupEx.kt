package com.vodjk.yst.extension

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by LRP1989 on 2017/7/31.
 */
fun ViewGroup.inflate(context: Context, layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}