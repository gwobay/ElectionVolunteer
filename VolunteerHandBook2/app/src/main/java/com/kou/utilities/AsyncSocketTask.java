package com.kou.utilities;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncSocketTask extends AsyncTask<byte[], Boolean, Vector<byte[]> >//Vector<RowStruct>> 
{
	Vector<byte[]> mVector;
	boolean hasResponse;
	public AsyncSocketTask()
	{
		super();
		hasResponse=true;
	}
	public void setDataBox(Vector<byte[]> inV)
	{
		mVector=inV;
	}
	public void needResponse(boolean T_F){
		hasResponse=T_F;
	}
	
	public static byte[] convertStringToBytes(String inS)
	{
		byte[] buf0=inS.getBytes();
		int i0=buf0.length/127;
		int i1=buf0.length % 127;
		byte[] buf=new byte[buf0.length+2];
		buf[0]=(byte)i0;
		buf[1]=(byte)i1;
		System.arraycopy(buf0,  0,  buf,  2,  buf0.length);
		return buf;
	}
	@Override
	protected //Vector<RowStruct> 
	Vector<byte[]> doInBackground(byte[]... args) 
	{
// the call socket was created with the request from caller
		String hostname=new String(args[0]);
		int port=Integer.parseInt(new String(args[1]));
		byte[] sendBytes=args[2];
		int waitTime=sendBytes.length/10000;
		if (waitTime < 1) waitTime=1;
		//String text=args[2];
		int secs=10*waitTime;
		if (args.length > 3) secs=Integer.parseInt(new String(args[3]));
		long finishTime=(new Date()).getTime()+secs*1000;
		/*byte[] buf0=text.getBytes();
		int i0=buf0.length/127;
		int i1=buf0.length % 127;
		byte[] buf=new byte[buf0.length+2];
		buf[0]=(byte)i0;
		buf[1]=(byte)i1;
		System.arraycopy(buf0,  0,  buf,  2,  buf0.length);*/
		ArrayBlockingQueue<byte[]> inBox=new ArrayBlockingQueue<byte[]>(50, true);
		CallSocket mSocket;
	try {
			mSocket=new CallSocket(hostname, port);
			mSocket.setFinishTime(finishTime);
			mSocket.setInBox(inBox);
			mSocket.needResponse(hasResponse);
		} 
		catch (UnknownHostException e){return null;}
		catch (IOException e){e.printStackTrace();
			return null;}
		mSocket.setCallerBuffer(sendBytes, sendBytes.length);
		Log.i("CALLSKT", "SEND : "+new String(sendBytes));
		//publishProgress(true);
		Thread channel=new Thread(mSocket);
		channel.start();
		//try {mSocket.close();}catch(IOException e){}
		if (!hasResponse)
		{
			inBox=null;
			try {
				Thread.sleep(1000*waitTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSocket.setFinishTime(1);
			mSocket.close();			
			return null;
		}
		Vector<byte[]>  sktData;
		if (mVector != null) sktData=mVector;
		else sktData=new Vector<byte[]>();
		while (inBox.size() > 0 || (new Date()).getTime() < finishTime )
		{	
			byte[] data=null;
				try {
					data=inBox.poll(200, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
				if (data != null)
				{
					Log.i("CALLSKT", "GOT : "+data.length);
					if (data.length > 4) sktData.add(data);//{003,003,003}
					else break; //3 bytes indicates end of response
				}
		}
		mSocket.setFinishTime(1);
		mSocket.close();
		channel.interrupt();
		inBox.clear();
		inBox=null;
		//Vector<byte[]>  sktData=mSocket.getSocketDataBuffer();
		//Vector<RowStruct> response=new Vector<RowStruct>();
/*		for (int i=0; i<sktData.size(); i++)
		{
			RowStruct aRow=(new MessageParser()).lineToRow(new String(sktData.get(i)));
			response.add(aRow);
		}*/
	//try {mSocket.close();} catch (IOException e){}
		Log.i("CALLSKT", "finish with output "+sktData.size()+" Lines");
		return sktData;//response;
	}


protected void onProgressUpdate(Boolean a)
{
		//showDialog("Thank You! Waiting for server data .....");
}

protected void onPostExcute(Vector<byte[]>... response)
{
	//mDialog.dismiss();
	//processResponse(response[0]);
}
}
