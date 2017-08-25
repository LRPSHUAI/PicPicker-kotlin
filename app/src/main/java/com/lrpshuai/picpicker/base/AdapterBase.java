package com.lrpshuai.picpicker.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.LinkedList;
import java.util.List;

/**
 * 基类adapter
 */
public abstract class AdapterBase<T> extends BaseAdapter {

    protected List<T> mList = new LinkedList<T>();
    protected Context mContext;
    protected int mItemLayoutId;

    public AdapterBase() {
    }

    public AdapterBase(Context mContext, List<T> list, int mItemLayoutId) {
        initAdp(mContext, list, mItemLayoutId);
    }

    public List<T> getList() {
        return mList;
    }

    public void appendToItem(T item) {
        if (item == null) {
            return;
        }
        mList.add(item);
        afterAddLists(mList);
        notifyDataSetChanged();
    }

    public void setList(List<T> list) {
        if (list == null) {
            return;
        }
        mList = list;
        afterAddLists(mList);
        notifyDataSetChanged();
    }

    public void appendToList(List<T> list) {
        if (list == null) {
            return;
        }
        mList.addAll(list);
        afterAddLists(mList);
        notifyDataSetChanged();
    }

    public void appendToTopList(List<T> list) {
        if (list == null) {
            return;
        }
        mList.addAll(0, list);
        afterAddLists(mList);
        notifyDataSetChanged();
    }

    /**
     * 删除指定对象
     * @param t
     */
    public void deleteItem(T t) {
        if (t == null) {
            return;
        }
        mList.remove(t);
        notifyDataSetChanged();
    }

    private void initAdp(Context mContext, List<T> list, int mItemLayoutId) {
        if (list == null) {
            return;
        }
        this.mList = list;
        this.mContext = mContext;
        this.mItemLayoutId = mItemLayoutId;
        notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        if (position > mList.size() - 1) {
            return null;
        }
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = getViewHolder(position, convertView,
                parent);
        convertView(viewHolder, getItem(position));
        return viewHolder.getConvertView();
    }

    private ViewHolder getViewHolder(int position, View convertView,
                                     ViewGroup parent) {
        return ViewHolder.Companion.get(mContext, convertView, parent, mItemLayoutId,
                position);
    }

    public abstract void convertView(ViewHolder helper, T item);

    protected void afterAddLists(List<T> lists) {
    }

    protected void startActi(Bundle bundle, Class<?> acti) {
        Intent intent = new Intent(mContext, acti);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        mContext.startActivity(intent);
    }

}