package com.mp3sdk.fragments;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mp3sdk.activities.MP3SDKUtils;
import com.mp3sdk.activities.adapters.SearchListAdapter;
import com.mp3sdk.activities.adapters.SearchSuggestAdapter;
import com.mp3sdk.common.constants.Constants;
import com.mp3sdk.common.utils.ConnectionChecker;
import com.mp3sdk.common.utils.MyLog;
import com.mp3sdk.common.utils.Utils;
import com.mtunemp3sdk.mp3downloader.R;
import com.ytsdk.mp3lib.MP3SDK;
import com.ytsdk.mp3lib.model.Tracks;

public class SearchListFragment extends Fragment {

	private static final String KEY_CONTENT = "SearchListFragment:Items";
	private static final String KEY_FILE_NAME = "SearchListFragment:fileName";
	private static final String TAG_SearchListFragment = "SearchListFragment";

	private ListView mListView;

	LinearLayout progressBar;
	SearchListAdapter mListAdapter;
	List<Tracks> item = new ArrayList<Tracks>();

	private static SearchListFragment thisPointer;
	private int engineType;

	private AutoCompleteTextView mEditText;
	private LinearLayout mSearchButton;

	private static MP3SDK engine = null;

	SuggestTask suTask;
	private List<String> suggestions = new ArrayList<String>();
	SearchSuggestAdapter suggetionAdapter = null;

	private static int numSearches = 0;

	public static SearchListFragment getInstance(int sdktype) {

		// if (gridFragent == null) {
		thisPointer = new SearchListFragment();
		// }
		Bundle args = new Bundle();
		args.putInt("engineType", sdktype);
		thisPointer.setArguments(args);
		return thisPointer;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		restoreData(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null) {
			this.engineType = bundle.getInt("engineType");
		}
		super.onCreate(savedInstanceState);

		engine = MP3SDKUtils.getMP3SDK(getActivity(), this.engineType);
		Utils.loadFullScreenAd(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_search_video, container,
				false);

		progressBar = (LinearLayout) view.findViewById(R.id.loadingPanel);
		mListView = (ListView) view.findViewById(R.id.listView);

		mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_seach);
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
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (s.toString().trim().equals(""))
					return;
				if (suTask != null) {
					suTask.cancel(true);
					suTask = null;
				}
				suTask = new SuggestTask();
				suTask.execute(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		suggetionAdapter = new SearchSuggestAdapter(getActivity(), suggestions);
		mEditText.setAdapter(suggetionAdapter);

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

		if (++numSearches % Constants.NUM_ADS_PER_SEARCH == 0) {
			Utils.showFullScreenAd(getActivity());
		}

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

		new GetVideoListFromYouTube().execute(decodedSearchStr);

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

	public void onListItemClick(AdapterView<?> parent, View v, int position,
			long id) {
		Tracks entry = (Tracks) parent.getAdapter().getItem(position);
		try {
			engine.previewOrDownloadMP3(getActivity(), entry.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void restoreData(Bundle savedInstanceState) {
		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			item = (ArrayList<Tracks>) savedInstanceState.get(KEY_CONTENT);
			this.engineType = savedInstanceState.getInt(KEY_FILE_NAME);
		}
	}

	private class GetVideoListFromYouTube extends
			AsyncTask<String, Void, List<Tracks>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}

		@Override
		protected List<Tracks> doInBackground(String... params) {
			List<Tracks> allTracks = null;
			try {
				allTracks = engine.searchMP3Tracks(getActivity(), params[0]);
				return allTracks;
			} catch (SocketTimeoutException e) {
				Log.d(TAG_SearchListFragment, e.getMessage());
			} catch (IOException e) {
				Log.d(TAG_SearchListFragment, e.getMessage());
			} catch (Exception e) {
				Log.d(TAG_SearchListFragment, e.getMessage());
			}
			return allTracks;
		}

		@Override
		protected void onPostExecute(List<Tracks> result) {
			super.onPostExecute(result);
			// Cancel the Loading Dialog
			progressBar.setVisibility(View.GONE);
			addVideoList(result);

		}

	}

	private void addVideoList(List<Tracks> videoList) {

		if (videoList == null || videoList.size() == 0) {

			if (!progressBar.isShown()) {

			}
			return;
		}

		// mErrorTextView.setVisibility(View.GONE);

		item = videoList;

		mListAdapter = new SearchListAdapter(getActivity(), R.layout.list_item,
				item);
		if (mListView != null) {
			mListView.setAdapter(mListAdapter);
		}

		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view,
					int position, long id) {
				onListItemClick(parent, view, position, id);
			}
		});
		// }

		// This has to run only at fist time from next the on scroll down will
		// run
		if (!item.isEmpty()) {
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

	class SuggestTask extends AsyncTask<String, Object, List<String>> {
		@Override
		protected List<String> doInBackground(String... params) {
			return engine.getSearchSuggestion(getActivity(), params[0]);
		}

		@Override
		protected void onPostExecute(List<String> result) {
			suggestions.clear();
			suggestions.addAll(result);
			suggetionAdapter.notifyDataSetChanged();
		}
	}

}