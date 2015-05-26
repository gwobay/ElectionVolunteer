package com.kou.utilities;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class CallSocket extends IOSocket implements Runnable 
{ //this is a simple one round-trip socket
String mHost;
int mPort;
//InputStream mIn;
//OutputStream mOut;
byte[] mCallerDataBuffer;
int callerDataLength;
byte[] mSocketDataBuffer;
Lock mResponseLock;
byte[] mSocketPartialData;
Vector<byte[]> callerReadBuffer;
int socketDataLength;
int callerReadStarts;
int callerReadLength;
int newReadStarts;
int newReadLength;
SocketAddress mAddress;
boolean hasMore; //for mutiple rows communications
boolean waitForResponse;
Boolean isSocketDataReady;//=new Boolean(false);
ArrayBlockingQueue<byte[]> inBox;
	public CallSocket (String h, int p) throws UnknownHostException, IOException
	{
		super(h, p);
		
		callerDataLength=0;
		socketDataLength=0;
		newReadStarts=0;
		newReadLength=0;
		callerReadLength=0;
		hasMore=false;
		waitForResponse=true;
		inBox=null;
		// shared with caller thread
		isSocketDataReady=false;
		mCallerDataBuffer=new byte[4000];
		mSocketDataBuffer=new byte[40000]; //data are compressed
		mResponseLock=new ReentrantLock();
		mSocketPartialData=new byte[40000]; // uncompressed data for client
		callerReadBuffer=new Vector<byte[]>();
	}
	
	void needResponse(boolean T_F)
	{
		waitForResponse=T_F;
	}
	void connect() throws IOException
	{
		super.connect(getLocalSocketAddress());
		
			mIn=mSocket.getInputStream();
			mOut=mSocket.getOutputStream();						
		
        //try
        {
        	//mSocket.setSoTimeout(timeWait);
        	//mSocket.setTcpNoDelay(true);
        }
        //catch (SocketException e){ }
		return;
	}
	

	// app to server always singleton message so put before run
	public void setCallerBuffer(byte[] dataBuf, int buf_len)
	{
		mCallerDataBuffer=dataBuf;//new byte[buf_len];
		callerDataLength=buf_len;
		//System.arraycopy(dataBuf, 0,  mCallerDataBuffer, 0, buf_len);
	}
/*	public Vector<byte[]> getSocketDataBuffer()
	{
		if (!waitForResponse) return null;
		synchronized (mResponseLock){
			while (!isSocketDataReady)
			{
				try {mResponseLock.wait(500);}
					catch(InterruptedException e){break;}
			}
		}
		return callerReadBuffer;
	}
	*/
	long finishTime;
	public void setFinishTime(long tm)
	{
		finishTime=tm; 
	}
	
	public void setInBox(ArrayBlockingQueue<byte[]> box)
	{
		inBox=box;
	}
	
	void cleanUp()
	{
		mCallerDataBuffer=null;
		mSocketDataBuffer=null; //data are compressed
		mResponseLock=null;
		mSocketPartialData=null; // uncompressed data for client
		callerReadBuffer=null;
	}
	public void run()
	{
		int failedCount=0;
		//try {connect(); } catch (IOException e){e.printStackTrace(); return;}
		while (!sendRawByte(mCallerDataBuffer, 0, callerDataLength) && ++failedCount < 3)
		{
			if (mSocket.isClosed())
			{
				try {
				connect(); } catch (IOException e){e.printStackTrace();return;}
			}
		}
		hasMore=true;
		if (failedCount > 2) {return;}
		failedCount=0;

		if (!waitForResponse && inBox == null){
			close();
			return;
		}
			/*long lifeTime=(new Date()).getTime()+30000;
	synchronized (mResponseLock){
			isSocketDataReady=false;
			
			//do multiple read
*/		
			while (hasMore || (new Date()).getTime() < finishTime )
			{	
					byte[] data=readStreamData();
					if (data != null) {
						//callerReadBuffer.add(data);
						try {
							inBox.put(data);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			//else continue;
			//String readLine=new String(data, Charset.forName("UTF-8"));
					hasMore=hasLeftOver();
					if (!hasMore)
					{				
						if (readFlag < 0) {
							try {
								inBox.put(new byte[]{0x03});
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
						try {
							if (data != null) Thread.sleep(300);
							else Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}
			//isSocketDataReady=true;
			//mResponseLock.notifyAll();
			cleanUp();
			close();			
		}
		//wait for data got read
		//try {Thread.sleep(100);}catch(InterruptedException e){}
		//close();
}
