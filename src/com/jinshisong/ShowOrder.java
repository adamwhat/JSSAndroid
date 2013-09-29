package com.jinshisong;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract.Contacts.Data;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShowOrder extends Activity {
	private static final int SHOW_DISHES_REQUEST_CODE = 1;
	private static final int RATING_RQUEST_CODE = 2;

	public static final int SUCESS_RESULT_CODE = 1;
	public static final int FAILURE_RESULT_CODE = 0;

	private CookingTimeCountDown countDown;
	private TextView countDownTextView;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.show_order);

		Order currentOrder = DataCenter.currentOrder;

		countDownTextView = (TextView) findViewById(R.id.countdown);
		long initialCountDown = DataCenter.currentOrder.fetchDeadLine
				- System.currentTimeMillis();
		if (initialCountDown > 0) {
			countDown = new CookingTimeCountDown(initialCountDown, 1000);
			countDown.start();
		} else {
			countDownTextView.setText("烹调结束时间已过!");
		}

		TextView orderIdTV = (TextView) findViewById(R.id.orderid);
		orderIdTV.setText("订单编号: " + currentOrder.id);

		DataCenter.currentRestaurant = DataCenter.restaurants
				.get(currentOrder.restaurantID);

		TextView restaurantTitleTV = (TextView) findViewById(R.id.restaurant_name);
		restaurantTitleTV.setText(DataCenter.currentRestaurant.title);

		TextView restaurantAddressTV = (TextView) findViewById(R.id.restaurant_address);
		restaurantAddressTV.setText(DataCenter.currentRestaurant.address);

		Button restaurantMapButton = (Button) findViewById(R.id.maprestaurant);
		if (DataCenter.currentRestaurant.latitude > 0
				&& DataCenter.currentRestaurant.longitude > 0) {
			restaurantMapButton
					.setOnClickListener(new Button.OnClickListener() {

						public void onClick(View v) {
							Intent intent = new Intent(ShowOrder.this, baiduRoute.class);
							Bundle bundle = new Bundle();
							bundle.putString("Address", DataCenter.currentRestaurant.address);
							bundle.putDouble("RLat", DataCenter.currentRestaurant.latitude);
							bundle.putDouble("RLng", DataCenter.currentRestaurant.longitude);
							bundle.putBoolean("isRestaurant", true);
							intent.putExtras(bundle);
							startActivity(intent);
						}
					});
		} else {
			restaurantMapButton.setEnabled(false);
		}

		TextView customerDetailTV = (TextView) findViewById(R.id.customerdetails);
		customerDetailTV.setText(DataCenter.currentOrder.getCustomerDetail());

		TextView totalPriceTV = (TextView) findViewById(R.id.price);
		totalPriceTV.setText(Html.fromHtml(DataCenter.currentOrder
				.getHtmlTotalPrice()));

		Button customerCallButton = (Button) findViewById(R.id.callcustomer);
		customerCallButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				String customerPhoneNumber = DataCenter.currentOrder.customerPhoneNumber;
				try {
					Intent geoIntent = new Intent(Intent.ACTION_DIAL, Uri
							.parse("tel:" + customerPhoneNumber));
					startActivity(geoIntent);
				} catch (Exception e) {
					AlertDialog alert = new AlertDialog.Builder(ShowOrder.this)
							.create();
					alert.setMessage(e.getMessage());
					alert.show();
				}
			}
		});

		Button customerMapButton = (Button) findViewById(R.id.customermap);
		if (currentOrder.customerLatitude > 0
				&& currentOrder.customerLongitude > 0) {
			customerMapButton.setOnClickListener(new Button.OnClickListener() {

				public void onClick(View v) {
					startActivity(new Intent(ShowOrder.this,
							ShowCustomerOnMap.class));
				}
			});
		} else {
			customerMapButton.setEnabled(false);
		}

		Button routeButton = (Button) findViewById(R.id.delivery_route);
		if (currentOrder.customerLatitude > 0
				&& currentOrder.customerLongitude > 0) {
			routeButton.setOnClickListener(new Button.OnClickListener() {

				public void onClick(View v) {
					startActivity(new Intent(ShowOrder.this, ShowRoute.class));
				}
			});
		} else {
			routeButton.setEnabled(false);
		}

		adjustToCurrentStatus();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case SHOW_DISHES_REQUEST_CODE:
		case RATING_RQUEST_CODE:
			adjustToCurrentStatus();
		}
	}

	private void adjustToCurrentStatus() {
		Order currentOrder = DataCenter.currentOrder;

		TextView orderTV = (TextView) findViewById(R.id.orderdetails);
		orderTV.setText(Html.fromHtml(currentOrder.getHtmlOrderDetail()));

		TextView commentTV = (TextView) findViewById(R.id.ordercomment);
		if (currentOrder.comment != null) {
			commentTV.setText(Html.fromHtml("备注：<font color='red'>"
					+ currentOrder.comment + "</font>"));
			commentTV.setVisibility(View.VISIBLE);
		} else {
			commentTV.setVisibility(View.GONE);
		}

		Button bproductinfo = (Button) findViewById(R.id.dishinfo);
		Button completeAndRateButton = (Button) findViewById(R.id.closeorder);

		String currentStatus = currentOrder.status;
		if (currentStatus.equals(DataCenter.INITIAL_STATUS)) {
			bproductinfo.setText("第一步：核对菜品和酒水");
			bproductinfo.setEnabled(true);
			bproductinfo.getBackground().setColorFilter(0xFFFFD700,
					PorterDuff.Mode.MULTIPLY);
			completeAndRateButton.setEnabled(false);
		} else if (currentStatus.equals(DataCenter.DISH_FETCHED_STATUS)) {
			bproductinfo.setText("第二步：取餐成功");
			bproductinfo.setEnabled(true);
			completeAndRateButton.setEnabled(true);
			completeAndRateButton.getBackground().setColorFilter(0xFFFFD700,
					PorterDuff.Mode.MULTIPLY);
		} else if (currentStatus.equals(DataCenter.COMPLETE_STATUS)) {
			bproductinfo.setText("第二步：取餐成功");
			bproductinfo.setEnabled(true);
			completeAndRateButton.setEnabled(false);
		}

		if (bproductinfo.isEnabled()) {
			bproductinfo.setOnClickListener(new Button.OnClickListener() {

				public void onClick(View v) {
					try {
						startActivityForResult(new Intent(ShowOrder.this,
								ShowDishes.class), SHOW_DISHES_REQUEST_CODE);
					} catch (Exception e) {
						AlertDialog alert = new AlertDialog.Builder(
								ShowOrder.this).create();
						alert.setMessage(e.getMessage());
						alert.show();
					}
				}
			});
		}

		if (completeAndRateButton.isEnabled()) {
			completeAndRateButton
					.setOnClickListener(new Button.OnClickListener() {

						public void onClick(View v) {
							try {
								startActivityForResult(new Intent(
										ShowOrder.this, FivestarRating.class),
										RATING_RQUEST_CODE);
							} catch (Exception e) {
								AlertDialog alert = new AlertDialog.Builder(
										ShowOrder.this).create();
								alert.setMessage(e.getMessage());
								alert.show();
							}
						}
					});
		}
	}

	private class CookingTimeCountDown extends CountDownTimer {

		public CookingTimeCountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			countDownTextView.setText("烹调结束时间已到!");

		}

		@Override
		public void onTick(long millisUntilFinished) {
			long seconds = millisUntilFinished / 1000;
			long minutes = seconds / 60;
			seconds = seconds - minutes * 60;
			countDownTextView.setText("烹调结束时间: " + minutes + "分" + seconds
					+ "秒");

		}

	}

}