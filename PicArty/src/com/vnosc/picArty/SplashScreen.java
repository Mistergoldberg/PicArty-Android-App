package com.vnosc.picArty;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * The Class ASplashscreen.
 */
public class SplashScreen extends Activity {

	/** The is actived. */
	private boolean isActived = true;

	private boolean isExit = true;

	/** The splash thread. */
	private Thread splashThread;

	/**
	 * progress bar
	 * */

	/*********************************************************************************
	 * Override methods
	 *********************************************************************************/

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		startSplash();
	}

	/**
	 * Start splash.
	 */
	private void startSplash() {
		isExit = false;
		splashThread = new Thread() {
			@Override
			public void run() {
				try {
					int waited = 0;
					while (isActived && (waited < 10)) {
						sleep(100);
						if (isActived) {
							waited += 1;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					if (!isExit) {
						Intent mainIntent = new Intent(SplashScreen.this,
								CategoryActivity.class);
						SplashScreen.this.startActivity(mainIntent);
					}
					SplashScreen.this.finish();
					finish();
				}
			}
		};
		splashThread.start();
	}

}