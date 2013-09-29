package com.jinshisong;

import java.util.TimerTask;

import org.xmlrpc.android.XMLRPCClient;

import android.graphics.PorterDuff;
import android.util.Log;
import android.widget.Button;

public class myTask extends TimerTask {
	private static boolean isRunning = false;
	private Record record;

	myTask(Record record) {
		this.record = record;
	}

	public void run() {

		if (!isRunning) {
			isRunning = true;
			// context.log("开始执行任务");
			doTask();
			isRunning = false;
			// context.log("任务执行结束");
		} 
	}
	private void doTask() {
		// 需要执行的动作
			XMLRPCClient client = new XMLRPCClient(record.serverURL);
			if (record.currentOrder.status == record.value)
			{
				super.cancel();
				return;
			}
			try {
				boolean success = (Boolean) client.call(record.method,
						record.currentOrder.id, record.value);

				if (success) {
					record.currentOrder.status = record.value;
					/*
					if (record.currentOrder.status.equals(DataCenter.DISH_FETCHED_STATUS))
					{
						Button bproductinfo = (Button) findViewById(R.id.dishinfo);
						Button completeAndRateButton = (Button) findViewById(R.id.closeorder);
						bproductinfo.setText("第二步：取餐成功");
						bproductinfo.setEnabled(true);
						completeAndRateButton.setEnabled(true);
						completeAndRateButton.getBackground().setColorFilter(0xFFFFD700,
								PorterDuff.Mode.MULTIPLY);
					}
					*/				
				}
			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
		}

}