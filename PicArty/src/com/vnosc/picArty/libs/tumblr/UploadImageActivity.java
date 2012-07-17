package com.vnosc.picArty.libs.tumblr;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.vnosc.picArty.R;

public class UploadImageActivity extends PostActivity {
	private static final String TAG = "UploadImageActivity";

	Uri outputFileUri;
	int TAKE_PICTURE = 0;
	int SELECT_IMAGE = 1;
	// TumblrApi api;
	GoogleAnalyticsTracker tracker;

	private String fileName;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start("UA-9100060-3", 20, this);

		// api = new TumblrApi(this);
		setContentView(R.layout.uploadimageview);

		Intent startIntent = getIntent();
		fileName = startIntent.getStringExtra("filename");
		setSelectedImageThumbnail();
		if (startIntent != null && startIntent.getExtras() != null
				&& startIntent.getExtras().containsKey(Intent.EXTRA_STREAM)) {
			Uri startData = (Uri) startIntent.getExtras().get(
					Intent.EXTRA_STREAM);
			Log.d(TAG, "got initial data: " + startData.toString());
			outputFileUri = startData;
			setSelectedImageThumbnail();
		}

		setupButtons();

		Intent intent = getIntent();
		String action = intent.getAction();
		if (Intent.ACTION_SEND.equals(action)) {
			outputFileUri = (Uri) (intent.getExtras().get(Intent.EXTRA_STREAM));
			setSelectedImageThumbnail();
		}
	}

	private void setupButtons() {

		Button btnPostPhoto = (Button) findViewById(R.id.btnPostImage);
//		btnPostPhoto.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
				uploadImage();
//			}
//		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

	}

	private void setSelectedImageThumbnail() {
		try {
			ImageView iv = (ImageView) findViewById(R.id.selectedImage);
			try {
				Bitmap bitmap = BitmapFactory.decodeFile(fileName);
				iv.setImageBitmap(bitmap);
			} catch (OutOfMemoryError ome) {
				Log.e("ttTumblr", ome.getMessage());
			}
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			iv.invalidate();
		} catch (Exception e) {
			Log.d("ttTumblr", e.getMessage());
		}
	}

	@SuppressWarnings("unused")
	private String getRealPathFromURI(Uri contentUri) {
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(contentUri, proj, null, null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();

			return cursor.getString(column_index);
		} catch (Exception ex) {
			return "";
		}
	}

	private void uploadImage() {

		Toast.makeText(getApplicationContext(),
				"Your image uploading in background", Toast.LENGTH_LONG).show();
//		EditText text = (EditText) findViewById(R.id.tbImageCaption);
//		final String caption = text.getText().toString();

		Intent uploadIntent = new Intent(TumblrService.ACTION_POST_PHOTO);
		uploadIntent.putExtra("photo", fileName);// getRealPathFromURI(outputFileUri)
		uploadIntent.putExtra("caption", "PicArty - Best Photo App Ever!");
		uploadIntent.putExtra("options", mPostOptions);
		startService(uploadIntent);

		setResult(RESULT_OK);
		finish();
	}
}
