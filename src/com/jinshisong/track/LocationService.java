package com.jinshisong.track;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.xmlrpc.android.XMLRPCClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.jinshisong.DataCenter;
import com.jinshisong.Deliveryman;
import com.jinshisong.R;

public class LocationService extends Service {
	private final String TAG = "LocationService";
	private final boolean DEBUG = true;
	public static final int ONGOING_NOTIFICATION = 1000;
	private static final int NETWORK_ERROR_NOTIFICATION_ID = 1;
    private static final int MSG_EVENT_START_GET_LOCATION = 101;
    private static final int MSG_EVENT_STOP_GET_LOCATION = 102;
    private static final int MSG_EVENT_UPLOAD_LOCATION = 103;
    
    private final int FIVE_SECONDS = 5*1000;
    private final int TEN_SECONDS = 10*1000;
    private final int HALF_MINUTE = 30*1000;
    private final int ONE_MINUTE = 60*1000;
    private final int TWO_MINUTES = 2*60*1000;
    //private final int FOUE_AND_HALF_MINUTES = 9*30*1000;
	
	private ServiceHandler mServiceHandler;
    
    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();
    private ActivityCallback mCallback;
    private LocationManager mLocationManager;
    private Location mCurrentBestLocation = null;
    private Object mCurrentBestLocationSynchronizedObj = new Object();
    private PowerManager.WakeLock mWakeLock;
    private int mUploadFailedTimes = 0;
    private JinShiSongLocationListenter mGPSLocationListener;
    private JinShiSongLocationListenter mNetWorkLocationListener;
    private NotificationManager mNotificationManager;
    private String mMessage;
    private long mGetLocationStartedTime = 0;

    @Override
    public void onCreate() {
    	if(DEBUG){
    		Log.d(TAG, "onCreate in");
    	}
    	mMessage = getString(R.string.track_not_started);
        HandlerThread thread = new HandlerThread("LocationServiceThread");
        thread.start();
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mGPSLocationListener = new JinShiSongLocationListenter();
        mNetWorkLocationListener = new JinShiSongLocationListenter();
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
        
        Notification notification = new Notification(R.drawable.icon, "JinShiSong Tracking",
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, Deliveryman.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, "JinShiSong Tracking",
                "JinShiSong is tracking the location", pendingIntent);
        startForeground(ONGOING_NOTIFICATION, notification);

        mServiceHandler.sendEmptyMessage(MSG_EVENT_START_GET_LOCATION);
        
        super.onCreate();
        if(DEBUG){
    		Log.d(TAG, "onCreate out");
    	}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
	public void onDestroy() {
    	if(DEBUG){
    		Log.d(TAG, "onDestroy in");
    	}
    	if(mLocationManager!=null){
			mLocationManager.removeUpdates(mGPSLocationListener);
			//mLocationManager.removeUpdates(mNetWorkLocationListener);
        }
    	mWakeLock.release();
		super.onDestroy();
		if(DEBUG){
    		Log.d(TAG, "onDestroy out");
    	}
	}

	public void registerCallback(ActivityCallback callback){
    	mCallback = callback;
    }

    public void unRegisterCallback(ActivityCallback callback){
    	mCallback = null;
    }

    
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
    	LocationService getService() {
            return LocationService.this;
        }
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	if(DEBUG){
        		Log.d(TAG, "handleMessage");
        	}
            switch(msg.what){
	            case MSG_EVENT_START_GET_LOCATION:
	            	if(DEBUG){
	            		Log.d(TAG, "handleMessage:MSG_EVENT_START_GET_LOCATION");
	            	}
	            	// Register the listener with the Location Manager to receive
	    			// location updates
	    			if (mLocationManager != null) {
	    				if(DEBUG){
	    					Log.d(TAG, "mLocationManager != null");
	    				}
	    				
	    				if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
    						if(DEBUG){
    							Log.d(TAG, "GPS_PROVIDER enabled");
    						}
	    					
    						mLocationManager.requestLocationUpdates(
    	    						LocationManager.GPS_PROVIDER, 0, 0, mGPSLocationListener);
    					}else{
    						if(DEBUG){
    							Log.d(TAG, "GPS_PROVIDER disabled");
    						}
    					}
	    				
	    				if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
    						if(DEBUG){
    							Log.d(TAG, "NETWORK_PROVIDER enabled");
    						}
    						/*mLocationManager.requestLocationUpdates(
    	    						LocationManager.NETWORK_PROVIDER, 0, 0,
    	    						mNetWorkLocationListener);*/
    					}else{
    						if(DEBUG){
    							Log.d(TAG, "NETWORK_PROVIDER disabled");
    						}
    					}
	    			}else{
	    				if(DEBUG){
	    					Log.d(TAG, "mLocationManager == null");
	    				}
	    			}
	    			
