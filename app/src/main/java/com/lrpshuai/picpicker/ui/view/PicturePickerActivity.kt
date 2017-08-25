package com.lrpshuai.picpicker.ui.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import com.lrpshuai.picpicker.R
import com.lrpshuai.picpicker.base.BaseActivity
import com.lrpshuai.picpicker.constant.ReceiverConstant
import com.lrpshuai.picpicker.entity.AlbumEntity
import com.lrpshuai.picpicker.entity.PictureEntity
import com.lrpshuai.picpicker.entity.PicturesEntity
import com.lrpshuai.picpicker.ui.adapter.PicPickerGvAdapter
import com.lrpshuai.picpicker.utils.FileUtlis
import com.lrpshuai.picpicker.utils.MD5
import com.lrpshuai.picpicker.utils.ViewUtil
import com.lrpshuai.picpicker.utils.imagecompress.CompressPic
import com.lrpshuai.picpicker.weight.AlbumSelectView
import com.vodjk.yst.extension.*
import kotlinx.android.synthetic.main.activity_pic_picker.*
import kotlinx.android.synthetic.main.state_loading.*
import kotlinx.android.synthetic.main.view_toolbar.*
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.*
import java.util.Map

/**
 * 相册
 * Created by LRP1989 on 2017/2/20.
 */
class PicturePickerActivity : BaseActivity(), View.OnClickListener {


    companion object {
        val SELECT_PICTURE_NUM = "selectPictureNum"
    }

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
    private lateinit var picPickerGvAdapter: PicPickerGvAdapter
    private var mSelectPicSucessReceiver: SelectPicSucessReceiver? = null
    private var mCurrentAlbumName = ""
    private var picCount = 0
    private lateinit var resultDataList: ArrayList<PictureEntity>

    override fun getLayoutId(): Int {
        return R.layout.activity_pic_picker
    }

    override fun initData() {
        setSupportActionBar(tb_pp_top)
        tb_pp_top.setTxtBtnClickable(false)

        //获取参数 是单选还是多选
        val bundle = intent.extras
        if (bundle != null) {
            mLimitNum = bundle.getInt(SELECT_PICTURE_NUM, 1).let { if (it < 1) 1 else it }
            mIsSingleSelect = mLimitNum == 1
        } else {
            //未设置参数时默认为单选模式
            mLimitNum = 1
            mIsSingleSelect = true
        }

        if (mLimitNum == 1) {
            //单选模式下，本界面不显示“确定”和“预览”按钮，点击图片后会直接进入预览界面，然后点预览界面的“确定”按钮发送图片
            tb_pp_top.isShowRightTextButton(false)
            tv_pp_preview.gone()
            v_pp_line.gone()
        }

        //是否原生界面进来的
        mFileUtlis = FileUtlis.getIntence()
    }

    override fun afterViewInit() {
        setViewClick(this, iv_vtb_back, tv_vtb_textbtn, tv_pp_album_name, tv_pp_preview)
        //监听图片浏览界面是否选择图片成功(用于关闭自己)
        registerReceiver()
        //初始化列表
        initListview()
        //获取所有相册文件夹及图片的信息 完成之后展示"所有图片"
        Thread(Runnable { getAllAlbums() }).start()
    }

    /**
     * 初始化图片列表的相关设置
     */
    private fun initListview() {
        pgbar_login.visiable()

        //图片列表适配器
        picPickerGvAdapter = PicPickerGvAdapter(this, currentPhotos, R.layout.adapter_pic_picker, mIsSingleSelect, mSelectedPic.size < mLimitNum, mLimitNum)
        gv_pp_photos.adapter = picPickerGvAdapter

        //设置图片列表的点击事件
        picPickerGvAdapter.setItemClickListener(object : PicPickerGvAdapter.OnPicSelectListener {
            /**点击选择框
             * @param position
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

                    setSelectedPicNum(size)
                }
            }

            /**点击图片
             * @param position
             */
            override fun clickPicute(position: Int) {
                if (!mIsSingleSelect) {
                    //多选 进入预览模式 当前相册内的所有图片
                    startPreview(currentPhotos, position)
                } else {
                    //单选：直接回传被点击的图片信息
                    val pictureEntity = currentPhotos[position]
                    pictureEntity.isSelected = true
                    mSelectedPic.add(pictureEntity)
                    mSelectedPicPath.add(pictureEntity.getOriginal())
                    startPreview(mSelectedPic, 0)
                    this@PicturePickerActivity.finish()
                }
            }
        })
    }

    /**
     * 设置已经选定的图片的数量
     * @param size
     */
    private fun setSelectedPicNum(size: Int) {
        if (mLimitNum == 1) {
            tb_pp_top.isShowRightTextButton(false)
            tv_pp_preview.gone()
            v_pp_line.gone()
        } else {
            tb_pp_top.setTextBtnText(if (size > 0) "确定($size/$mLimitNum)" else "确定")
            tv_pp_preview.text = if (size > 0) "预览($size/$mLimitNum)" else "预览"
            tb_pp_top.setTxtBtnClickable(size > 0)
            tv_pp_preview.isClickable = size > 0
            tv_pp_preview.setTextColor(getResColor(if (size > 0) R.color.color_ffffff else R.color.color_dddddd))
        }
    }

    /**
     * 打开预览界面
     * @param list
     * @param position
     */
    private fun startPreview(list: ArrayList<PictureEntity>, position: Int) {
        val intent = Intent(this, PicturePreviewActivity::class.java)
        val bundle = Bundle().apply {
            putSerializable(PicturePreviewActivity.PICTURE_DATA, list)
            putSerializable(PicturePreviewActivity.SELECTED_PICTURE_NAME, mSelectedPicPath)
            putSerializable(PicturePreviewActivity.SELECTED_PICTURE_DATA, mSelectedPic)
            putInt(PicturePreviewActivity.LIMIT_NUM, mLimitNum)
            putInt(PicturePreviewActivity.CURRENT_PIC_INDEX, position)
        }
        intent.putExtras(bundle)
        startActivityForResult(intent, 201)
    }

