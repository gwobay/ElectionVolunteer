package com.example.volunteerhandbook;

import java.net.SocketException;
import java.util.Vector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public class JobActivity extends FragmentActivity 
		implements FixLineService.Listener, FixLineTranslator.Listener
{
	static final int TYPE_SERVICE=0;
	static final int TYPE_THREAD=1;
	static Vector<String> mPendingQ=new Vector<String>();
	protected FixLineService.FixBinder mBinder;
	protected Vector<String> mServerData;
	protected boolean isBound;
	static protected FixLineTranslator translator;
	static boolean useService=false;

	public JobActivity() {
		// TODO Auto-generated constructor stub
	}

	public void setServerType(int type)
	{
		if (type == TYPE_SERVICE ) useService=true;
		else useService=false;
	}
	void assureService()
	{
		if (useService)
		{
		if (mBinder == null) bindingFixService();
		}
		else
		{
			if (translator==null) connectToServer();
		}			
	}
	protected String getJobTableName()
	{
		return null;
	}
	public void readyToRead()
	{
		assureService();
		{
			mServerData=mBinder.getFixLine(getJobTableName());
		}
		if (useService)
		{
			if (mBinder != null) mServerData=mBinder.getFixLine(getJobTableName());
		}
		else
		{
			if (translator!=null) mServerData=translator.getFixLine(getJobTableName());
		}			
	}
	
	public void socketReady()
	{
		isBound=true;
		transmitData(null);
	}
	
	protected void putDataIntoFile()
	{
		return;
	}
	public void onServerQuit()
	{
		translator=null;
		if (mPendingQ.size() > 0)
		{
			putDataIntoFile();
		}
	}
	public void transmitData(String fixLine)
	{
		assureService();
		if (fixLine !=null)
		mPendingQ.add(fixLine);
		if (!isBound) return;
		synchronized(this){
		try {			
			while (mPendingQ.size() >0)//for (int i=0; i< mPendingQ.size(); i++)
			{
				translator.sendFixLine(mPendingQ.get(0));
				mPendingQ.remove(0);				
			}		
				mPendingQ.clear();	
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			connectToServer();
		}}
					
	}
	
	protected boolean noQ()
	{
		return (mPendingQ.size() ==0);
	}
	public void connectToServer()
	{
		translator=FixLineTranslator.getInstance(JobActivity.this);
		
		if (translator != null)
			translator.addDataReadyListener(getJobTableName(), this);
	}

	public void stopConnection()
	{
		if (translator != null)
			translator.destroyIt(JobActivity.this);
	}

	public void sendDataToServer(String fixLine)
	{
		if (useService) sendDataToService(fixLine);
		else transmitData(fixLine);
	}
	public void sendDataToService(String fixLine)
	{
		assureService();
		if (fixLine !=null)
		mPendingQ.add(fixLine);
		if (mBinder == null) return;
		for (int i=0; i< mPendingQ.size(); i++)
			mBinder.sendFixLine(mPendingQ.get(i));
			mPendingQ.clear();		
	}
	
	public void startFixService()
	{
		startService(new Intent(this, FixLineService.class));
	}
	
	 private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder binder) {
             // This is called when the connection with the service has been
             // established, giving us the service object we can use to
             // interact with the service.  Because we have bound to a explicit
             // service that we know is running in our own process, we can
             // cast its IBinder to a concrete class and directly access it.
        	 mBinder = (FixLineService.FixBinder)binder; 
        	 mBinder.addDataReadyListener(getJobTableName(), JobActivity.this);
        	 sendDataToServer(null);
        	 isBound=true;
         }

         public void onServiceDisconnected(ComponentName className) {
             // This is called when the connection with the service has been
             // unexpectedly disconnected -- that is, its process crashed.
             // Because it is running in our same process, we should never
             // see this happen.
        	 mBinder = null;
        	 isBound=false;
         }
     };

	public void bindingFixService()
	{
	            // Establish a connection with the service.  We use an explicit
	            // class name because we want a specific service implementation that
	            // we know will be running in our own process (and thus won't be
	            // supporting component replacement by other applications).
	     	bindService(new Intent(this, FixLineService.class), mConnection, Context.BIND_AUTO_CREATE);
	     	isBound = true;
	}
}
