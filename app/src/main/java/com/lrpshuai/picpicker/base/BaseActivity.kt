package com.lrpshuai.picpicker.base

import android.content.Intent
import android.os.Bundle

/**
 * 基类

 * @author jian_zhou
 */
abstract class BaseActivity : SwipeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        isSwipeAnyWhere = true
        initData()
        afterViewInit()
    }

    /**
     * 得到layout布局文件，R.layout.activity_xxxx
     * @return
     */
    protected abstract fun getLayoutId(): Int

    /**
     * 初始化数据，包括从bundle中获取数据保存到当前activity中
     */
    protected abstract fun initData()

    /**
     * 界面初始化之后的后处理，如启动网络读取数据、启动定位等
     */
    protected abstract fun afterViewInit()

    protected fun startActi(acti: Class<*>) {
        startActi(null, acti)
    }

    protected fun startActi(bundle: Bundle?, acti: Class<*>) {
        val intent = Intent(this, acti)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}