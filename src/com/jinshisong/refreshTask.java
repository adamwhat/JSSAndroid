package com.jinshisong;

import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;

public class refreshTask extends TimerTask{

	RefreshAndShowOrders r;
	int orderNum;
	public refreshTask(RefreshAndShowOrders r)
	{
		this.r = r;
		orderNum = 0;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//Looper.prepare(); 
		if(!r.isRunning)
			new Thread(){
				public void run()
				{
					Looper.prepare();
					if(!r.isRunning && r.isCreated)
					{
						
						r.launchDownloadingOrders();
						Log.e("REFRESH","Success");
						if(r.orders.size() > orderNum)
						{
							//vibrate
							Vibrator vibrator = (Vibrator)r.getSystemService(Context.VIBRATOR_SERVICE);   
					       // long[] pattern = {800, 50, 400, 30}; // OFF/ON/OFF/ON...  
					        vibrator.vibrate(1500);//-1不重复，非-1为从pattern的指定下标开始重复
						}
						orderNum = r.orders.size();
					}
					Looper.loop();
				}
			}.start();
		
		//Looper.loop();
	}

}
