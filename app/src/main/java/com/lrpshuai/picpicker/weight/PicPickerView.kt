package com.lrpshuai.picpicker.weight

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.lrpshuai.picpicker.R
import com.lrpshuai.picpicker.entity.AlbumEntity
import com.lrpshuai.picpicker.entity.PictureEntity
import com.lrpshuai.picpicker.entity.PicturesEntity
import com.lrpshuai.picpicker.ui.adapter.PicPickerGvAdapter
import com.lrpshuai.picpicker.ui.adapter.PicturePreviewAdapter
import com.lrpshuai.picpicker.utils.FileUtlis
import com.lrpshuai.picpicker.utils.MD5
import com.lrpshuai.picpicker.utils.ViewUtil
import com.lrpshuai.picpicker.utils.imagecompress.CompressPic
import com.vodjk.yst.extension.*
import kotlinx.android.synthetic.main.state_loading.view.*
import kotlinx.android.synthetic.main.view_pic_picker.view.*
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.*
import java.util.Map

/**
 * Created by LRP1989 on 2017/8/31.
 */
class PicPickerView : RelativeLayout {

    lateinit var mActivity: Activity
    /**
     * 当前图片列表
     */
    private var currentPhotos = arrayListOf<PictureEntity>()
    /**
     * 选中的图片的路径
     */
    private var mSelectedPicPath = arrayListOf<String>()
    /**
     * 选中的图片
     */
    private var mSelectedPic = arrayListOf<PictureEntity>()
    /**
     * 全部图片
     */
    private var mAllPicture = arrayListOf<PictureEntity>()
    /**
     * 是否是单选模式
     */
    private var mIsSingleSelect: Boolean = false
    private lateinit var mFileUtlis: FileUtlis
    /**
     * 图片选择限制数量
     */
    private var mLimitNum: Int = 0
    /**
     * 所有相册文件夹name
     */
    var mAllAlbumName = arrayListOf<AlbumEntity>()
    /**
     * key:相册name；  value：对应的文件夹内的所有图片data
     */
    private var mAllPhotos = HashMap<String, ArrayList<PictureEntity>>()
    private var mCurrentPics: ArrayList<PictureEntity> = ArrayList()
    private var mNeedOrignal: ArrayList<String> = ArrayList()
    private var mSelectedPicNames: ArrayList<String> = ArrayList()
    private lateinit var picPickerGvAdapter: PicPickerGvAdapter
    private lateinit var mPreviewAdapter: PicturePreviewAdapter
    private var mCurrentPicIndex: Int = 0
    private var mCurrentAlbumName = ""
    private var picCount = 0
    private lateinit var resultDataList: ArrayList<PictureEntity>
    private var mIsPickerState = true

    internal constructor(context: Context) : super(context) {
        this.mActivity = context as Activity
        initView()
    }

