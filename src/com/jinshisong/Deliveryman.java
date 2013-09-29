package com.jinshisong;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jinshisong.track.LocationService;

public class Deliveryman extends Activity {
	public static final int ACTIVITY_MYORDERS = 1;
	public static final int ACTIVITY_SETTINGS = 2;
	private static final int DIALOG_SETTING_ID = 10;
	
	private Button refreshOrdersButton;
	private Button settingsButton;
	private Intent mServiceIntent;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
	}

	
    @Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.deliveryman);
		
		LocationManager mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showDialog(DIALOG_SETTING_ID);
        }else{
        	mServiceIntent = new Intent(Deliveryman.this,LocationService.class);
    		
    		// refresh orders
    		refreshOrdersButton = (Button) findViewById(R.id.myorders);
    		// settings
    		settingsButton = (Button) findViewById(R.id.settings);

    		// see if we have a deliveryman ID set yet....
    		refreshUserInfo();

    		settingsButton.setOnClickListener(new Button.OnClickListener() {

    			public void onClick(View v) {
    				startActivityForResult(new Intent(v.getContext(),
    						ShowSettings.class), ACTIVITY_SETTINGS);
    			}
    		});
    		refreshOrdersButton.setOnClickListener(new Button.OnClickListener() {

    			public void onClick(View v) {
    				startActivityForResult(new Intent(v.getContext(),
    						RefreshAndShowOrders.class), ACTIVITY_MYORDERS);
    			}
    		});
        }
    }


	protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = null;
        switch(id) {
        case DIALOG_SETTING_ID:
            builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.setting_dialog_message))
                   .setCancelable(false)
                   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                       }
                   })
                   .setNegativeButton("No", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           Deliveryman.this.finish();
                       }
                   });
            dialog = builder.create();
            break;
        default:
            dialog = null;
        }
        return dialog;
    }

    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_MYORDERS:
		case ACTIVITY_SETTINGS:
			refreshUserInfo();
			break;
		}
	}

	private void refreshUserInfo() {
		final TextView deliverymanLabel = (TextView) findViewById(R.id.deliverymanlabel);
		int deliverymanID = -1;
		if ((deliverymanID = DataCenter.deliverymanID) > 0) {
			refreshOrdersButton.setEnabled(true);
			refreshOrdersButton.requestFocus();
			refreshOrdersButton.getBackground().setColorFilter(0xFFFFD700,
					PorterDuff.Mode.MULTIPLY);
			settingsButton.getBackground().setColorFilter(null);
			deliverymanLabel.setText(Html
					.fromHtml("欢迎你，第<font color=\"yellow\">" + deliverymanID
							+ "</font>号送餐员"));
			
            startService(mServiceIntent);
		} else {
			refreshOrdersButton.setEnabled(false);
			settingsButton.requestFocus();
			settingsButton.getBackground().setColorFilter(0xFFFFD700,
					PorterDuff.Mode.MULTIPLY);
			refreshOrdersButton.getBackground().setColorFilter(null);
			deliverymanLabel.setText("未知送餐员，请登录");
			Intent mServiceIntent = new Intent(Deliveryman.this,LocationService.class);
            stopService(mServiceIntent);
		}
	}
}
