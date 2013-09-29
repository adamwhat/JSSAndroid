package com.jinshisong;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShowRestaurantDishes extends Activity implements OnItemClickListener {
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.show_restaurant_dishes);
        
        Order order = DataCenter.currentOrder;
        
        List<Dish> dishes = order.restaurant_dishes;
        
        ListView dishListView = (ListView) findViewById(R.id.restaurant_dishes_list);

        // setup data adapter
        ArrayAdapter<Dish> dish_adapter = new ArrayAdapter<Dish>(this, android.R.layout.simple_list_item_1,
        		dishes.toArray(new Dish[dishes.size()]) );

        // assign adapter to list view
        dishListView.setAdapter(dish_adapter);
        dishListView.setOnItemClickListener(ShowRestaurantDishes.this);
        
        TextView dish_tv = (TextView) findViewById(R.id.restaurant_dishes_label);
        dish_tv.setText("餐馆支付费用：" + (order.restaurantCost) + "元");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Dish dish = DataCenter.currentOrder.restaurant_dishes.get(position);
		DataCenter.currentDish = dish;
		if (dish.setMenuDishes.size() > 0) {
			startActivity(new Intent(this, ShowSetMenuDishes.class));
		}
	}
}
