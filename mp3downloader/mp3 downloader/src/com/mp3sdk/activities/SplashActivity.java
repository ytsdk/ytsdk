package com.mp3sdk.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.mp3sdk.common.utils.Utils;
import com.startapp.android.publish.StartAppSDK;
import com.mtunemp3sdk.mp3downloader.R;
import com.ytsdk.mp3lib.MP3SDK;

public class SplashActivity extends Activity {

	private boolean networkCheck = false;
	private static SplashTimer timer;
	private final static int TIMER_INTERVAL = 2000; // 2 sec

	private boolean activityStarted;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.layout_splash);
		
		networkCheck = Utils.isNetworkAvailable(this);

		// Initilize StartApp
		StartAppSDK.init(this, getString(R.string.startapp_developer_id),
				getString(R.string.startapp_app_id), true);

		if (!networkCheck) {
			Utils.showConnectivityErrorDialog(this);
		} else {
			// startHomePageActivity();
			timer = new SplashTimer();
			timer.sendEmptyMessageDelayed(1, TIMER_INTERVAL);
		}
		Utils.loadFullScreenAd(this);
	}

	private void startHomePageActivity() {

		if (activityStarted) {
			return;
		}
		activityStarted = true;

		SplashActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				startActivity(new Intent(SplashActivity.this,
						HomePageActivity.class));
				finish();
			}
		});

	}

	final class SplashTimer extends Handler {
		@Override
		public void handleMessage(Message msg) {
			post(new Runnable() {

				public void run() {
					timer = null;
					startHomePageActivity();
				}
			});
		}
	}

}