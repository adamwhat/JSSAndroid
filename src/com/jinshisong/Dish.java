package com.jinshisong;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Dish {
	int id = -1;
	int restaurantId = -1;
	String name;
	int count = -1;
	List<Dish> setMenuDishes = new ArrayList<Dish>();
	
	Dish(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) obj;
		Object value;
		if ((value = map.get("nid")) != null && !value.equals("")) {
			this.id = Integer.valueOf((String) value);
		}
		if ((value = map.get("restaurant")) != null && !value.equals("")) {
			this.restaurantId = Integer.parseInt((String) value);
		}
		if ((value = map.get("name")) != null) {
			this.name = new String((String) value);
		}
		if ((value = map.get("qty")) != null && !value.equals("")) {
			this.count = Integer.valueOf((String) value);
		}
		Object items[] = map.containsKey("menu_dishes") ? (Object[]) map.get("menu_dishes") : new Object[0];
		for (Object item : items) {
			Dish dish = new Dish(item);
			setMenuDishes.add(dish);
		}
	}
	@Override
	public boolean equals(Object o) {
		Dish that = (Dish) o;
		return this.id == that.id;
	}
	@Override
	public int hashCode() {
		return this.id;
	}
	@Override
	public String toString() {
		return 	  "名称： " + (name == null ? "未知" : name) + "\n" 
				+ "数量： " + (count < 0 ? "未知" : count);
	}
}
