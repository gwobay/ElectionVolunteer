package com.example.volunteerhandbook;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.kou.utilities.DaemonCallSocket;

public class FixLineTranslator extends Thread {

	static FixLineTranslator mInstance=null;
	private static int iCount=0; //only one instance, who call this will get a binder back
									//if iCount > 0
									//
	private FixLineTranslator() {
		super();		
	}

	static public FixLineTranslator getInstance(Activity av)
	{
		if (mInstance==null) 
			{
				mInstance=new FixLineTranslator();
				mInstance.start();
				mStarter=av;
			}
		return mInstance;
	}
	
	public FixLineTranslator(Runnable runnable) {
		super(runnable);
		// TODO Auto-generated constructor stub
	}

	public FixLineTranslator(String threadName) {
		super(threadName);
		// TODO Auto-generated constructor stub
	}

	public FixLineTranslator(Runnable runnable, String threadName) {
		super(runnable, threadName);
		// TODO Auto-generated constructor stub
	}

	public FixLineTranslator(ThreadGroup group, Runnable runnable) {
		super(group, runnable);
		// TODO Auto-generated constructor stub
	}

	public FixLineTranslator(ThreadGroup group, String threadName) {
		super(group, threadName);
		// TODO Auto-generated constructor stub
	}

	public FixLineTranslator(ThreadGroup group, Runnable runnable,
			String threadName) {
		super(group, runnable, threadName);
		// TODO Auto-generated constructor stub
	}

	public FixLineTranslator(ThreadGroup group, Runnable runnable,
			String threadName, long stackSize) {
		super(group, runnable, threadName, stackSize);
		// TODO Auto-generated constructor stub
	}

	static HashMap<String, String> myResources=null;
	static DaemonCallSocket mSocket=null;
	static long stopTime=0;
	/**
	 * 
	 */
	
	public interface Listener
	{
		public void socketReady();
		public void readyToRead();
		public void onServerQuit();
	}
	static HashMap<String, Vector<String> > dataInventory=new HashMap<String, Vector<String> >();
	static HashMap<String, FixLineTranslator.Listener> listeners=new HashMap<String, FixLineTranslator.Listener>();

	static Activity mStarter;
	
	public void setStartBy(Activity av)
	{
		mStarter=av;
	}
				public Vector<String> getAllData(String tag){
						return dataInventory.get(tag);
				}

				public void addDataReadyListener(String tag, FixLineTranslator.Listener listener)
				{
					listeners.put(tag, listener);
				}
				
				static final int STATUS_OK=1;
				static final int STATUS_QUIT=-1;
				static void notifySocketReady(int status) //1 : OK, 9: quit
				{
					Iterator<String> itr=listeners.keySet().iterator();
					while (itr.hasNext())
					{
						FixLineTranslator.Listener l=listeners.get(itr.next());
						if (status == STATUS_OK)
						l.socketReady();
						else if (status == STATUS_QUIT)
							l.onServerQuit();
					}
				}
				
				public void iQuit(String tag)
				{
					listeners.remove(tag);
				}
				
				public void sendFixLine(String fixLine) throws SocketException 
				{
					stopTime=(new Date()).getTime()+10*60*1000;
					if (mSocket==null) 
						{
							throw new SocketException("Connection Gone");
						}
					mSocket.putInstruction(fixLine);
				}
				
				public Vector<String> getFixLine(String tag)
				{
					return dataInventory.get(tag);
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
	    Log.i("READFILE", "Could not read file "+fileName);
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
        SharedPreferences sharedPref = mStarter.getSharedPreferences(fileName, Context.MODE_PRIVATE);
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
			Log.i("FIXLOOPER", "Just Started");
			//wait 10 seconds for system to connect
			try {
				sleep(30*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				while (mSocket != null && !mSocket.isClosed())
				{
					notifySocketReady(STATUS_OK);
					byte[] dataB=mSocket.readStreamData();
					if (dataB == null) 
						{
							if ((new Date()).getTime() > stopTime) break;
							Log.i("FIX", "I am on");
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
					
					FixLineTranslator.Listener reader=listeners.get(tableName);
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
			try {
				sleep(3*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mInstance==null) return;
			mSocket.stop();
			//mNM.cancel(NOTIFICATION);
			notifySocketReady(STATUS_QUIT);
			
			mInstance=null;
		}
	}
	
	public void run()
		{
		//if (myResources==null)
			//myResources=new HashMap<String, String>();
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
				//mNM = (NotificationManager)mStarter.getSystemService(Context.NOTIFICATION_SERVICE);
				//showNotification();

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
	
	public void destroyIt(Activity av)
	{
		if (av.getClass()!=mStarter.getClass()) return;
		if (mInstance==null) return;
		mSocket.stop();
		//mNM.cancel(NOTIFICATION);
		mInstance=null;
		mStarter=null;
		notifySocketReady(STATUS_QUIT);
		Log.i("FIX", "I am Gone");
	}

	private NotificationManager mNM;
	private int NOTIFICATION = 10;;
	

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mStarter)
        //.setLargeIcon(Bitmap.createBitmap(metrics,80, 80, Bitmap.Config.ARGB_4444))//
        .setSmallIcon(R.drawable.ic_action_open)//kp37)//ic_stat_gcm)
        .setContentTitle("service running")
        .setStyle(new NotificationCompat.BigTextStyle().bigText("test of service"))
        .setContentText("test of service")
        .setLights(0xFF0000, 2000, 5000)
        .setAutoCancel(true);

        Intent nullIntent=new Intent(mStarter, AgendaActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mStarter, 0, nullIntent, Intent.FLAG_ACTIVITY_NEW_TASK);//PendingIntent.FLAG_ONE_SHOT); //the intent to open when clicked
        
        mBuilder.setContentIntent(contentIntent); //so MainActivity will be opened when notification is clicked
        mNM.notify(NOTIFICATION, mBuilder.build());
        
    }
}
