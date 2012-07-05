package com.vnosc.picArty.adapter;

import com.vnosc.picArty.R;
import com.vnosc.picArty.common.Common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends ArrayAdapter<String> {

	private Context context;
	private String[] values;
	private boolean isEdit = false;
	private boolean[] filter;

	public ListViewAdapter(Context context, String[] values, boolean[] filter) {
		super(context, R.layout.select_share, values);

		this.context = context;
		this.values = values;
		this.filter = filter;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public int getCount() {
		return values.length;
	}

	public String getShareIndex(int index) {
		if (index >= values.length)
			return "";
		return values[index];
	}

	public void setIsEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}

	public void setFilter(boolean[] filter) {
		this.filter = filter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.select_share, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.lv_label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
		if (position < values.length) {
			textView.setText(values[position]);
			if (values[position] == Common.SHARE_NAMES[1]) {
				rowView.setId(12345);
			}
		}
		if (!isEdit) {
			imageView.setVisibility(View.GONE);

		}
		if (filter[position]) {
			imageView.setImageDrawable(context.getResources().getDrawable(
					R.drawable.tick_icon));
		} else {
			imageView.setImageDrawable(context.getResources().getDrawable(
					R.drawable.un_tick_icon));
		}

		return rowView;
	}

}