	    			mGetLocationStartedTime = System.currentTimeMillis();
	    			sendEmptyMessageDelayed(MSG_EVENT_STOP_GET_LOCATION, TEN_SECONDS);
	            	break;
	            case MSG_EVENT_STOP_GET_LOCATION:
	            	if(DEBUG){
	            		Log.d(TAG, "handleMessage:MSG_EVENT_STOP_GET_LOCATION");
	            	}
    					
	            	if(mCurrentBestLocation==null){
	            		sendEmptyMessageDelayed(MSG_EVENT_STOP_GET_LOCATION, TEN_SECONDS);
	            	}else{
	            		// Unregister the listener with the Location Manager to receive location updates
		    			if(mLocationManager!=null){
		    				mLocationManager.removeUpdates(mGPSLocationListener);
		    				//mLocationManager.removeUpdates(mNetWorkLocationListener);
		    	        }
		    			sendEmptyMessage(MSG_EVENT_UPLOAD_LOCATION);
	            	}
	    			break;
	            case MSG_EVENT_UPLOAD_LOCATION:
	            	if(DEBUG){
	            		Log.d(TAG, "handleMessage:MSG_EVENT_UPLOAD_LOCATION");
	            	}
	            	
	    			boolean success = false;
	    			if(mCurrentBestLocation!=null){
	    				if(DEBUG){
		            		Log.d(TAG, "mCurrentBestLocation!=null");
		            		Log.d(TAG, String.valueOf(mCurrentBestLocation.getLatitude()));
		            		Log.d(TAG, String.valueOf(mCurrentBestLocation.getLongitude()));
		            	}
	    				success = updateLocation();
	    				mCurrentBestLocation = null;
	    			}else{
	    				if(DEBUG){
	    					Log.d(TAG, "mCurrentBestLocation==null");
	    				}
	    			}
	    			
