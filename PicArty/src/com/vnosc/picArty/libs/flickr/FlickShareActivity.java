package com.vnosc.picArty.libs.flickr;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.contacts.ContactsInterface;
import com.aetrion.flickr.people.PeopleInterface;
import com.aetrion.flickr.photosets.PhotosetsInterface;
import com.aetrion.flickr.uploader.UploadMetaData;
import com.aetrion.flickr.uploader.Uploader;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import com.vnosc.picArty.R;

@SuppressLint({ "WorldWriteableFiles", "WorldWriteableFiles" })
public class FlickShareActivity extends Activity {
	public static final String CALLBACK_SCHEME = "picarty-oauth"; //$NON-NLS-1$
	public static final String PREFS_NAME = "flickrj-android-sample-pref"; //$NON-NLS-1$
	public static final String KEY_OAUTH_TOKEN = "flickrj-android-oauthToken"; //$NON-NLS-1$
	public static final String KEY_TOKEN_SECRET = "flickrj-android-tokenSecret"; //$NON-NLS-1$
	public static final String KEY_USER_NAME = "flickrj-android-userName"; //$NON-NLS-1$
	public static final String KEY_USER_ID = "flickrj-android-userId"; //$NON-NLS-1$

	private String fileName;
	
	private static final Logger logger = LoggerFactory
			.getLogger(FlickShareActivity.class);

	private TextView textUserTitle;

