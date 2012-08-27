package com.photalicious.picArty.adapter;

import java.util.Vector;

import com.photalicious.picArty.bo.CellGrid;
import com.photalicious.picArty.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	private Context mContext;
	private float ratio;

	// Keep all Images in array
	public Integer[] mThumbIds;
	private Vector<CellGrid> mySDCardImages;

	// Constructor
	public ImageAdapter(Context c, float ratio, Vector<CellGrid> mySDCardImages) {
		this.mySDCardImages = mySDCardImages;
		mContext = c;
		this.ratio = ratio;
	}

	@Override
	public int getCount() {
		return mySDCardImages.size();
	}

	@Override
	public Object getItem(int position) {
		return mySDCardImages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View gridView = new View(mContext);
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		gridView = inflater.inflate(R.layout.cell_grid, null);
		ImageView imageView = (ImageView) gridView
				.findViewById(R.id.image_grid);
		imageView.setImageBitmap(mySDCardImages.get(position).getImage());
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		imageView.setLayoutParams(new FrameLayout.LayoutParams(
				(int) (75 * ratio), (int) (75 * ratio)));
		
		ImageView maskImageView = (ImageView)gridView.findViewById(R.id.mask_image);
		maskImageView.setLayoutParams(new FrameLayout.LayoutParams(
				(int) (75 * ratio), (int) (75 * ratio)));
		maskImageView.setScaleType(ImageView.ScaleType.FIT_XY);

		mySDCardImages.get(position).setMaskImage(maskImageView);
		if(mySDCardImages.get(position).isSelect()){
			maskImageView.setVisibility(View.VISIBLE);
		}else{
			maskImageView.setVisibility(View.GONE);
		}

		return gridView;
	}

}
