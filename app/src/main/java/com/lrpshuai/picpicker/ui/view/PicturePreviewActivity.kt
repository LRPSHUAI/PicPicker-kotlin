package com.lrpshuai.picpicker.ui.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.lrpshuai.picpicker.R
import com.lrpshuai.picpicker.base.BaseActivity
import com.lrpshuai.picpicker.constant.ReceiverConstant
import com.lrpshuai.picpicker.entity.PictureEntity
import com.lrpshuai.picpicker.entity.PicturesEntity
import com.lrpshuai.picpicker.ui.adapter.PicturePreviewAdapter
import com.lrpshuai.picpicker.utils.FileUtlis
import com.lrpshuai.picpicker.utils.MD5
import com.lrpshuai.picpicker.utils.imagecompress.CompressPic
import com.vodjk.yst.extension.*
import kotlinx.android.synthetic.main.activity_pic_preview.*
import kotlinx.android.synthetic.main.view_toolbar.*
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * 相册图片预览
 * Created by LRP1989 on 2017/2/24.
 */
class PicturePreviewActivity : BaseActivity(), View.OnClickListener {

    companion object {
        val LIMIT_NUM = "limitNum"
        val PICTURE_DATA = "pictureData"
        val CURRENT_PIC_INDEX = "currentPicIndex"
        val SELECTED_PICTURE_NAME = "selectedPictureName"
        val SELECTED_PICTURE_DATA = "selectedPictureData"
        val NEED_ORIGINAL_PICTURE_NAME = "needOriginalPicName"
    }

