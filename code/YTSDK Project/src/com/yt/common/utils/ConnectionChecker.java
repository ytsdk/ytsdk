package com.yt.common.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionChecker {

	private ConnectivityManager cm;
	private Context context;
	private Activity activity;

	/**
	 * Constructor for the ConnectionChecker class
	 * 
	 * @param context
	 *            The context in which the class was defined
	 * @param cm
	 *            A connection manager defined in the calling class
	 * @param activity
	 *            The activity of the calling class
	 */
	public ConnectionChecker(Context context, ConnectivityManager cm,
			Activity activity) {
		this.cm = cm;
		this.context = context;
		this.activity = activity;
	}

	/**
	 * This method returns whether the user's internet connection is functioning
	 * 
	 * @param context
	 *            The context with which to do the check
	 * 
	 * @return True if the internet connection is functional
	 */
	public boolean isOnline() {
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnected());
	}

	/**
	 * Checks for an Internet connection. If there is no connection, or we are
	 * unable to retrieve information about our connection, display a message
	 * alerting the user about lack of connection.
	 * 
	 * @param context
	 *            The context with which to do the check
	 * 
	 * @return True if the internet connection is functional
	 */
	public void connectionError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder
				.setMessage(
						"Error: You must enable your data connection (Wifi or 3G) to use this app")

				// Force the user to exit the app if there is no connectivity
				.setNeutralButton("Exit",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								activity.finish();
							}
						});

		AlertDialog alert = builder.create();
		alert.show();
	}
}
