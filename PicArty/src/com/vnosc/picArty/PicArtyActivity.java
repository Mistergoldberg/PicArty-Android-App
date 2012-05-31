package com.vnosc.picArty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import com.vnosc.picArty.libs.HorizontalPager;
import com.vnosc.picArty.common.Common;

import android.R.color;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class PicArtyActivity extends Activity implements
		SurfaceHolder.Callback, OnClickListener {
	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	LayoutInflater controlInflater = null;
	View viewControl;
	private ViewFinder scrollImageTab;
	private ImageButton showImageScroll;
	private ImageButton selectCamera;
	private ImageButton flashSwitch;
	private ImageButton showGallery;
	private ImageView maskImageView;
	private int currentScrollPosstion = 0;
	private float ratioHeight;
	private float ratioWidth;
	private DisplayMetrics displayMetric;
	private ImageButton buttonTakePicture;
	private HorizontalPager realViewSwitcher;
	private boolean isShowTemplate = false;
	private boolean isFirstRunning = true;
	private boolean isFlashOn = false;
	private boolean isAfterCamera = true;
	private RelativeLayout bottomLayout;

	// parameter for camera setting
	private Parameters parameters;

	// layout out params for page view image

	// get config
	private Configuration config;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		config = getResources().getConfiguration();
		// create ratio screen
		displayMetric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetric);
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			ratioWidth = (float) (displayMetric.widthPixels / Common.SCREEN_WIDTH);
			ratioHeight = (float) (displayMetric.heightPixels / Common.SCREEN_HEIGHT);
		} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			ratioWidth = (float) (displayMetric.widthPixels / Common.SCREEN_HEIGHT);
			ratioHeight = (float) (displayMetric.heightPixels / Common.SCREEN_WIDTH);
		}

		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		// get variables from xml resource
		showImageScroll = (ImageButton) findViewById(R.id.show_scroll);
		selectCamera = (ImageButton) findViewById(R.id.select_camera);
		flashSwitch = (ImageButton) findViewById(R.id.on_off);
		showGallery = (ImageButton) findViewById(R.id.show_gallery);
		bottomLayout = (RelativeLayout) findViewById(R.id.id_selection_layout);
		realViewSwitcher = new HorizontalPager(getApplicationContext());

		// set onclick for buttons selection
		showImageScroll.setOnClickListener(this);
		flashSwitch.setOnClickListener(this);
		selectCamera.setOnClickListener(this);
		showGallery.setOnClickListener(this);

		// get surfaceview for camera
		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		initGUI();

		// OPTIONAL: listen for screen changes
		realViewSwitcher.setOnScreenSwitchListener(onScreenSwitchListener);

		// button take image
		buttonTakePicture = (ImageButton) findViewById(R.id.takepicture);
		if (isFirstRunning) {
			buttonTakePicture.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					camera.takePicture(myShutterCallback,
							myPictureCallback_RAW, myPictureCallback_JPG);
				}
			});
			isFirstRunning = false;
		}
	}

	public void initGUI() {

		// all width and height have got from layout file

		// create image slideshow
		updateRealPage();

		// add view page to main layout(scroll image)
		FrameLayout.LayoutParams layout = null;
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			this.addContentView(
					realViewSwitcher,
					new LayoutParams(
							displayMetric.widthPixels,
							(int) (displayMetric.heightPixels - (int) (53 * displayMetric.density))));
			layout = new FrameLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT,
					(int) (46 * displayMetric.density));
			layout.setMargins(0, 0, 0, (int) (53 * displayMetric.density));
		} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			this.addContentView(realViewSwitcher, new LayoutParams(
					displayMetric.widthPixels
							- (int) (53 * displayMetric.density),
					displayMetric.heightPixels));
			layout = new FrameLayout.LayoutParams(
					(int) (displayMetric.widthPixels - (53 * displayMetric.density)),
					(int) (62 * displayMetric.density));
			layout.setMargins(0, 0, 0, 0);

		}
		layout.gravity = Gravity.BOTTOM;
		scrollImageTab = new ViewFinder(this, displayMetric.density,
				displayMetric.density, config.orientation);

		this.addContentView(scrollImageTab, layout);

		scrollImageTab.setVisibility(View.GONE);
	}

	/******* variables for camera setting *******/
	ShutterCallback myShutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {

		}
	};
	PictureCallback myPictureCallback_RAW = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {

		}
	};

	PictureCallback myPictureCallback_JPG = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {

			Bitmap bitmap = null;
			ByteArrayInputStream bytes = new ByteArrayInputStream(arg0);
			BitmapDrawable bmd = new BitmapDrawable(bytes);
			bitmap = bmd.getBitmap();

			bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(),
					bitmap.getHeight(), true);

			Canvas canvas = new Canvas();

			canvas.setBitmap(bitmap);
			Bitmap overlap = null;
			try {
				if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
					overlap = Common.getBitmapFromAsset(
							getApplicationContext(),
							"images/templates/portrait/graphic_portrait_"
									+ (currentScrollPosstion + 1) + ".png");
					overlap = Bitmap
							.createScaledBitmap(overlap, 480, 640, true);
				} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					overlap = Common.getBitmapFromAsset(
							getApplicationContext(),
							"images/templates/landscape/graphic_landscape_"
									+ (currentScrollPosstion + 1) + ".png");
					overlap = Bitmap
							.createScaledBitmap(overlap, 640, 480, true);

				}
			} catch (Exception ex) {
			}
			canvas.drawBitmap(overlap, 0, 0, null);
			canvas.save();

			File myDir = new File("/sdcard/PicArty");
			myDir.mkdirs();
			Random generator = new Random();
			int n = 10000;
			n = generator.nextInt(n);
			String fname = "Image-" + n + ".png";
			File file = new File(myDir, fname);

			if (file.exists())
				file.delete();

			// Uri uriTarget = getContentResolver().insert(
			// Media.EXTERNAL_CONTENT_URI, values);
			try {

				FileOutputStream out = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.flush();
				out.close();

			} catch (Exception e) {

				e.printStackTrace();

			}

			camera.startPreview();

		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// if (previewing) {
		// camera.stopPreview();
		// previewing = false;
		// }

		// if (camera != null) {
		// try {
		// Parameters parameters = camera.getParameters();
		// parameters.setRotation(0);
		// Display display = getWindowManager().getDefaultDisplay();
		//
		// // if (display.getRotation() == Surface.ROTATION_0) {
		// // parameters.setPreviewSize(width, height);
		// // camera.setDisplayOrientation(90);
		// // }
		//
		// parameters.set("camera-id", 1);
		// camera.setPreviewDisplay(surfaceHolder);
		// camera.setParameters(parameters);
		// camera.startPreview();
		// previewing = true;
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		config = newConfig;
		getWindowManager().getDefaultDisplay().getMetrics(displayMetric);
		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

			realViewSwitcher.removeAllViews();
			updateRealPage();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			realViewSwitcher.removeAllViews();
			updateRealPage();
		}
	}

	public void updateRealPage() {
		for (int i = 0; i < Common.NUMBER_OF_MASK; i++) {
			RelativeLayout pageLayout = new RelativeLayout(
					getApplicationContext());
			Bitmap bitmap;
			ImageView imageview;
			try {

				// if having current mask > 2 then hide camera by image mask
				if (i > 2) {
					// create image mask view
					maskImageView = new ImageView(getApplicationContext());
					try {
						Bitmap maskBitMap;
						if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
							maskBitMap = Common
									.getBitmapFromAsset(
											getApplicationContext(),
											"images/templates/portrait/Camera-In-App-P-Portrait-1.jpg");
							maskBitMap = Bitmap
									.createScaledBitmap(
											maskBitMap,
											displayMetric.widthPixels,
											(int) (displayMetric.heightPixels - (int) (53 * ratioHeight)),
											true);
							maskImageView.setImageBitmap(maskBitMap);
							pageLayout
									.addView(
											maskImageView,
											new LayoutParams(
													LayoutParams.FILL_PARENT,
													(int) (displayMetric.heightPixels - (int) (53 * ratioHeight))));
						} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
							maskBitMap = Common
									.getBitmapFromAsset(
											getApplicationContext(),
											"images/templates/landscape/Camera-In-App-P-Landscape-1.jpg");
							maskBitMap = Bitmap.createScaledBitmap(maskBitMap,
									displayMetric.widthPixels
											- (int) (53 * ratioWidth),
									(int) (displayMetric.heightPixels), false);
							maskImageView.setImageBitmap(maskBitMap);
							pageLayout.addView(maskImageView, new LayoutParams(
									displayMetric.widthPixels
											- (int) (53 * ratioWidth),
									(int) (displayMetric.heightPixels)));
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
					bitmap = Common.getBitmapFromAsset(getApplicationContext(),
							"images/templates/portrait/graphic_portrait_"
									+ (i + 1) + ".png");
					bitmap = Bitmap.createScaledBitmap(bitmap,
							displayMetric.widthPixels,
							displayMetric.heightPixels
									- (int) (53 * displayMetric.density), true);
					imageview = new ImageView(this);
					imageview.setImageBitmap(bitmap);
					pageLayout
							.addView(
									imageview,
									new LinearLayout.LayoutParams(
											LayoutParams.FILL_PARENT,
											displayMetric.heightPixels
													- (int) (53 * displayMetric.density)));
					realViewSwitcher
							.addView(
									pageLayout,
									new LinearLayout.LayoutParams(
											LayoutParams.FILL_PARENT,
											displayMetric.heightPixels
													- (int) (53 * displayMetric.density)));

				} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					bitmap = Common.getBitmapFromAsset(getApplicationContext(),
							"images/templates/landscape/graphic_landscape_"
									+ (i + 1) + ".png");
					bitmap = Bitmap.createScaledBitmap(bitmap,
							displayMetric.widthPixels
									- (int) (53 * displayMetric.density),
							displayMetric.heightPixels, true);
					imageview = new ImageView(this);
					imageview.setImageBitmap(bitmap);
					pageLayout
							.addView(
									imageview,
									new LinearLayout.LayoutParams(
											displayMetric.widthPixels
													- (int) (53 * displayMetric.density),
											LayoutParams.FILL_PARENT));
					realViewSwitcher
							.addView(
									pageLayout,
									new LinearLayout.LayoutParams(
											displayMetric.widthPixels
													- (int) (53 * displayMetric.density),
											displayMetric.heightPixels));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		realViewSwitcher.setCurrentScreen(currentScrollPosstion, false);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();

		if (camera != null) {
			try {
				parameters = camera.getParameters();
				parameters.setRotation(0);
				// Display display = getWindowManager().getDefaultDisplay();

				// if (display.getRotation() == Surface.ROTATION_0) {
				// parameters.setPreviewSize(width, height);
				// camera.setDisplayOrientation(90);
				// }
				// parameters.setFlashMode(Parameters.FLASH_MODE_ON);
				parameters.set("camera-id", 1);
				camera.setDisplayOrientation(0);
				camera.setPreviewDisplay(holder);
				camera.setParameters(parameters);
				camera.startPreview();
				previewing = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
			previewing = false;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.show_scroll:
			if (isShowTemplate) {
				scrollImageTab.setVisibility(View.GONE);
				// realViewSwitcher.setVisibility(View.GONE);
				isShowTemplate = false;
			} else {
				scrollImageTab.setVisibility(View.VISIBLE);
				// realViewSwitcher.setVisibility(View.VISIBLE);
				isShowTemplate = true;
			}
			break;
		case R.id.on_off:
			if (!isFlashOn) {
				parameters.setFlashMode(Parameters.FLASH_MODE_ON);
				flashSwitch.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.toggle_flash));
				isFlashOn = !isFlashOn;
				parameters.set("camera-id", 2);

			} else {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				flashSwitch.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.toggle_flash_on));
				isFlashOn = !isFlashOn;

				// Toast.makeText(getApplicationContext(), "" +
				// camera.getNumberOfCameras(), Toast.LENGTH_LONG).show();
			}
			camera.setParameters(parameters);

		case R.id.select_camera:
			if (isAfterCamera) {
				parameters.set("camera-id", 2);
				isAfterCamera = !isAfterCamera;
			} else {
				parameters.set("camera-id", 1);
				isAfterCamera = !isAfterCamera;
			}
			break;
		}

	}

	private final HorizontalPager.OnScreenSwitchListener onScreenSwitchListener = new HorizontalPager.OnScreenSwitchListener() {

		public void onScreenSwitched(final int screen) {
			/*
			 * this method is executed if a screen has been activated, i.e. the
			 * screen is completely visible and the animation has stopped (might
			 * be useful for removing / adding new views)
			 */
			/** change current page for paginator */
			if (!isShowTemplate) {
				scrollImageTab.setVisibility(View.VISIBLE);
				// realViewSwitcher.setVisibility(View.VISIBLE);
				isShowTemplate = true;
			}
			currentScrollPosstion = realViewSwitcher.getCurrentScreen();

			scrollImageTab.drawScroll(realViewSwitcher.getCurrentScreen(),
					config.orientation);

		}
	};
}