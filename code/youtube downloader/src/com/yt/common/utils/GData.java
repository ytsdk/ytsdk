package com.yt.common.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import com.yt.common.constants.XmlKeys;
import com.yt.item.VideoItem;

public class GData {

	private static String URL_TV_SHOW = "/shows?";
	private static String URL_USER_PLAYLIST = "playlists/";
	private static String URL_USER_UPLOADS = "/uploads?";
	private static String URL_MOVIE_QUERRY = "charts/movies/";
	private static String URL_MOVIE_TRAILERS = "charts/trailers";

	private static ArrayList<VideoItem> mVideoItems;
	
	private static Boolean enableLog = false;

	synchronized public static ArrayList<VideoItem> getGData(String urlStr,
			Context context, String cacheFolderPath) {
		
		if(enableLog){
			MyLog.Log("Requesting URL : " + urlStr);
		}
		
		mVideoItems = getVdieoListIfCached(urlStr, context, cacheFolderPath);
		if(mVideoItems != null && mVideoItems.size() > 2){
			if(enableLog){
				MyLog.Log("Response Got from Cache. ");
			}
			
			return mVideoItems;
		}
		
		mVideoItems = new ArrayList<VideoItem>();
		
		try {
			HttpClient lClient = new DefaultHttpClient();
			HttpGet lGetMethod = new HttpGet(urlStr);
			
			AndroidHttpClient.modifyRequestToAcceptGzipResponse(lGetMethod);
			
			HttpResponse lResp = null;

			lResp = lClient.execute(lGetMethod);
			String lInfoStr = null;
			
			HttpEntity entity = lResp.getEntity();
			InputStream  in = null;
			if (entity != null) {
				in = AndroidHttpClient.getUngzippedContent(entity);
			}
			
			//Note, InputStream cant be read twice, so save to file first, if mentioned
			if (cacheFolderPath != null) {
				// First Save the response into a file
				writeResponseToFile(urlStr, context, cacheFolderPath,
						in);
				
				// This Func will read the xml response from file and parse it
				getVdieoListIfCached(urlStr, context, cacheFolderPath);
				
			}else{
				// Parse directly the resonse
				parseFromInputStream(urlStr, in);
			}

			
		} catch (Exception e) {
			e.printStackTrace();
			if(enableLog)
				MyLog.Log("Exception @getData: " + e.toString());
		}

		return mVideoItems;

	}

	synchronized public static ArrayList<VideoItem> getVdieoListIfCached(String url,
			Context context, String cachedFolder) {
		
		if(cachedFolder == null ){
			return null;
		}
		
		mVideoItems = new ArrayList<VideoItem>();

		File cacheDir;

		// Find the dir to save/cache http response
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					cachedFolder);
		else
			cacheDir = context.getCacheDir();

		if (!cacheDir.exists())
			cacheDir.mkdirs();

		// identify url by hashcode. Not a perfect solution, good for the demo.
		// String filename = String.valueOf(url.hashCode());

		// Another possible solution (thanks to grantland)
		String filename = URLEncoder.encode(url) + ".txt";
		File f = new File(cacheDir, filename);
		if (f.exists()) {
			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(f));

