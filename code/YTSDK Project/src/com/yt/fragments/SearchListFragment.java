package com.yt.fragments;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import yt.sdk.access.YTSDK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.video.downloader99.R;
import com.yt.activities.YTSDKUtils;
import com.yt.activities.adapters.SearchListAdapter;
import com.yt.application.GlobalAppData;
import com.yt.common.utils.ConnectionChecker;
import com.yt.common.utils.GData;
import com.yt.common.utils.MyLog;
import com.yt.item.VideoItem;

public class SearchListFragment extends Fragment {

	private static final String KEY_CONTENT = "ItemListFragment:Items";
	private static final String KEY_FILE_NAME = "ItemListFragment:fileName";

	private final String SEARCH_URL_1 = "http://gdata.youtube.com/feeds/api/videos?q=";
	private final String SEARCH_URL_2 = "&max-results=50&v=2.1"; // &category=Music

	private GridView mGridView;

	LinearLayout progressBar;
	SearchListAdapter mListAdapter;
	ArrayList<VideoItem> item = new ArrayList<VideoItem>();

	private static SearchListFragment thisPointer;
	private int position;

	private EditText mEditText;
	private LinearLayout mSearchButton;

	public static SearchListFragment getInstance(int position) {

		// if (gridFragent == null) {
		thisPointer = new SearchListFragment();
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
		View view = inflater.inflate(R.layout.layout_search_video, container,
				false);

		progressBar = (LinearLayout) view.findViewById(R.id.loadingPanel);
		mGridView = (GridView) view.findViewById(R.id.list);

		mEditText = (EditText) view.findViewById(R.id.edit_seach);
		mEditText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							performSearch();
							return true;
						}
						return false;
					}

				});

		mSearchButton = (LinearLayout) view
				.findViewById(R.id.searchButtonLayout);
		mSearchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				performSearch();
			}
		});

		return view;
	}

	private void performSearch() {

		String searchText = mEditText.getText().toString();
		if (searchText == null || searchText.length() == 0) {
			return;
		}

		closeKeyboard();

		if (!isConnectivityPresent()) {
			return;
		}

		Pattern pattern = Pattern.compile("\\s+");
		Matcher matcher = pattern.matcher(searchText);
		String decodedSearchStr = matcher.replaceAll("%20");

		String searchUrl = SEARCH_URL_1 + decodedSearchStr + SEARCH_URL_2;

		new GetVideoListFromYouTube().execute(searchUrl);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onPause() {
		super.onPause();

		closeKeyboard();
	}

	private void closeKeyboard() {

		if (SearchListFragment.this == null) {
			return;
		}

		try {
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(getActivity().INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void onListItemClick(GridView g, View v, int position, long id) {
		// GlobalAppData.numItemClicked++;
		// TODO::Wrtie Code to Call YT SDK
		YTSDKUtils.getYTSDK().download(getActivity(),
				item.get(position).getVideoId());

	}

	private void restoreData(Bundle savedInstanceState) {
		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			item = (ArrayList<VideoItem>) savedInstanceState.get(KEY_CONTENT);
			position = savedInstanceState.getInt(KEY_FILE_NAME);
		}
	}

	private class GetVideoListFromYouTube extends
			AsyncTask<String, Void, ArrayList<VideoItem>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.GONE);
		}

		@Override
		protected ArrayList<VideoItem> doInBackground(String... params) {
			ArrayList<VideoItem> videoList = null;
			String searchUrl = params[0];

			if (searchUrl != null && searchUrl.length() > 0) {

				videoList = GData.getGData(searchUrl,
						SearchListFragment.this.getActivity(), null);
			}

			return videoList;
		}

		@Override
		protected void onPostExecute(ArrayList<VideoItem> result) {
			super.onPostExecute(result);
			// Cancel the Loading Dialog
			progressBar.setVisibility(View.GONE);
			addVideoList(result);

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

		item = videoList;
		mListAdapter = new SearchListAdapter(getActivity(), item, position,
				R.layout.grid_item);
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
		if (!item.isEmpty()) {
			mGridView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
		}

	}

	public String getRawData() {
		String result = "";
		try {

			InputStream is = null;
			if (position == 0) {
				is = getResources().openRawResource(R.raw.devanagari);
			} else {
				is = getResources().openRawResource(R.raw.english);
			}

			ZipInputStream zis = new ZipInputStream(is);
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
			MyLog.Log(e.getMessage());
		}

		return result;

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