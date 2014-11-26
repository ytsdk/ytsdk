package com.yt.common.utils;

public class MyLog {

	// All App Constants will be defined here
	private final static String LOG_TAG = "TV";

	// SHould be set to false, for Testing use it as true
	public final static boolean enableLog = true;

	// SHould be set to false, for Testing use it as true
	public final static boolean disableConnectionCheckForDebug = false;

	// SHould be set to false, for screenShots, set to true
	public final static boolean disableBannerAds = false;

	public static void Log(String msg) {
		if (enableLog) {
			android.util.Log.i(LOG_TAG, msg);
		}
	}
}