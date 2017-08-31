package com.lrpshuai.picpicker.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object ViewUtil {

    /**
     * 按指定比例压缩图片
     * @param filePath
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    fun decodeSampledBitmapFromFile(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap {
        //取得原始图片的属性
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeFile(filePath, options)
        // 然后计算最终的属性
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        options.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeFile(filePath, options)
        return bitmap
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // 先从options 取原始图片的 宽高
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            //一直对这个图片进行宽高 缩放，每次都是缩放1倍，然后这么叠加，当发现叠加以后 也就是缩放以后的宽或者高小于我们想要的宽高
            //这个缩放就结束 跳出循环 然后就可以得到我们极限的inSampleSize值了。
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

}
