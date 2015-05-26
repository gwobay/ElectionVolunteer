package com.example.volunteerhandbook;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

	public class MapActivity extends Activity {
		public static final String LOCATION="location";
		
		GoogleMap map=null;
		static LatLng where=null;
        
		public static LatLng whereAmI(Activity mAv, String myAddress)
		{
	        Geocoder coder=new Geocoder(mAv, Locale.TAIWAN);	
	        if (Geocoder.isPresent())
	        {
	        try {
				List<Address> addr=coder.getFromLocationName(myAddress, 2);
				if (addr.size() > 0)
				{
					return new LatLng(addr.get(0).getLatitude(), addr.get(0).getLongitude());
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        } else {
	        	Log.d("NO CODER", "geocoder service not available");
	        }
	        return null;
		}

	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.map_activity);

	        Intent me=getIntent();
	        String location=me.getExtras().getString(LOCATION);
	        String fixKey=getResources().getString(R.string.fix_line_key);
	        String fixLine=me.getExtras().getString(fixKey);
	        
	        LatLng where=whereAmI(this, location);
	        
	        if (where==null)
	        	{
	        		finish();
	        		return;
	        	}
/*	        
	        String geoString="geo:"+where.latitude+","+where.longitude+"?q=";
	        geoString += fixLine.replace(" ", "%20").replace(", ", "%2C");//location.replace(" ", "%20").replace(", ", "%2C");
	        showMap(Uri.parse(geoString));
	        */
	        
	        try {
	        // Get a handle to the Map Fragment
	        map = ((MapFragment) getFragmentManager()
	                .findFragmentById(R.id.map)).getMap();
	        
	        if (map==null) GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

	        //LatLng taipei = new LatLng(25.046375, 121.520884);
	        map.setMyLocationEnabled(true);
	        
	        map.moveCamera(CameraUpdateFactory.newLatLngZoom(where, 15));

	       /* map.addMarker(new MarkerOptions()
	                .title("Taipei")
	                .snippet("The most populous city in Taiwan.")
	                .position(taipei));
	       */
	        } catch (NullPointerException e){
	        	return;
	        }
	        Marker mkr=map.addMarker(new MarkerOptions()
	                .title(fixLine)
	                .snippet(location)
	                .position(where));
	    
	    map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

	        // Use default InfoWindow frame
	        @Override
	        public View getInfoWindow(Marker arg0) {
	            return null;
	        }

	        // Defines the contents of the InfoWindow
	        @Override
	        public View getInfoContents(Marker mkr) {

	            View v = getLayoutInflater().inflate(R.layout.map_info, null);

	            //LatLng latLng = arg0.getPosition();

	            TextView xT = (TextView) v.findViewById(R.id.info_title);
	            String fixLine=mkr.getTitle();
	            String tt=fixLine;
	            String dd="";
	            int i0=fixLine.indexOf('(');
	            if (i0 > 0)
	            {
	            	xT.setText(fixLine.substring(0, i0));
	            	dd=fixLine.substring(i0+1, fixLine.length()-1);
	            } else xT.setText(fixLine);
	            // Getting reference to the TextView to set longitude
	            xT = (TextView) v.findViewById(R.id.info_date);
	            xT.setText(dd);
	            
	            xT = (TextView) v.findViewById(R.id.info_addr);
	            xT.setText(mkr.getSnippet());

	            return v;

	        }
	    });

	    // Adding and showing marker while touching the GoogleMap
	    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
	    	//in the future all markers will be put in set arraylist and recovered
	        @Override
	        public void onMapClick(LatLng arg0) {
	            // Clears any existing markers from the GoogleMap
	            //map.clear();

	            // Creating an instance of MarkerOptions to set position
	            //MarkerOptions markerOptions = new MarkerOptions();

	            // Setting position on the MarkerOptions
	            //markerOptions.position(where);

	            // Animating to the currently touched position
	            map.animateCamera(CameraUpdateFactory.newLatLng(arg0));

	            // Adding marker on the GoogleMap
	            //Marker marker = map.addMarker(markerOptions);

	            // Showing InfoWindow on the GoogleMap
	           // marker.showInfoWindow();

	        }
	        
	    });

        if (fixLine!=null) mkr.showInfoWindow();
        getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        // Respond to the action bar's Up/Home button
	        case android.R.id.home:
	            NavUtils.navigateUpFromSameTask(this);
	            return true;
	        }
	        return super.onOptionsItemSelected(item);
	    }
	    
	    static Menu mActionMenu=null;
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			getMenuInflater().inflate(R.menu.main, menu);
			mActionMenu=menu;
			for (int i=0; i<mActionMenu.size(); i++)
	    	{
	    		//if (mActionMenu.getItem(i).getItemId()==R.id.action_check ||
	    				//mActionMenu.getItem(i).getItemId()==R.id.action_join)
	    			//continue;
	    		mActionMenu.getItem(i).setVisible(false);
	    			//setOnMenuItemClickListener(aListener);
	    	}
			
			return true;
		}
		
	   
	    void showMap(Uri httpGeoLocation)
	    {
	    	Intent intent = new Intent(Intent.ACTION_VIEW);
	        intent.setData(httpGeoLocation);
	        if (intent.resolveActivity(getPackageManager()) != null) {
	            startActivity(intent);
	        }
	    }
}
