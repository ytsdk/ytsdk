package com.yt.activities.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ytsdk.testapp.stp.R;
import com.yt.item.VideoItem;

public class SearchListAdapter extends ArrayAdapter<VideoItem> {

	private final Activity context;
	private final ArrayList<VideoItem> catObj;
	private int position;
	private int resourceID;
	private ArrayList<View> mStactViewArray;

	public SearchListAdapter(Activity context, ArrayList<VideoItem> objects, int position, int resourceID) {
		super(context, resourceID, objects);
		this.context = context;
		this.catObj = objects;
		this.position = position;
		this.resourceID = resourceID;
		
		if(objects != null){
			mStactViewArray = new ArrayList<View>(objects.size());
		}
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		try {
			if (mStactViewArray.size() > position + 1 && mStactViewArray.get(position) != null) {
				return mStactViewArray.get(position);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(resourceID, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.itemTitle);
		ImageView videoIcon = (ImageView) rowView.findViewById(R.id.item_image);

		txtTitle.setText(catObj.get(position).getTitle());

		if (catObj.get(position).getIconUrl() != null) {
			// lisIcon.setImageResource(iconRId);
			ImageLoader.getInstance().displayImage(catObj.get(position).getIconUrl(), videoIcon);
		}

		mStactViewArray.add(position, rowView);
		return rowView;
	}

}
