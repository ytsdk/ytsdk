package com.yt.common.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
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

import com.video.downloader99.R;

public class Utils {
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";

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

		String marketLink = "market://details?id=" + contex.getPackageName();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(marketLink));
		contex.startActivity(intent);
	}

	public static void shareApp(Context contex) {

		String marketWebLink = "https://play.google.com/store/apps/details?id="
				+ contex.getPackageName();
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

	public static String doHttpWithGZIP(String url) {
		String result = null;
		try {

			final HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);

			DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);

			httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
				public void process(HttpRequest request, HttpContext context) {
					// Add header to accept gzip content
					if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
						request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
					}
				}
			});

			httpclient.addResponseInterceptor(new HttpResponseInterceptor() {
				public void process(HttpResponse response, HttpContext context) {
					// Inflate any responses compressed with gzip
					final HttpEntity entity = response.getEntity();
					final Header encoding = entity.getContentEncoding();
					if (encoding != null) {
						for (HeaderElement element : encoding.getElements()) {
							if (element.getName().equalsIgnoreCase(
									ENCODING_GZIP)) {
								response.setEntity(new InflatingEntity(response
										.getEntity()));
								break;
							}
						}
					}
				}
			});

			HttpGet httpget = new HttpGet(url);

			// ResponseHandler<String> responseHandler = new
			// BasicResponseHandler();

			HttpEntity httpEntity = httpclient.execute(httpget).getEntity();

			if (httpEntity != null) {
				result = EntityUtils.toString(httpEntity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getEncodedJsonFile(String url) {
		String result = null;
		try {

			final HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 8000);

			DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);

			HttpGet httpget = new HttpGet(url);

			HttpEntity httpEntity = httpclient.execute(httpget).getEntity();

			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
					httpEntity.getContent()));
			ZipEntry ze = zis.getNextEntry();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (ze != null) {
				byte[] buffer = new byte[4 * 1024];
				int len;
				while ((len = zis.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
			}
			result = new String(out.toByteArray(), "UTF-8");
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

	public static ArrayList<String> getlatestYears() {
		ArrayList<String> values = new ArrayList<String>();
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		values.add(year + "");
		if (month < 6) {
			values.add((year - 1) + "");
		}
		return values;
	}

	public static int getCurrentDay() {
		Calendar cal = Calendar.getInstance();
		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		return dayOfYear;
	}
	
	

}
