package com.vnosc.picArty.common;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Common {
	public static final float HEIGHT_SCREEN = 480;
	public static final float WIDTH_SCREEN = 320;
	public static final int NUMBER_OF_MASK = 12;
	public static final int NUMBER_IN_APP_BUILDING = 9;
	public static final int SCROLL_PORT[][] = { { 416, 290 }, { 416, 254 },
			{ 416, 231 }, { 416, 209 }, { 416, 181 }, { 416, 156 },
			{ 416, 135 }, { 416, 112 }, { 416, 90 }, { 416, 69 }, { 416, 39 },
			{ 416, 6 } };
	public static final int SCROLL_LAND[][] = { { 19, 49 }, { 66, 49 },
			{ 99, 49 }, { 129, 49 }, { 165, 49 }, { 197, 49 }, { 228, 49 },
			{ 257, 49 }, { 288, 49 }, { 317, 49 }, { 356, 49 }, { 400, 49 } };
	public static final int CAMERA_WITH = 1024;
	public static final int CAMERA_HEIGHT = 768;

	public static final int IMAGE_WIDTH = 1024;
	public static final int IMAGE_HEIGHT = 768;
	public static final String CAMERA_LOG = "CAMERA_ERROR";
	public static final String APP_FACEBOOK_ID = "241765749274372";

	// Twitter key
	public static final String OAUTH_CONSUMER_KEY = "c3fUtVSaGssiO0BOiuuGbw";
	public static final String OAUTH_CONSUMER_SECRET = "lXlOV7pvnVLKZWumfdaNfbDFIH7SkEK7GE95UlMcus";

	// RESULTS OK for share login
	public static final int OK_FACEBOOK = 1;
	public static final int OK_TWITTER = 2;
	public static final int OK_TUMBLR = 3;
	public static final int OK_FLICKR = 4;
	public static final int OPTION_SHARE = 5;

	// sort share by numberic
	public static final String PREFERENCE_SHARE = "order_share";
	public static final String PREFERENCE_SWITCH_SHARE = "switch_share";
	public static final int FACEBOOK_ORDER = 1;
	public static final int TWITTER_ORDER = 2;
	public static final int TUMBLR_ORDER = 3;
	public static final int FLICKR_ORDER = 4;
	public static final int EMAIL_ORDER = 5;
	public static final int LOGOUT_ORDER = 6;

	// sort name
	public static final String FIRST_ORDER = "first_order";
	public static final String SECOND_ORDER = "second_order";
	public static final String THIRD_ORDER = "third_order";

	// share name

	public static final String[] SHARE_NAMES = { "Noname", "Facebook",
			"Twitter", "Tumblr", "Flickr", "Email", "Logout" };

	public static final String[] ADAPTER_LOCAL = { "Email", "Logout" };
	public static final String[] ADAPTER_SOCIAL = new String[] { "Facebook",
			"Twitter", "Tumblr", "Flickr" };

	// number of share visible
	public static final String NUMBER_SHARE_VISIBLE = "NUMBER_SHARE_VISIBLE";

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
