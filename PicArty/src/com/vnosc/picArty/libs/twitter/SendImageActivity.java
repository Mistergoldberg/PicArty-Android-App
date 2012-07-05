package com.vnosc.picArty.libs.twitter;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.AccessToken;
import twitter4j.http.OAuthAuthorization;
import twitter4j.util.ImageUpload;

import java.io.File;
import java.net.URL;

import com.vnosc.picArty.R;
import com.vnosc.picArty.common.Common;

import android.app.Activity;
import android.app.ProgressDialog;


import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.util.Log;
import android.net.Uri;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.os.AsyncTask;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

/**
 * This example shows how to send image to Twitpic. The image is choosen from camera or from sdcard.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class SendImageActivity extends Activity {
	private ImageView mImageView;
	private ProgressDialog mProgressDialog;
	
	private String mPath;
	
	private static final String twitpic_api_key = "e4b006909aab25404439244e0a5348f7";
	private static final String twitter_consumer_key = "ycwLejracx4aChmINat1Q";
	private static final String twitter_secret_key = "T7sBuUJzAM5SRXtajiqzA495hAmNLhzJGCHWZs78";
	
	private static final String TAG = "AndroidTwitpic";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_send_image);
		
		TwitterApp twitterApp = new TwitterApp(this, twitter_consumer_key,twitter_secret_key);
		
		if (twitterApp.hasAccessToken()) {
			String username 	= twitterApp.getUsername();
			username			= (username.equals("")) ? "No Name" : username;
			
			((TextView) findViewById(R.id.tv_user)).setText("Current Twitter User: "+ username);
		}
		
		mImageView = (ImageView) findViewById(R.id.iv_pic);
		
		
		((Button) findViewById(R.id.btn_send)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new ImageSender().execute();
			}
		});		
		getFile();
    }
    
    private class ImageSender extends AsyncTask<URL, Integer, Long> {
    	private String url;
    	
    	protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(SendImageActivity.this, "", "Sending image...", true);
			
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}
    	
        protected Long doInBackground(URL... urls) {            
            long result = 0;
                       
            TwitterSession twitterSession	= new TwitterSession(SendImageActivity.this);            
            AccessToken accessToken 		= twitterSession.getAccessToken();
			
			Configuration conf = new ConfigurationBuilder()                 
            .setOAuthConsumerKey(Common.OAUTH_CONSUMER_KEY) 
            .setOAuthConsumerSecret(Common.OAUTH_CONSUMER_SECRET) 
            .setOAuthAccessToken(accessToken.getToken()) 
            .setOAuthAccessTokenSecret(accessToken.getTokenSecret()) 
            .build(); 
			
			OAuthAuthorization auth = new OAuthAuthorization (conf, conf.getOAuthConsumerKey (), conf.getOAuthConsumerSecret (),
	                new AccessToken (conf.getOAuthAccessToken (), conf.getOAuthAccessTokenSecret ()));
	        
	        ImageUpload upload = ImageUpload.getTwitpicUploader (twitpic_api_key, auth);
	        
	        Log.d(TAG, "Start sending image...");
	        
	        try {
	        	url = upload.upload(new File(mPath));
	        	result = 1;
	        	
	        	Log.d(TAG, "Image uploaded, Twitpic url is " + url);	        
	        } catch (Exception e) {		   
	        	Log.e(TAG, "Failed to send image");
	        	
	        	e.printStackTrace();
	        }
	        
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {            
        }

        protected void onPostExecute(Long result) {
        	mProgressDialog.cancel();
        	
        	String text = (result == 1) ? "Image sent successfully.\n Twitpic url is: " + url : "Failed to send image";
        	
        	Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
    }
    
    public void getFile(){
    	mPath = getIntent().getStringExtra("filename");

		Bitmap bitmap  = BitmapFactory.decodeFile(mPath);
		mImageView.setImageBitmap(bitmap);
    }
	
	public String getRealPathFromURI(Uri contentUri) {
        String [] proj 		= {MediaStore.Images.Media.DATA};
        Cursor cursor 		= managedQuery( contentUri, proj, null, null,null);
        
        if (cursor == null) return null;
        
        int column_index 	= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        
        cursor.moveToFirst();

        return cursor.getString(column_index);
	}
}