    override fun onClick(view: View) {
        if (pgbar_login.isVisibility())
            return

        when (view.id) {
            R.id.iv_vtb_back -> {
                //返回
                this.finish()
            }

            R.id.tv_vtb_textbtn -> {
                //确定
                if (mSelectedPic.size == 0) return

                tb_pp_top.setTxtBtnClickable(false)
                pgbar_login.visiable()
                //回传数据
                Thread(Runnable { setBackData() }).start()
            }

            R.id.tv_pp_album_name ->
                //相册名称
                displaySelectAlbumList()

            R.id.tv_pp_preview -> {
                //预览  选定的图片
                if (mSelectedPic.size == 0) return
                startPreview(mSelectedPic, 0)
            }
        }
    }

    /**
     * 展示选择相册的列表界面
     */
    private fun displaySelectAlbumList() {
        val albumSelectView = AlbumSelectView(this@PicturePickerActivity)
        albumSelectView.show(llt_pp_content, mAllAlbumName)

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
                tv_pp_album_name.text = name
                //展示所选文件夹内的图片
                currentPhotos.addAll(mAllPhotos[name]!!)
                picPickerGvAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 设置向上一个界面回传的数据
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
                //如果是聊天界面 并且选择的是原图模式 就直接添加到待回传的集合中
                resultDataList.add(pictureEntity)
                handler.sendMessage(Message())
            } else {
                //需要添加压缩图片地址
                val picCacheName = MD5.md5(pictureEntity.picName)

                val oldCachePath = mFileUtlis.getSavedCacheBitmapPath(picCacheName) ?: ""
                val oldCacheBitmap = mFileUtlis.getSavedCacheBitmap(picCacheName)
                if (oldCachePath.isNotNull() && oldCacheBitmap != null) {
                    //设备中已经存在该压缩图片
                    setCompressImgPath(pictureEntity, oldCachePath, picCacheName)
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
                                setCompressImgPath(pictureEntity, file.path, picCacheName)
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

        pgbar_login.gone()
        this.finish()
    }

    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            picCount++
            if (picCount == mSelectedPic.size) {
                finishActivity()
            }
        }
    }

    /**
     * 设置压缩图片的地址
     */
    private fun setCompressImgPath(pictureEntity: PictureEntity, cachePath: String, picName: String) {
        if (true) {
            //给原生界面的数据直接设置地址
            pictureEntity.setCompressed(cachePath)
        } else {
            //给wap界面的数据 需要将压缩图片转成base64文件 然后设置
            pictureEntity.setCompressed(picName)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null || resultCode != -1) return
        if (requestCode != 201) return
        val bundle = data.extras ?: return

        //获取数据  选中的图片的name集合
        val list = bundle.getSerializable(PicturePreviewActivity.SELECTED_PICTURE_NAME) as? ArrayList<String>
        val needOriginalList = bundle.getSerializable(PicturePreviewActivity.NEED_ORIGINAL_PICTURE_NAME) as? ArrayList<String>
        if (list == null) return

        mSelectedPicPath = list
        mSelectedPic.clear()

        //设置数据
        if (mSelectedPicPath.size > 0) {
            mAllPicture.forEach {
                if (mSelectedPicPath.contains(it.getOriginal())) {
                    it.isSelected = true
                    mSelectedPic.add(it)
                } else {
                    it.isSelected = false
                    mSelectedPic.remove(it)
                }
            }
        } else {
            mAllPicture.forEach { it.isSelected = false }
        }

        if (needOriginalList != null && needOriginalList.isNotEmpty()) {
            mAllPicture.forEach { it.isNeedOriginal = needOriginalList.contains(it.getOriginal()) }
        } else {
            mAllPicture.forEach { it.isNeedOriginal = false }
        }

        picPickerGvAdapter.notifyDataSetChanged()
        setSelectedPicNum(mSelectedPicPath.size)
    }

    /**
     * 获取所有相册文件夹及其中图片的信息
     * @return
     */
    private fun getAllAlbums() {
        //大图遍历字段
        val STORE_IMAGES = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION)
        //获取大图的游标
        val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 大图URI
                STORE_IMAGES, null, null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC") ?: return //根据时间升序

        while (cursor.moveToNext()) {
            val path = cursor.getString(1)//大图路径
            val folderName = File(path).parentFile.name//获取目录名

//            if (不想让显示的目录名称) { continue}

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

        runOnUiThread {
            pgbar_login.gone()
            llt_pp_content.visiable()
            view_pp_color.gone()

            //展示"所有图片"文件夹
            currentPhotos.addAll(mAllPicture)
            tv_pp_album_name.text = "所有图片"
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
     * 注册广播 监听选中图片成功
     */
    private fun registerReceiver() {
        mSelectPicSucessReceiver = SelectPicSucessReceiver()
        registerReceiver(mSelectPicSucessReceiver, IntentFilter().apply {
            addAction(ReceiverConstant.SELECT_PICTURE_SUCESS)
            addAction(ReceiverConstant.SELECT_PICTURE_NOW)
        })
    }

    /**
     * 选中图片成功后的广播
     */
    private inner class SelectPicSucessReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action == ReceiverConstant.SELECT_PICTURE_NOW || action == ReceiverConstant.SELECT_PICTURE_SUCESS) {
                //已经选择图片成功
                this@PicturePickerActivity.finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mSelectPicSucessReceiver)
    }

}