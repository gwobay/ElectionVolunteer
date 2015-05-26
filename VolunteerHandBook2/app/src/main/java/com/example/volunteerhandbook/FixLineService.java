/**
 * 
 */
package com.example.volunteerhandbook;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.kou.utilities.DaemonCallSocket;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * @author eric
 *
 */
public class FixLineService extends Service {

	static HashMap<String, String> myResources;
	static DaemonCallSocket mSocket=null;
	static long stopTime=0;
	/**
	 * 
	 */
	public FixLineService() {
		// TODO Auto-generated constructor stub
	}

	public interface Listener
	{
		public void readyToRead();
	}
	static HashMap<String, Vector<String> > dataInventory=new HashMap<String, Vector<String> >();
	static HashMap<String, FixLineService.Listener> listeners=new HashMap<String, FixLineService.Listener>();

	public class FixBinder extends Binder {
		
				public Vector<String> getAllData(String tag){
						return dataInventory.get(tag);
				}

				public void addDataReadyListener(String tag, FixLineService.Listener listener)
				{
					listeners.put(tag, listener);
				}

				public void iQuit(String tag)
				{
					listeners.remove(tag);
				}
				
				public void sendFixLine(String fixLine) 
				{
					stopTime=(new Date()).getTime()+10*60*1000;
					mSocket.putInstruction(fixLine);
				}
				
				public Vector<String> getFixLine(String tag)
				{
					return dataInventory.get(tag);
				}
	}
	
	static final String SERVER_NAME="SERVER_NAME";
	static final String SERVER_PORT="SERVER_PORT";
	
	private void readFromResourceFile(String fileName)
	{		  
	  BufferedReader reader=null;
	  try {
		  reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
	  
		  String aLine;
		  while ((aLine=reader.readLine())!=null)
		  {
			  int i0=0; while (aLine.charAt(i0)<= ' ') i0++;
			  int iE=i0; while (++iE<aLine.length() && aLine.charAt(iE) > ' ');
			  String key=aLine.substring(i0, iE).toUpperCase();
			  i0=iE; 
			  while (aLine.charAt(i0)!= '=' && ++i0 <aLine.length());
			  iE=++i0; 
			  while (++iE<aLine.length() && aLine.charAt(iE) > ' ');
			  String value=aLine.substring(i0, iE);//no case change .toUpperCase();
			  if (key.length() > 0)
				  myResources.put(key, value);
		  }
	    
	  } catch (IOException e) {
		  e.printStackTrace();
	    System.out.println("Could not read file "+fileName);
	  } finally {
	    try {
	      reader.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	System.out.println("Exception closing "+fileName);
	    }
	  }
	    
	}
	
	void  getMyResources()
	{		
    	String fileName=MainActivity.getFileHeader()+"ConnectionResources";
        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String host = sharedPref.getString(SERVER_NAME, "--");
    	 if (host.charAt(0) != '-')
    	 {
    		 String sPort = sharedPref.getString(SERVER_PORT, "--");
    		 myResources.put(SERVER_PORT, sPort);
    		 myResources.put(SERVER_NAME, host);
    	 }
    	 else
    	 {
    		 readFromResourceFile("ConnectionResources");
    	 }
	}

	//start my activity
	private class Looper extends Thread 
	{
		public void run()	
		{
			//wait 10 seconds for system to connect
			try {
				sleep(30*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				while (mSocket != null && !mSocket.isClosed())
				{
					byte[] dataB=mSocket.readStreamData();
					if (dataB == null) 
						{
							if ((new Date()).getTime() > stopTime) break;
							continue;
						}
					String data=new String(dataB);
					int ix0=data.indexOf("170=");
					int ix9=data.indexOf("|", ix0);
					if (ix9 < 0) ix9=data.length();
					String tableName=data.substring(ix0+4, ix9);
					Vector<String>  k=dataInventory.get(tableName);
					if (k==null) 
					{
						k=new Vector<String>();
					}
					k.add(data);
					dataInventory.put(tableName, k);
					
					FixLineService.Listener reader=listeners.get(tableName);
					if (reader != null)					
						reader.readyToRead();
						stopTime=(new Date()).getTime()+10*60*1000;
					
					//only idle for 10 mins
				}
			if (mSocket != null) 
			{
				mSocket.close();
				mSocket=null;
			}
		}
	}
	
	private class Starter extends Thread
	{
		public void run()
		{
			myResources=new HashMap<String, String>();
			/*getMyResources();
			if (myResources.size() < 1)
			{
				Log.i("FixLineService", "failed for no resources");
				 return ;
			}*/
			String  sPort="9779";//myResources.get(SERVER_PORT);
			String host="220.134.85.189";//myResources.get(SERVER_NAME);
			if (sPort == null || host == null)
			{
				Log.i("FixLineService", "failed for no resources");
				 return ;
			}
			
		try {
				mSocket=new DaemonCallSocket(host, Integer.parseInt(sPort));
				mSocket.setDelayReadTime(2000);
				new Thread(mSocket).start();				
				Looper aLooper=new Looper();
				aLooper.start();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return ;
		}
	}
	 @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
		 return START_STICKY;
		 
	  } 
	
	 @Override
	 public void onCreate() 
	 {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		        // Display a notification about us starting.  We put an icon in the status bar.
		showNotification();
		  
		stopTime=(new Date()).getTime()+10*60*1000;	
		if (mSocket != null)  return ;
		
		Starter aStarter=new Starter();
		aStarter.start();
		
	 return ;
	}
	
	public void onDestroy()
	{
		mSocket.stop();
		mNM.cancel(NOTIFICATION);
	}
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		//if (mSocket==null) return null;
		return new FixBinder();
	}

	private NotificationManager mNM;
	private int NOTIFICATION = 10;;
	

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        //.setLargeIcon(Bitmap.createBitmap(metrics,80, 80, Bitmap.Config.ARGB_4444))//
        .setSmallIcon(R.drawable.ic_action_open)//kp37)//ic_stat_gcm)
        .setContentTitle("service running")
        .setStyle(new NotificationCompat.BigTextStyle().bigText("test of service"))
        .setContentText("test of service")
        .setLights(0xFF0000, 2000, 5000)
        .setAutoCancel(true);

        Intent nullIntent=new Intent(this, AgendaActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, nullIntent, Intent.FLAG_ACTIVITY_NEW_TASK);//PendingIntent.FLAG_ONE_SHOT); //the intent to open when clicked
        
        mBuilder.setContentIntent(contentIntent); //so MainActivity will be opened when notification is clicked
        mNM.notify(NOTIFICATION, mBuilder.build());
        
    }
}
