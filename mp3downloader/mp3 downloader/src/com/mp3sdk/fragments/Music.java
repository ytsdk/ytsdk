package com.mp3sdk.fragments;

import java.io.FileDescriptor;
import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class Music implements OnCompletionListener{
	MediaPlayer mediaPlayer;
	boolean isPrepared = false;
	
	public Music(AssetFileDescriptor assetDescriptor){
		mediaPlayer = new MediaPlayer();
		try{
			mediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(), assetDescriptor.getStartOffset(), assetDescriptor.getLength());
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
		} catch(Exception ex){
			throw new RuntimeException("Couldn't load music, uh oh!");
		}
	}
	
	public Music(FileDescriptor fileDescriptor){
		mediaPlayer = new MediaPlayer();
		try{
			mediaPlayer.setDataSource(fileDescriptor);
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
		} catch(Exception ex){
			throw new RuntimeException("Couldn't load music, uh oh!");
		}
	}
	
	public void onCompletion(MediaPlayer mediaPlayer) {
		synchronized(this){
			isPrepared = false;
		}
	}

	public void play() {
		if(mediaPlayer.isPlaying()){
			return;
		}
		try{
			synchronized(this){
				if(!isPrepared){
					mediaPlayer.prepare();
				}
				mediaPlayer.start();
			}
		} catch(IllegalStateException ex){
			ex.printStackTrace();
		} catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public void stop() {
		mediaPlayer.stop();
		synchronized(this){
			isPrepared = false;
		}
	}
	
	public void switchTracks(){
		mediaPlayer.seekTo(0);
		mediaPlayer.pause();
	}
	
	public void pause() {
		mediaPlayer.pause();
	}

	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}
	
	public boolean isLooping() {
		return mediaPlayer.isLooping();
	}
	
	public void setLooping(boolean isLooping) {
		mediaPlayer.setLooping(isLooping);
	}

	public void setVolume(float volumeLeft, float volumeRight) {
		mediaPlayer.setVolume(volumeLeft, volumeRight);
	}

	public void dispose() {
		if(mediaPlayer.isPlaying()){
			stop();
		}
		mediaPlayer.release();
	}
}