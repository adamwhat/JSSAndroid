package com.jinshisong;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlrpc.android.XMLRPCClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RefreshAndShowOrders extends Activity implements
		OnItemClickListener {
	private final int SHOW_ORDER_REQUEST_CODE = 1;

	private ProgressDialog myprogress;
	public List<Order> orders;
	private ListView orderListView;
	private ArrayAdapter<Order> adapter;
	public Timer timer;
	public boolean isRunning;
	private refreshTask refresh;
	private final int refreshTime = 60 * 1000;
	public boolean isCreated = false;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.refresh_and_show_orders);

		Button refreshOrdersButton = (Button) findViewById(R.id.refresh_button);

		refreshOrdersButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				launchDownloadingOrders();
			}
		});
		launchDownloadingOrders();
		timer = new Timer();
		refresh = new refreshTask(this);
		timer.schedule(refresh, 10000, refreshTime);
		isCreated = true;

	}

	public void launchDownloadingOrders() {
		
		this.myprogress = ProgressDialog.show(this, "正在获取订单信息", "请稍候", true,
				false);
		isRunning = true;
		refreshOrders();
		isRunning = false;
	}
	

	private void refreshOrders() {
		// set up our message - used to convey progress information
	//	new Thread() {
		//	public void run() {
				try {
					// Looper.prepare();
				//	Looper.prepare(); 
					Message msg = new Message();
					msg.what = 0;
					msg.obj = ("正在下载数据...");
					RefreshAndShowOrders.this.refreshHandler.sendMessage(msg);
					
					Log.e("Download", "Downloading");
					XMLRPCClient client = new XMLRPCClient(DataCenter.serverURL);
					Log.e("Download", "Connected");
					
					@SuppressWarnings("unchecked")
					Map<String, Object> result = (Map<String, Object>) client
							.call("order.query", DataCenter.deliverymanID);

					Log.e("Download", "Downloaded");
					
					msg = new Message();
					msg.what = 0;
					msg.obj = ("下载完成");
					RefreshAndShowOrders.this.refreshHandler.sendMessage(msg);

					if (result.size() > 0) {
						msg = new Message();
						msg.what = 0;
						msg.obj = ("正在解析数据...");
						RefreshAndShowOrders.this.refreshHandler
								.sendMessage(msg);

						DataCenter.update(result);

						msg = new Message();
						msg.what = 0;
						msg.obj = ("解析完成");
						RefreshAndShowOrders.this.refreshHandler
								.sendMessage(msg);
					}

					msg = new Message();
					msg.what = 1;
					RefreshAndShowOrders.this.refreshHandler.sendMessage(msg);
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = 2; // error occurred
					msg.obj = ("获取订单信息失败: " + e.getMessage());
					RefreshAndShowOrders.this.refreshHandler.sendMessage(msg);	
				}
				//Looper.loop();
			}
		//}.start();
	//}
	@Override
	public void onResume()
	{
		isCreated = false;
		super.onResume();
		if(timer!=null) timer.cancel();
		timer = new Timer();
		refresh = new refreshTask(this);
		timer.schedule(refresh, 10000, refreshTime);
	}
	private Handler refreshHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// process incoming messages here
			switch (msg.what) {
			case 0:
				// update progress bar
				RefreshAndShowOrders.this.myprogress.setMessage(""
						+ (String) msg.obj);
				break;
			case 1:
				RefreshAndShowOrders.this.myprogress.cancel();

				orders = new ArrayList<Order>(DataCenter.orders.values());

				TextView tv = (TextView) findViewById(R.id.statuslabel);

				if (orders.size() == 0) {
					tv.setText("目前暂无订单");
					Button logoutButton = (Button) findViewById(R.id.logout_button);
					logoutButton.setVisibility(View.VISIBLE);
					logoutButton
							.setOnClickListener(new Button.OnClickListener() {

								public void onClick(View v) {
									logout();
								}
							});
				} else {
					ImageView alertImage = (ImageView) findViewById(R.id.alert);
					alertImage.setVisibility(View.VISIBLE);
					tv.setText(Html.fromHtml("发现<font color=\"yellow\">"
							+ orders.size() + "</font>个订单"));
					Button logoutButton = (Button) findViewById(R.id.logout_button);
					logoutButton.setVisibility(View.GONE);
				}
				// get a reference to the list view
				RefreshAndShowOrders.this.orderListView = (ListView) findViewById(R.id.orderlist);

				// setup data adapter
				adapter = new ArrayAdapter<Order>(RefreshAndShowOrders.this,
						android.R.layout.simple_list_item_1, orders);

				// assign adapter to list view
				RefreshAndShowOrders.this.orderListView.setAdapter(adapter);

				// install handler
				RefreshAndShowOrders.this.orderListView
						.setOnItemClickListener(RefreshAndShowOrders.this);

				break;
			case 2: // error occurred
				Button logoutButton = (Button) findViewById(R.id.logout_button);
				logoutButton.setVisibility(View.GONE);
				RefreshAndShowOrders.this.myprogress.cancel();
				AlertDialog alert = new AlertDialog.Builder(
						RefreshAndShowOrders.this).create();
				alert.setMessage(msg.obj.toString());
				alert.setButton(DialogInterface.BUTTON_POSITIVE, "重试",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								refreshOrders();
							}
						});
				alert.setButton(DialogInterface.BUTTON_NEGATIVE, "放弃",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				alert.show();
				break;
			}
			// super.handleMessage(msg);
			Button refreshOrdersButton = (Button) findViewById(R.id.refresh_button);
			refreshOrdersButton.setVisibility(View.VISIBLE);
			refreshOrdersButton.getBackground().setColorFilter(0xFFFFD700,
					PorterDuff.Mode.MULTIPLY);
		}
	};

	private void logout() {
		myprogress = ProgressDialog.show(RefreshAndShowOrders.this, "正在退出登录",
				"请等待", true, false);
		new Thread() {
			public void run() {
				try {
					XMLRPCClient client = new XMLRPCClient(DataCenter.serverURL);
					Boolean success = (Boolean) client.call(
							"deliveryman.logout", DataCenter.deliverymanID);
					Message msg = new Message();
					msg.what = 0;
					msg.obj = success ? "退出登录成功" : "退出登录失败";
					RefreshAndShowOrders.this.logoutHandler.sendMessage(msg);
					msg = new Message();
					if (success) {
						msg.what = 1;
					} else {
						msg.what = 2;
						msg.obj = "退出登录失败";
					}
					RefreshAndShowOrders.this.logoutHandler.sendMessage(msg);
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = 2;
					msg.obj = e.getMessage();
					RefreshAndShowOrders.this.logoutHandler.sendMessage(msg);
				}
			}
		}.start();

	}

	private Handler logoutHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// process incoming messages here
			switch (msg.what) {
			case 0:
				// update progress bar
				RefreshAndShowOrders.this.myprogress.setMessage(""
						+ (String) msg.obj);
				break;
			case 1:
				RefreshAndShowOrders.this.myprogress.cancel();
				DataCenter.deliverymanID = -1;
				finish();
				break;
			case 2: // error occurred
				RefreshAndShowOrders.this.myprogress.cancel();
				AlertDialog alert = new AlertDialog.Builder(
						RefreshAndShowOrders.this).create();
				alert.setMessage(msg.obj.toString());
				alert.setButton(DialogInterface.BUTTON_POSITIVE, "重试",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								logout();
							}
						});
				alert.setButton(DialogInterface.BUTTON_NEGATIVE, "放弃",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				alert.show();
				break;
			}
			// super.handleMessage(msg);
		}
	};

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Order order = orders.get(position);
		DataCenter.currentOrder = order;
		timer.cancel();
		startActivityForResult(new Intent(this, ShowOrder.class),
				SHOW_ORDER_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		launchDownloadingOrders();
	}
	
	 public void onStart() {
         isCreated = true;
         super.onStart();
      } 

      public void onStop() {
         isCreated = false;
         super.onStop();
      }

}