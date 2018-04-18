package com.spreadwin.m3setting.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.spreadwin.m3setting.R;

@SuppressLint("NewApi")
public class TitleBar extends RelativeLayout {
	ImageView ivTitle;
	TextView tvTitle;
	TextView tvBack;
//	CheckBox cbChoice;

	OnClickListener backClickListener;

	public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	public TitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public TitleBar(Context context) {
		super(context);
		initView(context);
	}

	public void initView(Context context) {
		// View
		// view=LayoutInflater.from(context).inflate(R.layout.custom_title_bar,null);
		ivTitle = (ImageView) findViewById(R.id.iv_title);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvBack = (TextView) findViewById(R.id.tv_back);
	}

	public void setTvBackVisibility(int visible) {
		tvBack.setVisibility(visible);
	}
	
    public ImageView getIvTitle() {
		return ivTitle;
	}

	public void setIvTitle(ImageView ivTitle) {
		this.ivTitle = ivTitle;
	}

	//	public void setCbChoiceVisibility(int visible) {
//		cbChoice.setVisibility(visible);
//	}
//	
//	public void setCbChoiceState(boolean checked) {
//		cbChoice.setChecked(checked);;
//	}
	public void setTitleTextSize(float size) {
		tvTitle.setTextSize(size);
	}
//   public CheckBox getCbChoice(){
//	return cbChoice;
//   }
	public void setIvtitleBackground(int id) {
		ivTitle.setImageResource(id);
	}

	public void setTvtitleContent(String content) {
		tvTitle.setText(content);
	}
}
