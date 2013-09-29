package com.jinshisong;

import org.xmlrpc.android.XMLRPCClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ShowSettings extends Activity {
	private ProgressDialog myprogress;
	private int deliverymanID;
    private Handler progresshandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // process incoming messages here
        	if(msg.obj == null) msg.obj = "WTF";
            switch (msg.what) {
                case 0:
                    // update progress bar
                	ShowSettings.this.myprogress.setMessage("" + (String) msg.obj);
                    break;
                case 1:
                	ShowSettings.this.myprogress.cancel();
                	DataCenter.deliverymanID = (Integer)msg.obj;
                	finish();
                	break;
                case 2: // error occurred
                	ShowSettings.this.myprogress.cancel();
                	AlertDialog alert = new AlertDialog.Builder(ShowSettings.this).create();
                    alert.setMessage(msg.obj.toString());
                    alert.setButton(DialogInterface.BUTTON_POSITIVE, "重试", new DialogInterface.OnClickListener() {
    					
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						login();
    					}
    				});
                    alert.setButton(DialogInterface.BUTTON_NEGATIVE, "放弃", new DialogInterface.OnClickListener() {
    					
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						finish();
    					}
    				});
                    alert.show();
                    break;
            }
            // super.handleMessage(msg);
        }
    };
    
	private AlertDialog.Builder adb;// = new AlertDialog.Builder(this);

	private void login() {
		String deliverymanText = ((EditText) findViewById(R.id.deliveryman)).getText().toString();
		String passwordText = ((EditText) findViewById(R.id.password)).getText().toString();
    	
    	if (!deliverymanText.matches("\\d+")) {

            AlertDialog ad = ShowSettings.this.adb.create();
            ad.setMessage("请输入正确的编号");
            ad.show();
            return;
        }
    	/*
    	if (!passwordText.matches("11")){

            AlertDialog ad = ShowSettings.this.adb.create();
            ad.setMessage("密码不正确，请重新输入");
            ad.show();
            return;
        }
    	*/
    	myprogress = ProgressDialog.show(ShowSettings.this, "正在登录", "请等待", true, false);
    	deliverymanID = Integer.parseInt(deliverymanText);
    	new Thread(){
    		public void run(){
    			
    	    	Message msg = new Message();
    	        try {
    	        	XMLRPCClient client = new XMLRPCClient(DataCenter.serverURL);
    	        	
    	        	Boolean success = (Boolean) client.call("deliveryman.login", deliverymanID);
    	        	msg = new Message();
    	        	msg.what = 0;
    	        	msg.obj = success ? "登录成功" : "登录失败";
    	        	ShowSettings.this.progresshandler.sendMessage(msg);
    	        	msg = new Message();
    	        	if (success) {
    	        		msg.what = 1;
    	        		msg.obj = deliverymanID;
    	        	} else {
    	        		msg.what = 2;
    	        		msg.obj = "登录失败";
    	        	}
    	        	ShowSettings.this.progresshandler.sendMessage(msg);
    	        } catch (Exception e) {
    	        	msg = new Message();
    	        	msg.what = 2;
    	        	msg.obj = e.getMessage();
    	        	ShowSettings.this.progresshandler.sendMessage(msg);
    	        }
    		};
    	}.start();
    	
    }
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.show_settings);
        
        // load screen
        PopulateScreen();

        this.adb = new AlertDialog.Builder(this);

        final Button loginButton = (Button) findViewById(R.id.login);
        loginButton.requestFocus();
        loginButton.getBackground().setColorFilter(0xFFFFD700, PorterDuff.Mode.MULTIPLY);
        
        // create anonymous click listener to handle the "save"
        loginButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {
            	login();
            }
        });
    }

    private void PopulateScreen() {
        final EditText deliverymanfield = (EditText) findViewById(R.id.deliveryman);
        String deliverymanID = DataCenter.deliverymanID < 0 ? "" : String.valueOf(DataCenter.deliverymanID);
        deliverymanfield.setText(deliverymanID);
    }
}
