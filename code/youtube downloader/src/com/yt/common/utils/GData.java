package com.yt.common.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.yt.item.VideoItem;

public class GData {

	public static ArrayList<VideoItem> getGData(String url) {
		ArrayList<VideoItem> result = new ArrayList<VideoItem>();
		try {
			String jsonData = Utils.doZIPHttp(url);
			JSONObject jsonObj = new JSONObject(jsonData);
			JSONArray itemsArr = jsonObj.getJSONArray("items");
			for (int i = 0; i < itemsArr.length(); i++) {
				VideoItem vItem = new VideoItem();

				JSONObject item = itemsArr.getJSONObject(i);

				JSONObject id = item.getJSONObject("id");
				vItem.setVideoId(id.getString("videoId"));

				JSONObject snippet = item.getJSONObject("snippet");
				vItem.setTitle(snippet.getString("title"));

				vItem.setIconUrl(snippet.getJSONObject("thumbnails")
						.getJSONObject("medium").getString("url"));
				result.add(vItem);
			}
		} catch (Exception e) {

		}
		return result;
	}

}