package com.example.mp3testapp;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.chiragapps.mp3downloader.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.startapp.android.publish.StartAppAd;
import com.ytsdk.mp3lib.MP3SDK;
import com.ytsdk.mp3lib.model.Tracks;

public class MainActivity extends Activity {

	private static final String TAG_MainActivity = "MainActivity";
	ListView listView = null;
	MP3SDK engine = null;
	private AutoCompleteTextView mEditText;
	SuggestTask suTask;
	private List<String> suggestions = new ArrayList<String>();
	private List<Tracks> tracks = new ArrayList<Tracks>();
	private StartAppAd startAppAd;

	private InterstitialAd interstitial;

	SearchSuggestAdapter suggetionAdapter = null;
	Button mSearchButton;

	TextView errorText;
	ProgressBar progress;
	RadioGroup searchEngines;
	SongListAdapter adapter = null;
	private AdView adView;
	private static final String AD_UNIT_ID = "ca-app-pub-5717054630162926/9091129899";

	private static final String Intestitial_AD_UNIT_ID = "ca-app-pub-5717054630162926/1567863096";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		creatAdmob();
		createIntestitialAd();

		listView = (ListView) findViewById(R.id.listView);
		searchEngines = (RadioGroup) findViewById(R.id.searchEngines);
		searchEngines.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int id) {
				try {
					switch (id) {
					case R.id.goear:
						engine = MP3SDK.getInstance(MainActivity.this,
								MP3SDK.MP3_ENGINE_GO_EAR);
						suggestions.clear();
						tracks.clear();
						adapter.notifyDataSetChanged();
						group.check(R.id.goear);
						break;
					case R.id.mp3skull:
						engine = MP3SDK.getInstance(MainActivity.this,
								MP3SDK.MP3_ENGINE_MP3_SKULL);
						suggestions.clear();
						tracks.clear();
						adapter.notifyDataSetChanged();
						break;
					case R.id.soundboul:
						engine = MP3SDK.getInstance(MainActivity.this,
								MP3SDK.MP3_ENGINE_SOUND_BOWL);
						suggestions.clear();
						tracks.clear();
						adapter.notifyDataSetChanged();
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Tracks entry = (Tracks) parent.getAdapter().getItem(position);
				try {
					engine.previewOrDownloadMP3(MainActivity.this,
							entry.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mEditText = (AutoCompleteTextView) findViewById(R.id.searchText);

		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (engine == null) {
					Toast.makeText(MainActivity.this,
							"please select search engine.", Toast.LENGTH_LONG)
							.show();
					return;
				}
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
		suggetionAdapter = new SearchSuggestAdapter(this, suggestions);
		mEditText.setAdapter(suggetionAdapter);
		mSearchButton = (Button) findViewById(R.id.search);
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (engine == null) {
					Toast.makeText(MainActivity.this,
							"please select search engine.", Toast.LENGTH_LONG)
							.show();
					return;
				}
				String keyword = mEditText.getText().toString().trim();
				try {

					if (keyword == null || keyword.equals("")) {
						Toast.makeText(MainActivity.this, "Search text null",
								Toast.LENGTH_SHORT).show();
					} else {
						new TestMp3().execute(keyword);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		errorText = (TextView) findViewById(R.id.errorText);
		progress = (ProgressBar) findViewById(R.id.loading);

		adapter = new SongListAdapter(MainActivity.this, R.layout.list_item,
				tracks);
		listView.setAdapter(adapter);

		Button mydownloads = (Button) findViewById(R.id.mydownloads);
		mydownloads.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (interstitial.isLoaded()) {
					interstitial.show();
				}

				Intent intent = new Intent(MainActivity.this, MyDownloads.class);
				startActivity(intent);
			}
		});
	}

	class SuggestTask extends AsyncTask<String, Object, List<String>> {
		@Override
		protected List<String> doInBackground(String... params) {
			return engine.getSearchSuggestion(MainActivity.this, params[0]);
		}

		@Override
		protected void onPostExecute(List<String> result) {
			suggestions.clear();
			suggestions.addAll(result);
			suggetionAdapter.notifyDataSetChanged();
		}
	}

	class TestMp3 extends AsyncTask<String, Object, List<Tracks>> {
		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
			errorText.setVisibility(View.GONE);
			listView.setVisibility(View.GONE);
		}

		@Override
		protected List<Tracks> doInBackground(String... params) {
			List<Tracks> allTracks = null;
			try {
				allTracks = engine
						.searchMP3Tracks(MainActivity.this, params[0]);
				return allTracks;
			} catch (SocketTimeoutException e) {
				Log.d(TAG_MainActivity, e.getMessage());
			} catch (IOException e) {
				Log.d(TAG_MainActivity, e.getMessage());
			} catch (Exception e) {
				Log.d(TAG_MainActivity, e.getMessage());
			}
			return allTracks;
		}

		@Override
		protected void onPostExecute(List<Tracks> local) {
			progress.setVisibility(View.GONE);
			if (local != null && !local.isEmpty()) {
				tracks.clear();
				tracks.addAll(local);
				adapter.notifyDataSetChanged();
				listView.setVisibility(View.VISIBLE);
			} else {
				errorText.setVisibility(View.VISIBLE);
			}
		}
	}

	class SongListAdapter extends ArrayAdapter<Tracks> {
		Context context;
		int layoutResourceId;
		List<Tracks> data = new ArrayList<Tracks>();

		public SongListAdapter(Context context, int layoutResourceId,
				List<Tracks> data) {
			super(context, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			RecordHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new RecordHolder();
				holder.txtTitle = (TextView) row.findViewById(R.id.title);
				row.setTag(holder);
			} else {
				holder = (RecordHolder) row.getTag();
			}

			Tracks item = data.get(position);
			holder.txtTitle.setText(item.getName());
			return row;

		}

		class RecordHolder {
			TextView txtTitle;
		}
	}

	// private void createAdmobAdd() {
	// adView = new AdView(this);
	// adView.setAdSize(AdSize.BANNER);
	// adView.setAdUnitId(AD_UNIT_ID);
	// LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
	// layout.addView(adView);
	// AdRequest adRequest = new AdRequest.Builder();
	// adView.loadAd(adRequest);
	//
	// }

	private void creatAdmob() {
		AdRequest adRequest = new AdRequest.Builder().build();
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(adRequest);
	}

	private void createIntestitialAd() {

		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId(Intestitial_AD_UNIT_ID);
		AdRequest adRequest = new AdRequest.Builder().build();
		interstitial.loadAd(adRequest);
		

	}
}