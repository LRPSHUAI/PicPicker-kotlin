package com.lrpshuai.picpicker.entity

import android.graphics.Bitmap

import java.io.Serializable

/**
 * 相册
 * Created by LRP1989 on 2017/2/21.
 */
class AlbumEntity(var name: String, var photoNum: Int,
                  var firstPic: Bitmap) : Serializable {
    var isCurrent: Boolean = false
}
