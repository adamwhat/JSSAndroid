package com.jinshisong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        DataCenter.serverURL = getResources().getString(R.string.server_url);
        // setup handler to close the splash screen
        Handler x = new Handler();
        x.postDelayed(new splashhandler(), 2000);
    }
    
    class splashhandler implements Runnable {
        public void run() {
            // start new activity
        	
        	
//        	startService(new Intent(getApplication(), Cache.class));
            startActivity(new Intent(getApplication(), Deliveryman.class));
        	
       // 	startActivity(new Intent(getApplication(), baiduRoute.class));
        	
            // close out this activity
            finish();
        }
    }
}