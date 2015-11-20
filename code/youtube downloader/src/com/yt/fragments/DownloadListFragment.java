package com.yt.fragments;

import java.io.File;
import java.util.ArrayList;

import yt.sdk.jar.player.PlayerActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.ytsdk.testapp.stp.R;
import com.yt.activities.YTSDKUtils;
import com.yt.activities.adapters.DownloadedListAdapter;
import com.yt.application.GlobalAppData;
import com.yt.common.utils.ConnectionChecker;
import com.yt.common.utils.MyLog;
import com.yt.item.VideoItem;

public class DownloadListFragment extends Fragment {

	private static final String KEY_CONTENT = "ItemListFragment:Items";
	private static final String KEY_FILE_NAME = "ItemListFragment:fileName";

	private File currentDirectory = new File("/");
	// public static final String VIDEO_DIR_PATH = "/YT Videos/";

	private GridView mGridView;
	private Button mRefreshButton;
	LinearLayout progressBar;
	DownloadedListAdapter mListAdapter;
	ArrayList<VideoItem> downloadedList = new ArrayList<VideoItem>();

	private static DownloadListFragment thisPointer;
	private int position;

	private AutoCompleteTextView mEditText;
	private Button mSearchButton;

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
		mGridView = (GridView) view.findViewById(R.id.list);

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

	public void onListItemClick(GridView g, View v, int position, long id) {
		GlobalAppData.numItemClicked++;

		Intent intent = new Intent(getActivity(), PlayerActivity.class);
		intent.putExtra("path", downloadedList.get(position).getLocalPath());

		startActivityForResult(intent, 3);

	}

	private void restoreData(Bundle savedInstanceState) {
		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			downloadedList = (ArrayList<VideoItem>) savedInstanceState
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
			mGridView.setVisibility(View.GONE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (downloadedList == null) {
				downloadedList = new ArrayList<VideoItem>();
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
		File f = new File(Environment.getExternalStorageDirectory() + "/"
				+ YTSDKUtils.getYTSDK().getDownloadFolderPath() + "/");
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
			VideoItem videoItem = new VideoItem();
			int fileLength = file.getAbsolutePath().length();
			videoItem.setTitle(file.getAbsolutePath().substring(
					currentPosOfFileName, fileLength));

			videoItem.setLocalPath(file.getPath());

			downloadedList.add(videoItem);
		}

	}

	private void addVideoList(ArrayList<VideoItem> videoList) {

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
				position, R.layout.grid_item);
		if (mGridView != null) {
			mGridView.setAdapter(mListAdapter);
		}

		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view,
					int position, long id) {
				onListItemClick((GridView) parent, view, position, id);
			}
		});
		// }

		// This has to run only at fist time from next the on scroll down will
		// run
		if (!downloadedList.isEmpty()) {
			mGridView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
		}

	}

//	public String getRawData() {
//		String result = "";
//		try {
//
//			InputStream is = null;
//			if (position == 0) {
//				is = getResources().openRawResource(R.raw.devanagari);
//			} else {
//				is = getResources().openRawResource(R.raw.english);
//			}
//
//			ZipInputStream zis = new ZipInputStream(is);
//			ZipEntry ze = zis.getNextEntry();
//
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			if (ze != null) {
//				byte[] buffer = new byte[4 * 1024];
//				int len;
//				while ((len = zis.read(buffer)) > 0) {
//					out.write(buffer, 0, len);
//				}
//			}
//			result = new String(out.toByteArray(), "UTF-8");
//
//		} catch (Exception e) {
//			MyLog.Log(e.getMessage());
//		}
//
//		return result;
//
//	}

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