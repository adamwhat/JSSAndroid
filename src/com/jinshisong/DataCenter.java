package com.jinshisong;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DataCenter {
	public static final String RESTAURANTS_KEY = "restaurants";
	public static final String ORDERS_KEY = "orders";
	
	public static final String INITIAL_STATUS = "processing";
	public static final String DISH_FETCHED_STATUS = "dish_fetched";
	public static final String COMPLETE_STATUS = "completed";
	
	public static Map<Integer, Restaurant> restaurants = new HashMap<Integer, Restaurant>();
	public static Map<Integer, Order> orders = new TreeMap<Integer, Order>();
	
	public static int deliverymanID = -1;
	public static String serverURL;
	
	public static Order currentOrder;
	public static Dish currentDish;
	public static Restaurant currentRestaurant;
	
	public static void update(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) obj;
		updateRestaurants((Object[]) map.get(RESTAURANTS_KEY));
		updateOrders((Object[]) map.get(ORDERS_KEY));
		currentOrder = null;
		currentRestaurant = null;
	}
	private static void updateRestaurants(Object[] objs) {
		restaurants.clear();
		for (Object obj : objs) {
			Restaurant restaurant = new Restaurant(obj);
			int restaurantId = restaurant.id;
			restaurants.put(restaurantId, restaurant);
		}
	}
	private static void updateOrders(Object[] objs) {
		orders.clear();
		for (Object obj : objs) {
			Order order = new Order(obj);
			int orderId = order.id;
			orders.put(orderId, order);
		}
	}
}
