package com.jinshisong;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ShowJinshisongDishes extends Activity {
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.show_jinshisong_dishes);
        
        Order order = DataCenter.currentOrder;
        
        List<Dish> dishes = order.jinshisong_dishes;
        
        ListView dishListView = (ListView) findViewById(R.id.jinshisong_dishes_list);

        // setup data adapter
        ArrayAdapter<Dish> dish_adapter = new ArrayAdapter<Dish>(this, android.R.layout.simple_list_item_1,
        		dishes.toArray(new Dish[dishes.size()]) );

        // assign adapter to list view
        dishListView.setAdapter(dish_adapter);
        
        TextView dish_tv = (TextView) findViewById(R.id.jinshisong_dishes_label);
        dish_tv.setText("酒水支付费用：" + (order.jinshisongCost) + "元");

	}
}
