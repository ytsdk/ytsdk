package com.mp3sdk.activities.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mtunemp3sdk.mp3downloader.R;

public class SearchSuggestAdapter extends BaseAdapter implements Filterable {

	@SuppressWarnings("unused")
	private Context context = null;

	private List<String> datas = new ArrayList<String>();

	private LayoutInflater inflater = null;

	private Filter mFilter = null;

	public SearchSuggestAdapter(Context context, List<String> datas) {
		super();
		this.context = context;
		this.datas = datas;
		this.inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<String> data) {
		if (data != null) {
			this.datas = data;
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (position >= 0 && position < datas.size()) {
			return datas.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = new ViewHolder();
		if (v == null) {
			v = inflater.inflate(R.layout.suggestion_list_item, null);
			holder.keywordTv = (TextView) v.findViewById(R.id.itemTitle);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		holder.keywordTv.setText(datas.get(position));
		return v;
	}

	private class ViewHolder {
		TextView keywordTv = null;
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		if (mFilter == null) {
			mFilter = new MyFilter();
		}
		return mFilter;
	}

	private class MyFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// TODO Auto-generated method stub
			FilterResults results = new FilterResults();
			Log.i("tag", "mOriginalValues.size=" + datas.size());
			ArrayList<String> list = new ArrayList<String>(datas);
			results.values = list;
			results.count = list.size();
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// TODO Auto-generated method stub
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

}
