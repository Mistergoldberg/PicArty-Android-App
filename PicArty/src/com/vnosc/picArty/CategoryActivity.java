package com.vnosc.picArty;

import com.vnosc.picArty.common.Common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class CategoryActivity extends Activity implements OnClickListener {

	/***************************************************
	 * override method
	 ***************************************************/
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category);
		initVariables();
		context = this;
		initGui();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_goto_gallery:
			Intent intent = new Intent(getApplicationContext(),
					GridImagesActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	/***************************************************
	 * public method
	 ***************************************************/
	// initation variables
	public void initVariables() {
		SharedPreferences settings = getSharedPreferences(
				Common.PREFERENCE_SHARE, 0);
		if (settings.getAll().isEmpty()) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(Common.FIRST_ORDER, Common.FACEBOOK_ORDER);
			editor.putInt(Common.SECOND_ORDER, Common.TWITTER_ORDER);
			editor.putInt(Common.THIRD_ORDER, Common.EMAIL_ORDER);
			editor.putInt(Common.NUMBER_SHARE_VISIBLE, 3);
			editor.commit();
		}
		SharedPreferences settingSwitch = getSharedPreferences(
				Common.PREFERENCE_SWITCH_SHARE, 0);
		if (settingSwitch.getAll().isEmpty()) {
			SharedPreferences.Editor switchEditor = settingSwitch.edit();
			for (int i = 1; i < Common.SHARE_NAMES.length; i++) {
				switchEditor.putBoolean(Common.SHARE_NAMES[i], true);
			}
			switchEditor.commit();
		}
		display = getWindowManager().getDefaultDisplay();
		display.getWidth();
		heightScreen = display.getHeight();
		widthScreen = display.getWidth();
		lContainGallery = (LinearLayout) findViewById(R.id.contain_gallery);
		btnGotoGallerry = (ImageButton) findViewById(R.id.btn_goto_gallery);
		scrGalerry = (ScrollView) findViewById(R.id.scr_galerry);
		btnGotoGallerry.setOnClickListener(this);
	}

	// init GUI
	public void initGui() {

		RelativeLayout.LayoutParams scrParam = new RelativeLayout.LayoutParams(
				(int) (200 * (float) widthScreen / Common.WIDTH_SCREEN),
				(int) (330 * (float) heightScreen / Common.HEIGHT_SCREEN));
		scrParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
		scrParam.addRule(RelativeLayout.CENTER_VERTICAL);
		scrGalerry.setLayoutParams(scrParam);

		try {
			for (int i = 0; i < Common.NUMBER_OF_MASK; i++) {
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						(int) (196 * getResources().getDisplayMetrics().density),
						(int) (196 * getResources().getDisplayMetrics().density));
				param.setMargins(0, (int) (15 * getResources()
						.getDisplayMetrics().density), 0,
						(int) (15 * getResources().getDisplayMetrics().density));

				param.gravity = Gravity.CENTER_HORIZONTAL;
				Bitmap bitmap = Common.getBitmapFromAsset(
						getApplicationContext(), "images/gallery/sample_"
								+ (i + 1) + ".jpg");
				bitmap = Bitmap
						.createScaledBitmap(
								bitmap,
								(int) (196 * getResources().getDisplayMetrics().density),
								(int) (196 * getResources().getDisplayMetrics().density),
								true);
				ImageView imageView = new ImageView(this);

				imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
				imageView.setId(i);
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getApplicationContext(),
								PicArtyActivity.class);
						intent.putExtra("position", v.getId());
//						intent.putExtra("islandscape", context.getResources().getDisplayMetrics().heightPixels > context.getResources().getDisplayMetrics().widthPixels? false:true);
						startActivity(intent);
					}
				});
				lContainGallery.addView(imageView, param);
			}
			
			scrGalerry.post(new Runnable() { 
		        public void run() { 
		        	scrGalerry.scrollTo(0, 200 * (int)(getResources().getDisplayMetrics().heightPixels/ (float)Common.HEIGHT_SCREEN));
		        }
		    });
		} catch (Exception ex) {
			Log.d("Image found:", ex.toString());
		}
		scrGalerry.setOverScrollMode(ScrollView.OVER_SCROLL_ALWAYS);
		scrGalerry.scrollBy(100, scrGalerry.getBottom());
	}
	

	/* private variables */
	// private ScrollView scrGallery;
	private LinearLayout lContainGallery;
	private ImageButton btnGotoGallerry;
	private ScrollView scrGalerry;
	Display display;
	// private DisplayMetrics displayMetric;
	private int heightScreen;
	private int widthScreen;
	private Context context;
}
