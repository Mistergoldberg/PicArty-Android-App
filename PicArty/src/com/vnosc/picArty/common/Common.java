package com.vnosc.picArty.common;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Common {
	public static final float SCREEN_WIDTH = 320;
	public static final float SCREEN_HEIGHT = 480;
	public static final int NUMBER_OF_MASK = 12;
	public static final int SCROLL_PORT[][] = { { 11, 34 }, { 48, 34 },
			{ 72, 34 }, { 95, 34 }, { 121, 34 }, { 146, 34 }, { 168, 34 },
			{ 190, 34 }, { 212, 34 }, { 233, 34 }, { 263, 34 }, { 296, 34 } };
	public static final int SCROLL_LAND[][] = { { 19, 49 }, { 66, 49 },
			{ 99, 49 }, { 129, 49 }, { 165, 49 }, { 197, 49 }, { 228, 49 },
			{ 257, 49 }, { 288, 49 }, { 317, 49 }, { 356, 49 }, { 400, 49 } };

	public static Bitmap getBitmapFromAsset(Context context, String strName)
			throws IOException {
		Runtime.getRuntime().gc();
		AssetManager assetManager = context.getAssets();
		InputStream istr = assetManager.open(strName);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inDither = false; // Disable Dithering mode
		opts.inPurgeable = true; // Tell to gc that whether it needs free
									// memory, the Bitmap can be cleared
		opts.inInputShareable = true; // Which kind of reference will be used to
										// recover the Bitmap data after being
										// clear, when it will be used in the
										// future
		opts.inTempStorage = new byte[32 * 1024];
		Bitmap bitmap = BitmapFactory.decodeStream(istr, null, opts);
		return bitmap;
	}
}
