package com.lrpshuai.picpicker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件夹管理
 * Created by jian_zhou on 2016/5/11.
 */
public class FileUtlis {

    public String sd_card = Environment.getExternalStorageDirectory().getPath() + "/picpicker";
    /**
     * 存储图片压缩图片的地址
     */
    public String img_cache = "imgcache";
    /**
     * 子目录
     */
    public String SlantLine = File.separator;
    public static FileUtlis fileUtils;

    public static FileUtlis getIntence() {
        if (fileUtils == null)
            fileUtils = new FileUtlis();
        return fileUtils;
    }

    public void initFile(Context aty) {
        if (!isExistSDCard())
            sd_card = aty.getFilesDir().getPath().toString() + "/picpicker";
        File projectFile = new File(sd_card);
        if (!projectFile.exists()) {
            projectFile.mkdirs();
        }
    }

    /**
     * 获取图片存储路径
     *
     * @return
     */
    public String getImageLocalPath() {
        String path = sd_card + SlantLine + img_cache;
        File imageFile = new File(path);
        if (!imageFile.exists()) {
            imageFile.mkdirs();
        }
        return path;
    }

    public String getCacheBitmapPath() {
        return getImageLocalPath();
    }

    /**
     * 判断sdk是否可用
     *
     * @return
     */
    private boolean isExistSDCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取保存到本地的图片
     */
    public Bitmap getSavedBimap(String picName) {
        Bitmap bitmap = null;
        String path = getImageLocalPath() + SlantLine + picName + ".png";
        File file = new File(path);
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(path);
        }
        return bitmap;
    }

    /**
     * 获取保存到本地的压缩图片缓存地址
     */
    public String getSavedCacheBitmapPath(String picName) {
        String path = getCacheBitmapPath() + SlantLine + picName + ".png";
        File file = new File(path);
        if (file.exists()) {
            return path;
        }
        return null;
    }

    /**
     * 获取保存到本地的压缩图片缓存
     */
    public Bitmap getSavedCacheBitmap(String picName) {
        Bitmap bitmap = null;
        String path = getCacheBitmapPath() + SlantLine + picName + ".png";
        File file = new File(path);
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(path);
        }
        return bitmap;
    }

    /**
     * 获取存储到本地的图片的地址
     */
    public String getSavedBimapPath(String picName) {
        String path = getImageLocalPath() + SlantLine + picName + ".png";
        File file = new File(path);
        if (file.exists()) {
            return path;
        }
        return null;
    }

    /**
     * 将图片文件转成base64
     *
     * @param imgPath
     * @param bitmap
     * @return
     */
    public String imgToBase64(String imgPath, Bitmap bitmap) {
        if (imgPath != null && imgPath.length() > 0) {
            bitmap = BitmapFactory.decodeFile(imgPath);
        }
        if (bitmap == null) {
            return null;
        }

        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();

            byte[] imgBytes = out.toByteArray();
            return Base64.encodeToString(imgBytes, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从本地path中获取bitmap，压缩后保存小图片到本地
     *
     * @param context
     * @param path    图片存放的路径
     * @return 返回压缩后图片的存放路径
     */
    public String saveCacheBitmap(Context context, String path, String cacheName) {
        String compressdPicPath = "";
        //首先得到手机屏幕的高宽，根据此来压缩图片
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        //获取按照屏幕高宽压缩比压缩后的bitmap
        Bitmap bitmap = decodeSampledBitmapFromPath(path, width, height);

        String name = cacheName + ".png";
        File dir = new File(sd_card + SlantLine + img_cache);
        if (!dir.exists()) {
            dir.mkdir();
        }

        // 保存入sdCard
        File file = new File(sd_card + SlantLine + img_cache, name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 50;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(baos.toByteArray());
            out.flush();
            out.close();
            compressdPicPath = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressdPicPath;
    }

    /**
     * 根据图片要显示的宽和高，对图片进行压缩，避免OOM
     *
     * @param path
     * @param width  要显示的imageview的宽度
     * @param height 要显示的imageview的高度
     * @return
     */
    private static Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = caculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    /**
     * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     *
     * @param options
     * @param reqWidth  要显示的imageview的宽度
     * @param reqHeight 要显示的imageview的高度
     * @return
     * @compressExpand 这个值是为了像预览图片这样的需求，他要比所要显示的imageview高宽要大一点，放大才能清晰
     */
    private static int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width >= reqWidth || height >= reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(width * 1.0f / reqHeight);
            inSampleSize = Math.max(widthRadio, heightRadio);
        }

        return inSampleSize;
    }

    /**
     * 通过图片路径获取图片name
     *
     * @param picPath
     * @param isNeedExt 是否需要后缀
     * @return
     */
    public String getPicNameFromPath(String picPath, boolean isNeedExt) {
        int start = picPath.lastIndexOf("/") + 1;
        String name;
        if (isNeedExt) {
            name = picPath.substring(start);
        } else {
            int end = picPath.lastIndexOf(".");
            name = picPath.substring(start, end);
        }
        return name;
    }

}
