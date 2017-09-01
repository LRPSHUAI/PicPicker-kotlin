package com.lrpshuai.picpicker.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment

import java.io.File

/**
 * 文件夹管理
 */
class FileUtlis {

    var sd_card = Environment.getExternalStorageDirectory().path + "/picpicker"
    /**
     * 存储图片压缩图片的地址
     */
    var img_cache = "imgcache"
    /**
     * 子目录
     */
    var SlantLine = File.separator

    /**
     * 获取图片存储路径
     * @return
     */
    val imageLocalPath: String
        get() {
            val path = sd_card + SlantLine + img_cache
            val imageFile = File(path)
            if (!imageFile.exists()) {
                imageFile.mkdirs()
            }
            return path
        }

    val cacheBitmapPath: String
        get() = imageLocalPath

    /**
     * 获取保存到本地的压缩图片缓存地址
     */
    fun getSavedCacheBitmapPath(picName: String): String? {
        val path = cacheBitmapPath + SlantLine + picName + ".png"
        val file = File(path)
        if (file.exists()) {
            return path
        }
        return null
    }

    /**
     * 获取保存到本地的压缩图片缓存
     */
    fun getSavedCacheBitmap(picName: String): Bitmap? {
        var bitmap: Bitmap? = null
        val path = cacheBitmapPath + SlantLine + picName + ".png"
        val file = File(path)
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(path)
        }
        return bitmap
    }

    /**
     * 通过图片路径获取图片name
     * @param picPath
     * @param isNeedExt 是否需要后缀
     * @return
     */
    fun getPicNameFromPath(picPath: String, isNeedExt: Boolean): String {
        val start = picPath.lastIndexOf("/") + 1
        val name: String
        if (isNeedExt) {
            name = picPath.substring(start)
        } else {
            val end = picPath.lastIndexOf(".")
            name = picPath.substring(start, end)
        }
        return name
    }

    companion object {
        var fileUtils: FileUtlis? = null

        val intence: FileUtlis
            get() {
                if (fileUtils == null)
                    fileUtils = FileUtlis()
                return fileUtils!!
            }
    }

}
