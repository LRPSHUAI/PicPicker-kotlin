package com.lrpshuai.picpicker.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.bumptech.glide.Glide
import com.lrpshuai.picpicker.R
import com.lrpshuai.picpicker.base.BaseActivity
import com.lrpshuai.picpicker.entity.PictureEntity
import com.lrpshuai.picpicker.entity.PicturesEntity
import com.lrpshuai.picpicker.ui.adapter.PicPickerGvAdapter
import com.vodjk.yst.extension.gone
import com.vodjk.yst.extension.setViewClick
import com.vodjk.yst.extension.showToast
import com.vodjk.yst.extension.visiable
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity(), View.OnClickListener {

    val REQUEST_EXTERNAL_STORAGE_PERMISSON = 6
    lateinit var picPickerGvAdapter: PicPickerGvAdapter
    var mCanOperate = false
    var mIsSingleSelect = true

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
        EventBus.getDefault().register(this)
    }

    override fun afterViewInit() {
        setViewClick(this, tv_main_select_single, tv_main_select_more)
        picPickerGvAdapter = PicPickerGvAdapter(this, arrayListOf<PictureEntity>(), R.layout.adapter_pic_picker, true, true, 1)
        gv_main_pics.adapter = picPickerGvAdapter

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
            mCanOperate = false
        } else {
            mCanOperate = true
        }
    }

    override fun onClick(v: View) {
        if (!mCanOperate) {
            requestPermission()
            return
        }
        when (v.id) {
            R.id.tv_main_select_single -> {
                mIsSingleSelect = true
                startActi(PicturePickerActivity::class.java)
            }

            R.id.tv_main_select_more -> {
                mIsSingleSelect = false
                val bundle = Bundle().apply { putInt(PicturePickerActivity.SELECT_PICTURE_NUM, 9) }
                startActi(bundle, PicturePickerActivity::class.java)
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE_PERMISSON)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSON) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mCanOperate = true
            } else {
                showToast("请检查存储权限")
                mCanOperate = false
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receivePicData(entity: PicturesEntity) {
        if (entity.total == 0) return
        val pics: ArrayList<PictureEntity> = entity.items
        if (pics.size > 1) {
            iv_main_pic.gone()
            gv_main_pics.visiable()
            picPickerGvAdapter.list = pics
        } else {
            iv_main_pic.visiable()
            gv_main_pics.gone()
            var picUrl = pics[0].picPath
            Glide.with(this).load(picUrl).asBitmap().dontAnimate().into(iv_main_pic)
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}
