package com.lrpshuai.picpicker.weight;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.lrpshuai.picpicker.R;
import com.lrpshuai.picpicker.entity.AlbumEntity;
import com.lrpshuai.picpicker.ui.adapter.AlbumSelectLvAdapter;

import java.util.List;

/**
 * 图片文件夹的选择列表
 */
public class AlbumSelectView extends PopupWindow {

    private ListView mlvAlbumList;
    private View mView;
    public Context mContext;

    public AlbumSelectView(Activity cx) {
        this.mContext = cx;
        this.mView = LayoutInflater.from(cx).inflate(R.layout.view_album_select, null);
        mlvAlbumList = (ListView) mView.findViewById(R.id.lv_album_list);

        this.setOutsideTouchable(true);
        this.mView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int height = mView.findViewById(R.id.lv_album_list).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        this.setContentView(this.mView);
        this.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x50000000);
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.AnimBottom);
    }

    /**
     * 显示相册列表
     *
     * @param albums
     */
    public void show(View view, List<AlbumEntity> albums) {
        showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

        AlbumSelectLvAdapter winAdapter = new AlbumSelectLvAdapter(mContext,albums,R.layout.adapter_album);
        mlvAlbumList.setAdapter(winAdapter);
    }

    /**
     * 为列表设置条目的点击事件
     *
     * @param listener
     */
    public void setLvOnItemClickListener(final OnItemClickListener listener) {
        mlvAlbumList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                listener.onItemClick(position);
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}