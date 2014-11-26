package com.yt.application;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.ytsdk.testapp.mbc.R;

public class GlobalAppData extends Application {

	public static boolean loggingOut = false;
	public static int numItemClicked = 0;
	public static String APPLICATION_ID = "52e24cbe511e37c2380000b6";
	public static final String INMOBI_AD_ID = "4ff25222e9884095adbf72989b364728";
	public static boolean adFlip = true;

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Create global configuration and initialize ImageLoader
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
				.resetViewBeforeLoading(false)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.EXACTLY)
				.showImageForEmptyUri(R.drawable.ic_launcher).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).defaultDisplayImageOptions(
				defaultOptions).build();

		ImageLoader.getInstance().init(config);

	}


	public int getAppVersion() {
		int version = 0;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = pInfo.versionCode;
		} catch (Exception e) {

		}
		return version;
	}

}