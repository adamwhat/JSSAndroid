package com.jinshisong.route;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.LocationListener;
import com.jinshisong.R;
import com.jinshisong.baiduRoute;


import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

public class MyLocation {
	
	LocationListener mLocationListener = null;
	baiduRoute app;
	public boolean prepared;
	public double lng, lat;
	public MyLocation(baiduRoute _app)
	{
		app = _app;
		//app.getmBMapMan().start();
		prepared = false;
	}
}
