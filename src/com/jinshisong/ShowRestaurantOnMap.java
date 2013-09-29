package com.jinshisong;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class ShowRestaurantOnMap extends MapActivity {
	GeoPoint p;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_map);
        
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        Restaurant restaurant = DataCenter.currentRestaurant;
        double latitude = restaurant.latitude, longitude = restaurant.longitude;
        
        TextView tv = (TextView) findViewById(R.id.title);
        tv.setText(restaurant.address);
        
        MapController mc = mapView.getController();
        
        p = new GeoPoint(
            (int) (latitude * 1E6), 
            (int) (longitude * 1E6));
 
        mc.animateTo(p);
        mc.setZoom(17); 
        
        Overlay overlay = new RestaurantOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(overlay);        
 
        mapView.invalidate();
	}

    @Override
    protected boolean isRouteDisplayed() { return false; }
    
    class RestaurantOverlay extends com.google.android.maps.Overlay {
        @Override
        public boolean draw(Canvas canvas, MapView mapView, 
        boolean shadow, long when) 
        {
            super.draw(canvas, mapView, shadow);                   
 
            //---translate the GeoPoint to screen pixels---
            Point screenPts = new Point();
            mapView.getProjection().toPixels(p, screenPts);
 
            //---add the marker---
            Bitmap bmp = BitmapFactory.decodeResource(
                getResources(), R.drawable.pushpin);            
            canvas.drawBitmap(bmp, screenPts.x-10, screenPts.y-34, null);         
            return true;
        }
    } 

}
