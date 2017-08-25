package com.lrpshuai.picpicker.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ViewUtil {

    /**
     * 按指定比例压缩图片
     *
     * @param filePath
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        //取得原始图片的属性
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        // 然后计算最终的属性
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(filePath, options);
        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 先从options 取原始图片的 宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            //一直对这个图片进行宽高 缩放，每次都是缩放1倍，然后这么叠加，当发现叠加以后 也就是缩放以后的宽或者高小于我们想要的宽高
            //这个缩放就结束 跳出循环 然后就可以得到我们极限的inSampleSize值了。
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
