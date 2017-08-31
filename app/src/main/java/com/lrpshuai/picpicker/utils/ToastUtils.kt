package com.lrpshuai.picpicker.utils

import android.widget.Toast
import android.content.Context

object ToastUtils {

    private var mToast: Toast? = null
    private var context: Context? = null

    fun show(ctx: Context, text: String) {
        if (mToast == null || context == null
                || context!!.javaClass != ctx.javaClass) {
            // 同步块，线程安全的创建实例
            synchronized(ToastUtils::class.java) {
                // 再次检查实例是否存在，如果不存在才真正的创建实例
                mToast = Toast.makeText(ctx.applicationContext, text, Toast.LENGTH_SHORT)
                context = ctx.applicationContext
            }
        } else {
            mToast!!.setText(text)
        }
        mToast!!.show()
    }

    fun show(ctx: Context, strID: Int) {
        show(ctx, ctx.getString(strID))
    }

}