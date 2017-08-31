package com.lrpshuai.picpicker.ui.adapter

import android.app.Activity
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.lrpshuai.picpicker.R
import com.lrpshuai.picpicker.entity.PictureEntity
import com.lrpshuai.picpicker.weight.photoviews.PhotoView

/**
 * 图片预览的viewpager的适配器
 */
class PicturePreviewAdapter(pictures: List<PictureEntity>, private val mActivity: Activity) : PagerAdapter() {

    var mPictures = pictures

    fun setContentData(pictures: List<PictureEntity>){
        this.mPictures = pictures
        this.notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mPictures.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val photoView = PhotoView(container.context)
        val url = mPictures[position].getOriginal()
        Glide.with(mActivity)
                .load(url)
                .placeholder(R.mipmap.icon_album_default)
                .error(R.mipmap.icon_album_default)
                .dontAnimate()
                .fitCenter()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(photoView)

        container.addView(photoView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        return photoView
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view === any
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }

    override fun getItemPosition(any: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }

}
