package com.yt.activities;

import java.util.Random;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.video.downloader99.R;
import com.yt.activities.adapters.DownloadedListAdapter;
import com.yt.application.GlobalAppData;
import com.yt.common.utils.ShowFullScreenAdListener;
import com.yt.common.utils.Utils;
import com.yt.fragments.DownloadListFragment;
import com.yt.fragments.SearchListFragment;
import com.yt.lib.src.TabPageIndicator;

public class HomePageActivity extends FragmentActivity implements OnClickListener, ShowFullScreenAdListener {

	private static final String[] CONTENT = new String[] { "Search", "Downloads" };

	final CharSequence[] moreOptions = { "Rate", "Share", "Exit" };

	ImageButton search, moreButton;
	Dialog moreDialog;
	ImageView dummyImageViewToCloseMore;
	Button share, exit, about, rate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.layout_home);

		if (GlobalAppData.loggingOut) {
			GlobalAppData.loggingOut = false;
			GlobalAppData.numItemClicked = 0;
		}

		FragmentPagerAdapter adapter = new GoogleMusicAdapter(getSupportFragmentManager());

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);

		indicator.setViewPager(pager);

		// // Get the search bar and more image buttons and set the on
		// clicklistner
		search = (ImageButton) findViewById(R.id.search);
		search.setVisibility(View.GONE);
		search.setOnClickListener(this);
		moreButton = (ImageButton) findViewById(R.id.more);
		moreButton.setVisibility(View.VISIBLE);
		moreButton.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (GlobalAppData.loggingOut) {
			exitApp();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class GoogleMusicAdapter extends FragmentPagerAdapter {
		public GoogleMusicAdapter(FragmentManager fm) {
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

		moreDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);

		// Setting dialogview
		Window window = moreDialog.getWindow();
		window.setGravity(Gravity.CENTER);

		moreDialog.setTitle(null);
		moreDialog.setContentView(R.layout.layout_more_items);

		moreDialog.setCancelable(true);
		moreDialog.setCanceledOnTouchOutside(true);

		dummyImageViewToCloseMore = (ImageView) moreDialog.findViewById(R.id.cancelOutside);
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

		quitDialog.setPositiveButton("OK, Quit!", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				exitApp();
			}

		});

		quitDialog.setNegativeButton("Rate US", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// quitDialog.dismiss();
				Utils.rateApp(HomePageActivity.this);
			}
		});

		AlertDialog dailog = quitDialog.create();
		dailog.show();
	}

	@Override
	public void showFullScreenAd() {
		// TODO Auto-generated method stub

	}

}
