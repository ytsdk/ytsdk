package com.yt.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ironsource.mobilcore.MobileCore;
import com.startapp.android.publish.StartAppAd;
import com.ytsdk.testapp.stp.R;
import com.yt.application.GlobalAppData;
import com.yt.common.constants.Constants;
import com.yt.common.utils.Utils;
import com.yt.fragments.DownloadListFragment;
import com.yt.fragments.SearchListFragment;
import com.yt.lib.src.TabPageIndicator;

public class HomePageActivity extends FragmentActivity implements
		OnClickListener {

	private static final String[] CONTENT = new String[] { "Search",
			"Downloads" };

	final CharSequence[] moreOptions = { "Rate", "Share", "Exit" };

	ImageButton search, moreButton;
	Dialog moreDialog;
	ImageView dummyImageViewToCloseMore;
	Button share, exit, about, rate;

	private StartAppAd startapp = new StartAppAd(this);
	boolean exitAdShown = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.layout_home);

		if (GlobalAppData.loggingOut) {
			GlobalAppData.loggingOut = false;
			GlobalAppData.numItemClicked = 0;
		}

		FragmentPagerAdapter adapter = new TabAdapter(
				getSupportFragmentManager());

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);

		indicator.setViewPager(pager);

		// Get the search bar and more image buttons and set the on
		// clicklistner
		search = (ImageButton) findViewById(R.id.search);
		search.setVisibility(View.GONE);
		search.setOnClickListener(this);
		moreButton = (ImageButton) findViewById(R.id.more);
		moreButton.setVisibility(View.VISIBLE);
		moreButton.setOnClickListener(this);

		loadBannerAds();
		Utils.showFullScreenAd(this);
		// MobileCore.showStickee(this);
		MobileCore.loadAdUnit(MobileCore.AD_UNITS.STICKEEZ, MobileCore.AD_UNIT_TRIGGER.MAIN_MENU);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (startapp != null) {
			startapp.onResume();
		}

		if (GlobalAppData.loggingOut) {
			exitApp();
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (startapp != null) {
			startapp.onResume();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (exitAdShown) {
				exitApp();
				return true;
			} else {
				Toast.makeText(this, "Press Back to exit.", Toast.LENGTH_LONG)
						.show();
				Utils.showFullScreenAd(this);
				exitAdShown = true;
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	class TabAdapter extends FragmentPagerAdapter {
		public TabAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment result = null;
			switch (position) {
			case 0:
				result = SearchListFragment.getInstance(position);
				break;
			case 1:
				result = DownloadListFragment.getInstance(position);
				break;
			}
			return result;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return CONTENT[position % CONTENT.length].toUpperCase();
		}

		@Override
		public int getCount() {
			return CONTENT.length;
		}
	}

	public void onClick(View v) {
		if (moreDialog != null) {
			moreDialog.dismiss();
		}
		if (v.getId() == R.id.more) {
			showMoreDailog();
		} else if (v.getId() == R.id.cancelOutside) {
			if (moreDialog != null) {
				moreDialog.dismiss();
			}
		} else if (v.getId() == R.id.share) {
			Utils.shareApp(this);
		} else if (v.getId() == R.id.rate) {
			Utils.rateApp(this);
		} else if (v.getId() == R.id.exit) {
			exitApp();
		}
	}

	private void showMoreDailog() {

		moreDialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);

		// Setting dialogview
		Window window = moreDialog.getWindow();
		window.setGravity(Gravity.CENTER);

		moreDialog.setTitle(null);
		moreDialog.setContentView(R.layout.layout_more_items);

		moreDialog.setCancelable(true);
		moreDialog.setCanceledOnTouchOutside(true);

		dummyImageViewToCloseMore = (ImageView) moreDialog
				.findViewById(R.id.cancelOutside);
		dummyImageViewToCloseMore.setOnClickListener(this);

		rate = (Button) moreDialog.findViewById(R.id.rate);
		rate.setOnClickListener(this);
		share = (Button) moreDialog.findViewById(R.id.share);
		share.setOnClickListener(this);
		about = (Button) moreDialog.findViewById(R.id.about);
		about.setOnClickListener(this);
		exit = (Button) moreDialog.findViewById(R.id.exit);
		exit.setOnClickListener(this);
		moreDialog.show();

	}

	private void exitApp() {
		if (moreDialog != null) {
			moreDialog.dismiss();
		}
		this.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void openQuitDialog() {
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);

		quitDialog.setTitle(R.string.app_name);

		quitDialog.setPositiveButton("OK, Quit!",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						exitApp();
					}

				});

		quitDialog.setNegativeButton("Rate US",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						Utils.rateApp(HomePageActivity.this);
					}
				});

		AlertDialog dailog = quitDialog.create();
		dailog.show();
	}

	private void loadBannerAds() {
		AdView adView = (AdView) this.findViewById(R.id.adView);

		if (Constants.ADMOB_BANNER_ADS) {
			// Banner
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
			adView.setVisibility(View.VISIBLE);
		}
	}

}