				parseFromInputStream(url, in);

				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				if(enableLog)
					MyLog.Log("Exception @getVdieoListIfCached: " + e.toString());
			}

		}

		return mVideoItems;
	}
	
	private static void writeResponseToFile(String url, Context context,
			String cacheFolder, InputStream in) {
		File cacheDir;

		// Find the dir to save/cache http response
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					cacheFolder);
		else
			cacheDir = context.getCacheDir();

		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}

		// identify url by hashcode. Not a perfect solution, good for the demo.
		// String filename = String.valueOf(url.hashCode());

		// Another possible solution (thanks to grantland)
		String filename = URLEncoder.encode(url) + ".txt";
		File file = new File(cacheDir, filename);

		try {
			// write the inputStream to a FileOutputStream
			OutputStream out = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

			out.flush();
			out.close();

		} catch (Exception e) {
			if(enableLog)
				MyLog.Log("Exception @writeResponseToFile: " + e.toString());
		}

	}

	private static void parseFromInputStream(String urlStr, InputStream in) {

		if (urlStr.indexOf(URL_TV_SHOW) > 0) {
			parseTVShows(in);
		} else if (urlStr.indexOf(URL_USER_PLAYLIST) > 0) {
			// IF video data should be diff, write a diff parse Func
			parseForVideosDetails(in);
		} else if (urlStr.indexOf(URL_USER_UPLOADS) > 0) {
			// IF video data should be diff, write a diff parse Func
			parseForVideosDetails(in);
		} else if (urlStr.indexOf(URL_MOVIE_QUERRY) > 0) {
			// IF video data should be diff, write a diff parse Func
			parseForVideosDetails(in);
		} else if (urlStr.indexOf(URL_MOVIE_TRAILERS) > 0) {
			// IF video data should be diff, write a diff parse Func
			parseForVideosDetails(in);
		}else{
			parseForVideosDetails(in);
		}
	}

	private static void parseTVShows(InputStream in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document dom = db.parse(in);
			Element docEle = dom.getDocumentElement();

			NodeList nl = docEle.getElementsByTagName("entry");
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element entry = (Element) nl.item(i);
					VideoItem item = null;

					try {
						Element seasonEle = (Element) entry
								.getElementsByTagName(XmlKeys.YT_countHint)
								.item(0);
						int countHint = 0;
						String str = seasonEle.getFirstChild().getNodeValue();
						countHint = Integer.parseInt(str);

						if (countHint == 0) {
							continue;
						}

						item = new VideoItem();

						// Get Video Title
						item.setTitle(getTagByName(XmlKeys.Title, entry));

						// Get Video Published Date
						item.setPubDate(getTagByName(XmlKeys.Published, entry));

						// Get Video Updated Date
						item.setUpdated(getTagByName(XmlKeys.Updated, entry));

						// Get Video Summary
						item.setDesc(getTagByName(XmlKeys.Summary, entry));

						mVideoItems.add(item);

					} catch (Exception e) {
						e.printStackTrace();
						if(enableLog)
							MyLog.Log("Exception @parseTVShows Inside for Loop: "
									+ e.toString());
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(enableLog)
				MyLog.Log("Exception @parseTVShows: " + e.toString());
		}

	}

	private static void parseForVideosDetails(InputStream in) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document dom = db.parse(in);
			Element docEle = dom.getDocumentElement();

			NodeList nl = docEle.getElementsByTagName("entry");
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element entry = (Element) nl.item(i);
					VideoItem item = null;

					try {

						item = new VideoItem();

						// Get Video Title
						item.setTitle(getTagByName(XmlKeys.Title, entry));

						// Get Video Published Date
						item.setPubDate(getTagByName(XmlKeys.Published, entry));

						// Get Video Id
						String videoId = getTagByName(XmlKeys.Yt_videoid, entry);
						
						if(videoId == null){
							Element id = (Element) entry.getElementsByTagName("id")
									.item(0);
							String idValue = id.getFirstChild().getNodeValue();
							if (idValue != null){
								int index = idValue.lastIndexOf("/");
								videoId = idValue.substring(index + 1);
							}
			
						}
						item.setVideoId(videoId);

						// Get Video duration
						item.setDuration(getAttributeByName(
								XmlKeys.Yt_duration, XmlKeys.Seconds, entry));

						// Get Video Updated Date
						item.setUpdated(getTagByName(XmlKeys.Yt_uploaded, entry));

						// Get Video Description
						item.setDesc(getTagByName(XmlKeys.Media_description,
								entry));

						// Get Video Rating
						item.setRating(getAttributeByName(XmlKeys.Gd_rating,
								XmlKeys.Average, entry));

						// Get Video ViewCount
						item.setViewCount(getAttributeByName(
								XmlKeys.Yt_statistics, XmlKeys.ViewCount, entry));

						// Get Video Director
						item.setDirector(getMediaCredits(XmlKeys.Media_credit,
								XmlKeys.Role, XmlKeys.DDirector, entry));

						// Get Video Cast
						item.setActors(getMediaCredits(XmlKeys.Media_credit,
								XmlKeys.Role, XmlKeys.AActor, entry));

						// Get RTSP Url Link
						item.setRtspUrl(getRtspUrl(XmlKeys.Media_content,
								XmlKeys.Yt_format, XmlKeys.RTSP_FORMAT,
								XmlKeys.Url, entry));
						
//						//Get Video Genre
//						item.setGenreId(getTagByName(XmlKeys.Media_category,
//								entry));

						mVideoItems.add(item);

					} catch (Exception e) {
						e.printStackTrace();
						if(enableLog)
							MyLog.Log("Exception @parseForVideosDetails Inside for Loop: "
									+ e.toString());
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(enableLog)
				MyLog.Log("Exception @parseForVideosDetails: " + e.toString());
		}

	}

	private static String getTagByName(String tag, Element entry) {
		try {
			Element element = (Element) entry.getElementsByTagName(tag).item(0);

			String retValue = element.getFirstChild().getNodeValue();

			// MyLog.Log("getTagByName on Tag: " + tag + " & Value: " + retValue);

			return retValue;
		} catch (Exception e) {
			if(enableLog)
				MyLog.Log("Exception @getTagByName on Tag: " + tag + "\n"
						+ e.toString());
		}

		return null;
	}

	private static String getAttributeByName(String tag, String attritube,
			Element entry) {
		try {
			NodeList node = entry.getElementsByTagName(tag);
			Element element = (Element) node.item(0);

			String retValue = element.getAttribute(attritube);

			// MyLog.Log("getTagByName on Tag: " + tag + " & Attribute: " + attritube + " & Value: " + retValue);

			return retValue;
		} catch (Exception e) {
			if(enableLog)
				MyLog.Log("Exception @getAttributeByName on Tag: " + tag
						+ " & Attribute: " + attritube + "\n" + e.toString());
		}

		return null;
	}

	private static String getMediaCredits(String tag, String attKey,
			String attValue, Element entry) {
		try {
			NodeList node = entry.getElementsByTagName(tag);

			String retValue = null;
			for (int i = 0; i < node.getLength(); i++) {
				Element element = (Element) node.item(i);
				String attributeValue = element.getAttribute(attKey);
				if (attributeValue.compareTo(attValue) == 0) {
					if (retValue != null) {
						retValue += ", ";
					} else {
						retValue = "";
					}
					retValue += "" + element.getFirstChild().getNodeValue();
				}
			}

			if (retValue.length() == 0) {
				retValue = null;
			}

			// MyLog.Log("getTagByName on Tag: " + tag + " & AttributeValue: " + attValue + " & Value: " + retValue);

			return retValue;
		} catch (Exception e) {
			if(enableLog)
				MyLog.Log("Exception @getMediaCredits on Tag: " + tag
						+ " & AttributeValue: " + attValue + "\n" + e.toString());
		}

		return null;
	}

	private static String getRtspUrl(String tag, String attKey,
			String attValue, String urlTag, Element entry) {
		try {
			NodeList node = entry.getElementsByTagName(tag);

			String retValue = null;
			for (int i = 0; i < node.getLength(); i++) {
				Element element = (Element) node.item(i);
				String attributeValue = element.getAttribute(attKey);
				if (attributeValue.compareTo(attValue) == 0) {
					retValue = element.getAttribute(urlTag);
				}
			}

			// MyLog.Log("getRtspUrl on Tag: " + tag + " & urlTag: " + urlTag + " & Value: " + retValue);

			return retValue;
		} catch (Exception e) {
			if(enableLog)
				MyLog.Log("Exception @getRtspUrl on Tag: " + tag + " & urlTag: "
						+ urlTag + "\n" + e.toString());
		}

		return null;
	}
	
	
	synchronized public static void getVimeoVideos(String urlStr,
			Context context, String cacheFolderPath) {
		
		if(enableLog){
			MyLog.Log("Requesting URL : " + urlStr);
		}

		
		try {
			HttpClient lClient = new DefaultHttpClient();
			HttpGet lGetMethod = new HttpGet(urlStr);
			
			AndroidHttpClient.modifyRequestToAcceptGzipResponse(lGetMethod);
			
			HttpResponse lResp = null;

			lResp = lClient.execute(lGetMethod);
			String lInfoStr = null;
			
			HttpEntity entity = lResp.getEntity();
			InputStream  in = null;
			if (entity != null) {
				in = AndroidHttpClient.getUngzippedContent(entity);
			}
			
			//TODO::Write Parse Logic

			
		} catch (Exception e) {
			e.printStackTrace();
			if(enableLog)
				MyLog.Log("Exception @getData: " + e.toString());
		}
		

	}

}
