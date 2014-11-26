package com.yt.item;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.yt.common.constants.XmlKeys;

public class VideoItem {

	private int id;// Currently Not Used
	private String title;
	private String[] genreId = null;
	private String pubDate;
	private String firstReleaseDate;
	private String duration;
	private String language;
	private String videoId;
	private int numLikes;
	private int viewCount;
	private String rating;
	private String director;
	private String actors;
	private String rtspUrl;
	private String desc;
	private String iconUrl;
	private boolean myFav = false;
	private int parentId;
	private int countHint;
	private String updated;
	private String localPath;
	
	private Boolean addedToPlayer = false;
	private Boolean tempDeletedItem = false;
	
	private Boolean isAd = false;
	
	public VideoItem(){
		
	}
	
	public VideoItem(Node node){
		try {
			Element element = (Element) node;
			
			this.title = element.getAttribute(XmlKeys.Title);
			if(this.title != null && this.title.length() > 50){
				this.title = this.title.substring(0, 50);
			}
			
			String genStr = element.getAttribute(XmlKeys.GenreIds);
			this.genreId = genStr.split(",");
			
			this.pubDate = element.getAttribute(XmlKeys.PubDate);
			if(this.pubDate != null && this.pubDate.length() > 8){
				this.pubDate = this.pubDate.substring(0, 8);
			}
			
			this.firstReleaseDate = element.getAttribute(XmlKeys.FirstReleaseDate);
			if(this.firstReleaseDate != null && this.firstReleaseDate.length() > 8){
				this.firstReleaseDate = this.firstReleaseDate.substring(0, 8);
			}
			
			this.duration = splitTimeInMins(element.getAttribute(XmlKeys.Duration));
			this.language = element.getAttribute(XmlKeys.Language);
			this.videoId = element.getAttribute(XmlKeys.VideoId);
			this.numLikes = Integer.parseInt(element.getAttribute(XmlKeys.NumLikes));
			this.viewCount = Integer.parseInt(element.getAttribute(XmlKeys.ViewCount));
			this.rating = element.getAttribute(XmlKeys.Rating);
			this.director = element.getAttribute(XmlKeys.Director);
			this.actors = element.getAttribute(XmlKeys.Actor);
			
			// Parsing Rtsp Url
			Element rtspEle = (Element) element
					.getElementsByTagName(XmlKeys.RtspUrl).item(0);
			this.rtspUrl = rtspEle.getFirstChild().getNodeValue();
//			this.rtspUrl = rtspStr.split(",");
			
			this.desc = element.getAttribute(XmlKeys.Desc);
			this.parentId = Integer.parseInt(element.getAttribute(XmlKeys.ParentId));
			
			this.iconUrl = "http://i.ytimg.com/vi/" + this.videoId +  "/mqdefault.jpg"; 
			// Use mqdefault.jpg - height='180' width='320
			// hqdefault.jpg -  height='360' width='480'
			// 1.jpg' height='90' width='120 - Start video thumbail image
			// 2.jpg' height='90' width='120 - middle thumbail image

		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public VideoItem(String readBuf){
		readFromString(readBuf);
	}
	
	
	private String splitTimeInMins(String millisecondsStr) {
		
		if(millisecondsStr.indexOf(":") > 0){
			return millisecondsStr;
		}
		
		String retString = millisecondsStr;
		
		try {
			int seconds = Integer.parseInt(millisecondsStr);
			
			int hours = seconds / 3600;
			int minutes = (seconds / 60) % 60;
			seconds = seconds % 60;
			
			String secStr = "" + seconds;
			if(seconds < 10){
				secStr = "0" + seconds;
			}
			
			if(hours > 0){
				String minsStr = "" + minutes;
				if(minutes < 10){
					minsStr = "0" + minutes;
				}
				
				retString = "" + hours + ":" + minsStr + ":" + secStr;
				
			}else if(minutes > 0){
				
				retString = "" + minutes + ":" + secStr;
			}else{
				
				retString = "" + secStr;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return retString;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String[] getGenreId() {
		return genreId;
	}

	public void setGenreId(String[] genreId) {
		this.genreId = genreId;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
		if(this.pubDate != null && this.pubDate.length() > 8){
			this.pubDate = this.pubDate.substring(0, 8);
		}
	}

	public String getFirstReleaseDate() {
		return firstReleaseDate;
	}

	public void setFirstReleaseDate(String firstReleaseDate) {
		this.firstReleaseDate = firstReleaseDate;
		if(this.firstReleaseDate != null && this.firstReleaseDate.length() > 8){
			this.firstReleaseDate = this.firstReleaseDate.substring(0, 8);
		}
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getIconUrl() {
		if(this.iconUrl == null && this.videoId != null){
			this.iconUrl = "http://i.ytimg.com/vi/" + this.videoId +  "/mqdefault.jpg"; 
			// Use mqdefault.jpg - height='180' width='320
			// hqdefault.jpg -  height='360' width='480'
			// default.jpg - height='90' width='120 nothing but 2.jpg
			// 1.jpg' height='90' width='120 - Start video thumbail image
			// 2.jpg' height='90' width='120 - middle thumbail image
			// 3.jpg' height='90' width='120 - end thumbail image
		}
		return this.iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getRating() {
		return rating;
	}
	
	public float getRatingFloatValue() {
		if(rating != null && rating.length() > 0){
			return Float.parseFloat(rating);
		}
		
		return 0;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = splitTimeInMins(duration);
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public int getNumLikes() {
		return numLikes;
	}

	public void setNumLikes(int numLikes) {
		this.numLikes = numLikes;
	}
	
	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public void setNumLikes(String numLikesStr) {
		if(numLikesStr != null && numLikesStr.length() > 0){
			this.numLikes = Integer.parseInt(numLikesStr);
		}
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	
	public void setViewCount(String viewCount) {
		if(viewCount != null){
			this.viewCount = Integer.parseInt(viewCount);
		}
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getActors() {
		return actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getRtspUrl() {
		return rtspUrl;
	}

	public void setRtspUrl(String rtspUrl) {
		this.rtspUrl = rtspUrl;
	}
	
	public boolean getMyFav() {
		return myFav;
	}


	public void setMyFav(boolean myFav) {
		this.myFav = myFav;
	}
	
	public void setMyFav(String myFav) {
		
		if(myFav != null && myFav.length() == 1){
			if(myFav.compareToIgnoreCase("1") == 0){
				this.myFav = true;
			}else{
				this.myFav = false;
			}
		}
		else if(myFav != null && myFav.length() > 2){
			if(myFav.compareToIgnoreCase("true") == 0){
				this.myFav = true;
			}else{
				this.myFav = false;
			}
		}
	}
	
	public int getCountHint() {
		return countHint;
	}

	public void setCountHint(int countHint) {
		this.countHint = countHint;
	}
	
	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
		if(this.updated != null && this.updated.length() > 10){
			this.updated = this.updated.substring(0, 10);
		}
	}
	
	public void setAddedToPlayer(Boolean value){
		addedToPlayer = value;
	}
	
	public Boolean isAddedToPlayer(){
		return addedToPlayer;
	}
	
	public void setDeleteItem(Boolean value){
		tempDeletedItem = value;
	}
	
	public Boolean isDeletedItem(){
		return tempDeletedItem;
	}
	
	public void setAd(Boolean value){
		isAd = value;
	}
	
	public Boolean isAd(){
		return isAd;
	}
	
	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	
	public String writeToString(){
		String buffer = "";
		
		buffer += writeVariable(title);
		buffer += writeVariable(genreId);
		buffer += writeVariable(pubDate);
		buffer += writeVariable(firstReleaseDate);
		buffer += writeVariable(duration);
		buffer += writeVariable(language);
		buffer += writeVariable(videoId);
		buffer += writeVariable(numLikes);
		buffer += writeVariable(viewCount);
		buffer += writeVariable(rating);
		buffer += writeVariable(director);
		buffer += writeVariable(actors);
		buffer += writeVariable(rtspUrl);
		buffer += writeVariable(desc);
		buffer += writeVariable(iconUrl);
		buffer += writeVariable(myFav);
		buffer += writeVariable(parentId);
		buffer += writeVariable(countHint);
		buffer += writeVariable(updated);
		buffer += writeVariable(localPath);
		buffer += writeVariable(addedToPlayer);
		buffer += writeVariable(isAd);
		
		// Addding extra id to the string
		return buffer + XmlKeys.KEY_DIFF_1;
	}
	
	public void readFromString(String readBuf){
		// Read in Same order of Insertion/written
		
		String[] elements = readBuf.split(XmlKeys.KEY_DIFF_1);
		if(elements.length > 15){
			
			title = elements[0];
			
			String genStr = elements[1];
			if(genStr != null && genStr.length() > 0)
				genreId = genStr.split(XmlKeys.KEY_DIFF_2);
			
			pubDate = elements[2];
			firstReleaseDate = elements[3];
			duration = elements[4];
			language = elements[5];
			videoId = elements[6];
			
			try {
				numLikes = Integer.parseInt(elements[7]);
				viewCount = Integer.parseInt(elements[8]);
			} catch (Exception e) {
			}
			
			
			rating = elements[9];
			director = elements[10];
			actors = elements[11];
			rtspUrl = elements[12];
			desc = elements[13];
			iconUrl = elements[14];
			
			try {
				myFav = Boolean.parseBoolean(elements[15]);
				parentId = Integer.parseInt(elements[16]);
				countHint = Integer.parseInt(elements[17]);
				updated = elements[18];
				localPath = elements[19];
				
				addedToPlayer = Boolean.parseBoolean(elements[20]);
				isAd = Boolean.parseBoolean(elements[21]);

			} catch (Exception e) {
				myFav = false;
			}
			
			
		}
		
		
	}
	
	private String writeVariable(String var){
		if(var != null){
			return var + XmlKeys.KEY_DIFF_1;
		}
		
		return "" + XmlKeys.KEY_DIFF_1;
	}
	
	private String writeVariable(String[] var){
		String arr = "";
		for(int i = 0; var != null && i < var.length; i++){
			arr += var[i] + XmlKeys.KEY_DIFF_2;
		}
		return arr + XmlKeys.KEY_DIFF_1;
	}
	
	private String writeVariable(int var){
		return "" + var + XmlKeys.KEY_DIFF_1;
	}
	
	private String writeVariable(boolean var){
		return "" + var + XmlKeys.KEY_DIFF_1;
	}

	


}
