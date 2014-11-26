package com.mp3sdk.activities;

import java.util.HashMap;

import android.app.Activity;

import com.ytsdk.mp3lib.MP3SDK;

public class MP3SDKUtils {

	private static HashMap<Integer, MP3SDK> mapMp3SDKs = new HashMap<Integer, MP3SDK>();

	public static MP3SDK getMP3SDK(Activity activity, int sdkType) {
		MP3SDK mp3SDK = mapMp3SDKs.get(sdkType);
		try {
			if (mp3SDK == null) {
				mp3SDK = MP3SDK.getInstance(activity, sdkType);
				mapMp3SDKs.put(sdkType, mp3SDK);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mp3SDK;
	}
}