	    			if(success){
	    				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
	    				String curtime = formatter.format(curDate);   
	                	mMessage = getString(R.string.last_update_time)+curtime;
	                	if(mCallback!=null){
	                		mCallback.success(mMessage);
	                	}
	                	if(DEBUG){
	    					Log.d(TAG, "updateLocation success!");
	    				}
	                	
	    				mUploadFailedTimes = 0;
	    				mNotificationManager.cancel(NETWORK_ERROR_NOTIFICATION_ID);
	    				
	    				long currentTimeMillis = System.currentTimeMillis();
	    				if(currentTimeMillis - mGetLocationStartedTime > TWO_MINUTES){
	    					if(DEBUG){
		    					Log.d(TAG, "currentTimeMillis - mGetLocationStartedTime > TWO_MINUTES");
		    				}
	    					sendEmptyMessage(MSG_EVENT_START_GET_LOCATION);
	    				}else{
	    					if(DEBUG){
		    					Log.d(TAG, "currentTimeMillis - mGetLocationStartedTime < TWO_MINUTES");
		    				}
	    					sendEmptyMessageDelayed(MSG_EVENT_START_GET_LOCATION, TWO_MINUTES - (currentTimeMillis - mGetLocationStartedTime));
	    				}
	    			}else{
	    				if(mCurrentBestLocation!=null){
	    					mMessage = getString(R.string.network_error);
		                	if(mCallback!=null){
		                		mCallback.success(mMessage);
		                	}
		    				mUploadFailedTimes++;
		    				if(DEBUG){
		    					Log.d(TAG, "updateLocation failed "+mUploadFailedTimes+" times");
		    				}
		    				
		    				if(mUploadFailedTimes>5){
		    					sendNetwworkErrorNotification();
		    				}
	    				
	    				}else{
	    					if(DEBUG){
		    					Log.d(TAG, "get location failed ");
	    					}
	    				}
	    				
	    				sendEmptyMessage(MSG_EVENT_START_GET_LOCATION);
	    				/*if(mUploadFailedTimes%5==0){
	    					sendEmptyMessage(MSG_EVENT_START_GET_LOCATION);
	    				}else{
	    					sendEmptyMessageDelayed(MSG_EVENT_UPLOAD_LOCATION, ONE_MINUTES);
	    				}*/
	    			}
	            	break;
	            default:
	            	break;

            }
        }
    }

    private boolean updateLocation(){
    	Boolean success = false;
        try {
        	XMLRPCClient client = new XMLRPCClient(DataCenter.serverURL);
        	
			if (DataCenter.deliverymanID > 0) {
				client.call("deliveryman.gps",
						DataCenter.deliverymanID, mCurrentBestLocation
								.getLatitude()+0.001, mCurrentBestLocation
								.getLongitude()+0.0058);
				success = true;
			}
        	
        } catch (Exception e) {
        	Log.d(TAG, e.getMessage());
        	e.printStackTrace();
        }
        
        return success;
    }
    
    private void sendNetwworkErrorNotification(){
    	
    	int icon = R.drawable.icon;
    	CharSequence tickerText = getString(R.string.network_error_notification_title);
    	long when = System.currentTimeMillis();

    	Notification notification = new Notification(icon, tickerText, when);
    	Context context = getApplicationContext();
    	CharSequence contentTitle = getString(R.string.network_error_notification_title);
    	CharSequence contentText = getString(R.string.network_error_notification_content);
    	
    	Intent notificationIntent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
    	//Intent notificationIntent = new Intent(this, JinShiSongActivity.class);
    	//notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    	notification.flags = Notification.FLAG_AUTO_CANCEL;
    	
    	mNotificationManager.notify(NETWORK_ERROR_NOTIFICATION_ID, notification);
    }
    

    // Define a listener that responds to location updates
    private class JinShiSongLocationListenter implements LocationListener {

        public void onLocationChanged(Location location) {
        	if(DEBUG){
        		Log.d(TAG, "locationListener.onLocationChanged");
        		if(location==null){
        			Log.d(TAG, "location==null");
        		}else{
        			Log.d(TAG, "location!=null");
        		}
        	}
        	
            // Called when a new location is found by the network location provider.
        	synchronized(mCurrentBestLocationSynchronizedObj){
        		if (isBetterLocation(location, mCurrentBestLocation)) {
                	if(DEBUG){
                		Log.d(TAG, "A better location set");
                	}
                	mCurrentBestLocation = location;
                }else{
                	if(DEBUG){
                		Log.d(TAG, "Use old location");
                	}
                }
        	}
        }

		public void onStatusChanged(String provider, int status, Bundle extras) {
        	if(DEBUG){
        		Log.d(TAG, "onStatusChanged: provider"+provider+"status="+status);
        	}
        }

        public void onProviderEnabled(String provider) {
        	if(DEBUG){
        		Log.d(TAG, "onProviderEnabled: provider"+provider);
        	}
        }

        public void onProviderDisabled(String provider) {
        	if(DEBUG){
        		Log.d(TAG, "onProviderDisabled: provider"+provider);
        	}
        }

        /**
         * Determines whether one Location reading is better than the current
         * Location fix
         *
         * @param location The new Location that you want to evaluate
         * @param currentBestLocation The current Location fix, to which you
         *            want to compare the new one
         */
        protected boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }
            
            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location,
            // use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must
                // be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and
            // accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
            return false;
        }

        /** Checks whether two providers are the same */
        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            }
            return provider1.equals(provider2);
        }
    }
}
