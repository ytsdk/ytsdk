package com.example.mp3testapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScanSdReceiver extends BroadcastReceiver {

	private AlertDialog.Builder builder = null;
	private AlertDialog ad = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
			// System.out.println("scanning sdcard...");
			// builder = new AlertDialog.Builder(context);
			// builder.setMessage("ÕýÔÚÉ¨Ãè´æ´¢¿¨...");
			// ad = builder.create();
			// ad.show();

		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
			// System.out.println("scan finished!");
			// ad.cancel();
		}

	}

}
