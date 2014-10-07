package com.example.mp3testapp;

import java.util.ArrayList;
import java.util.List;

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

import com.chiragapps.mp3downloader.R;
import com.ytsdk.mp3lib.model.Tracks;

public class DownloadedListAdapter extends ArrayAdapter<Tracks> {

	private final Activity context;
	private final List<Tracks> trackInfo;
	//
	private int resourceID;

	private ArrayList<View> mStactViewArray;

	public DownloadedListAdapter(Activity context, List<Tracks> objects,
			int position, int resourceID) {
		super(context, resourceID, objects);
		this.context = context;
		this.trackInfo = objects;

		this.resourceID = resourceID;

		if (objects != null) {
			mStactViewArray = new ArrayList<View>(objects.size());
		}
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		try {
			if (mStactViewArray.size() > position + 1
					&& mStactViewArray.get(position) != null) {
				return mStactViewArray.get(position);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(resourceID, null, true);

		TextView txtTitle = (TextView) rowView.findViewById(R.id.itemTitle);
		txtTitle.setText(trackInfo.get(position).getName());

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
