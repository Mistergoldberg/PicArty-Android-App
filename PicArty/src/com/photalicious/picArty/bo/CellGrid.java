package com.photalicious.picArty.bo;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class CellGrid {
	private Bitmap image;
	private ImageView maskImage;
	private boolean isSelect;
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	public ImageView getMaskImage() {
		return maskImage;
	}
	public void setMaskImage(ImageView maskImage) {
		this.maskImage = maskImage;
	}
	public boolean isSelect() {
		return isSelect;
	}
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}
	
}