	private Button button;
	private ImageView mImageView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flcikr);

		this.button = (Button) this.findViewById(R.id.btn_upload);
		this.textUserTitle = (TextView) this
				.findViewById(R.id.profilePageTitle);
		this.mImageView = (ImageView) findViewById(R.id.imv_image_share);
		
		
		fileName = getIntent().getStringExtra("filename");
		
		Bitmap bitmap  = BitmapFactory.decodeFile(fileName);
		mImageView.setImageBitmap(bitmap);

		OAuth oauth = getOAuthToken();
		if (oauth == null || oauth.getUser() == null) {
			OAuthTask task = new OAuthTask(this);
			task.execute();
		} else {
			load(oauth);
		}
		
		this.button.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				try {
					String apiKey = "7341ad55ff81bc0b2e3152aa7813edd1";
					String token = "72157630279059850-d14def4a64bffd8f";
					String secretKey = "b0702c7712f6ba51";
					@SuppressWarnings("unused")
					String title = "PicArty App";
					String photo = fileName;
					String description = "This photo shared from PicArty Android App";

					Flickr f;
					@SuppressWarnings("unused")
					ContactsInterface c;
					@SuppressWarnings("unused")
					PeopleInterface p;
					@SuppressWarnings("unused")
					PhotosetsInterface o;
					Uploader up = new Uploader(apiKey, secretKey);
					@SuppressWarnings("unused")
					REST rest;

					RequestContext requestContext;

					AuthInterface authInterface;
					String frob = "";
					// void setup()

					InputStream in = new FileInputStream(photo);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					int i;
					byte[] buffer = new byte[1024];
					while ((i = in.read(buffer)) != -1) {
						out.write(buffer, 0, i);
					}
					in.close();
					// byte[] result = out.toByteArray();

					byte data[] = out.toByteArray();
					// size(500, 500);
					f = new Flickr(apiKey, secretKey, (new Flickr(apiKey))
							.getTransport());
					up = f.getUploader();
					authInterface = f.getAuthInterface();
					requestContext = RequestContext.getRequestContext();
					requestContext.setSharedSecret(secretKey);
					frob = authInterface.getFrob();
					System.out.println(frob);
					URL joep = authInterface.buildAuthenticationUrl(Permission.WRITE,
							frob);
					System.out.println(joep.toExternalForm());
					System.out
							.println("Press return after you granted access at this URL:");

					//upload
					Auth auth = new Auth();
					requestContext.setAuth(auth);
					// authInterface.addAuthToken();
					auth.setToken(token);
					auth.setPermission(Permission.WRITE);
					System.out.println("Token Is: " + auth.getToken());
					System.out.println("Permission for token: " + auth.getPermission());
					f.setAuth(auth);
					UploadMetaData uploadMetaData = new UploadMetaData();
					uploadMetaData.setTitle("PicArty Demo");
					uploadMetaData.setDescription(description);
					uploadMetaData.setPublicFlag(true);
					up.upload(data, uploadMetaData);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	private void load(OAuth oauth) {
		if (oauth != null) {
			new LoadUserTask(this, null).execute(oauth);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		// this is very important, otherwise you would get a null Scheme in the
		// onResume later on.
		setIntent(intent);
	}

	public void setUser(User user) {
		textUserTitle.setText(user.getUsername());

	}

	public ImageView getUserIconImageView() {
		return null;
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		String scheme = intent.getScheme();
		OAuth savedToken = getOAuthToken();
		if (CALLBACK_SCHEME.equals(scheme)
				&& (savedToken == null || savedToken.getUser() == null)) {
			Uri uri = intent.getData();
			String query = uri.getQuery();
			logger.debug("Returned Query: {}", query); //$NON-NLS-1$
			String[] data = query.split("&"); //$NON-NLS-1$
			if (data != null && data.length == 2) {
				String oauthToken = data[0].substring(data[0].indexOf("=") + 1); //$NON-NLS-1$
				String oauthVerifier = data[1]
						.substring(data[1].indexOf("=") + 1); //$NON-NLS-1$
				logger.debug(
						"OAuth Token: {}; OAuth Verifier: {}", oauthToken, oauthVerifier); //$NON-NLS-1$

				OAuth oauth = getOAuthToken();
				if (oauth != null && oauth.getToken() != null
						&& oauth.getToken().getOauthTokenSecret() != null) {
					GetOAuthTokenTask task = new GetOAuthTokenTask(this);
					task.execute(oauthToken, oauth.getToken()
							.getOauthTokenSecret(), oauthVerifier);
				}
			}
		}

	}

	public void onOAuthDone(OAuth result) {
		if (result == null) {
			Toast.makeText(this, "Authorization failed", //$NON-NLS-1$
					Toast.LENGTH_LONG).show();
		} else {
			User user = result.getUser();
			OAuthToken token = result.getToken();
			if (user == null || user.getId() == null || token == null
					|| token.getOauthToken() == null
					|| token.getOauthTokenSecret() == null) {
				Toast.makeText(this, "Authorization failed", //$NON-NLS-1$
						Toast.LENGTH_LONG).show();
				return;
			}
//			String message = String
//					.format(Locale.US,
//							"Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s", //$NON-NLS-1$
//							user.getUsername(), user.getId(),
//							token.getOauthToken(), token.getOauthTokenSecret());
//			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			saveOAuthToken(user.getUsername(), user.getId(),
					token.getOauthToken(), token.getOauthTokenSecret());
			load(result);
		}
	}

	public OAuth getOAuthToken() {
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				Context.MODE_WORLD_WRITEABLE);
		String oauthTokenString = settings.getString(KEY_OAUTH_TOKEN, null);
		String tokenSecret = settings.getString(KEY_TOKEN_SECRET, null);
		if (oauthTokenString == null && tokenSecret == null) {
			logger.warn("No oauth token retrieved"); //$NON-NLS-1$
			return null;
		}
		OAuth oauth = new OAuth();
		String userName = settings.getString(KEY_USER_NAME, null);
		String userId = settings.getString(KEY_USER_ID, null);
		if (userId != null) {
			User user = new User();
			user.setUsername(userName);
			user.setId(userId);
			oauth.setUser(user);
		}
		OAuthToken oauthToken = new OAuthToken();
		oauth.setToken(oauthToken);
		oauthToken.setOauthToken(oauthTokenString);
		oauthToken.setOauthTokenSecret(tokenSecret);
		logger.debug(
				"Retrieved token from preference store: oauth token={}, and token secret={}", oauthTokenString, tokenSecret); //$NON-NLS-1$
		return oauth;
	}

	public void saveOAuthToken(String userName, String userId, String token,
			String tokenSecret) {
		logger.debug(
				"Saving userName=%s, userId=%s, oauth token={}, and token secret={}", new String[] { userName, userId, token, tokenSecret }); //$NON-NLS-1$
		SharedPreferences sp = getSharedPreferences(PREFS_NAME,
				Context.MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putString(KEY_OAUTH_TOKEN, token);
		editor.putString(KEY_TOKEN_SECRET, tokenSecret);
		editor.putString(KEY_USER_NAME, userName);
		editor.putString(KEY_USER_ID, userId);
		editor.commit();
	}
}