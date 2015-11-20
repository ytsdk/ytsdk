package com.yt.activities.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ytsdk.testapp.stp.R;
import com.yt.item.VideoItem;

public class DownloadedListAdapter extends ArrayAdapter<VideoItem> {

	private final Activity context;
	private final ArrayList<VideoItem> catObj;
	private int position;
	private int resourceID;
	
	private ArrayList<View> mStactViewArray;

	public DownloadedListAdapter(Activity context, ArrayList<VideoItem> objects, int position, int resourceID) {
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

		try {

			MyImageItem imageItem = new MyImageItem(videoIcon,
					catObj.get(position).getLocalPath());
			new LoadMediaThumbnail().execute(imageItem);

		} catch (Exception e) {
			// TODO: handle exception
		}

		mStactViewArray.add(position, rowView);
		return rowView;
	}
	
	
	private class MyImageItem {

		public ImageView mImageView;
		public String mLocalPath;

		public MyImageItem(ImageView imageView, String localPath) {
			mImageView = imageView;
			mLocalPath = localPath;
		}
	}

	private class LoadMediaThumbnail extends
			AsyncTask<MyImageItem, String, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(MyImageItem... params) {

			final MyImageItem imageItem = params[0];

			final Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(
					imageItem.mLocalPath, Thumbnails.MINI_KIND);

			context.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					imageItem.mImageView.setImageBitmap(bmThumbnail);

				}
			});

			return null;
		}

	}

}
