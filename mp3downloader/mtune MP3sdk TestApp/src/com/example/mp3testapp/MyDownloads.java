package com.example.mp3testapp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chiragapps.mp3downloader.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ytsdk.mp3lib.model.Tracks;

public class MyDownloads extends Activity {

	private ScanSdReceiver scanSdReceiver = null;
	private AlertDialog ad = null;
	private AlertDialog.Builder builder = null;
	private ListView mListView;
	LinearLayout progressView;
	private int position;
	private DownloadedListAdapter adapter;
	private ProgressBar progressBar;
	private TextView progressText;

	List<Tracks> downloadedList = new ArrayList<Tracks>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_downloaded_items);

		scanSdCard();

		mListView = (ListView) findViewById(R.id.list);
		progressView = (LinearLayout) findViewById(R.id.loadingPanel);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressText = (TextView) findViewById(R.id.progressText);

		adapter = new DownloadedListAdapter(this, downloadedList, position,
				R.layout.download_item_row);

		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new ListItemClickListener());

		new ScanSdCardAndPrepareList().execute();
		creatAdmob();

	}

	class ListItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long id) {

			// GlobalAppData.numItemClicked++;

			Tracks item = downloadedList.get(position);
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			File file = new File(item.getPath());
			intent.setDataAndType(Uri.fromFile(file), "audio/*");
			startActivity(intent);

		}
	}

	private void creatAdmob() {
		AdRequest adRequest = new AdRequest.Builder().build();
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(adRequest);
	}

	private void scanSdCard() {
		Uri myUri = Uri.parse("file://"
				+ Environment.getExternalStorageDirectory().getAbsolutePath());

		IntentFilter intentfilter = new IntentFilter(
				Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentfilter.addDataScheme("file");
		scanSdReceiver = new ScanSdReceiver();
		this.registerReceiver(scanSdReceiver, intentfilter);
		this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				myUri));
	}

	private List<Tracks> getCurserToObj(Cursor cursor) {
		List<Tracks> trackInfo = new ArrayList<Tracks>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Tracks tmp = new Tracks();
			tmp.setName(cursor.getString(0));
			tmp.setDuration(toTime(cursor.getInt(1)));
			tmp.setId(cursor.getInt(3));
			// tmp.setName(cursor.getString(4));
			tmp.setPath(cursor.getString(5));
			trackInfo.add(tmp);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return trackInfo;
	}

	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	private class ScanSdCardAndPrepareList extends
			AsyncTask<String, Void, List<Tracks>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}

		@Override
		protected List<Tracks> doInBackground(String... params) {

			// Uri myUri = Uri.parse(Environment.getExternalStorageDirectory());
			// Uri myUri = Uri.parse("file://"
			// + Environment.getExternalStorageDirectory()
			// .getAbsolutePath());
			Cursor c = MyDownloads.this.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Audio.Media.TITLE,
							MediaStore.Audio.Media.DURATION,
							MediaStore.Audio.Media.ARTIST,
							MediaStore.Audio.Media._ID,
							MediaStore.Audio.Media.DISPLAY_NAME,
							MediaStore.Audio.Media.DATA }, null, null, null);
			if (c == null || c.getCount() == 0) {
				progressBar.setVisibility(View.GONE);
				progressText.setText("No Items found.");
			}
			return getCurserToObj(c);

		}

		@Override
		protected void onPostExecute(List<Tracks> result) {
			super.onPostExecute(result);
			if (result != null && !result.isEmpty()) {
				downloadedList.clear();
				downloadedList.addAll(result);
				adapter.notifyDataSetChanged();
				progressView.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);
			} else {
				progressBar.setVisibility(View.GONE);
				progressText.setText("No Items found.");
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// unregisterReceiver(scanSdReceiver);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// scanSdCard();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(scanSdReceiver);
	}

}
