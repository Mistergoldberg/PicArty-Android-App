package com.vnosc.picArty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.vnosc.picArty.adapter.ListViewAdapter;
import com.vnosc.picArty.common.Common;
import com.vnosc.picArty.libs.facebook.BaseRequestListener;
import com.vnosc.picArty.libs.facebook.SessionEvents;
import com.vnosc.picArty.libs.facebook.SessionEvents.AuthListener;
import com.vnosc.picArty.libs.facebook.SessionEvents.LogoutListener;
import com.vnosc.picArty.libs.flickr.FlickShareActivity;
import com.vnosc.picArty.libs.tumblr.AccountActivity;
import com.vnosc.picArty.libs.tumblr.UploadImageActivity;
import com.vnosc.picArty.libs.twitter.ConnectActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class SelectShare extends Activity {

	private String fileName;
	private Button btnEdit;
	private Button btnCancel;
	GoogleAnalyticsTracker tracker;
	private ListViewAdapter adapter;
	private ListViewAdapter adapterSocialShare;
	private boolean isEdit = false;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	SharedPreferences settingSwitch;
	private String[] permissionsFace = { "offline_access", "publish_stream",
			"user_photos", "publish_checkins", "photo_upload" };
	private ArrayList<Integer> listOrderShare;
	private boolean[] listSwitchShareSocial;
	private boolean[] listSwitchShareLocal;

	public SelectShare() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTitle("My Account");

		super.onCreate(savedInstanceState);

		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start("UA-9100060-3", 20, this);
		tracker.trackPageView("/OS/" + Build.VERSION.SDK);
		tracker.trackPageView("/rev/" + getApplicationVersion());
		fileName = getIntent().getStringExtra("filename");
		setContentView(R.layout.layout_select_share);
		mContext = this;
		initVariables();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Common.OK_TUMBLR:
			if (resultCode == RESULT_OK) {
				tracker.trackPageView("/UploadImageActivity");
				Intent intent = new Intent(SelectShare.this,
						UploadImageActivity.class);
				intent.putExtra("filename", fileName);
				startActivity(intent);
			}
			break;
		case Common.OPTION_SHARE:
			break;
		}
	}

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

	public void initVariables() {
		listOrderShare = new ArrayList<Integer>();
		settings = getSharedPreferences(Common.PREFERENCE_SHARE, 0);
		listOrderShare.add(settings.getInt(Common.FIRST_ORDER, 0));
		listOrderShare.add(settings.getInt(Common.SECOND_ORDER, 0));
		listOrderShare.add(settings.getInt(Common.THIRD_ORDER, 0));
		for (int i = 1; i <= 6; i++) {
			if (!checkShowShare(i)) {
				listOrderShare.add(i);
			}
		}

		settingSwitch = getSharedPreferences(Common.PREFERENCE_SWITCH_SHARE, 0);

		listSwitchShareSocial = new boolean[4];
		listSwitchShareLocal = new boolean[2];
		readShareOnOff();

		mFacebook = new Facebook(Common.APP_FACEBOOK_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		mHandler = new Handler();

		btnEdit = (Button) findViewById(R.id.btn_edit);
		btnEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isEdit = !isEdit;
				if (!isEdit) {
					setShareOnOff();
					initDataAdapter();
					adapter.setValues(adapterLocal);
					adapterSocialShare.setValues(adapterSocial);
					updateShareDialog();
				} else {
					adapterLocal = Common.ADAPTER_LOCAL;
					adapter.setValues(adapterLocal);

					adapterSocial = Common.ADAPTER_SOCIAL;
					adapterSocialShare.setValues(adapterSocial);
				}
				adapter.setIsEdit(isEdit);
				adapter.notifyDataSetChanged();
				adapterSocialShare.setIsEdit(isEdit);
				adapterSocialShare.notifyDataSetChanged();

			}
		});
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectShare.this.finish();
			}
		});

		lvShareLocalSerVice = (ListView) findViewById(R.id.lv_list_local);
		lvShareSocial = (ListView) findViewById(R.id.lv_list_social);
		adapterLocal = new String[] { "Email", "Logout" };
		adapterSocial = new String[] { "Twitter", "Facebook", "Tumblr",
				"Flickr" };
		initDataAdapter();
		adapter = new ListViewAdapter(getApplicationContext(), adapterLocal,
				listSwitchShareLocal);
		adapterSocialShare = new ListViewAdapter(this, adapterSocial,
				listSwitchShareSocial);
		lvShareLocalSerVice.setAdapter(adapter);
		lvShareSocial.setAdapter(adapterSocialShare);

		lvShareLocalSerVice
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						if (isEdit) {
							listSwitchShareLocal[position] = !listSwitchShareLocal[position];
							adapter.setFilter(listSwitchShareLocal);
							adapter.notifyDataSetChanged();
							return;
						}
						ListViewAdapter adapter = (ListViewAdapter) arg0
								.getAdapter();
						if (adapter.getShareIndex(position) == Common.SHARE_NAMES[5]) {
							// email
							if (settings.getInt(Common.FIRST_ORDER, 0) != Common.EMAIL_ORDER) {
								settingShare(Common.EMAIL_ORDER);
							}
							Intent sharingIntent = new Intent(
									Intent.ACTION_SEND);

							Uri screenshotUri = Uri
									.fromFile(new File(fileName));

							sharingIntent.setType("image/png");
							sharingIntent.putExtra(Intent.EXTRA_STREAM,
									screenshotUri);
							startActivity(Intent.createChooser(sharingIntent,
									"Share image using"));
							return;
						}
						if (adapter.getShareIndex(position) == Common.SHARE_NAMES[6]) {
							// logout
							if (settings.getInt(Common.FIRST_ORDER, 0) != Common.LOGOUT_ORDER) {
								settingShare(Common.LOGOUT_ORDER);
							}
							if (mFacebook.isSessionValid()) {
								SessionEvents.onLogoutBegin();
								AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(
										mFacebook);
								asyncRunner.logout(mContext,
										new LogoutRequestListener());
							}
							return;
						}

					}
				});

		lvShareSocial
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						if (isEdit) {
							listSwitchShareSocial[position] = !listSwitchShareSocial[position];
							adapterSocialShare.setFilter(listSwitchShareSocial);
							adapterSocialShare.notifyDataSetChanged();
							return;
						}
						ListViewAdapter adapter = (ListViewAdapter) arg0
								.getAdapter();
						if (adapter.getShareIndex(position) == Common.SHARE_NAMES[1]) {
							// facebook
							if (settings.getInt(Common.FIRST_ORDER, 0) != Common.FACEBOOK_ORDER) {
								settingShare(Common.FACEBOOK_ORDER);
							}
							if (mFacebook.isSessionValid()) {
								uploadPhotoFacebook();
							} else {
								mFacebook.authorize(SelectShare.this,
										permissionsFace,
										new LoginDialogListener());
							}
							return;
						}
						if (adapter.getShareIndex(position) == Common.SHARE_NAMES[2]) {
							// twitter
							if (settings.getInt(Common.FIRST_ORDER, 0) != Common.TWITTER_ORDER) {
								settingShare(Common.TWITTER_ORDER);
							}
							// twitter share
							Intent intent = new Intent(getApplicationContext(),
									ConnectActivity.class);
							intent.putExtra("filename", fileName);
							startActivity(intent);
							return;
						}
						if (adapter.getShareIndex(position) == Common.SHARE_NAMES[3]) {
							// tumblr
							if (settings.getInt(Common.FIRST_ORDER, 0) != Common.TUMBLR_ORDER) {
								settingShare(Common.TUMBLR_ORDER);
							}

							// tumblr share
							tracker.trackPageView("/AccountActivity");
							startActivityForResult(new Intent(SelectShare.this,
									AccountActivity.class), Common.OK_TUMBLR);
							return;
						}
						if (adapter.getShareIndex(position) == Common.SHARE_NAMES[4]) {
							// flickr
							if (settings.getInt(Common.FIRST_ORDER, 0) != Common.FLICKR_ORDER) {
								settingShare(Common.FLICKR_ORDER);
							}

							// flickr share
							Intent intentFlickr = new Intent(
									getApplicationContext(),
									FlickShareActivity.class);
							intentFlickr.putExtra("filename", fileName);
							startActivity(intentFlickr);
							return;
						}
					}
				});
	}

	/*
	 * init adapter
	 */

	public void initDataAdapter() {
		int countLocal = 0;
		int countSocial = 0;
		int count = 0;
		for (int i = 0; i < listSwitchShareLocal.length; i++) {
			if (listSwitchShareLocal[i])
				countLocal++;
		}

		adapterLocal = new String[countLocal];
		for (int i = 0; i < listSwitchShareLocal.length; i++) {
			if (listSwitchShareLocal[i]) {
				adapterLocal[count] = Common.ADAPTER_LOCAL[i];
				count++;
			}
		}

		for (int i = 0; i < listSwitchShareSocial.length; i++) {
			if (listSwitchShareSocial[i])
				countSocial++;
		}
		count = 0;
		adapterSocial = new String[countSocial];
		for (int i = 0; i < listSwitchShareSocial.length; i++) {

			if (listSwitchShareSocial[i]) {
				adapterSocial[count] = Common.ADAPTER_SOCIAL[i];
				count++;
			}
		}
	}

	/*
	 * read share on/off
	 */
	public void readShareOnOff() {

		for (int i = 0; i < 2; i++) {
			listSwitchShareLocal[i] = settingSwitch.getBoolean(
					Common.ADAPTER_LOCAL[i], true);

		}

		for (int i = 0; i < 4; i++) {
			listSwitchShareSocial[i] = settingSwitch.getBoolean(
					Common.ADAPTER_SOCIAL[i], true);
		}

	}

	/*
	 * set share on/off
	 */
	public void setShareOnOff() {

		editor = settingSwitch.edit();

		for (int i = 0; i < listSwitchShareLocal.length; i++) {
			editor.putBoolean(Common.ADAPTER_LOCAL[i], listSwitchShareLocal[i]);
		}

		for (int i = 0; i < listSwitchShareSocial.length; i++) {
			editor.putBoolean(Common.ADAPTER_SOCIAL[i],
					listSwitchShareSocial[i]);
		}
		editor.commit();
	}

	/*
	 * setting order share
	 */
	public void settingShare(int currentClicked) {
		for (int i = 1; i < listOrderShare.size(); i++) {
			if (currentClicked == listOrderShare.get(i)) {
				listOrderShare.remove(i);
				listOrderShare.add(0, currentClicked);
				saveSetting();
				return;
			}
		}
		listOrderShare.remove(listOrderShare.size() - 1);
		listOrderShare.add(0, currentClicked);
		saveSetting();
	}

	public void saveSetting() {
		editor = settings.edit();

		if (listOrderShare.size() >= 3) {
			editor.putInt(Common.FIRST_ORDER, listOrderShare.get(0));
			editor.putInt(Common.SECOND_ORDER, listOrderShare.get(1));
			editor.putInt(Common.THIRD_ORDER, listOrderShare.get(2));
		} else {
			switch (listOrderShare.size()) {
			case 1:
				editor.putInt(Common.FIRST_ORDER, listOrderShare.get(0));
				break;
			case 2:
				editor.putInt(Common.FIRST_ORDER, listOrderShare.get(0));
				editor.putInt(Common.SECOND_ORDER, listOrderShare.get(1));
				break;
			default:
				break;
			}
		}
		editor.commit();
	}

	public void updateShareDialog() {
		for (int i = 1; i >= 0; i--) {
			if (!listSwitchShareLocal[i] && checkShowShare(i + 5)) {
				listOrderShare.remove(getIndexItem(i + 5));
			} else if (listSwitchShareLocal[i] && !checkShowShare(i + 5)) {
				listOrderShare.add(i + 5);
			}
		}
		for (int i = 3; i >= 0; i--) {
			if (!listSwitchShareSocial[i] && checkShowShare(i + 1)) {
				listOrderShare.remove(getIndexItem(i + 1));
			} else if (listSwitchShareSocial[i] && !checkShowShare(i + 1)) {
				listOrderShare.add(i + 1);
			}
		}

		editor = settings.edit();
		editor.putInt(Common.NUMBER_SHARE_VISIBLE, listOrderShare.size());
		editor.commit();
		saveSetting();
	}

	// check the share have shown in dialog
	public boolean checkShowShare(int shareIndex) {
		for (int i = 0; i < listOrderShare.size(); i++) {
			if (shareIndex == listOrderShare.get(i))
				return true;
		}
		return false;
	}

	// get index item
	public int getIndexItem(int shareIndex) {
		for (int i = 0; i < listOrderShare.size(); i++) {
			if (shareIndex == listOrderShare.get(i))
				return i;
		}
		return -1;
	}

	/*
	 * facebook area
	 */
	public void uploadPhotoFacebook() {
		uploadDialog = ProgressDialog.show(mContext, "", "Uploading...");
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
					Toast.makeText(mContext, "Logout Facebook",
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
								Toast.makeText(mContext, "Shared",
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				};
				new Thread(runnable).start();
				// process the response here: (executed in background thread)
				Log.d("Facebook-Example", "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				final String src = json.getString("src");
				src.toString();
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

	/*
	 * end facebook
	 */

	private ListView lvShareLocalSerVice;
	private ListView lvShareSocial;
	private String[] adapterLocal;
	private String[] adapterSocial;

	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler;

	private Context mContext;
	public static ProgressDialog uploadDialog;
}
