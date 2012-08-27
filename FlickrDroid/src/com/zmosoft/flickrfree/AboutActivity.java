package com.zmosoft.flickrfree;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.about);

		((Button)findViewById(R.id.ButtonContact)).setOnClickListener(this);
		((Button)findViewById(R.id.ButtonUpgrade)).setOnClickListener(this);
		((Button)findViewById(R.id.ButtonUpgradeFree)).setOnClickListener(this);
		
		fillTextFields();
	}

	protected void fillTextFields() {
		String text = getString(R.string.txtupgradenotice);
		
		((TextView)findViewById(R.id.NoticeText)).setText(text);
		
		text = getString(R.string.txtversion);
		
		text = text.replace("{%VER}", GlobalResources.getVersionName(this));
		((TextView)findViewById(R.id.VersionText)).setText(text);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ButtonContact:
			try {
				Intent i = new Intent(android.content.Intent.ACTION_SEND)
								.setType("plain/text")
								.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{GlobalResources.FEEDBACK_EMAIL})
								.putExtra(android.content.Intent.EXTRA_SUBJECT, "FlickrFree Feedback");
				startActivity(Intent.createChooser(i, "Send Feedback..."));
			} catch (ActivityNotFoundException e) {
			}
			break;
		case R.id.ButtonUpgradeFree:
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalResources.UPGRADE_MARKET_FREE_URI)));
			} catch (ActivityNotFoundException e) {
			}
			break;
		case R.id.ButtonUpgrade:
			try {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalResources.UPGRADE_MARKET_URI)));
			} catch (ActivityNotFoundException e) {
			}
			break;
		}
	}
}
