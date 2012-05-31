package com.vnosc.picArty;

import com.vnosc.picArty.common.Common;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

// TODO: Auto-generated Javadoc

/**
 * The Class SideBar.
 */
public class ViewFinder extends View {

	/**
	 * Instantiates a new side bar.
	 * 
	 * @param context
	 *            the context
	 */
	public ViewFinder(Context context) {
		super(context);
	}

	/**
	 * Instantiates a new side bar.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public ViewFinder(Context context, float ratioWidth, float ratioHeight,
			int orientation) {
		super(context);
		this.orientation = orientation;
		this.ratioHeight = ratioHeight;
		this.ratioWidth = ratioWidth;
		this.context = context;
		scrollPos = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			bitmapAll = BitmapFactory.decodeResource(getResources(),
					R.drawable.all_portrait);
			bitmapAll = Bitmap.createScaledBitmap(bitmapAll,
					(int) (320 * ratioWidth), (int) (46 * ratioHeight), true);
			scroll = BitmapFactory.decodeResource(getResources(),
					R.drawable.scroller);
			// scroll = Bitmap.createScaledBitmap(scroll, (int) (21 *
			// ratioWidth),
			// (int) (13 * ratioHeight), true);
			canvas.drawBitmap(bitmapAll, 0, 0, paint);
			canvas.drawBitmap(
					scroll,
					(int) (Common.SCROLL_PORT[scrollPos][0] * (context
							.getResources().getDisplayMetrics().density)),
					(int) (Common.SCROLL_PORT[scrollPos][1] * (context
							.getResources().getDisplayMetrics().density)),
					paint);
		} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			bitmapAll = BitmapFactory.decodeResource(getResources(),
					R.drawable.all_landscape);
			bitmapAll = Bitmap
					.createScaledBitmap(
							bitmapAll,
							(int) (context.getResources().getDisplayMetrics().widthPixels - (53 * ratioWidth)),
							(int) (62 * ratioHeight), true);
			scroll = BitmapFactory.decodeResource(getResources(),
					R.drawable.scroller);
			// scroll = Bitmap.createScaledBitmap(scroll, (int) (21 *
			// ratioWidth),
			// (int) (13 * ratioHeight), true);
			canvas.drawBitmap(bitmapAll, 0, 0, paint);
			canvas.drawBitmap(
					scroll,
					(int) (Common.SCROLL_LAND[scrollPos][0] * (context
							.getResources().getDisplayMetrics().widthPixels / Common.SCREEN_HEIGHT)),
					(int) (Common.SCROLL_LAND[scrollPos][1] * (context
							.getResources().getDisplayMetrics().heightPixels / Common.SCREEN_WIDTH)),
					paint);
		}
		paint.setColor(Color.WHITE);

		super.onDraw(canvas);
	}

	public void drawScroll(int position, int orientation) {
		this.scrollPos = position;
		this.orientation = orientation;
		invalidate();
	}

	// private variable
	private Context context;
	private Bitmap bitmapAll = null;
	private Bitmap scroll;
	private float ratioWidth;
	private float ratioHeight;
	private int scrollPos;
	private int orientation;
}