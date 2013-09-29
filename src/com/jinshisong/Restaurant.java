package com.jinshisong;

import java.util.Map;

public class Restaurant {
	int id = -1;
	String title;
	String address;
	double latitude = -1;
	double longitude = -1;
	
	Restaurant(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) obj;
		Object value;
		if ((value = map.get("nid")) != null) {
			this.id = Integer.valueOf((String) value);
		}
		if ((value = map.get("title")) != null) {
			this.title = new String((String) value);
		}
		if ((value = map.get("address")) != null) {
			this.address = new String((String) value);
		}
		try {
			if ((value = map.get("latitude")) != null) {
				this.latitude = Double.valueOf((String) value);
			}
			if ((value = map.get("longitude")) != null) {
				this.longitude = Double.valueOf((String) value);
			}
		} catch (Exception e) {}
	}
	@Override
	public boolean equals(Object o) {
		Restaurant that = (Restaurant) o;
		return this.id == that.id;
	}
	@Override
	public int hashCode() {
		return id;
	}
	@Override
	public String toString() {
		return title;
	}
	public String getDetail() {
		return    "餐馆: " + (title == null ? "未知" : title) + "\n"
				+ "地址: " + (address == null ? "未知" : address);
	}
}
