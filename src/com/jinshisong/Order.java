package com.jinshisong;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Order implements Comparable<Order> {
	int id = -1;
	int deliveryFee = -1;
	int restaurantCost = -1;
	int jinshisongCost = -1;
	int customerCost = -1;
	int orderTotal = -1;
	String customerName;
	String customAddress;
	String customAddressComment;
	double customerLatitude = -1;
	double customerLongitude = -1;
	String customerPhoneNumber;
	String status;
	Date createTime;
	String comment;
	long fetchDeadLine = -1;
	
	int restaurantID = -1;
	List<Dish> restaurant_dishes = new ArrayList<Dish>();
	List<Dish> jinshisong_dishes = new ArrayList<Dish>();
	
	public static final SimpleDateFormat format = new SimpleDateFormat("H点mm分");
	
	Order(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) obj;
		Object value;
		if ((value = map.get("order_id")) != null) {
			this.id = Integer.valueOf((String) value);
		}
		if ((value = map.get("order_total")) != null) {
			this.orderTotal = Integer.valueOf((String) value);
		}
		if ((value = map.get("delivery_title")) != null) {
			this.customerName = new String((String) value);
		}
		if ((value = map.get("delivery_phone")) != null) {
			this.customerPhoneNumber = new String((String) value);
		}
		if ((value = map.get("delivery_address")) != null) {
			this.customAddress = new String((String) value);
		}
		if ((value = map.get("address_comment")) != null) {
			this.customAddressComment = new String((String) value);
		}
		if ((value = map.get("created")) != null) {
			this.createTime = new Date(Long.valueOf((String) value) * 1000);
		}
		if ((value = map.get("order_status")) != null) {
			this.status = new String((String) value);
		}
		if ((value = map.get("delivery_fee")) != null) {
			this.deliveryFee = Integer.valueOf((String) value);
		}
		if ((value = map.get("restaurant_cost")) != null) {
			this.restaurantCost = Integer.valueOf((String) value);
		}
		if ((value = map.get("jinshisong_cost")) != null) {
			this.jinshisongCost = Integer.valueOf((String) value);
		}
		if ((value = map.get("customer_cost")) != null) {
			this.customerCost = Integer.valueOf((String) value);
		}
		if (map.containsKey("dispatched_time")) {
			long dispatchTime = Long.valueOf((String)map.get("dispatched_time")) * 1000;
			if (map.containsKey("cooking_time")) {
				int cookingTimeInMinutes = Integer.valueOf((String) map.get("cooking_time"));
				this.fetchDeadLine = dispatchTime + cookingTimeInMinutes * 60 * 1000;
			}
		}
		if ((value = map.get("restaurant")) != null) {
			this.restaurantID = Integer.valueOf((String) value);
		}
		if ((value = map.get("order_comments")) != null) {
			if (((String)value).length() > 0) {
				this.comment = new String((String) value);
			}
		}
		Object items[] = map.containsKey("items") ? (Object[]) map.get("items") : new Object[0];
		for (Object item : items) {
			Dish dish = new Dish(item);
			if(dish.restaurantId == -1){
				//don't add dish if error occures when parser dish.
			} else if (dish.restaurantId != restaurantID) {
				jinshisong_dishes.add(dish);
			} else {
				restaurant_dishes.add(dish);
			}
		}
		Pattern pattern = Pattern.compile("(\\d+\\.\\d*),(\\d+\\.\\d*)");
		Matcher matcher = pattern.matcher(this.customAddressComment);
		if (matcher.find()) {
			this.customerLatitude = Double.parseDouble(matcher.group(1));
			this.customerLongitude = Double.parseDouble(matcher.group(2));
		}
	}
	@Override
	public boolean equals(Object o) {
		Order that = (Order) o;
		return this.id == that.id;
	}
	@Override
	public int hashCode() {
		return this.id;
	}
	@Override
	public String toString() {
		return    "订单编号: " + (id < 0 ? "未知" : id) + "\n"
				+ "订餐时间: " + (createTime == null ? "未知" : format.format(createTime)) + "\n"
				+ "订单状态: " + (status == null ? "未知" : translateOrderStatus(status));
	}
	@Override
	public int compareTo(Order another) {
		return this.createTime.compareTo(another.createTime);
	}
	public String getHtmlOrderDetail() {
		return 	  "订餐时间: " + (createTime == null ? "未知" : format.format(createTime)) + "<br/>"
				+ "菜品数量: " + restaurant_dishes.size();
	}
	public String getHtmlTotalPrice() {
		int change = 0;
		if (customerCost % 100 == 0) {
			change = 0;
		} else {
			change = (customerCost / 100 + 1) * 100 - customerCost;
		}
		return  "<font color=\"yellow\">菜品合计: " + (orderTotal < 0 ? "未知" : orderTotal) + "元</font><br/>"
			  +	"<font color=\"yellow\">餐馆费用: " + (restaurantCost < 0 ? "未知" : restaurantCost) + "元</font><br/>"
		      + "<font color=\"yellow\">酒水费用: " + (jinshisongCost < 0 ? "未知" : jinshisongCost) + "元</font><br/>"
		      + "<font color=\"yellow\">送餐费用: " + (deliveryFee < 0 ? "未知" : deliveryFee) + "元</font><br/>"
		      + "<font color=\"yellow\">顾客费用: " + (customerCost < 0 ? "未知" : customerCost) + "元</font><br/>"
		      + "<font color=\"yellow\">顾客找零: " + (customerCost < 0 ? "未知" : change) + "元</font><br/>";
	}
	
	public String getCustomerDetail() {
		String customAddressCommentText = ""; 
		if (customAddressComment != null && customAddressComment.trim().length() > 0) {
			customAddressCommentText = "\n地址注释: " + customAddressComment;
		}
		return 	  "称呼: " + (customerName == null ? "未知" : customerName) + "\n"
				+ "电话: " + (customerPhoneNumber == null ? "未知" : customerPhoneNumber) + "\n"
				+ "地址: " + (customAddress == null ? "未知" : customAddress) + customAddressCommentText;
	}
	
	public String translateOrderStatus(String status) {
		if (status.equals(DataCenter.INITIAL_STATUS)) {
			return "未处理";
		} else if (status.equals(DataCenter.DISH_FETCHED_STATUS)) {
			return "已取餐";
		} else if (status.equals(DataCenter.COMPLETE_STATUS)) {
			return "已送餐";
		} else {
			return status;
		}
	}
}