    private lateinit var mCurrentPics: ArrayList<PictureEntity>
    private lateinit var mSelectedPicNames: ArrayList<String>
    private lateinit var mSelectedPicData: ArrayList<PictureEntity>
    private lateinit var mNeedOrignal: ArrayList<String>
    private lateinit var resultDataList: ArrayList<PictureEntity>
    private lateinit var mFileUtlis: FileUtlis
    private lateinit var mPreviewAdapter: PicturePreviewAdapter
    private var mLimitNum: Int = 0
    private var mCurrentPicIndex: Int = 0
    private var picCount = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_pic_preview
    }

    override fun initData() {
        isSwipeAnyWhere = false
        setSupportActionBar(tb_picpre_top)
        val bundle = intent.extras
        mCurrentPics = bundle.getSerializable(PICTURE_DATA) as ArrayList<PictureEntity>
        mSelectedPicNames = bundle.getSerializable(SELECTED_PICTURE_NAME) as ArrayList<String>
        mSelectedPicData = bundle.getSerializable(SELECTED_PICTURE_DATA) as ArrayList<PictureEntity>
        mLimitNum = bundle.getInt(LIMIT_NUM, 1)
        mCurrentPicIndex = bundle.getInt(CURRENT_PIC_INDEX, 1)
        mFileUtlis = FileUtlis.getIntence()
    }

    override fun afterViewInit() {
        setViewClick(this, iv_vtb_back, tv_vtb_textbtn, llt_picpre_original, llt_picpre_select)

        if (mCurrentPics == null || mCurrentPics.isEmpty()) return

        if (mLimitNum == 1) {
            tv_picpre_index.invisiable()
        }

        //创建存储需要原图的图片path，用于向选择图片界面返回数据时使用
        mNeedOrignal = ArrayList<String>()
        if (mSelectedPicData.size > 0) {
            mSelectedPicData.filter { it.isNeedOriginal }
                    .forEach { mNeedOrignal.add(it.getOriginal()) }
        }

        mPreviewAdapter = PicturePreviewAdapter(mCurrentPics, this)
        pvp_picpre_pic.adapter = mPreviewAdapter
        pvp_picpre_pic.setOnPageChangeListener(pageChangeListener)

        llt_picpre_original.visiable()

        pvp_picpre_pic.currentItem = mCurrentPicIndex
        setSelectedPicNum()
        setViewData()
    }

    /**
     * 设置相关控件的数据
     */
    private fun setViewData() {
        tv_picpre_index.text = "${mCurrentPicIndex + 1}/${mCurrentPics.size}"
        val pictureEntity = mCurrentPics[mCurrentPicIndex]
        tb_picpre_top.setTitleText(pictureEntity.picName)
        setPicSelectState(pictureEntity)
        setPicOriginalSelectState(pictureEntity)
    }

    /**
     * 设置图片的选择状态
     * @param pictureEntity
     */
    private fun setPicSelectState(pictureEntity: PictureEntity) {
        if (mLimitNum == 1) {
            iv_picpre_select.setImageResource(R.mipmap.btn_picker_checked_white)
            iv_picpre_select.isClickable = false
        } else {
            //设置选择状态置反
            iv_picpre_select.setImageResource(if (pictureEntity.isSelected) R.mipmap.btn_picker_checked_white else R.mipmap.btn_picker_normal_white)
        }
    }

    /**
     * 设置已经选中的图片的数量
     */
    private fun setSelectedPicNum() {
        if (mLimitNum > 1) {
            val size = mSelectedPicNames.size
            if (size > 0) {
                tb_picpre_top.setTextBtnText("确定($size/$mLimitNum)")
                tb_picpre_top.setTxtBtnClickable(true)
            } else {
                tb_picpre_top.setTextBtnText("确定")
                tb_picpre_top.setTxtBtnClickable(false)
            }
        }
    }

    /**
     * 设置原图的选择状态

     * @param pictureEntity
     */
    private fun setPicOriginalSelectState(pictureEntity: PictureEntity) {
        //设置原图的选择性
        if (pictureEntity.isNeedOriginal) {
            //设置为已选择
            iv_picpre_original.setImageResource(R.mipmap.btn_state_selecte)
            if (!mNeedOrignal.contains(pictureEntity.getOriginal())) {
                mNeedOrignal.add(pictureEntity.getOriginal())
            }
        } else {
            //设置为未选择
            iv_picpre_original.setImageResource(R.mipmap.btn_state_normal)
            if (mNeedOrignal.contains(pictureEntity.getOriginal())) {
                mNeedOrignal.remove(pictureEntity.getOriginal())
            }
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_vtb_back -> {
                //返回:将现在的数据带回去  包括 检测返回按钮的点击
                setBackData()
                this.finish()
            }

            R.id.tv_vtb_textbtn -> {
                //确定
                if (mSelectedPicNames.size == 0) return

                tb_picpre_top.setTxtBtnClickable(false)
                rlt_picpre_wait.visiable()
                //传递数据
                Thread(Runnable {
                    setResultData()
                    runOnUiThread {
                        rlt_picpre_wait.gone()
                        this@PicturePreviewActivity.finish()
                    }
                }).start()
            }

            R.id.llt_picpre_original -> {
                //原图 (点击事件与选择按钮有某种相关联关系)
                val pictureEntity1 = mCurrentPics[mCurrentPicIndex]
                if (mSelectedPicNames.size == mLimitNum) {
                    //达到了选择的上限
                    if (pictureEntity1.isSelected) {
                        //当前已经选择
                        mSelectedPicData.forEach {
                            if (it.getOriginal() == pictureEntity1.getOriginal()) {
                                it.isNeedOriginal = !pictureEntity1.isNeedOriginal
                            }
                        }
                        pictureEntity1.isNeedOriginal = !pictureEntity1.isNeedOriginal
                        setPicOriginalSelectState(pictureEntity1)
                    } else {
                        //当前未选择
                        showToast("最多可以选择$mLimitNum 张图片")
                    }
                } else {
                    //未达到选择的上限
                    if (!pictureEntity1.isSelected) {
                        //当前未选择-->设置同时设置为选择状态
                        operateDataFromSelectedList(pictureEntity1)
                        pictureEntity1.isSelected = true
                        setPicSelectState(pictureEntity1)
                        setSelectedPicNum()
                    }
                    pictureEntity1.isNeedOriginal = !pictureEntity1.isNeedOriginal
                    setPicOriginalSelectState(pictureEntity1)
                }
            }

            R.id.llt_picpre_select -> {
                //选择 :判断是否已经达到了上限  达到上限后将不能再添加选择  但是可以取消选择
                val pictureEntity = mCurrentPics[mCurrentPicIndex]
                if (mSelectedPicNames.size == mLimitNum) {
                    //达到了选择的上限
                    if (pictureEntity.isSelected) {
                        //当前已经是选中状态
                        operateDataFromSelectedList(pictureEntity)
                        pictureEntity.isSelected = false
                        setPicSelectState(pictureEntity)
                        setSelectedPicNum()
                    } else {
                        //当前不是选中
                        showToast("最多可以选择$mLimitNum 张图片")
                    }
                } else {
                    //未达到选择的上限
                    operateDataFromSelectedList(pictureEntity)
                    pictureEntity.isSelected = !pictureEntity.isSelected
                    setPicSelectState(pictureEntity)
                    setSelectedPicNum()
                }
            }
        }
    }

    /**
     * 设置向上一个界面回传的数据
     */
    private fun setBackData() {
        if (mLimitNum == 1) {
            val intent = Intent()
            intent.putExtras(Bundle())
            intent.action = ReceiverConstant.SELECT_PICTURE_CANCLE
            sendBroadcast(intent)
        } else {
            val intent = Intent()
            val bundle = Bundle()
            bundle.putSerializable(SELECTED_PICTURE_NAME, mSelectedPicNames)
            bundle.putSerializable(NEED_ORIGINAL_PICTURE_NAME, mNeedOrignal)
            intent.putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
        }
    }

    /**
     * 设置回传的数据值
     */
    private fun setResultData() {
        //首先关闭前面的界面
        val intent = Intent()
        intent.action = ReceiverConstant.SELECT_PICTURE_NOW
        sendBroadcast(intent)

        resultDataList = ArrayList<PictureEntity>()
        //对选中的图片数据集合中进行处理
        for (i in mSelectedPicData.indices) {
            val pictureEntity = mSelectedPicData[i]
            val originalPath = pictureEntity.getOriginal()
            if (TextUtils.isEmpty(originalPath)) {
                continue
            }
            pictureEntity.setCompressed("")

            if (pictureEntity.isNeedOriginal) {
                //选择的是原图模式 就直接添加到待回传的集合中
                resultDataList.add(pictureEntity)
                handler.sendMessage(Message())
            } else {
                //需要添加压缩图片地址
                val picCacheName = MD5.md5(pictureEntity.picName)

                val oldCachePath = mFileUtlis.getSavedCacheBitmapPath(picCacheName)
                val oldCacheBitmap = mFileUtlis.getSavedCacheBitmap(picCacheName)
                if (!TextUtils.isEmpty(oldCachePath) && oldCacheBitmap != null) {
                    //设备中已经存在该压缩图片
                    pictureEntity.setCompressed(oldCachePath)
                    if (!TextUtils.isEmpty(pictureEntity.getCompressed())) {
                        resultDataList.add(pictureEntity)
                    }
                    handler.sendMessage(Message())
                } else {
                    //不存在压缩图片  进行图片的压缩、保存
                    CompressPic.get(this)
                            .load(File(originalPath))
                            .setFilename(picCacheName)
                            .putGear(CompressPic.THIRD_GEAR)
                            .launch()
                            .asObservable()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError { throwable ->
                                throwable.printStackTrace()
                                handler.sendMessage(Message())
                            }
                            .onErrorResumeNext {
                                handler.sendMessage(Message())
                                Observable.empty()
                            }
                            .subscribe { file ->
                                //压缩成功后调用，返回压缩后的图片文件
                                val cachePath = file.path
                                pictureEntity.setCompressed(cachePath)

                                if (!TextUtils.isEmpty(pictureEntity.getCompressed())) {
                                    resultDataList.add(pictureEntity)
                                }
                                handler.sendMessage(Message())
                            }
                }
            }
        }
    }

    /**
     * 获取所有的数据 关闭界面
     */
    private fun finishActivity() {
        EventBus.getDefault().post(PicturesEntity(resultDataList, resultDataList.size))
        rlt_picpre_wait.gone()
        this@PicturePreviewActivity.finish()
    }

    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            picCount++
            if (picCount == mSelectedPicData.size) {
                finishActivity()
            }
        }
    }

    /**
     * 向存储已经选中的图片的集合中进行数据的操作
     * @param pictureEntity
     */
    private fun operateDataFromSelectedList(pictureEntity: PictureEntity) {
        if (pictureEntity.isSelected) {
            //当前已经是选中状态
            mSelectedPicNames.remove(pictureEntity.getOriginal())
            mSelectedPicData.remove(pictureEntity)

            //取消原图的选择
            if (pictureEntity.isNeedOriginal) {
                pictureEntity.isNeedOriginal = false
                setPicOriginalSelectState(pictureEntity)
            }
        } else {
            //当前不是选中
            mSelectedPicNames.add(pictureEntity.getOriginal())
            mSelectedPicData.add(pictureEntity)
        }
    }

    private val pageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            mCurrentPicIndex = position
            setViewData()
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        setBackData()
        return super.onKeyDown(keyCode, event)
    }

}