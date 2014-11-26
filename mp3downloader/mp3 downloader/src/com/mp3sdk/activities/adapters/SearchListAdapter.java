package com.mp3sdk.activities.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mtunemp3sdk.mp3downloader.R;
import com.ytsdk.mp3lib.model.Tracks;

public class SearchListAdapter extends ArrayAdapter<Tracks> {
	Context context;
	int layoutResourceId;
	List<Tracks> data = new ArrayList<Tracks>();

	public SearchListAdapter(Context context, int layoutResourceId,
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
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
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
