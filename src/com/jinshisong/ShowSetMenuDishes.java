package com.jinshisong;

import java.util.List;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ShowSetMenuDishes extends Activity {
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.show_set_menu_dishes);
        Dish currentDish = DataCenter.currentDish;
        String setMenuName = currentDish.name;
        
        TextView tv = (TextView) findViewById(R.id.setmenu_dishes_label);
        tv.setText(Html.fromHtml("单个<font color='yellow'>" + setMenuName + "</font>包含菜品:"));
        
        List<Dish> menuDishes = currentDish.setMenuDishes;
        
        ListView dishListView = (ListView) findViewById(R.id.setmenu_dishes_list);

        // setup data adapter
        ArrayAdapter<Dish> dish_adapter = new ArrayAdapter<Dish>(this, android.R.layout.simple_list_item_1,
        		menuDishes.toArray(new Dish[menuDishes.size()]) );

        // assign adapter to list view
        dishListView.setAdapter(dish_adapter);
        
        Button cancelButton = (Button) findViewById(R.id.show_menu_dishes_return);
        cancelButton.getBackground().setColorFilter(0xFFFFD700, PorterDuff.Mode.MULTIPLY);
        cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ShowSetMenuDishes.this.finish();
				
			}
		});
	}
}
