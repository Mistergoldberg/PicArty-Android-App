package com.photalicious.picArty;

import com.photalicious.picArty.common.Common;
import com.photalicious.picArty.R;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

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
		initView();
	}

	public void initView() {
		// bitmap for portrait orientation
		bitmapPort = BitmapFactory.decodeResource(getResources(),
				R.drawable.all_portrait);
		bitmapPort = Bitmap.createScaledBitmap(bitmapPort,
				(int) (46 * context.getResources().getDisplayMetrics().density), (int) (320 * ratioHeight), true);
		scrollPort = BitmapFactory.decodeResource(getResources(),
				R.drawable.scroller_portrait);
		scrollPort = Bitmap.createScaledBitmap(scrollPort,
				(int) (10 * context.getResources().getDisplayMetrics().density), (int) (16 * ratioHeight), true);

		// bitmap for landscape orientation
		bitmapLand = BitmapFactory.decodeResource(getResources(),
				R.drawable.all_landscape);
		bitmapLand = Bitmap
				.createScaledBitmap(bitmapLand, (int) (context.getResources()
						.getDisplayMetrics().widthPixels - (53 * ratioWidth)),
						(int) (62 * ratioHeight), true);
		scrollLand = BitmapFactory.decodeResource(getResources(),
				R.drawable.scroller_landscape);
		scrollLand = Bitmap.createScaledBitmap(scrollLand,
				(int) (21 * ratioWidth), (int) (13 * ratioHeight), true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {

			canvas.drawBitmap(
					bitmapPort,
					context.getResources().getDisplayMetrics().widthPixels
							- (99 * context.getResources().getDisplayMetrics().density),
					0, paint);
			canvas.drawBitmap(scrollPort,
					(int) ((context.getResources().getDisplayMetrics().widthPixels - (63 * context.getResources().getDisplayMetrics().density))),
					(int) ((Common.SCROLL_PORT[scrollPos][1] - 1) * (context
							.getResources().getDisplayMetrics().density)),
					paint);
		} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

			canvas.drawBitmap(
					bitmapLand,
					0,
					context.getResources().getDisplayMetrics().heightPixels
							- (62 * context.getResources().getDisplayMetrics().density),
					paint);
			canvas.drawBitmap(
					scrollLand,
					(int) (Common.SCROLL_LAND[scrollPos][0] * (context
							.getResources().getDisplayMetrics().widthPixels / Common.HEIGHT_SCREEN)),
					context.getResources().getDisplayMetrics().heightPixels
							- (int) (13 * context.getResources()
									.getDisplayMetrics().density), paint);
		}

		super.onDraw(canvas);
	}

	public void drawScroll(int position, int orientation) {
		this.scrollPos = position;
		this.orientation = orientation;
		invalidate();
	}

	// private variable
	private Context context;
	private Bitmap bitmapPort = null;
	private Bitmap bitmapLand;
	private Bitmap scrollPort;
	private Bitmap scrollLand;
	private float ratioWidth;
	private float ratioHeight;
	private int scrollPos;
	private int orientation;
}