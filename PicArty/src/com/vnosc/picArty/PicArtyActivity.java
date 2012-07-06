package com.vnosc.picArty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vnosc.picArty.libs.HorizontalPager;
import com.vnosc.picArty.common.Common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * The Class PicArtyActivity.
 */
@SuppressLint("HandlerLeak")
public class PicArtyActivity extends Activity implements
		SurfaceHolder.Callback, OnClickListener, SensorEventListener {
	private int heightScreen;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		config = getResources().getConfiguration();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// detect orientation physical
		SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor s = sm.getSensorList(Sensor.TYPE_ORIENTATION).get(0);
		sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);

		// Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		// init variables of this class
		initVariables();

		// init graphic user interface
		initGUI();

		// OPTIONAL: listen for screen changes
		scrollImage.setOnScreenSwitchListener(onScreenSwitchListener);

		// button take image
		buttonTakePicture = (ImageButton) findViewById(R.id.takepicture);
		if (isFirstRunning) {
			buttonTakePicture.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (!isTaking) {
						isTaking = true;
						camera.takePicture(shutterCallback, null, myPictureCallback_JPG);
					}
				}
			});
		}
	}

	/**
	 * Init variables.
	 */
	public void initVariables() {

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {

					ImageView imageview;
					int widthImage = (int) (Common.IMAGE_WIDTH
							* (float) heightScreen / Common.IMAGE_HEIGHT);
					for (int i = 0; i < Common.NUMBER_OF_MASK; i++) {

						// portrait images
						RelativeLayout pageLayout = new RelativeLayout(
								getApplicationContext());

						imageview = new ImageView(PicArtyActivity.this);
						imageview.setScaleType(ImageView.ScaleType.FIT_XY);
						imageview.setImageBitmap(listBitmapSolid.get(i * 2));
						RelativeLayout.LayoutParams verticalParam = new RelativeLayout.LayoutParams(
								LayoutParams.FILL_PARENT,
								displayMetric.heightPixels);
						verticalParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
						pageLayout.addView(imageview, verticalParam);
						listLayout.add(pageLayout);

						// landscape images
						pageLayout = new RelativeLayout(getApplicationContext());

						imageview = new ImageView(PicArtyActivity.this);
						imageview.setScaleType(ImageView.ScaleType.FIT_XY);
						imageview
								.setImageBitmap(listBitmapSolid.get(i * 2 + 1));
						pageLayout.addView(imageview,
								new LinearLayout.LayoutParams(widthImage,
										heightScreen));
						listLayout.add(pageLayout);

						// portrait images glow
						pageLayout = new RelativeLayout(getApplicationContext());

						imageview = new ImageView(PicArtyActivity.this);
						imageview.setScaleType(ImageView.ScaleType.FIT_XY);
						imageview.setImageBitmap(listBitmapGlow.get(i * 2));
						verticalParam = new RelativeLayout.LayoutParams(
								LayoutParams.FILL_PARENT,
								displayMetric.heightPixels);
						verticalParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
						pageLayout.addView(imageview, verticalParam);
						listLayoutGlow.add(pageLayout);

						// landscape images glow
						pageLayout = new RelativeLayout(getApplicationContext());

						imageview = new ImageView(PicArtyActivity.this);
						imageview.setScaleType(ImageView.ScaleType.FIT_XY);
						imageview.setImageBitmap(listBitmapGlow.get(i * 2 + 1));
						pageLayout.addView(imageview,
								new LinearLayout.LayoutParams(widthImage,
										heightScreen));
						listLayoutGlow.add(pageLayout);
					}
				} catch (Exception ex) {
				}
				loaded = true;
				updateScrollPage();
			}
		};
		listBitmapSolid = new ArrayList<Bitmap>();
		listBitmapGlow = new ArrayList<Bitmap>();
		currentPos = getIntent().getIntExtra("position", 0);
		isLandscape = true;
		Display display = getWindowManager().getDefaultDisplay();
		display.getWidth();
		heightScreen = display.getHeight();
		displayMetric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetric);
		density = getApplicationContext().getResources().getDisplayMetrics().density;
		// get variables from xml resource
		showImageScroll = (ImageButton) findViewById(R.id.show_scroll);
		selectCamera = (ImageButton) findViewById(R.id.select_camera);
		flashSwitch = (ImageButton) findViewById(R.id.on_off);
		showGallery = (ImageButton) findViewById(R.id.show_gallery);
		cameraButton = (ImageButton) findViewById(R.id.takepicture);
		scrollImage = new HorizontalPager(getApplicationContext());
		frameLayoutScroll = (FrameLayout) findViewById(R.id.id_frameLayout_mask);
		imageMask = (ImageView) findViewById(R.id.id_image_mask);
		// linearLayout = (LinearLayout) findViewById(R.id.id_layout_camera);
		linearToolLeft = (RelativeLayout) findViewById(R.id.id_selection_layout);
		// set onclick for buttons selection
		showImageScroll.setOnClickListener(this);
		flashSwitch.setOnClickListener(this);
		selectCamera.setOnClickListener(this);
		showGallery.setOnClickListener(this);

		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
		int widthCamera = (int) (Common.CAMERA_WITH * (float) heightScreen / Common.CAMERA_HEIGHT);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				widthCamera, LayoutParams.FILL_PARENT);

		if (density <= 1) {
			layoutParams = new LinearLayout.LayoutParams(widthCamera,
					LayoutParams.FILL_PARENT, Gravity.LEFT);
			layoutParams.setMargins(0, 0, 46 * (int) density, 0);
			linearToolLeft.setVisibility(View.GONE);
		}
		surfaceView.setLayoutParams(layoutParams);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		orientation = Configuration.ORIENTATION_LANDSCAPE;

		// init array mask list
		listLayout = new ArrayList<RelativeLayout>();
		listLayoutGlow = new ArrayList<RelativeLayout>();
		initDatapage();

		initParams();
	}

	public void initParams() {
		// maskListPageParam
		templateTabParam = new FrameLayout.LayoutParams(
				(int) (displayMetric.widthPixels - (53 * displayMetric.density)),
				(int) (displayMetric.heightPixels * displayMetric.density));
		templateTabParam.setMargins(0, 0, 0, 0);

		int widthScroll = (int) (Common.IMAGE_WIDTH * (float) heightScreen / Common.IMAGE_HEIGHT);
		if (getApplicationContext().getResources().getDisplayMetrics().density > 1) {
			paramLayout = new FrameLayout.LayoutParams(widthScroll,
					heightScreen);
			paramLayout.gravity = Gravity.CENTER_HORIZONTAL;
		} else {
			paramLayout = new FrameLayout.LayoutParams(widthScroll,
					heightScreen);
			paramLayout.gravity = Gravity.LEFT;
		}

	}

	public void initGUI() {
		/*
		 * This function used to initation graphic user interface
		 */

		/** all width and height have got from layout file **/

		frameLayoutScroll.addView(scrollImage, paramLayout);

		/** create view finder and add to top of main layout **/
		listPieces = new ViewFinder(this, displayMetric.density,
				displayMetric.density, config.orientation);
		this.addContentView(listPieces, templateTabParam);
		listPieces.setVisibility(View.GONE);
		/** end view finder **/
		imageMask.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updateScrollPage();
			}
		});

		// create image mask slide show
		updateScrollPage();

	}

	ShutterCallback shutterCallback = new ShutterCallback() {

	    @Override
	    public void onShutter() {

	    }
	};
	
	/** The my picture callback_ jpg. */
	Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			try {
				Bitmap bitmap = null;
				ByteArrayInputStream bytes = new ByteArrayInputStream(arg0);
				BitmapDrawable bmd = new BitmapDrawable(bytes);
				bitmap = bmd.getBitmap();

				if (isAfterCamera) {
					bitmap = Bitmap.createScaledBitmap(bitmap,
							bitmap.getWidth(), bitmap.getHeight(), true);
					Matrix mat = new Matrix();
					if (isLandscape)
						mat.preRotate(-90);// /in degree
					else
						mat.preRotate(90);// /in degree
					bitmap = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mat, true);
				} else {
					float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
					Matrix mat = new Matrix();
					mat.preRotate(-90);// /in degree
					mat.setValues(mirrorY);
					bitmap = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mat, true);
					mat = new Matrix();
					mat.preRotate(90);
					bitmap = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mat, true);
				}

				if (isLandscape) {
					Matrix mat = null;
					if (isAfterCamera) {
						mat = new Matrix();
						mat.preRotate(90);// /in degree
					} else {
						mat = new Matrix();
						mat.preRotate(-90);// /in degree
					}

					bitmap = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), mat, true);
				}

				Canvas canvas = new Canvas();
				canvas.setBitmap(bitmap);
				Bitmap overlap = null;
				try {
					if (!isLandscape) {
						overlap = Common.getBitmapFromAsset(
								getApplicationContext(),
								"images/templates/portrait/graphic_portrait_"
										+ (Common.NUMBER_OF_MASK - currentPos)
										+ ".png");
						overlap = Bitmap.createScaledBitmap(overlap, 480, 640,
								true);
					} else if (isLandscape) {
						overlap = Common.getBitmapFromAsset(
								getApplicationContext(),
								"images/templates/landscape/graphic_landscape_"
										+ (currentPos + 1) + ".png");
						overlap = Bitmap.createScaledBitmap(overlap, 640, 480,
								true);

					}
				} catch (Exception ex) {
					Log.d("Save image error", ex.toString());
				}
				canvas.drawBitmap(overlap, 0, 0, null);
				canvas.save();

				File myDir = new File("/sdcard/PicArty");
				myDir.mkdirs();
				String fname = timeStampFormat.format(new Date()) + ".jpg";
				File file = new File(myDir, fname);

				if (file.exists())
					file.delete();
				try {

					FileOutputStream out = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
					out.flush();
					out.close();

				} catch (Exception e) {

					e.printStackTrace();

				}

				camera.startPreview();
				isTaking = false;
			} catch (Exception ex) {

			}

		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		if (cameraCount == 1) {
			isAfterCamera = true;
			camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
			selectCamera.setEnabled(false);
			return;
		}

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					isAfterCamera = false;
					camera = Camera.open(camIdx);
					camera.setPreviewDisplay(holder);
					camera.setDisplayOrientation(180);
					parameters = camera.getParameters();
					flashSwitch.setEnabled(false);
				} catch (Exception e) {

				}
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
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (previewing) {
			camera.stopPreview();
			previewing = false;
		}

		if (camera != null) {
			try {
				parameters = camera.getParameters();
				List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
				for (Camera.Size size : sizes) {
					if (size.width == 640 && size.height == 480) {
						parameters.setPictureSize(640, 480);
					}
				}
				camera.setDisplayOrientation(0);
				camera.setPreviewDisplay(surfaceHolder);
				camera.setParameters(parameters);
				camera.startPreview();
				previewing = true;
			} catch (IOException e) {
			}
		}

	}

	@Override
	protected void onDestroy() {

		if (listBitmapGlow != null) {
			for (int i = 0; i < Common.NUMBER_OF_MASK; i++) {
				listBitmapGlow.get(i).recycle();
			}
			listBitmapGlow = null;
		}
		if (listBitmapSolid != null) {
			for (int i = 0; i < Common.NUMBER_OF_MASK; i++) {
				listBitmapSolid.get(i).recycle();
			}
			listBitmapSolid = null;
		}
		if (listLayout != null) {
			listLayout = null;
		}
		if (listLayoutGlow != null) {
			listLayoutGlow = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.show_gallery:
			Intent intent = new Intent(getApplicationContext(),
					GridImagesActivity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.show_scroll:
			animatePieces();
			break;
		case R.id.on_off:
			try {
				if (!isAfterCamera)
					return;
				parameters = camera.getParameters();
				if (!isFlashOn) {
					parameters.setFlashMode(Parameters.FLASH_MODE_ON);
					if (isLandscape)
						flashSwitch.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.toggle_flash));
					else
						flashSwitch.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.toggle_flash_portrait));
					isFlashOn = !isFlashOn;

				} else {
					parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
					if (isLandscape)
						flashSwitch.setBackgroundDrawable(getResources()
								.getDrawable(R.drawable.toggle_flash_on));
					else
						flashSwitch.setBackgroundDrawable(getResources()
								.getDrawable(
										R.drawable.toggle_flash_on_portrait));
					isFlashOn = !isFlashOn;
				}
				camera.setParameters(parameters);
			} catch (Exception ex) {
				Log.d(Common.CAMERA_LOG, ex.toString());
			}
			break;

		case R.id.select_camera:
			try {
				int cameraCount = 0;
				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				cameraCount = Camera.getNumberOfCameras();
				if (cameraCount == 1) {
					return;
				}
				if (camera != null) {
					camera.stopPreview();
					camera.release();
					camera = null;
				}
				if (isAfterCamera) {

					flashSwitch.setEnabled(false);
					for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
						Camera.getCameraInfo(camIdx, cameraInfo);
						if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
							try {
								camera = Camera.open(camIdx);
							} catch (Exception e) {
								Log.e("failed",
										"Camera failed to open: "
												+ e.getLocalizedMessage());
							}
						}
					}
				} else {
					flashSwitch.setEnabled(true);
					for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
						Camera.getCameraInfo(camIdx, cameraInfo);
						if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
							try {
								camera = Camera.open(camIdx);
								camera.setDisplayOrientation(0);
							} catch (Exception e) {
								Log.e("failed",
										"Camera failed to open: "
												+ e.getLocalizedMessage());
							}
						}
					}
				}
				camera.setPreviewDisplay(surfaceHolder);
				camera.startPreview();
				isAfterCamera = !isAfterCamera;
				previewing = true;
			} catch (Exception ex) {

			}
			break;
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		/*
		 * this function executed when the sensor of device changed
		 */

		float pitch = event.values[2];
		if (isLandscape && pitch <= 30 && pitch >= -30) {
			if (isLandscape) {
				currentPos = Common.NUMBER_OF_MASK - 1 - currentPos;// dao chieu
																	// neu
																	// portrait
				scrollImage.removeAllViews();
				isLandscape = !isLandscape;
				scrollImage.setIsHorizontal(false);
				updateScrollPage();

				orientation = Configuration.ORIENTATION_PORTRAIT;
				listPieces.drawScroll(Common.NUMBER_OF_MASK - 1 - currentPos,
						Configuration.ORIENTATION_PORTRAIT);
				cameraButton.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.camera_button_style_portrait));
				selectCamera.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.toggle_camera_portrait_style));
				if (!isFlashOn)
					flashSwitch.setBackgroundDrawable(getResources()
							.getDrawable(
									R.drawable.toggle_flash_on_portrait_style));
				else
					flashSwitch
							.setBackgroundDrawable(getResources().getDrawable(
									R.drawable.toggle_flash_portrait_style));
				showImageScroll.setBackgroundDrawable(getResources()
						.getDrawable(R.drawable.icon_small_portrait_style));
			}
		} else if (pitch < -60 || pitch > 60) {
			if (!isLandscape || isFirstRunning) {
				currentPos = Common.NUMBER_OF_MASK - 1 - currentPos;
				scrollImage.removeAllViews();
				scrollImage.setIsHorizontal(true);
				isLandscape = !isLandscape;

				updateScrollPage();
				orientation = Configuration.ORIENTATION_LANDSCAPE;
				listPieces.drawScroll(currentPos,
						Configuration.ORIENTATION_LANDSCAPE);
				cameraButton.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.camera_button_style_landscape));
				selectCamera.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.toggle_camera_landscape_style));

				if (!isFlashOn)
					flashSwitch
							.setBackgroundDrawable(getResources().getDrawable(
									R.drawable.toggle_flash_on_landscape_style));
				else
					flashSwitch.setBackgroundDrawable(getResources()
							.getDrawable(
									R.drawable.toggle_flash_landscape_style));
				showImageScroll.setBackgroundDrawable(getResources()
						.getDrawable(R.drawable.icon_small_landscape_style));
			}
		}
	}

	public void animatePieces() {
		/*
		 * This function used to show animate of Pieces bar
		 */
		if (isShowTemplate) {
			listPieces.setVisibility(View.GONE);
			if (density <= 1) {
				linearToolLeft.setVisibility(View.GONE);
			}
			// scrollImage.setVisibility(View.GONE);
			isShowTemplate = false;
			scrollImage.removeAllViews();
			updateScrollPage();
		} else {
			listPieces.setVisibility(View.VISIBLE);
			if (density <= 1) {
				linearToolLeft.setVisibility(View.VISIBLE);
			}
			// scrollImage.setVisibility(View.VISIBLE);
			isShowTemplate = true;
			scrollImage.removeAllViews();
			updateScrollPage();
		}
	}

	public void updateScrollPage() {
		/*
		 * This function used to update content of scroll
		 */
		if (loaded) {
			try {
				for (int i = 0; i < Common.NUMBER_OF_MASK; i++) {

					if (!isLandscape) {

						if (!isShowTemplate) {
							scrollImage.addView(listLayout
									.get(Common.NUMBER_OF_MASK * 2 - 2
											* (i + 1)));
							scrollImage.setCurrentScreen(currentPos, false);
						} else {
							scrollImage.addView(listLayoutGlow
									.get(Common.NUMBER_OF_MASK * 2 - 2
											* (i + 1)));
							scrollImage.setCurrentScreen(currentPos, false);
						}
					} else {
						if (!isShowTemplate) {
							scrollImage.addView(listLayout.get(i * 2 + 1));
							scrollImage.setCurrentScreen(currentPos, false);
						} else {
							scrollImage.addView(listLayoutGlow.get(i * 2 + 1));
							scrollImage.setCurrentScreen(currentPos, false);
						}
					}
				}
			} catch (Exception e) {

			}
		}
	}

	public void initDatapage() {
		/*
		 * This function used to initiation images and layouts for scroll
		 */
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Bitmap bitmap;
					// ImageView imageview;
					int widthImage = (int) (Common.IMAGE_WIDTH
							* (float) heightScreen / Common.IMAGE_HEIGHT);
					for (int i = 0; i < Common.NUMBER_OF_MASK; i++) {

						// portrait images
						bitmap = Common.getBitmapFromAsset(
								getApplicationContext(),
								"images/templates/portrait/solid/graphic_portrait_min_"
										+ (i + 1) + ".png");
						listBitmapSolid.add(bitmap);

						// landscape images
						bitmap = Common.getBitmapFromAsset(
								getApplicationContext(),
								"images/templates/landscape/solid/graphic_landscape_min_"
										+ (i + 1) + ".png");

						bitmap = Bitmap.createScaledBitmap(bitmap, widthImage,
								heightScreen, true);
						listBitmapSolid.add(bitmap);

						// portrait images glow
						bitmap = Common.getBitmapFromAsset(
								getApplicationContext(),
								"images/templates/portrait/glow/graphic_portrait_min_with_glow_"
										+ (i + 1) + ".png");
						listBitmapGlow.add(bitmap);

						// landscape images glow
						bitmap = Common.getBitmapFromAsset(
								getApplicationContext(),
								"images/templates/landscape/glow/graphic_landscape_min_with_glow_"
										+ (i + 1) + ".png");

						listBitmapGlow.add(bitmap);

					}
					handler.sendEmptyMessage(0);
				} catch (Exception ex) {
				}
				loaded = true;
				updateScrollPage();
				isFirstRunning = false;
			}
		};
		t.start();
	}

	/** The on screen switch listener. */
	private final HorizontalPager.OnScreenSwitchListener onScreenSwitchListener = new HorizontalPager.OnScreenSwitchListener() {

		public void onScreenSwitched(final int screen) {
			/*
			 * this method is executed if a screen has been activated, i.e. the
			 * screen is completely visible and the animation has stopped (might
			 * be useful for removing / adding new views)
			 */
			// Do something long
			/*
			 * loading other images
			 */
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						@Override
						public void run() {

						}
					});
				}
			};
			new Thread(runnable).start();
			/*
			 * end loading images
			 */
			imageMask.setVisibility(View.GONE);

			/** change current page for paginator */
			currentPos = scrollImage.getCurrentScreen();
			if (!isShowTemplate && !scrollImage.getIsTouch()) {
				animatePieces();
			}

			if (isLandscape)
				listPieces.drawScroll(scrollImage.getCurrentScreen(),
						orientation);
			else {
				listPieces.drawScroll(
						Common.NUMBER_OF_MASK - 1
								- scrollImage.getCurrentScreen(), orientation);
			}

		}

		public void onTouch(boolean onTouch) {

			if (onTouch) {
				animatePieces();
			}
		}

		@Override
		public void onChangeStartEnd(int screen) {
			currentPos = screen;
			if (isLandscape)
				listPieces.drawScroll(currentPos, orientation);
			else
				listPieces.drawScroll(Common.NUMBER_OF_MASK - 1 - currentPos,
						orientation);
		}

	};

	// private Bitmap maskBitmapPort;
	// private Bitmap maskBitmapLand;

	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	LayoutInflater controlInflater = null;
	View viewControl;
	private ViewFinder listPieces;
	private ImageButton showImageScroll;
	private ImageButton selectCamera;
	private ImageButton flashSwitch;
	private ImageButton showGallery;
	private ImageButton cameraButton;
	private FrameLayout frameLayoutScroll;
	private int currentPos = 0;
	private DisplayMetrics displayMetric;
	private ImageButton buttonTakePicture;
	private HorizontalPager scrollImage;
	private boolean isShowTemplate = false;
	private boolean isFirstRunning = true;
	private boolean isFlashOn = false;
	private boolean isAfterCamera = true;
	private boolean isTaking = false;
	private Parameters parameters;// parameter for camera setting
	private int orientation;// orientation of mobile
	private Configuration config;// get config
	private ArrayList<RelativeLayout> listLayout;// private array list layout
													// for list mask
	private ArrayList<RelativeLayout> listLayoutGlow;// array list for image
														// mask glow

	private ArrayList<Bitmap> listBitmapSolid;// bitmaps for solid image mask
	private ArrayList<Bitmap> listBitmapGlow;// bitmaps for glow image mask
	private boolean isLandscape;// check orientation physical
	private FrameLayout.LayoutParams templateTabParam;// variables for layout by
														// coding
	private SimpleDateFormat timeStampFormat = new SimpleDateFormat(
			"yyyyMMddHHmmssSS");// date format for name of picture
	private ImageView imageMask;
	private FrameLayout.LayoutParams paramLayout;
	private RelativeLayout linearToolLeft;
	private float density;

	private Handler handler;

	// check images loaded when camera screen start
	private boolean loaded = false;
}