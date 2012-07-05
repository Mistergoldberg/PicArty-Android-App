package com.vnosc.picArty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.Facebook.DialogListener;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.vnosc.picArty.common.Common;
import com.vnosc.picArty.libs.facebook.BaseRequestListener;
import com.vnosc.picArty.libs.facebook.SessionEvents;
import com.vnosc.picArty.libs.facebook.SessionEvents.AuthListener;
import com.vnosc.picArty.libs.facebook.SessionEvents.LogoutListener;
import com.vnosc.picArty.libs.flickr.FlickShareActivity;
import com.vnosc.picArty.libs.tumblr.AccountActivity;
import com.vnosc.picArty.libs.twitter.ConnectActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DialogShare extends Dialog implements OnClickListener {

	public DialogShare(Context context, String file) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_share);
		this.fileName = file;
		this.context = context;

		// init variables
		mFacebook = new Facebook(Common.APP_FACEBOOK_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		mHandler = new Handler();
		SessionEvents.addLogoutListener(new SampleLogoutListener());

		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start("UA-9100060-3", 20, context);
		tracker.trackPageView("/OS/" + Build.VERSION.SDK);
		tracker.trackPageView("/rev/" + context);

		SharedPreferences settings = context.getSharedPreferences(
				Common.PREFERENCE_SHARE, 0);
		firstOrder = settings.getInt(Common.FIRST_ORDER, 0);
		secondOrder = settings.getInt(Common.SECOND_ORDER, 0);
		thirdOrder = settings.getInt(Common.THIRD_ORDER, 0);
		numberVisible = settings.getInt(Common.NUMBER_SHARE_VISIBLE, 3);

		android.view.WindowManager.LayoutParams lp = this.getWindow()
				.getAttributes();
		lp.width = context.getResources().getDisplayMetrics().widthPixels;
		// lp.height = context.getResources().getDisplayMetrics().widthPixels;
		lp.gravity = Gravity.CENTER;
		lp.dimAmount = 0;
		this.getWindow().setAttributes(lp);
		btnMore = (Button) findViewById(R.id.btn_share_option);
		btnMore.setOnClickListener(this);
		btnShare1 = (Button) findViewById(R.id.btn_share_1);
		btnShare1.setOnClickListener(this);
		btnShare2 = (Button) findViewById(R.id.btn_share_2);
		btnShare2.setOnClickListener(this);
		btnShare3 = (Button) findViewById(R.id.btn_share_3);
		btnShare3.setOnClickListener(this);
		setButtonLabel();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_share_1:
			share(firstOrder);
			break;
		case R.id.btn_share_2:
			share(secondOrder);
			break;
		case R.id.btn_share_3:
			share(thirdOrder);
			break;
		case R.id.btn_share_option:
			Intent intent = new Intent(context, SelectShare.class);
			intent.putExtra("filename", fileName);
			// context.startActivity(intent);
			Activity activity = (Activity) context;
			activity.startActivityForResult(intent, Common.OPTION_SHARE);
			break;

		default:
			break;
		}
	}

	public void setButtonLabel() {
		if (numberVisible >= 3) {
			btnShare1.setText(Common.SHARE_NAMES[firstOrder]);
			btnShare2.setText(Common.SHARE_NAMES[secondOrder]);
			btnShare3.setText(Common.SHARE_NAMES[thirdOrder]);
		}else if(numberVisible == 2){
			btnShare1.setText(Common.SHARE_NAMES[firstOrder]);
			btnShare2.setText(Common.SHARE_NAMES[secondOrder]);
			btnShare3.setVisibility(View.GONE);
		}else if(numberVisible == 1){
			btnShare1.setText(Common.SHARE_NAMES[firstOrder]);
			btnShare2.setVisibility(View.GONE);
			btnShare3.setVisibility(View.GONE);
		}else{
			btnShare1.setVisibility(View.GONE);
			btnShare2.setVisibility(View.GONE);
			btnShare3.setVisibility(View.GONE);
		}
	}

	// share image via
	public void share(int id) {// id of target share facebook:1 twitter:2
								// tumblr:3 flickr:4 email:5 logout:6
		switch (id) {
		case Common.FACEBOOK_ORDER:
			if (mFacebook.isSessionValid()) {
				uploadPhotoFacebook();
			} else {
				mFacebook.authorize((Activity) context, permissionsFace,
						new LoginDialogListener());
			}
			break;

		case Common.TWITTER_ORDER:
			// twitter share
			Intent intent = new Intent(context, ConnectActivity.class);
			intent.putExtra("filename", fileName);
			context.startActivity(intent);
			break;

		case Common.TUMBLR_ORDER:
			// tumblr share
			tracker.trackPageView("/AccountActivity");
			((Activity) context).startActivityForResult(new Intent(context,
					AccountActivity.class), Common.OK_TUMBLR);
			break;

		case Common.FLICKR_ORDER:
			// flickr share
			Intent intentFlickr = new Intent(context, FlickShareActivity.class);
			intentFlickr.putExtra("filename", fileName);
			context.startActivity(intentFlickr);
			break;

		case Common.EMAIL_ORDER:
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);

			Uri screenshotUri = Uri.fromFile(new File(fileName));

			sharingIntent.setType("image/png");
			sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
			context.startActivity(Intent.createChooser(sharingIntent,
					"Share image using"));
			break;

		case Common.LOGOUT_ORDER:
			if (mFacebook.isSessionValid()) {
				SessionEvents.onLogoutBegin();
				AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(
						mFacebook);
				asyncRunner.logout(getContext(), new LogoutRequestListener());
			}
			break;

		default:
			break;
		}
	}

	// classes for facebook share

	public void uploadPhotoFacebook() {
		uploadDialog = ProgressDialog.show(context, "", "Uploading...");
		Bundle params = new Bundle();
		params.putString("method", "photos.upload");
		params.putString("caption", "Shared from PicArty Android app");
		params.putString("description", "Shared from PicArty Android app");
		File myPhoto = new File(fileName);
		try {
			byte[] imgData = new byte[(int) myPhoto.length()];
			FileInputStream fis = new FileInputStream(myPhoto);
			fis.read(imgData);
			params.putByteArray("picture", imgData);

		} catch (IOException e) {
			e.printStackTrace();
		}

		mAsyncRunner.request(null, params, "POST", new SampleUploadListener(),
				null);
	}

	private final class LoginDialogListener implements DialogListener {
		public void onComplete(Bundle values) {
			SessionEvents.onLoginSuccess();
			uploadPhotoFacebook();
		}

		public void onFacebookError(FacebookError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onError(DialogError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onCancel() {
			SessionEvents.onLoginError("Action Canceled");
		}
	}

	private class LogoutRequestListener extends BaseRequestListener {
		public void onComplete(String response, final Object state) {
			// callback should be run in the original thread,
			// not the background thread
			mHandler.post(new Runnable() {
				public void run() {
					SessionEvents.onLogoutFinish();
					Toast.makeText(context, "Logout Facebook",
							Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {

		}
	}

	public class SampleAuthListener implements AuthListener {

		public void onAuthSucceed() {

		}

		public void onAuthFail(String error) {

		}
	}

	public class SampleLogoutListener implements LogoutListener {
		public void onLogoutBegin() {

		}

		public void onLogoutFinish() {

		}
	}

	public class SampleUploadListener extends BaseRequestListener {

		public void onComplete(final String response, final Object state) {
			try {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								uploadDialog.hide();
								Toast.makeText(context, "Shared",
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				};
				new Thread(runnable).start();
				// process the response here: (executed in background thread)
				Log.d("Facebook-Example", "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				@SuppressWarnings("unused")
				final String src = json.getString("src");
				// then post the processed result back to the UI thread
				// if we do not do this, an runtime exception will be generated
				// e.g. "CalledFromWrongThreadException: Only the original
				// thread that created a view hierarchy can touch its views."

			} catch (JSONException e) {
				Log.w("Facebook-Example", "JSON Error in response");
			} catch (FacebookError e) {
				Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
			}
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {

		}
	}

	private Button btnMore;
	private Button btnShare1;
	private Button btnShare2;
	private Button btnShare3;
	private Context context;

	private String fileName;
	private int firstOrder;
	private int secondOrder;
	private int thirdOrder;
	private int numberVisible;

	// facebook share
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler;
	public static ProgressDialog uploadDialog;
	private String[] permissionsFace = { "offline_access", "publish_stream",
			"user_photos", "publish_checkins", "photo_upload" };
	GoogleAnalyticsTracker tracker;
}
