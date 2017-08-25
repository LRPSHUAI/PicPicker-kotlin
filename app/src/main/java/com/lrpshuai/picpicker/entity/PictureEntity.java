package com.lrpshuai.picpicker.entity;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by LRP1989 on 2017/2/16.
 */
public class PictureEntity implements Serializable {

    public String original;
    public String picName;
    public String compressed;
    public String extName;

    public boolean needOriginal = false;
    public boolean isSelected;

    public PictureEntity(String picName, String extName, String original) {
        this.original = original;
        this.picName = picName;
        this.extName = extName;
    }

    public String getOriginal() {
        return original;
    }

    public String getCompressed() {
        return compressed;
    }

    public void setCompressed(String compressed) {
        this.compressed = compressed;
    }

    public boolean isNeedOriginal() {
        return needOriginal;
    }

    public void setNeedOriginal(boolean needOriginal) {
        this.needOriginal = needOriginal;
    }

    public String getPicPath() {
        if (!TextUtils.isEmpty(getOriginal())) {
            return getOriginal();
        } else {
            return "";
        }
    }
}
