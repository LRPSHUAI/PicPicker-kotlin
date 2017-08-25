package com.lrpshuai.picpicker.weight;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.TintTypedArray;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lrpshuai.picpicker.R;

/**
 * 切换显示多个按钮的Toolbar ，左侧返回按钮、中间的标题，右侧的文字按钮及2个图片按钮
 */
public class ToolbarView extends Toolbar {
    private final Context mContext;
    private ImageView ivVtbBack;
    private ImageView ivVtbClock;
    private ImageView ivVtbImgBtn1;
    private ImageView iv_webview_close;
    private ImageView ivVtbImgBtn2;
    private TextView tvVtbTitle;
    private TextView tvVtbTextBtn;
    private Toolbar tbVtb;
    private View mView;

    public ToolbarView(Context context) {
        this(context, null);
    }

    public ToolbarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolbarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();

        if (attrs == null) return;

        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                R.styleable.CustomToolbar, defStyleAttr, 0);

        final Drawable rightFirstIcon = a.getDrawable(R.styleable.CustomToolbar_right_first_img_button);
        if (rightFirstIcon != null) {
            setFirImgBtnIcon(rightFirstIcon);
        }

        final Drawable rightIcon = a.getDrawable(R.styleable.CustomToolbar_right_second_img_button);
        if (rightIcon != null) {
            setSecImgBtnIcon(rightIcon);
        }

        final String rightTextButton = a.getString(R.styleable.CustomToolbar_right_text_button);
        if (rightTextButton != null) {
            setTextBtnText(rightTextButton);
        }

        final String title = a.getString(R.styleable.CustomToolbar_titleText);
        if (title != null) {
            setTitleText(title);
        }

        Boolean isShowClock = a.getBoolean(R.styleable.CustomToolbar_isShowClock, false);
        if (!isShowClock) {
            hideClock();
        } else {
            showClock();
        }

        final boolean isHideBackButton = a.getBoolean(R.styleable.CustomToolbar_is_hide_back_button, false);
        if (isHideBackButton) {
            hideBackButton();
        }
        a.recycle();
    }

    public void initView() {
        if (mView == null) {
            LayoutInflater mInflater = LayoutInflater.from(getContext());
            mView = mInflater.inflate(R.layout.view_toolbar, null);
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL);
            addView(mView, lp);
        }

        tbVtb = (Toolbar) mView.findViewById(R.id.tb_vtb);
        ivVtbBack = (ImageView) mView.findViewById(R.id.iv_vtb_back);
        ivVtbClock = (ImageView) mView.findViewById(R.id.iv_vtb_clock);
        ivVtbImgBtn1 = (ImageView) mView.findViewById(R.id.iv_vtb_imgbtn1);
        ivVtbImgBtn2 = (ImageView) mView.findViewById(R.id.iv_vtb_imgbtn2);
        iv_webview_close = (ImageView) mView.findViewById(R.id.iv_webview_close);
        tvVtbTitle = (TextView) mView.findViewById(R.id.tv_vtb_title);
        tvVtbTextBtn = (TextView) mView.findViewById(R.id.tv_vtb_textbtn);
    }

    public void hideIvButton2() {
        if (ivVtbImgBtn2 != null) {
            ivVtbImgBtn2.setVisibility(View.GONE);
        }
    }

    public void hideIvButton1() {
        if (ivVtbImgBtn1 != null) {
            ivVtbImgBtn1.setVisibility(View.GONE);
        }
    }

    public void isShowRightTextButton(boolean isShow) {
        if (tvVtbTextBtn != null) {
            if (isShow) {
                tvVtbTextBtn.setVisibility(View.VISIBLE);
            } else {
                tvVtbTextBtn.setVisibility(View.GONE);
            }
        }
    }

    public void hideBackButton() {
        if (ivVtbBack != null) {
            ivVtbBack.setVisibility(View.GONE);
        }
    }

    public void hideClock() {
        if (ivVtbClock != null) {
            ivVtbClock.setVisibility(View.GONE);
        }
    }

    public void showClock() {
        if (ivVtbClock != null) {
            ivVtbClock.setVisibility(View.VISIBLE);
        }
    }

    public void showTitle() {
        if (tvVtbTitle != null) {
            tvVtbTitle.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置右侧文字按钮上的文字的方法
     *
     * @param s 要显示的文字
     */
    public void setTextBtnText(String s) {
        if (tvVtbTextBtn != null) {
            tvVtbTextBtn.setText(s);
            isShowRightTextButton(true);
        }

        hideIvButton1();
        hideIvButton2();
    }

    public void setTxtBtnClickable(boolean canClick) {
        tvVtbTextBtn.setClickable(canClick);
        tvVtbTextBtn.setTextColor(getResources().getColor(canClick ? R.color.color_ffffff : R.color.color_dddddd));
    }

    public void setTxtBtnRedColor() {
        tvVtbTextBtn.setTextColor(getResources().getColor(R.color.color_ee4e4e));
    }

    /**
     * 设置右侧第二个图片按钮的图片的方法
     *
     * @param icon
     */
    public void setSecImgBtnIcon(Drawable icon) {
        if (ivVtbImgBtn2 != null) {
            ivVtbImgBtn2.setImageDrawable(icon);
            ivVtbImgBtn2.setVisibility(View.VISIBLE);
        }
    }

    public void showWebClose(int resources) {
        if (iv_webview_close != null) {
            iv_webview_close.setImageDrawable(getResources().getDrawable(resources));
            iv_webview_close.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置右侧第一个图片按钮的图片的方法
     *
     * @param icon
     */
    public void setFirImgBtnIcon(Drawable icon) {
        if (ivVtbImgBtn1 != null) {
            ivVtbImgBtn1.setImageDrawable(icon);
            ivVtbImgBtn1.setVisibility(View.VISIBLE);
        }
        isShowRightTextButton(false);
    }

    public void setFirImgUrl(String url, int sourceID) {
        if (ivVtbImgBtn1 != null) {
            ivVtbImgBtn1.setVisibility(View.VISIBLE);
            Glide.with(getContext())
                    .load(url)
                    .placeholder(sourceID)
                    .error(sourceID)
                    .dontAnimate()
                    .centerCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivVtbImgBtn1);
        }
    }

    /**
     * 设置标题的方法
     */
    public void setTitleText(String s) {
        if (tvVtbTitle != null) {
            tvVtbTitle.setText(s);
        }
    }

    public String getTitleTxt() {
        if (tvVtbTitle != null) {
            return tvVtbTitle.getText().toString();
        }
        return "";
    }

    /**
     * 获取tvVtbTextBtn文本
     *
     * @return
     */
    public String getTxtBtnText() {
        return tvVtbTextBtn.getText().toString();
    }

    /**
     * 获取toolbar
     *
     * @return support.v7.widget.Toolbar.
     */
    public Toolbar getToolbar() {
        return tbVtb;
    }

    public TextView getVtbTitle() {
        return tvVtbTitle;
    }


    public ImageView getVtbImgBtn1() {
        return ivVtbImgBtn1;
    }

}