package com.jinshisong;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.RouteOverlay;

public class baiduRoute extends com.baidu.mapapi.MapActivity {

	final String apiKey = "8FA96B5B8BB5C6B93CB332455766A774D7CAA01E";
	private BMapManager mBMapMan;
	private ProgressDialog myprogress;
	boolean routeDisplayed = false;
	double lat, lng;
	MapView mMapView;
	MyLocationOverlay mLocationOverlay = null;
	LocationListener mLocationListener = null;
	MKSearch mSearch = null;
	GeoPoint pt = null;

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return routeDisplayed;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baidu_route);

		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init(apiKey, new MKGeneralListener() {

			@Override
			public void onGetNetworkState(int iError) {
				Toast.makeText(getApplicationContext(), "您的网络出错啦！",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onGetPermissionState(int iError) {
				if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
					// 授权Key错误：
					Toast.makeText(getApplicationContext(),
							"请在BMapApiDemoApp.java文件输入正确的授权Key！",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (myprogress.isShowing())
					myprogress.dismiss();
				// 初始化搜索模块，注册事件监听
				mBMapMan.getLocationManager().removeUpdates(mLocationListener);
				mSearch = new MKSearch();
				mSearch.init(mBMapMan, new MKSearchListener() {

					@Override
					public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
						// TODO Auto-generated method stub
						Log.e("baiduRouteError", "ADD");
					}

					@Override
					public void onGetDrivingRouteResult(MKDrivingRouteResult res,
							int error) {
						// 错误号可参考MKEvent中的定义
						Log.e("baiduRouteError", new Integer(error).toString());
						
						// if (error != 0 || res == null) {
						// Toast.makeText(baiduRoute.this, "抱歉，未找到结果",
						// Toast.LENGTH_SHORT).show();
						// return;
						// }
						RouteOverlay routeOverlay = new RouteOverlay(baiduRoute.this,
								mMapView);
						// 此处仅展示一个方案作为示例
						routeOverlay.setData(res.getPlan(0).getRoute(0));
						mMapView.getOverlays().clear();
						mMapView.getOverlays().add(routeOverlay);
						mMapView.invalidate();

						mMapView.getController().animateTo(res.getStart().pt);
					}

					@Override
					public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
						// TODO Auto-generated method stub
						Log.e("baidu", "POI");

					}

					@Override
					public void onGetTransitRouteResult(MKTransitRouteResult arg0,
							int arg1) {
						// TODO Auto-generated method stub
						Log.e("baidu", "Trans");

					}

					@Override
					public void onGetWalkingRouteResult(MKWalkingRouteResult arg0,
							int arg1) {
						// TODO Auto-generated method stub
						Log.e("baidu", "Walk");

					}
				});
				Bundle bundle = getIntent().getExtras();
				boolean isRestaurant = bundle.getBoolean("isRestaurant");
				String name;
				double clat, clng;

				if (isRestaurant) {
					name = bundle.getString("Address");
					clat = bundle.getDouble("RLat");
					clng = bundle.getDouble("RLng");
				} else {
					name = bundle.getString("Address");
					clat = bundle.getDouble("CLat");
					clng = bundle.getDouble("CLng");
				}
				MKPlanNode stNode = new MKPlanNode();
				MKPlanNode enNode = new MKPlanNode();
				stNode.pt = pt;
				enNode.name = name;
				enNode.pt = new GeoPoint((int) (clat * 1E6), (int) (clng * 1E6));

				// app.mBMapMan.start();

				// this.myprogress = ProgressDialog.show(this, "正在获取路线", "请稍候", true,
				// false);
				Log.e("", "Map Started");
				mSearch.drivingSearch("北京", stNode, "北京", enNode);
				Log.e("", "Started");
				
			}
		};

		mLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					lng = location.getLongitude();
					lat = location.getLatitude();
					pt = new GeoPoint((int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					mMapView.getController().animateTo(pt);
					Log.e("Location Listener", "Changed");
					Log.e("Location", new Double(lng).toString());
					handler.sendEmptyMessage(0);
				}
			}
		};
		mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		
		// 如果使用地图SDK，请初始化地图Activity
		super.initMapActivity(mBMapMan);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(true);
		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		mMapView.setDrawOverlayWhenZooming(true);

		mLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMapView.getOverlays().add(mLocationOverlay);
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.enableCompass();
		mBMapMan.start();
		

		this.myprogress = ProgressDialog.show(this, "正在获取当前位置", "请稍候", true,
				false);

	}

	@Override
	public void onPause() {
		mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mLocationOverlay.disableMyLocation();
		mLocationOverlay.disableCompass();
		mBMapMan.stop();
		super.onPause();
	}

	@Override
	public void onResume() {
		mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.enableCompass(); // 打开指南针
		mBMapMan.start();
		super.onResume();
	}

}