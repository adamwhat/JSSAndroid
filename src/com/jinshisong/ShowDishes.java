package com.jinshisong;

import java.util.Timer;

import org.xmlrpc.android.XMLRPCClient;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

public class ShowDishes extends TabActivity {
	private ProgressDialog myprogress;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.show_dishes);

		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		Order order = DataCenter.currentOrder;
		if (order.jinshisong_dishes.size() > 0) {
			intent = new Intent().setClass(this, ShowJinshisongDishes.class);
			spec = tabHost.newTabSpec("drink_dishes").setIndicator("附带酒水")
					.setContent(intent);
			tabHost.addTab(spec);
		}

		intent = new Intent().setClass(this, ShowRestaurantDishes.class);
		spec = tabHost.newTabSpec("restaurant_dishes").setIndicator("菜品")
				.setContent(intent);
		tabHost.addTab(spec);

		Button dishfetchedbutton = (Button) findViewById(R.id.dishfetchedbutton);
		if (DataCenter.currentOrder.status.equals(DataCenter.INITIAL_STATUS)) {
			dishfetchedbutton.setEnabled(true);
			dishfetchedbutton.getBackground().setColorFilter(0xFFFFD700,
					PorterDuff.Mode.MULTIPLY);
			dishfetchedbutton.setOnClickListener(new Button.OnClickListener() {

				public void onClick(View v) {
					upload();
				}
			});
		} else {
			dishfetchedbutton.setEnabled(false);
		}
	}

	private void upload() {
		myprogress = ProgressDialog.show(ShowDishes.this, "正在向服务器上传", "请等待",true, false);
				XMLRPCClient client = new XMLRPCClient(DataCenter.serverURL);
				Message msg = new Message();
				msg.what = 0;
				msg.obj = ("正在上传状态...");
				ShowDishes.this.progresshandler.sendMessage(msg);
				try {
					Boolean success = (Boolean) client.call("order.update",
							DataCenter.currentOrder.id,
							DataCenter.DISH_FETCHED_STATUS);
					msg = new Message();
					msg.what = 0;
					msg.obj = success ? "上传成功" : "上传失败";
					ShowDishes.this.progresshandler.sendMessage(msg);
					msg = new Message();
					while (!success)
					{
						try {
							  Thread.sleep(1000L);	  // one second
							}
						catch (Exception e) {};
						success = (Boolean) client.call("order.update",
								DataCenter.currentOrder.id,
								DataCenter.DISH_FETCHED_STATUS);
					}
					if (success) {
						msg.what = 1;
						//send message
						 SmsManager smsMgr = SmsManager.getDefault();
						 //TODO
						 try{
							 Order order = DataCenter.currentOrder;
						//	 smsMgr.sendTextMessage(order.customerPhoneNumber, null, "锦食送Test", null, null);
						 }
						 catch (Exception e) {
		                        Log.e("SmsSending", "SendException", e);
		                   }
						 
					} else {
						msg.what = 2;
						msg.obj = "上传失败";
					}				
					ShowDishes.this.progresshandler.sendMessage(msg);
				} catch (Exception e) {
					msg = new Message();
					msg.what = 2;
					msg.obj = e.getMessage();
					ShowDishes.this.progresshandler.sendMessage(msg);
				}
			}
		

	private Handler progresshandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// process incoming messages here
			switch (msg.what) {
			case 0:
				// update progress bar
				ShowDishes.this.myprogress.setMessage("" + (String) msg.obj);
				break;
			case 1:
				ShowDishes.this.myprogress.cancel();
				DataCenter.currentOrder.status = DataCenter.DISH_FETCHED_STATUS;
				setResult(ShowOrder.SUCESS_RESULT_CODE);
				finish();
				break;
			case 2: // error occurred
				ShowDishes.this.myprogress.cancel();
								
				//add a record into the list
				Record record = new Record(DataCenter.serverURL, "order.update", DataCenter.currentOrder,
						DataCenter.DISH_FETCHED_STATUS);
				Timer timer = new Timer();
				myTask task = new myTask(record);
				timer.schedule(task, 1000,5000);
								
				setResult(ShowOrder.SUCESS_RESULT_CODE);
				finish();
				/*Intent intent = new Intent(ShowDishes.this, Cache.class);
				Bundle bundle = new Bundle();
				record.setActionListener(new cacheComplete() {
					
					@Override
					public void actionAfterCompletion(Record record) {
						 SmsManager smsMgr = SmsManager.getDefault();
						 try{
							 Order order = DataCenter.currentOrder;
						//	 smsMgr.sendTextMessage(order.customerPhoneNumber, null, "锦食送Test", null, null);
						 }
						 catch (Exception e) {
		                        Log.e("SmsSending", "SendException", e);
		                   }						
					}
				});
				bundle.putSerializable("Record", record);
				
				intent.putExtras(bundle);
				startService(intent);
				*/
				/*
				AlertDialog alert = new AlertDialog.Builder(ShowDishes.this)
						.create();
				alert.setMessage(msg.obj.toString());
				alert.setButton(DialogInterface.BUTTON_POSITIVE, "重试",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								upload();
							}
						});
				alert.setButton(DialogInterface.BUTTON_NEGATIVE, "放弃",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setResult(ShowOrder.FAILURE_RESULT_CODE);
								finish();
							}
						});
				alert.show();
				*/
				break;
			}
			// super.handleMessage(msg);
		}
	};
}
