package com.vnosc.picArty.libs.tumblr;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import com.vnosc.picArty.R;

import roboguice.activity.RoboActivity;

public class AccountActivity extends RoboActivity {
    @Inject private TumblrApi api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accountview);

		setupControls();

		loadUserNameAndPassword();
	}

	private void loadUserNameAndPassword() {
		EditText username = (EditText) findViewById(R.id.inputUsername);
		username.setText(api.getUserName());
		EditText password = (EditText) findViewById(R.id.inputPassword);
		password.setText(api.getPassword());
	}

	private void setupControls() {
		setupOkButton();
		setupCancelButton();
	}

	private void setupOkButton() {
		Button btnOk = (Button) findViewById(R.id.settingsBtnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				okButtonClick();
			}
		});
	}

	private void okButtonClick() {
		final ProgressDialog pd = ProgressDialog.show(this, "Authenticating",
				"Validating email/password with tumblr", true, false);
		final Handler mHandler = new Handler();
		final Runnable mUpdateResults = new Runnable() {
		    public void run() {
		    	checkAuthentication();
				pd.dismiss();
		    }
		};
		new Thread() {
		    public void run() {
		        mHandler.post(mUpdateResults);
		    }
		}.start();
		
	/*	new Thread(new Runnable() {
			public void run() {
				checkAuthentication();
				pd.dismiss();
			}
		}).start();*/
	}

	private void returnToMainActivity(int result) {
		setResult(result);
		finish();
	}

	private void setupCancelButton() {
		Button btnCancel = (Button) findViewById(R.id.settingsBtnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				returnToMainActivity(RESULT_CANCELED);
			}
		});
	}

	private void setSetting(String name, String value) {
		SharedPreferences settings = getSharedPreferences("tumblr", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(name, value);
		editor.commit();
	}

	private void saveSettings() {
		EditText text = (EditText) findViewById(R.id.inputUsername);
		setSetting("USERNAME", text.getEditableText().toString());

		text = (EditText) findViewById(R.id.inputPassword);
		setSetting("PASSWORD", text.getEditableText().toString());

	}

	private Boolean IsAuthenticationCorrect() {
		EditText text = (EditText) findViewById(R.id.inputUsername);
		String username = text.getEditableText().toString();

		text = (EditText) findViewById(R.id.inputPassword);
		String password = text.getEditableText().toString();

		return api.validateUsernameAndPassword(username, password);
	}

	private void checkAuthentication() {
		if (IsAuthenticationCorrect()) {
			saveSettings();
			returnToMainActivity(RESULT_OK);
		} else {
			Toast.makeText(this, "email and/or password incorrect",
					Toast.LENGTH_LONG).show();
		}
	}
}
