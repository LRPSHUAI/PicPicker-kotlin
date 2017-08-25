package com.lrpshuai.picpicker.ui.adapter

import android.content.Context
import android.widget.ImageView

import com.lrpshuai.picpicker.R
import com.lrpshuai.picpicker.base.AdapterBase
import com.lrpshuai.picpicker.base.ViewHolder
import com.lrpshuai.picpicker.entity.AlbumEntity

/**
 * 图片文件夹选择的列表适配器
 */
class AlbumSelectLvAdapter(mContext: Context, list: MutableList<AlbumEntity>, mItemLayoutId: Int) :
        AdapterBase<AlbumEntity>(mContext, list, mItemLayoutId) {

    override fun convertView(helper: ViewHolder, item: AlbumEntity?) {
        if (item == null)  return

        //设置相册的name、数量、是否是选中的、第一张图片的设置
        helper.setText(R.id.tv_pp_album_name1, item.name!!)
        helper.setText(R.id.tv_pp_album_num, "${item.photoNum}张")
        helper.setVisible(R.id.iv_album_selected, item.isCurrent)

        val imageView = helper.getView<ImageView>(R.id.iv_album_pic)
        val firstPicPath = item.firstPic
        if (firstPicPath != null) {
            imageView.setImageBitmap(firstPicPath)
        }
    }

}