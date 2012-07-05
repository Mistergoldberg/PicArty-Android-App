package com.vnosc.picArty;

import java.io.File;
import java.util.ArrayList;

import com.vnosc.picArty.libs.HorizontalPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ShowGalleryActivity extends Activity implements OnClickListener {
	/***************************************************
	 * override method
	 ***************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		initVariables();
		sdDir = new File("/sdcard/PicArty");
		sdDirFiles = sdDir.listFiles();
		updateDataScroll();
		setCurrentSlide();
		lGallery.addView(scrollGallery, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		dialogShare.hide();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_g_share:
			dialogShare = new DialogShare(this,
					sdDirFiles[currentPos].getAbsolutePath());
			dialogShare.show();
			break;
		case R.id.btn_back:
			((Activity) context).finish();
			break;
		case R.id.btn_g_show_camera:
			Intent intent = new Intent(getApplicationContext(),
					PicArtyActivity.class);
			startActivity(intent);
			break;
		case R.id.btn_g_delete:

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Permanently Delete Selected Image" + "?")
					.setCancelable(true)
					.setPositiveButton("Delete",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									boolean deleted = sdDirFiles[currentPos]
											.delete();
									if (currentPos == 0)
										currentPos++;
									else
										currentPos--;
									if (deleted) {
										scrollGallery = new HorizontalPager(
												getApplicationContext());
										sdDirFiles = sdDir.listFiles();
										updateDataScroll();
										setCurrentSlide();
										lGallery.removeAllViews();
										lGallery.addView(
												scrollGallery,
												new LinearLayout.LayoutParams(
														LayoutParams.FILL_PARENT,
														LayoutParams.FILL_PARENT));
									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			AlertDialog alert = builder.create();
			alert.show();
			break;
		default:
			break;
		}

	}

	@Override
	public Object onRetainNonConfigurationInstance() {

		return currentPos;
	}

	/***************************************************
	 * public method
	 ***************************************************/
	// initiation variables
	public void initVariables() {
		context = this;
		// currentBitmap = new ArrayList<Bitmap>();
		listGallary = new ArrayList<ImageView>();
		scrollGallery = new HorizontalPager(getApplicationContext());
		final Object data = (Object) getLastNonConfigurationInstance();
		if (data == null) {
			loadDataChanged();
		} else {
			currentPos = (Integer) data;
			oldPos = currentPos;
		}

		lGallery = (LinearLayout) findViewById(R.id.lg_gallery);
		btnShare = (ImageButton) findViewById(R.id.btn_g_share);
		btnBack = (ImageButton) findViewById(R.id.btn_back);
		btnShowCamera = (ImageButton) findViewById(R.id.btn_g_show_camera);
		btnDeleteImage = (ImageButton) findViewById(R.id.btn_g_delete);

		btnShare.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		btnShowCamera.setOnClickListener(this);
		btnDeleteImage.setOnClickListener(this);
		handler = new Handler();

	}

	public void updateDataScroll() {
		listGallary = new ArrayList<ImageView>();
		for (int i = 0; i < sdDirFiles.length; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setImageDrawable(getResources().getDrawable(
					R.drawable.loading));
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			listGallary.add(imageView);
			scrollGallery.addView(imageView, new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT));
		}
		scrollGallery.setOnScreenSwitchListener(onScreenSwitchListener);
	}

	public void loadDataChanged() {
		currentPos = this.getIntent().getExtras().getInt("position");
		oldPos = currentPos;
	}

	// set current image
	public void setCurrentSlide() {
		if (currentPos == 0) {// loading last image
			Runtime.getRuntime().gc();
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			Bitmap bitmap = BitmapFactory.decodeFile(
					sdDirFiles[listGallary.size() - 1].getAbsolutePath(), opts);
			ImageView currentSlide = listGallary.get(listGallary.size() - 1);
			currentSlide.setImageBitmap(bitmap);
		}
		if (currentPos == listGallary.size() - 1) {
			Runtime.getRuntime().gc();
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			Bitmap bitmap = BitmapFactory.decodeFile(
					sdDirFiles[0].getAbsolutePath(), opts);
			ImageView currentSlide = listGallary.get(0);
			currentSlide.setImageBitmap(bitmap);
		}
		for (int i = 0; i < numberLoaded; i++) {
			if (((currentPos + i - 1) >= 0)
					&& ((currentPos + i - 1) < listGallary.size())) {
				Runtime.getRuntime().gc();
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 1;
				Bitmap bitmap = BitmapFactory.decodeFile(sdDirFiles[currentPos
						+ i - 1].getAbsolutePath(), opts);
				ImageView currentSlide = listGallary.get(currentPos + i - 1);
				currentSlide.setImageBitmap(bitmap);
			}
		}

		scrollGallery.setCurrentScreen(currentPos, false);
	}

	public void setArrBitmap() {

		// Do something long
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Runtime.getRuntime().gc();
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inSampleSize = 1;
						if (oldPos < currentPos) {

							// if is last position then load first position
							if (currentPos == listGallary.size() - 1) {
								currentBitmap = BitmapFactory.decodeFile(
										sdDirFiles[0].getAbsolutePath(), opts);
								ImageView currentSlide = listGallary.get(0);
								currentSlide.setBackgroundColor(Color.BLACK);
								currentSlide.setImageBitmap(currentBitmap);
							} else {
								currentBitmap = BitmapFactory.decodeFile(
										sdDirFiles[currentPos + 1]
												.getAbsolutePath(), opts);
								ImageView currentSlide = listGallary
										.get(currentPos + 1);
								currentSlide.setBackgroundColor(Color.BLACK);
								currentSlide.setImageBitmap(currentBitmap);
								if (currentPos > 1) {
									listGallary.get(currentPos - 2)
											.setImageBitmap(null);
								}
							}
						} else if (oldPos > currentPos) {

							// if is first position then load last position
							if (currentPos == 0) {
								currentBitmap = BitmapFactory.decodeFile(
										sdDirFiles[listGallary.size() - 1]
												.getAbsolutePath(), opts);
								ImageView currentSlide = listGallary
										.get(listGallary.size() - 1);
								currentSlide.setBackgroundColor(Color.BLACK);
								currentSlide.setImageBitmap(currentBitmap);
							} else {
								currentBitmap = BitmapFactory.decodeFile(
										sdDirFiles[currentPos - 1]
												.getAbsolutePath(), opts);
								ImageView currentSlide = listGallary
										.get(currentPos - 1);
								currentSlide.setBackgroundColor(Color.BLACK);
								currentSlide.setImageBitmap(currentBitmap);
								if (currentPos < listGallary.size() - 2) {
									listGallary.get(currentPos + 2)
											.setImageBitmap(null);
								}
							}
						}
					}
				});
			}
		};
		new Thread(runnable).start();
	}

	/***************************************************
	 * private variables
	 ***************************************************/

	/** The on screen switch listener. */
	private final HorizontalPager.OnScreenSwitchListener onScreenSwitchListener = new HorizontalPager.OnScreenSwitchListener() {

		public void onScreenSwitched(final int screen) {
			/*
			 * this method is executed if a screen has been activated, i.e. the
			 * screen is completely visible and the animation has stopped (might
			 * be useful for removing / adding new views)
			 */
			oldPos = currentPos;
			currentPos = scrollGallery.getCurrentScreen();
			setArrBitmap();
		}

		public void onTouch(boolean onTouch) {

			if (onTouch) {

			}
		}

		@Override
		public void onChangeStartEnd(int screen) {
			if (currentPos == 0) {
				currentPos = scrollGallery.getCurrentScreen();
				oldPos = currentPos + 1;
			} else {
				currentPos = scrollGallery.getCurrentScreen();
				oldPos = currentPos - 1;
			}

			setArrBitmap();
		}

	};

	private Context context;

	private HorizontalPager scrollGallery;
	// private ArrayList<Bitmap> currentBitmap;
	private int currentPos;
	private int oldPos;
	private ArrayList<ImageView> listGallary;
	private File[] sdDirFiles;
	private LinearLayout lGallery;
	private ImageButton btnShare;
	private ImageButton btnShowCamera;
	private ImageButton btnDeleteImage;
	private ImageButton btnBack;
	private int numberLoaded = 3;
	private Bitmap currentBitmap;
	private Handler handler;
	private File sdDir;

	private DialogShare dialogShare;
}
