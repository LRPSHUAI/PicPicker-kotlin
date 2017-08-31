package com.lrpshuai.picpicker.ui.view

import android.view.KeyEvent
import android.view.View
import com.lrpshuai.picpicker.R
import com.lrpshuai.picpicker.base.BaseActivity
import com.lrpshuai.picpicker.entity.PicturesEntity
import com.vodjk.yst.extension.setViewClick
import com.vodjk.yst.extension.showToast
import kotlinx.android.synthetic.main.activity_pic_picker.*
import kotlinx.android.synthetic.main.view_toolbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 相册
 * Created by LRP1989 on 2017/2/20.
 */
class PicturePickerActivity : BaseActivity(), View.OnClickListener {


    companion object {
        val SELECT_PICTURE_NUM = "selectPictureNum"
    }

    /**
     * 图片选择限制数量
     */
    private var mLimitNum: Int = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_pic_picker
    }

    override fun initData() {
        setSupportActionBar(tb_pp_top)
        isSwipeAnyWhere = false
        EventBus.getDefault().register(this)

        //获取参数 是单选还是多选
        val bundle = intent.extras
        if (bundle != null) {
            mLimitNum = bundle.getInt(SELECT_PICTURE_NUM, 1).let { if (it < 1) 1 else it }
        } else {
            //未设置参数时默认为单选模式
            mLimitNum = 1
        }
    }

    override fun afterViewInit() {
        setViewClick(this, iv_vtb_back, tv_vtb_textbtn)
        //初始化本地相册（异步读取图片数据）
        ppv_pp_content.loadPicData(mLimitNum)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_vtb_back -> {
                //如果图片选中控件是预览模式就切换成选择列表模式
                if (!ppv_pp_content.isSelectListState()) {
                    return
                }

                this.finish()
            }

            R.id.tv_vtb_textbtn -> {
                //确定 异步返回选中的图片的数据
                ppv_pp_content.getSelectData()
            }
        }
    }

    /**
     * 返回选中的图片数据 在需要数据的地方进行异步数据的接收
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun getData(entity: PicturesEntity) {
        showToast(entity.total.toString())
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //如果图片选中控件是预览模式就切换成选择列表模式
        if (!ppv_pp_content.isSelectListState()) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}