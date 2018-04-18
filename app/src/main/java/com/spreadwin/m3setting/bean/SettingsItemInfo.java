package com.spreadwin.m3setting.bean;

public class SettingsItemInfo {

	String name;
	int imageId;

	public SettingsItemInfo(String name, int imageId) {
		super();
		this.name = name;
		this.imageId = imageId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

}
