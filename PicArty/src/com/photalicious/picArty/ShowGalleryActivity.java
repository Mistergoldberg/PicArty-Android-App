package com.photalicious.picArty;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.photalicious.picArty.libs.HorizontalPager;
import com.photalicious.picArty.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ShowGalleryActivity extends Activity implements OnClickListener {

	/**************************************************************************************************************
	 * override method
	 ***************************************************************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		initVariables();
		sdDir = new File("/sdcard/PicArty");
		sdDirFiles = sdDir.listFiles();
		Arrays.sort(sdDirFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(
						f1.lastModified());
			}
		});
		isSingleImg = getIntent().getBooleanExtra("isSingleImg", false);
		imageName = getIntent().getStringExtra("imageName");

		if (!isSingleImg) {
			updateDataScroll();
			setCurrentSlide();
			lGallery.addView(scrollGallery, new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		} else {
			btnBack.setVisibility(View.GONE);
			btnDeleteImage.setVisibility(View.GONE);
			ImageView imgview = new ImageView(this);
			Runtime.getRuntime().gc();
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			Bitmap bitmap = BitmapFactory.decodeFile(imageName);
			imgview.setImageBitmap(bitmap);
			lGallery.addView(imgview, new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (currentBitmap != null) {
			currentBitmap.recycle();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/**
		 * invite friends via emails
		 * */
		/*
		if (requestCode == 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Do you want to invite your friend using this app?")
					.setCancelable(true)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									ArrayList<String> listEmail = new ArrayList<String>();
									ContentResolver cr = getContentResolver();
									Cursor cur = cr
											.query(ContactsContract.Contacts.CONTENT_URI,
													null, null, null, null);
									while (cur.moveToNext()) {
										String idCon = cur.getString(cur
												.getColumnIndex(ContactsContract.Contacts._ID));
										Cursor emailCur = cr
												.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
														null,
														ContactsContract.CommonDataKinds.Email.CONTACT_ID
																+ " = ?",
														new String[] { idCon },
														null);
										while (emailCur.moveToNext()) {
											String email = emailCur.getString(emailCur
													.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
											listEmail.add(email);
										}
										emailCur.close();
									}
									cur.close();
									final String[] arrEmail = new String[listEmail
											.size()];
									for (int i = 0; i < listEmail.size(); i++) {
										arrEmail[i] = listEmail.get(i);
									}

									try {
										String temp[] = (Build.VERSION.RELEASE)
												.split("\\.");
										if (Integer.parseInt(temp[0]) < 4) {
											//version >= 4.0
											Thread t = new Thread(){
												@Override
												public void run(){
													GMailSender sender = new GMailSender(
															"team.vnosc@gmail.com",
															"tuoitrevn@2011");
													for (int i = 0; i < arrEmail.length; i++) {
														
															try {
																sender.sendMail("PicArty - Best Photo App Ever!",
																		"Greetings,\n\nYou have been invited to PicArty, an innovative photo app that creates portrait montages by mixing and matching users’ faces with professionally photographed masks making every PicArty pic a winner! Artistes simply select a mask, line up their face, strike a pose and click. Unlike other apps that just make you fat, old, or bald, PicArty is packed with twelve highly entertaining masks. The app is great for dinner parties, blind dates and facebook updates.\n\nCheck out www.PicArty.com now!",
																		"picarty@gmail.com",
																		arrEmail[i]);
																Log.d("EMAIL", arrEmail[i]);
															} catch (Exception e) {
																e.printStackTrace();
															}
													}
												}
											};
											t.start();
											
										} else {// version < 4.0

											Intent share = new Intent(
													Intent.ACTION_SEND);
											share.putExtra(
													android.content.Intent.EXTRA_SUBJECT,
													"PicArty - Best Photo App Ever!");
											share.putExtra(Intent.EXTRA_BCC,
													arrEmail);
											share.putExtra(
													android.content.Intent.EXTRA_TEXT,
													Html.fromHtml("Greetings,<br/>You have been invited to PicArty, an innovative photo app that creates portrait montages by mixing and matching users’ faces with professionally photographed masks making every PicArty pic a winner! Artistes simply select a mask, line up their face, strike a pose and click. Unlike other apps that just make you fat, old, or bald, PicArty is packed with twelve highly entertaining masks. The app is great for dinner parties, blind dates and facebook updates.<br/><br/>Check out www.PicArty.com now!"));
											share.setType("text/html");

											PackageManager pm = ShowGalleryActivity.this
													.getPackageManager();
											List<ResolveInfo> activityList = pm
													.queryIntentActivities(
															share, 0);
											for (final ResolveInfo app : activityList) {
												if ((app.activityInfo.packageName)
														.contains("google.android")) {
													final ActivityInfo activity = app.activityInfo;
													final ComponentName name = new ComponentName(
															activity.applicationInfo.packageName,
															activity.name);
													share.addCategory(Intent.CATEGORY_LAUNCHER);
													share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
															| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
													share.setComponent(name);
													ShowGalleryActivity.this
															.startActivity(share);
													break;
												}
											}
										}
									} catch (Exception ex) {

									}
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
								
									dialog.cancel();
								}
							});

			AlertDialog alert = builder.create();
			alert.show();
		}*/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_g_share:
			// dialogShare = new DialogShare(this,
			// sdDirFiles[currentPos].getAbsolutePath());
			// dialogShare.show();
			Intent share = new Intent(Intent.ACTION_SEND);
			Uri screenshotUri = null;
			if (!isSingleImg) {
				screenshotUri = Uri.fromFile(new File(sdDirFiles[currentPos]
						.getAbsolutePath()));
			} else {
				screenshotUri = Uri.fromFile(new File(imageName));
			}
			share.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"PicArty - Best Photo App Ever!");
			share.putExtra(Intent.EXTRA_STREAM, screenshotUri);
			share.setType("image/jpeg");

			startActivityForResult(Intent.createChooser(share, "Share Image"),
					1);
			break;
		case R.id.btn_back:
			Intent intentBack = new Intent(getApplicationContext(),
					GridImagesActivity.class);
			startActivity(intentBack);
			break;
		case R.id.btn_g_show_camera:
			Intent intent = new Intent(getApplicationContext(),
					PicArtyActivity.class);
			startActivity(intent);
			ShowGalleryActivity.this.finish();
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
									if (currentPos != 0)
										currentPos--;
									if (deleted) {
										scrollGallery = new HorizontalPager(
												getApplicationContext());
										sdDirFiles = sdDir.listFiles();
										if (sdDirFiles.length == 0) {
											ShowGalleryActivity.this.finish();
											return;
										}
										Arrays.sort(sdDirFiles,
												new Comparator<File>() {
													public int compare(File f1,
															File f2) {
														return Long
																.valueOf(
																		f2.lastModified())
																.compareTo(
																		f1.lastModified());
													}
												});
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

	/**************************************************************************************************************
	 * public method
	 **************************************************************************************************************/
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
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
			if (bitmap.getHeight() > bitmap.getWidth()) {
				widthImage = context.getResources().getDisplayMetrics().widthPixels;
				heightImage = (int) (widthImage * 640 / (float) 480);
			} else {
				heightImage = context.getResources().getDisplayMetrics().heightPixels;
				widthImage = (int) (heightImage * 640 / (float) 480);
			}

			bitmap = Bitmap.createScaledBitmap(bitmap, widthImage, heightImage,
					true);

			ImageView currentSlide = listGallary.get(listGallary.size() - 1);
			currentSlide.setImageBitmap(bitmap);
		}
		if (currentPos == listGallary.size() - 1) {
			Runtime.getRuntime().gc();
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			Bitmap bitmap = BitmapFactory.decodeFile(
					sdDirFiles[0].getAbsolutePath(), opts);

			if (bitmap.getHeight() > bitmap.getWidth()) {
				widthImage = context.getResources().getDisplayMetrics().widthPixels;
				heightImage = (int) (widthImage * 640 / (float) 480);
			} else {
				heightImage = context.getResources().getDisplayMetrics().heightPixels;
				widthImage = (int) (heightImage * 640 / (float) 480);
			}

			bitmap = Bitmap.createScaledBitmap(bitmap, widthImage, heightImage,
					true);

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
				if (bitmap.getHeight() > bitmap.getWidth()) {
					widthImage = context.getResources().getDisplayMetrics().widthPixels;
					heightImage = (int) (widthImage * 640 / (float) 480);
				} else {
					heightImage = context.getResources().getDisplayMetrics().heightPixels;
					widthImage = (int) (heightImage * 640 / (float) 480);
				}

				bitmap = Bitmap.createScaledBitmap(bitmap, widthImage,
						heightImage, true);

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

								if (currentBitmap.getHeight() > currentBitmap
										.getWidth()) {
									widthImage = context.getResources()
											.getDisplayMetrics().widthPixels;
									heightImage = (int) (widthImage * 640 / (float) 480);
								} else {
									heightImage = context.getResources()
											.getDisplayMetrics().heightPixels;
									widthImage = (int) (heightImage * 640 / (float) 480);
								}

								currentBitmap = Bitmap.createScaledBitmap(
										currentBitmap, widthImage, heightImage,
										true);

								ImageView currentSlide = listGallary.get(0);
								currentSlide.setBackgroundColor(Color.BLACK);
								currentSlide.setImageBitmap(currentBitmap);
							} else {
								currentBitmap = BitmapFactory.decodeFile(
										sdDirFiles[currentPos + 1]
												.getAbsolutePath(), opts);

								if (currentBitmap.getHeight() > currentBitmap
										.getWidth()) {
									widthImage = context.getResources()
											.getDisplayMetrics().widthPixels;
									heightImage = (int) (widthImage * 640 / (float) 480);
								} else {
									heightImage = context.getResources()
											.getDisplayMetrics().heightPixels;
									widthImage = (int) (heightImage * 640 / (float) 480);
								}

								currentBitmap = Bitmap.createScaledBitmap(
										currentBitmap, widthImage, heightImage,
										true);

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

								if (currentBitmap.getHeight() > currentBitmap
										.getWidth()) {
									widthImage = context.getResources()
											.getDisplayMetrics().widthPixels;
									heightImage = (int) (widthImage * 640 / (float) 480);
								} else {
									heightImage = context.getResources()
											.getDisplayMetrics().heightPixels;
									widthImage = (int) (heightImage * 640 / (float) 480);
								}

								currentBitmap = Bitmap.createScaledBitmap(
										currentBitmap, widthImage, heightImage,
										true);

								ImageView currentSlide = listGallary
										.get(listGallary.size() - 1);
								currentSlide.setBackgroundColor(Color.BLACK);
								currentSlide.setImageBitmap(currentBitmap);
							} else {
								currentBitmap = BitmapFactory.decodeFile(
										sdDirFiles[currentPos - 1]
												.getAbsolutePath(), opts);

								if (currentBitmap.getHeight() > currentBitmap
										.getWidth()) {
									widthImage = context.getResources()
											.getDisplayMetrics().widthPixels;
									heightImage = (int) (widthImage * 640 / (float) 480);
								} else {
									heightImage = context.getResources()
											.getDisplayMetrics().heightPixels;
									widthImage = (int) (heightImage * 640 / (float) 480);
								}

								currentBitmap = Bitmap.createScaledBitmap(
										currentBitmap, widthImage, heightImage,
										true);

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

	@SuppressWarnings("unused")
	private String getApplicationVersion() {
		PackageManager pm = getPackageManager();
		String version = "r0";
		try {
			PackageInfo pi = pm.getPackageInfo(
					"com.tacticalnuclearstrike.tttumblr", 0);
			version = pi.versionName;
		} catch (NameNotFoundException e) {

		}
		return version;
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

	private int widthImage;
	private int heightImage;

	// single image when capture and go to gallery
	private boolean isSingleImg = false;
	private String imageName;

}
