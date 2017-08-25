package com.lrpshuai.picpicker.ui.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.lrpshuai.picpicker.R
import com.lrpshuai.picpicker.base.AdapterBase
import com.lrpshuai.picpicker.base.ViewHolder
import com.lrpshuai.picpicker.entity.PictureEntity
import com.vodjk.yst.extension.gone
import com.vodjk.yst.extension.showToast

import java.io.File

/**
 * 相册图片列表
 */
class PicPickerGvAdapter(mContext: Context, list: ArrayList<PictureEntity>, mItemLayoutId: Int, isSingle: Boolean, canSelect: Boolean, limitNum: Int)
    : AdapterBase<PictureEntity>(mContext, list, mItemLayoutId) {

    private var mListener: OnPicSelectListener? = null
    val mIsSingle: Boolean = isSingle
    var mCanSelect: Boolean = canSelect
    val mLimitNum: Int = limitNum

    override fun convertView(helper: ViewHolder, item: PictureEntity?) {
        if (item == null) {
            return
        }

        val position = helper.position
        //设置图片及点击事件
        val original = item.getOriginal()
        val imageView = helper.getView<ImageView>(R.id.iv_app_img)
        Glide.with(mContext).load(File(original)).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(imageView)
        imageView.setOnClickListener {
            //打开图片预览
            if (mListener != null) {
                mListener!!.clickPicute(position)
            }
        }

        //设置复选框的显示状态及点击事件  以及蒙版的显示状态
        val checkBox = helper.getView<ImageView>(R.id.iv_app_check)
        if (mIsSingle) {
            checkBox.gone()
            helper.setVisible(R.id.iv_app_selected, false)
        } else {
            checkBox.visibility = View.VISIBLE
            checkBox.setImageResource(if (item.isSelected) R.mipmap.btn_picker_checked else R.mipmap.btn_picker_normal)
            helper.setVisible(R.id.iv_app_selected, item.isSelected)
            //设置选择框的点击事件
            checkBox.setOnClickListener {
                if (mCanSelect) {
                    item.isSelected = !item.isSelected
                    if (mListener != null) {
                        mListener?.clickCheckBox(position)
                    }
                } else {
                    if (item.isSelected) {
                        item.isSelected = false
                        if (mListener != null) {
                            mListener?.clickCheckBox(position)
                        }
                    } else {
                        mContext.showToast("${"最多可以选择" + mLimitNum + "张图片"}")
                    }
                }
                helper.setVisible(R.id.iv_app_selected, item.isSelected)
                checkBox.setImageResource(if (item.isSelected) R.mipmap.btn_picker_checked else R.mipmap.btn_picker_normal)
            }
        }
    }

    /**
     * 设置是否能够继续选择
     * @param canSelect
     */
    fun setIsCanSelect(canSelect: Boolean) {
        this.mCanSelect = canSelect
    }

    fun setItemClickListener(listener: OnPicSelectListener) {
        this.mListener = listener
    }

    interface OnPicSelectListener {

        /**
         * 点击选择框
         */
        fun clickCheckBox(position: Int)

        /**
         * 点击图片
         */
        fun clickPicute(position: Int)
    }

}