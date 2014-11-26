package com.mp3sdk.common.utils;

import java.io.File;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.mp3sdk.common.constants.Constants;
import com.startapp.android.publish.StartAppAd;
import com.mtunemp3sdk.mp3downloader.R;

public class Utils {

	private static InterstitialAd interstitial;
	private static StartAppAd startAppAd;

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isNetworkAvailable(Context ctx) {
		int networkStatePermission = ctx
				.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);

		if (networkStatePermission == PackageManager.PERMISSION_GRANTED) {

			ConnectivityManager mConnectivity = (ConnectivityManager) ctx
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			// Skip if no connection, or background data disabled
			NetworkInfo info = mConnectivity.getActiveNetworkInfo();
			if (info == null) {
				return false;
			}
			// Only update if WiFi
			int netType = info.getType();
			// int netSubtype = info.getSubtype();
			if ((netType == ConnectivityManager.TYPE_WIFI)
					|| (netType == ConnectivityManager.TYPE_MOBILE)) {
				return info.isConnected();
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static void rateApp(Context contex) {

		String marketLink = "http://anroidstore.com";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(marketLink));
		contex.startActivity(intent);
	}

	public static void shareApp(Context contex) {

		String marketWebLink = "http://anroidstore.com";
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				contex.getString(R.string.app_name));
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				contex.getString(R.string.shareText) + " --> Download the app "
						+ marketWebLink);
		contex.startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}

	public static void showConnectivityErrorDialog(final Activity activity) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCancelable(true);
		builder.setIcon(null);
		builder.setTitle(null);
		builder.setMessage("YOU MUST ENABLE YOUR DATA CONNECTION (WIFI or 3G), TO ACCESS TV");
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						Intent settingPage = new Intent(
								android.provider.Settings.ACTION_SETTINGS);
						activity.startActivityForResult(settingPage, 0);
						dialog.dismiss();
						activity.finish();

					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						activity.finish();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static String doHttp(String url) {
		String result = null;
		try {

			final HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);

			DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);

			HttpGet httpget = new HttpGet(url);

			HttpEntity httpEntity = httpclient.execute(httpget).getEntity();

			if (httpEntity != null) {
				result = EntityUtils.toString(httpEntity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean isDBExists(Context context) {
		boolean result = false;
		String DB_PATH = "/data/data/" + context.getPackageName()
				+ "/databases/" + getDBAppName(context);
		try {
			File dbFile = new File(DB_PATH);
			result = dbFile.exists();
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public static String getDBAppName(Context context) {
		String appName = null;
		try {
			appName = context.getResources().getString(R.string.app_name);
			appName = appName.replaceAll("\\s+", "").toLowerCase() + ".db";
		} catch (Exception e) {

		}
		return appName;
	}

	public static int getCurrentDay() {
		Calendar cal = Calendar.getInstance();
		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		return dayOfYear;
	}

	public static void loadFullScreenAd(Activity activity) {
		if (startAppAd == null) {
			startAppAd = new StartAppAd(activity);
		}
		if (Constants.ADMOB_FULLSCREEN_ADS) {
			// Create the interstitial.
			AdRequest adRequest = new AdRequest.Builder().build();
			interstitial = new InterstitialAd(activity);
			interstitial.setAdUnitId(activity
					.getString(R.string.admob_fullscreen_id));
			// Begin loading your interstitial.
			interstitial.loadAd(adRequest);
			startAppAd.loadAd();
		} else {
			startAppAd.loadAd();
		}
	}

	public static void showFullScreenAd(Activity context) {
		if (Constants.ADMOB_FULLSCREEN_ADS) {
			if (interstitial.isLoaded()) {
				interstitial.show();
				loadFullScreenAd(context);
			} else if (startAppAd.isReady()) {
				startAppAd.showAd();
				startAppAd.loadAd();
			}
		} else {
			if (startAppAd.isReady()) {
				startAppAd.showAd();
				startAppAd.loadAd();
			}
		}
	}

}
