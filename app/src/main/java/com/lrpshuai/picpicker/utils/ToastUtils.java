package com.lrpshuai.picpicker.utils;

import android.widget.Toast;
import android.content.Context;

public class ToastUtils {

    private static Toast mToast;
    private static Context context;

    public static void show(Context ctx, String text) {
        if (mToast == null || context == null
                || !context.getClass().equals(ctx.getClass())) {
            // 同步块，线程安全的创建实例
            synchronized (ToastUtils.class) {
                // 再次检查实例是否存在，如果不存在才真正的创建实例
                mToast = Toast.makeText(ctx.getApplicationContext(), text, Toast.LENGTH_SHORT);
                context = ctx.getApplicationContext();
            }
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    public static void show(Context ctx, int strID) {
        show(ctx, ctx.getString(strID));
    }

}