    internal constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.mActivity = context as Activity
        initView()
    }

    internal constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.mActivity = context as Activity
        initView()
    }

    /**
     * 开始导入本地相册数据
     */
    fun loadPicData(limitNum: Int) {
        mLimitNum = limitNum
        mIsSingleSelect = mLimitNum == 1
        if (mIsSingleSelect) {
            llt_vpicker_select.gone()
        } else {
            llt_vpicker_select.visiable()
        }
        picPickerGvAdapter.setLimitNum(mLimitNum)
        Thread(Runnable { getAllAlbums() }).start()
    }

    /**
     *获取选中的图片的数据：异步获取 2种方式：1.Eventbus接收（PicturesEntity）；2.广播接收（“SELECT_PICS_SUCCESS”，然后getSeralizable("picData")）
     */
    fun getSelectData() {
        if (mSelectedPic.size == 0) {
            mActivity.showToast("未选中任何图片")
            return
        }
        rlt_vpicker_wait.visiable()
        Thread(Runnable { setBackData() }).start()
    }

    /**
     * 获取已经选中的图片的数量
     */
    fun getSelectedPicNum(): Int = mSelectedPic.size

    fun getAllAlbumNames(): ArrayList<AlbumEntity> = mAllAlbumName

    fun getAllSelecctPicNames(): ArrayList<String> = mSelectedPicNames

    private fun initView() {
        val rootView = LayoutInflater.from(mActivity).inflate(R.layout.view_pic_picker, this, true)
        mFileUtlis = FileUtlis.intence
        initPickerview()
        initPreviewView()
    }

    /**
     * 初始化选择列表的相关设置
     */
    private fun initPickerview() {
        tv_vpicker_album_name.setOnClickListener { displaySelectAlbumList() }
        tv_vpicker_preview.setOnClickListener {
            //预览  选定的图片
            if (mSelectedPic.size == 0) return@setOnClickListener
            startPreview(mSelectedPic, 0)
        }

        rlt_vpicker_wait.visiable()

        //图片列表适配器
        picPickerGvAdapter = PicPickerGvAdapter(mActivity, currentPhotos, R.layout.adapter_pic_picker, mIsSingleSelect, mSelectedPic.size < mLimitNum, mLimitNum)
        gv_vpicker_photos.adapter = picPickerGvAdapter

        //设置图片列表的点击事件
        picPickerGvAdapter.setItemClickListener(object : PicPickerGvAdapter.OnPicSelectListener {
            /**点击选择框
             */
            override fun clickCheckBox(position: Int) {
                //多选模式
                if (!mIsSingleSelect) {
                    val pictureEntity = currentPhotos[position]
                    val originalPath = pictureEntity.getOriginal()
                    if (mSelectedPicPath.contains(originalPath)) {
                        //已选中
                        mSelectedPicPath.remove(originalPath)
                        mSelectedPic.remove(pictureEntity)
                    } else {
                        //未选中
                        mSelectedPicPath.add(originalPath)
                        mSelectedPic.add(pictureEntity)
                    }

                    //设置是否已经达到了图片选择的上限
                    val size = mSelectedPic.size
                    picPickerGvAdapter.setIsCanSelect(size < mLimitNum)

                    setSelectedPicNum()
                }
            }

            /**点击图片
             */
            override fun clickPicute(position: Int) {
                if (!mIsSingleSelect) {
                    //多选 进入预览模式 当前相册内的所有图片
                    startPreview(currentPhotos, position)
                } else {
                    //单选：直接回传被点击的图片信息
                    val entity = currentPhotos.firstOrNull { it.isSelected }
                    if (entity != null) {
                        entity.isSelected = false
                    }
                    mSelectedPic.clear()
                    mSelectedPicPath.clear()

                    val pictureEntity = currentPhotos[position]
                    pictureEntity.isSelected = true
                    mSelectedPic.add(pictureEntity)
                    mSelectedPicPath.add(pictureEntity.getOriginal())
                    startPreview(mSelectedPic, 0)
                }
            }
        })
    }

    /**
     * 初始化预览模式的相关设置
     */
    private fun initPreviewView() {
        if (mLimitNum == 1) {
            tv_vpicker_index.invisiable()
        }
        llt_vpicker_original.setOnClickListener { setPreviewPicOriginalState() }//原图的点击事件
        llt_vpicker_select.setOnClickListener { setPreviewPicSelectState() }//选择
        mPreviewAdapter = PicturePreviewAdapter(mCurrentPics, mActivity)
        pvp_picpre_pic.adapter = mPreviewAdapter
        pvp_picpre_pic.setOnPageChangeListener(pageChangeListener)
    }

    private fun setPreviewPicOriginalState() {
        val pictureEntity1 = mCurrentPics[mCurrentPicIndex]
        if (mSelectedPicNames.size == mLimitNum) {
            //达到了选择的上限
            if (pictureEntity1.isSelected) {
                //当前已经选择
                mSelectedPic.forEach {
                    if (it.getOriginal() == pictureEntity1.getOriginal()) {
                        it.isNeedOriginal = !pictureEntity1.isNeedOriginal
                    }
                }
                pictureEntity1.isNeedOriginal = !pictureEntity1.isNeedOriginal
                setPicOriginalSelectState(pictureEntity1)
            } else {
                //当前未选择
                mActivity.showToast("最多可以选择$mLimitNum 张图片")
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

    private val pageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            mCurrentPicIndex = position
            setViewData()
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    /**
     * 预览模式时的选择
     */
    private fun setPreviewPicSelectState() {
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
                mActivity.showToast("最多可以选择$mLimitNum 张图片")
            }
        } else {
            //未达到选择的上限
            operateDataFromSelectedList(pictureEntity)
            pictureEntity.isSelected = !pictureEntity.isSelected
            setPicSelectState(pictureEntity)
            setSelectedPicNum()
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
            mSelectedPic.remove(pictureEntity)

            //取消原图的选择
            if (pictureEntity.isNeedOriginal) {
                pictureEntity.isNeedOriginal = false
                setPicOriginalSelectState(pictureEntity)
            }
        } else {
            //当前不是选中
            mSelectedPicNames.add(pictureEntity.getOriginal())
            mSelectedPic.add(pictureEntity)
        }
    }

    /**
     * 设置相关控件的数据
     */
    private fun setViewData() {
        tv_vpicker_index.text = "${mCurrentPicIndex + 1}/${mCurrentPics.size}"
        val pictureEntity = mCurrentPics[mCurrentPicIndex]
        setPicSelectState(pictureEntity)
        setPicOriginalSelectState(pictureEntity)
    }

    /**
     * 设置原图的选择状态

     * @param pictureEntity
     */
    private fun setPicOriginalSelectState(pictureEntity: PictureEntity) {
        //设置原图的选择性
        if (pictureEntity.isNeedOriginal) {
            //设置为已选择
            iv_vpicker_original.setImageResource(R.mipmap.btn_state_selecte)
            if (!mNeedOrignal.contains(pictureEntity.getOriginal())) {
                mNeedOrignal.add(pictureEntity.getOriginal())
            }
        } else {
            //设置为未选择
            iv_vpicker_original.setImageResource(R.mipmap.btn_state_normal)
            if (mNeedOrignal.contains(pictureEntity.getOriginal())) {
                mNeedOrignal.remove(pictureEntity.getOriginal())
            }
        }
    }

    /**
     * 设置图片的选择状态
     * @param pictureEntity
     */
    private fun setPicSelectState(pictureEntity: PictureEntity) {
        if (mLimitNum == 1) {
            iv_vpicker_select.setImageResource(R.mipmap.btn_picker_checked_white)
            iv_vpicker_select.isClickable = false
        } else {
            //设置选择状态置反
            iv_vpicker_select.setImageResource(if (pictureEntity.isSelected) R.mipmap.btn_picker_checked_white else R.mipmap.btn_picker_normal_white)
        }
    }

    /**
     * 获取所有相册文件夹及其中图片的信息
     * @return
     */
    private fun getAllAlbums() {
        //大图遍历字段
        val STORE_IMAGES = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION)
        //获取大图的游标
        val cursor = mActivity.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 大图URI
                STORE_IMAGES, null, null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC") ?: return //根据时间升序

        while (cursor.moveToNext()) {
            val path = cursor.getString(1)//大图路径
            val folderName = File(path).parentFile.name//获取目录名

            val picture = PictureEntity(mFileUtlis.getPicNameFromPath(path, false), mFileUtlis.getPicNameFromPath(path, true), path)
            mAllPicture.add(picture)

            //判断文件夹是否已经存在
            if (mAllPhotos.containsKey(folderName)) {
                mAllPhotos[folderName]?.add(picture)
            } else {
                mAllPhotos.put(folderName, arrayListOf<PictureEntity>(picture))
            }
        }
        mAllPhotos.put("所有图片", mAllPicture)
        cursor.close()

        //向相册集合中添加数据
        setAlbumList()

        mActivity.runOnUiThread {
            rlt_vpicker_wait.gone()
            llt_vpicker_picker.visiable()

            //展示"所有图片"文件夹
            currentPhotos.addAll(mAllPicture)
            tv_vpicker_album_name.text = "所有图片"
            picPickerGvAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 创建相册集合
     */
    private fun setAlbumList() {
        val iter = mAllPhotos.entries.iterator()
        while (iter.hasNext()) {
            val entry = iter.next() as Map.Entry<*, *>
            val key = entry.key as String
            val pictureEntities = mAllPhotos[key]

            if (pictureEntities == null || pictureEntities.isEmpty()) continue
            val firstPicPath = pictureEntities[0].getOriginal()
            if (TextUtils.isEmpty(firstPicPath)) continue

            val firstBitmap = ViewUtil.decodeSampledBitmapFromFile(firstPicPath, 150, 150)
            val albumEntity = AlbumEntity(key, pictureEntities.size, firstBitmap)
            if (key == "所有图片") {
                albumEntity.isCurrent = true
                mAllAlbumName.add(0, albumEntity)
            } else {
                mAllAlbumName.add(albumEntity)
            }
        }
    }

    /**
     * 设置已经选定的图片的数量
     * @param size
     */
    private fun setSelectedPicNum() {
        val size = mSelectedPic.size
        if (mIsPickerState) {
            if (mLimitNum == 1) {
                tv_vpicker_preview.gone()
                v_vpicker_line.gone()
            } else {
                tv_vpicker_preview.text = if (size > 0) "预览($size/$mLimitNum)" else "预览"
                tv_vpicker_preview.isClickable = size > 0
                tv_vpicker_preview.setTextColor(mActivity.getResColor(if (size > 0) R.color.color_ffffff else R.color.color_dddddd))
            }
        } else {
            //预览模式下的处理
            if (mIsSingleSelect) return

            val size = mSelectedPicNames.size
            if (size > 0) {
                tv_vpicker_selected.text = "已选中($size/$mLimitNum)"
            } else {
                tv_vpicker_selected.text = "选择"
            }

        }
    }


    /**
     * 打开预览界面
     * @param list
     * @param position
     */
    private fun startPreview(list: ArrayList<PictureEntity>, position: Int) {
        mCurrentPics = list
        mIsPickerState = false
        llt_vpicker_picker.gone()
        llt_vpicker_preview.visiable()
        mPreviewAdapter.setContentData(list)
        mCurrentPicIndex = position
        pvp_picpre_pic.currentItem = mCurrentPicIndex
    }

    /**
     * 展示选择相册的列表界面
     */
    private fun displaySelectAlbumList() {
        val albumSelectView = AlbumSelectView(mActivity)
        albumSelectView.show(llt_vpicker_picker, mAllAlbumName)

        //设置列表的点击事件
        albumSelectView.setLvOnItemClickListener { position ->
            //将点击的条目置为当前选中项、将该条目对应的数据设置给gridViev进行展示、将对话框消失
            val entity = mAllAlbumName.firstOrNull { it.isCurrent }
            if (entity != null) {
                entity.isCurrent = false
            }

            val albumEntity = mAllAlbumName[position]
            albumEntity.isCurrent = true
            val name = albumEntity.name
            if (!TextUtils.equals(name, mCurrentAlbumName)) {
                //点击的不是当前相册
                currentPhotos.clear()
                picPickerGvAdapter.clear()

                mCurrentAlbumName = name
                tv_vpicker_album_name.text = name
                //展示所选文件夹内的图片
                currentPhotos.addAll(mAllPhotos[name]!!)
                picPickerGvAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     *选中的图片的数据
     */
    private fun setBackData() {
        resultDataList = arrayListOf<PictureEntity>()
        //对选中的图片数据集合进行处理
        for (i in mSelectedPic.indices) {
            val pictureEntity = mSelectedPic[i]
            val originalPath = pictureEntity.getOriginal() ?: ""
            if (originalPath.isNullOrEmpty()) continue

            pictureEntity.setCompressed("")
            if (pictureEntity.isNeedOriginal) {
                //原图模式 就直接添加到待回传的集合中
                resultDataList.add(pictureEntity)
                handler.sendMessage(Message())
            } else {
                //需要添加压缩图片地址
                val picCacheName = MD5.md5(pictureEntity.picName)

                val oldCachePath = mFileUtlis.getSavedCacheBitmapPath(picCacheName) ?: ""
                val oldCacheBitmap = mFileUtlis.getSavedCacheBitmap(picCacheName)
                if (oldCachePath.isNotNull() && oldCacheBitmap != null) {
                    //设备中已经存在该压缩图片
                    pictureEntity.setCompressed(oldCachePath)
                    if (!TextUtils.isEmpty(pictureEntity.getCompressed())) {
                        resultDataList.add(pictureEntity)
                    }
                    handler.sendMessage(Message())
                } else {
                    //不存在压缩图片  进行图片的压缩、保存
                    CompressPic.get(mActivity)
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
                                pictureEntity.setCompressed(file.path)
                                if (!TextUtils.isEmpty(pictureEntity.getCompressed())) {
                                    resultDataList.add(pictureEntity)
                                }
                                handler.sendMessage(Message())
                            }
                }
            }
        }
    }

    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            picCount++
            if (picCount == mSelectedPic.size) {
                val picturesEntity = PicturesEntity(resultDataList, resultDataList.size)
                EventBus.getDefault().post(picturesEntity)

                mActivity.sendBroadcast(Intent().apply {
                    putExtras(Bundle().apply { putSerializable("picData", picturesEntity) })
                    action = "SELECT_PICS_SUCCESS"
                })
                rlt_vpicker_wait.gone()
            }
        }
    }

    fun isSelectListState(): Boolean {
        if (!mIsPickerState) {
            mIsPickerState = true
            llt_vpicker_picker.visiable()
            llt_vpicker_preview.gone()
            return false
        }
        return true
    }

}