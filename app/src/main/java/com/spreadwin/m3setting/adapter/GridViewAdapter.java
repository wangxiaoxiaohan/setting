package com.spreadwin.m3setting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spreadwin.m3setting.R;
import com.spreadwin.m3setting.bean.SettingsItemInfo;

import java.util.List;

public class GridViewAdapter extends BaseAdapter {
	Context context;
	List<SettingsItemInfo> data;

	public GridViewAdapter(Context context) {

		this.context = context;
		
	}
	public void initData(List<SettingsItemInfo> data){
		this.data = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.settings_main_item, null);
			TextView tvName = (TextView) convertView.findViewById(R.id.textView);
			ImageView ivPicture = (ImageView) convertView.findViewById(R.id.imageView);
			viewHolder = new ViewHolder(tvName, ivPicture);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tvName.setText(data.get(position).getName());
		viewHolder.ivPicture.setImageResource(data.get(position).getImageId());
		return convertView;
	}

	class ViewHolder {
		ImageView ivPicture;
		TextView tvName;

		public ViewHolder(TextView tvName, ImageView ivPicture) {
			super();
			this.ivPicture = ivPicture;
			this.tvName = tvName;
		}

	}

}
