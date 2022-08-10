package com.java.health.care.bed.model;

public class Music {
	
	String name;
	long size;
	String url;
	long duration;
	int musictype;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public int getMusicType() {
		return musictype;
	}

	public void setMusicType(int musictype) {
		this.musictype = musictype;
	}
}
