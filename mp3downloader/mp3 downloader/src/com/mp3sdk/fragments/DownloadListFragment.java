package com.mp3sdk.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mp3sdk.activities.MP3SDKUtils;
import com.mp3sdk.activities.adapters.DownloadedListAdapter;
import com.mp3sdk.application.GlobalAppData;
import com.mp3sdk.common.constants.Constants;
import com.mp3sdk.common.utils.ConnectionChecker;
import com.mp3sdk.common.utils.MyLog;
import com.mtunemp3sdk.mp3downloader.R;
import com.ytsdk.mp3lib.MP3SDK;
import com.ytsdk.mp3lib.model.Tracks;

public class DownloadListFragment extends Fragment {

	private static final String KEY_CONTENT = "ItemListFragment:Items";
	private static final String KEY_FILE_NAME = "ItemListFragment:fileName";

	private File currentDirectory = new File("/");

	private ListView mListView;
	private Button mRefreshButton;
	LinearLayout progressBar;
	DownloadedListAdapter mListAdapter;
	List<Tracks> downloadedList = new ArrayList<Tracks>();

	private static DownloadListFragment thisPointer;
	private int position;

	public static DownloadListFragment getInstance(int position) {

		// if (gridFragent == null) {
		thisPointer = new DownloadListFragment();
		// }
		Bundle args = new Bundle();
		args.putInt("position", position);
		thisPointer.setArguments(args);
		return thisPointer;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		restoreData(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null) {
			this.position = bundle.getInt("position");
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_downloaded_video,
				container, false);

		progressBar = (LinearLayout) view.findViewById(R.id.loadingPanel);
		mListView = (ListView) view.findViewById(R.id.list);

		mRefreshButton = (Button) view.findViewById(R.id.refreshButton);
		mRefreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refreshList();
			}
		});

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		new GetVideoListFromYouTube().execute();

	}

	public void onListItemClick(ListView g, View v, int position, long id) {
		GlobalAppData.numItemClicked++;

		Tracks item = downloadedList.get(position);
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file = new File(item.getPath());
		intent.setDataAndType(Uri.fromFile(file), "audio/*");
		startActivity(intent);

	}

	private void restoreData(Bundle savedInstanceState) {
		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			downloadedList = (ArrayList<Tracks>) savedInstanceState
					.get(KEY_CONTENT);
			position = savedInstanceState.getInt(KEY_FILE_NAME);
		}
	}

	private void refreshList() {
		new GetVideoListFromYouTube().execute();
	}

	private class GetVideoListFromYouTube extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (downloadedList == null) {
				downloadedList = new ArrayList<Tracks>();
			} else {
				downloadedList.clear();
			}

			browseToRoot();

			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			// Cancel the Loading Dialog
			progressBar.setVisibility(View.GONE);
			addVideoList(downloadedList);

		}

	}

	/**
	 * This function browses to the root-directory of the file-system.
	 */
	private void browseToRoot() {
		File f = new File(Environment.getExternalStorageDirectory()
				+ "/"
				+ MP3SDKUtils.getMP3SDK(getActivity(), Constants.MP3SDK_ENGINE)
						.getDownloadFolderPath() + "/");
		System.out.println(f.getAbsolutePath());
		browseTo(f);
	}

	private void browseTo(final File aDirectory) {
		if (!aDirectory.exists()) {
			aDirectory.mkdirs();// Create Directory if doesnt Exists
		}

		if (aDirectory.isDirectory()) {
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
			return;
		}

	}

	private void fill(File[] files) {

		if (files == null || files.length == 0) {
			return;
		}

		// Add the "." and the ".." == 'Up one level'
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int currentPosOfFileName = this.currentDirectory.getAbsolutePath()
				.length() + 1;

		for (File file : files) {
			Tracks videoItem = new Tracks();
			int fileLength = file.getAbsolutePath().length();
			videoItem.setName(file.getAbsolutePath().substring(
					currentPosOfFileName, fileLength));

			videoItem.setPath(file.getPath());

			downloadedList.add(videoItem);
		}

	}

	private void addVideoList(List<Tracks> videoList) {

		if (videoList == null || videoList.size() == 0) {

			if (!progressBar.isShown()) {
				// Show Error Msg
				// mErrorTextView.setText("No Videos Found!");
				// mErrorTextView.setVisibility(View.VISIBLE);

			}
			return;
		}

		// mErrorTextView.setVisibility(View.GONE);

		downloadedList = videoList;
		mListAdapter = new DownloadedListAdapter(getActivity(), downloadedList,
				position, R.layout.list_item);
		if (mListView != null) {
			mListView.setAdapter(mListAdapter);
		}

		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view,
					int position, long id) {
				onListItemClick((ListView) parent, view, position, id);
			}
		});
		// }

		// This has to run only at fist time from next the on scroll down will
		// run
		if (!downloadedList.isEmpty()) {
			mListView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
		}

	}

	private boolean isConnectivityPresent() {
		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		ConnectionChecker connectionChecker = new ConnectionChecker(
				getActivity(), cm, getActivity());

		if (MyLog.disableConnectionCheckForDebug) {
			return true;
		}

		if (connectionChecker.isOnline()) {
			return true;
		} else {
			showConnectivityErrorDialog();
			return false;
		}
	}

	private void showConnectivityErrorDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setCancelable(true);
		builder.setIcon(null);
		builder.setTitle(null);
		builder.setMessage(getActivity().getString(R.string.enablewifiMsg));
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent settingPage = new Intent(
								android.provider.Settings.ACTION_SETTINGS);
						getActivity().startActivityForResult(settingPage, 0);
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

}