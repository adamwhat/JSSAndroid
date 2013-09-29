package com.jinshisong;

import java.util.Timer;

import org.xmlrpc.android.XMLRPCClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

public class FivestarRating extends Activity {
	private ProgressDialog myprogress;
	private RatingBar rb;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.five_star_rating);

		final TextView tv = (TextView) findViewById(R.id.rating);

		rb = (RatingBar) findViewById(R.id.ratingbar);
		//test

		rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				tv.setText(String.valueOf(rb.getRating()));
			}
		});

		Button ratingButton = (Button) findViewById(R.id.ratingbutton);
		if (DataCenter.currentOrder.status
				.equals(DataCenter.DISH_FETCHED_STATUS)) {
			ratingButton.setEnabled(true);
			ratingButton.getBackground().setColorFilter(0xFFFFD700,
					PorterDuff.Mode.MULTIPLY);
			ratingButton.setOnClickListener(new Button.OnClickListener() {

				public void onClick(View v) {
					upload();
				}
			});
		} else {
			ratingButton.setEnabled(false);
		}
	}

	private void upload() {
		myprogress = ProgressDialog.show(FivestarRating.this, "正在向服务器上传", "请等待", true, false);

		new Thread() {
			public void run() {
				XMLRPCClient client = new XMLRPCClient(DataCenter.serverURL);
				Message msg = new Message();
				msg.what = 0;
				msg.obj = ("正在上传状态...");
				FivestarRating.this.progresshandler.sendMessage(msg);
				try {
					Boolean success = (Boolean) client.call("order.update",
							DataCenter.currentOrder.id,
							DataCenter.COMPLETE_STATUS, rb.getRating());
					msg = new Message();
					msg.what = 0;
					msg.obj = success ? "上传成功" : "上传失败";
					FivestarRating.this.progresshandler.sendMessage(msg);
					msg = new Message();
					if (success) {
						msg.what = 1;
					} else {
						msg.what = 2;
						msg.obj = "上传失败";
					}
					FivestarRating.this.progresshandler.sendMessage(msg);
				} catch (Exception e) {
					
					msg = new Message();
					msg.what = 2;
					msg.obj = e.getMessage();
					FivestarRating.this.progresshandler.sendMessage(msg);
				}
			}
		}.start();
	}
	
	private Handler progresshandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// process incoming messages here
			switch (msg.what) {
			case 0:
				// update progress bar
				FivestarRating.this.myprogress
						.setMessage("" + (String) msg.obj);
				break;
			case 1:
				FivestarRating.this.myprogress.cancel();
				DataCenter.currentOrder.status = DataCenter.COMPLETE_STATUS;
				setResult(ShowOrder.SUCESS_RESULT_CODE);
				finish();
				break;
			case 2: // error occurred
				FivestarRating.this.myprogress.cancel();
				
				//add a record into the list
				Record record = new Record(DataCenter.serverURL, "order.update", DataCenter.currentOrder,
						DataCenter.COMPLETE_STATUS);
				Timer timer = new Timer();
				myTask task = new myTask(record);
				timer.schedule(task, 1000,5000);
				setResult(ShowOrder.SUCESS_RESULT_CODE);
				finish();
				/*
				Intent intent = new Intent(FivestarRating.this, Cache.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("Record", record);
				record.setActionListener(new cacheComplete() {
					
					@Override
					public void actionAfterCompletion(Record record) {
						//send SMS;
						
					}
				});
				intent.putExtras(bundle);
				
				startService(intent);
				*/
								
				/*
				FivestarRating.this.myprogress.cancel();
				AlertDialog alert = new AlertDialog.Builder(FivestarRating.this)
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
