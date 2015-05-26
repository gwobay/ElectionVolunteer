package com.kou.utilities;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

public class DaemonCallSocket extends IOSocket  implements Runnable {

	public DaemonCallSocket() {
		// TODO Auto-generated constructor stub
	}

	public DaemonCallSocket(Socket s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	public DaemonCallSocket(Proxy arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public DaemonCallSocket(SocketImpl impl) throws SocketException {
		super(impl);
		// TODO Auto-generated constructor stub
	}

	public DaemonCallSocket(InetAddress address, int port) throws IOException {
		super(address, port);
		// TODO Auto-generated constructor stub
	}

	public DaemonCallSocket(String host, int port, InetAddress localAddr,
			int localPort) throws IOException {
		super(host, port, localAddr, localPort);
		// TODO Auto-generated constructor stub
	}

	public DaemonCallSocket(InetAddress address, int port,
			InetAddress localAddr, int localPort) throws IOException {
		super(address, port, localAddr, localPort);
		// TODO Auto-generated constructor stub
	}

	/*
	 * this socket will use blockingQ as box for caller to deliver instruction
	 * and use listener to process response
	 * the elapse time will be the time elapsed after the last read/write
	 */
	String mHost;
	int mPort;
	//InputStream mIn;
	//OutputStream mOut;
	Lock mResponseLock;
	Vector<byte[]> callerReadBuffer;
	SocketAddress mAddress;
	int elapseTime;
	boolean hasMore; //for mutiple rows communications
	boolean waitForResponse;
	ArrayBlockingQueue<byte[]> instructionQ;//=new ArrayBlockingQueue<String>(100,true);
	//change to byte so I can work on media file
	ArrayBlockingQueue<byte[]> responseQ;//=new ArrayBlockingQueue<String>(100,true);
	boolean stopFlag;
	int mine_mode;
	static final int MODE_NO_NEW=4; //finish Q and call listener and kill by listener
	static final int MODE_NORMAL=0; //accept new in - out until killed other wise
	//static HashMap<String, DaemonCallSocket.Listener > listeners=new HashMap<String, DaemonCallSocket.Listener >();
	public interface Listener
	{
		public void readyToRead(Vector<byte[]> outQ);
		public void onServerQuit(Vector<byte[]> outQ);
	}
	String myTag;
	DaemonCallSocket.Listener myListener;
	protected void init()
	{
		hasMore=false;
		waitForResponse=true;
		
		// shared with caller thread
		mResponseLock=new ReentrantLock();
		callerReadBuffer=new Vector<byte[]>();
		timeWait=200;
		try {
		setSoTimeout(timeWait);
		} catch (SocketException e){}
		
		stopFlag=false;	
	}
	
	static void appendQ(ArrayBlockingQueue<byte[]> o, ArrayList<byte[]> n)
	{
		if (o==null || n == null) return;
		for (int i=0; i< n.size(); i++)
		{
			try {
			byte[] b=n.get(i); while (!o.offer(b, 5, TimeUnit.SECONDS));
			} catch (InterruptedException e){}
		}
	}
	
	//to save power, still have to use open-close pattern for each message
	static HashMap<String, DaemonCallSocket> openList=new HashMap<String, DaemonCallSocket>();
	//no duplicate request from the same table tag 
	static public DaemonCallSocket getInstance(String h, int p, Collection<byte[]>c, 
			String listenerTag, DaemonCallSocket.Listener l) 
	{	
		String key=listenerTag;
		DaemonCallSocket skt=openList.get(key);
		if (skt != null)
		{
			if (!skt.isClosed()){
				appendQ(skt.instructionQ, (ArrayList<byte[]>)c);
				skt.myListener= l;
				return skt;
			}
			skt=null;
		}			
					try {
						skt=new DaemonCallSocket(h, p, c,listenerTag, l);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch (ConnectException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					skt.myListener= l;
					int iSize=c.size();
					skt.instructionQ=new ArrayBlockingQueue<byte[]>(iSize+10, true, c);
					//responseQ=new ArrayBlockingQueue<String>(iSize+1, true);
					skt.myTag=listenerTag;
					skt.mine_mode=MODE_NO_NEW;
					return skt;
			}
	
	private DaemonCallSocket(String h, int p, Collection<byte[]>c, 
						String listenerTag, DaemonCallSocket.Listener l) throws UnknownHostException, IOException
	{
		super(h, p);
		int iSize=c.size();
		instructionQ=new ArrayBlockingQueue<byte[]>(iSize, true, c);
		myTag=listenerTag;
		mine_mode=MODE_NO_NEW;
		myListener=l;
	}
	
		public DaemonCallSocket (String h, int p) throws UnknownHostException, IOException
		{
			super(h, p);
			Log.i("DAEMONSKT", "Just Connected");
		}
		
		void connect() throws IOException
		{
			super.connect(getLocalSocketAddress());
			
				mIn=mSocket.getInputStream();
				mOut=mSocket.getOutputStream();						
			
	       /* try
	        {
	        	mSocket.setSoTimeout(timeWait);
	        	//mSocket.setTcpNoDelay(true);
	        }
	        catch (SocketException e){ }*/
			return;
		}
		
		public void needResponse(boolean T_F)
		{
			waitForResponse=T_F;
		}

		public void setElapseTime(int tmInMiliSec)
		{
			elapseTime=tmInMiliSec;
		}
		
		public void setDataReadyListener(Listener l)
		{
			myListener=l;
		}
		
		public DaemonCallSocket(String host, int port, int tmInMiliSec, Listener l) throws UnknownHostException, IOException
		{
			super(host, port);
			elapseTime=tmInMiliSec;
			myListener=l;
		}
		public void putInstruction(byte[] inst)
		{
			if (instructionQ==null) instructionQ=new ArrayBlockingQueue<byte[]>(10,true);
			
			try {
				instructionQ.put(inst);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public static byte[] getStringBytes(String inS)
		{
			byte[] sBytes=inS.getBytes();
			byte[] outB=new byte[2+sBytes.length];
			outB[0]=(byte)(sBytes.length/127);
			outB[1]=(byte)(sBytes.length % 127);
			System.arraycopy(sBytes, 0, outB, 2, sBytes.length);
			return outB;
		}
		
		public void putInstruction(String inst)
		{
			if (instructionQ==null) instructionQ=new ArrayBlockingQueue<byte[]>(10,true);
			
			try {
				instructionQ.put(getStringBytes(inst));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		boolean sendData(byte[] outData, int length)
		{
			int failedCount=0;
	
			while (!sendRawByte(outData, 0, length) && ++failedCount < 3)
			{
				if (mSocket.isClosed())
				{
					try {connect(); } 
					catch (IOException e){e.printStackTrace();return false;}
				}
			}
			if (failedCount > 2) {return false;}
			return true;
		}
		
		public void stop(){
			stopFlag=true;
		}
		
		public void run()
		{
			Log.i("DAEMONSKT", "Just Connected");
			Thread respThd=new Thread(new Runnable(){
						public void run(){
							forResponse();
						}
			});
			respThd.start();
			
			if (instructionQ==null) instructionQ=new ArrayBlockingQueue<byte[]>(10,true);
			int iMsg=instructionQ.size();
			do
			{
				byte[] instruction;
				try {
					instruction = instructionQ.poll(elapseTime, TimeUnit.MILLISECONDS);								
						if (instruction != null)
						{
							sendData(instruction, instruction.length);
						}
						
					} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Log.i("DMCKT", "No outbound data for the last "+elapseTime/1000);
					stopFlag=true;
					}
			} while (!stopFlag);
			stopFlag=true;
			close();
		}
		
		public void forResponse()
		{
			if (callerReadBuffer==null) callerReadBuffer=new Vector<byte[]>();
			
			do{
				hasMore=true;				
					//do multiple read
				while (hasMore )
				{	
					byte[] data=readStreamData();
					if (data == null) 
						{
							if (readFlag == BAD_STREAM ||
									readFlag == BAD_READ)
							{
								stopFlag=true;
								break;
							}
							continue;
						}
					callerReadBuffer.add(data);
					//String readLine=new String(data, Charset.forName("UTF-8"));
					hasMore=hasLeftOver();
				}	
				
				int iResp=callerReadBuffer.size();
				
				if (iResp >0)
				{
					//HashMap<String, Vector<byte[]> > allBytes=new HashMap<String, Vector<byte[]> >();
					final Vector<byte[]> aV=new Vector<byte[]>();
					for (int i=0; i<iResp; i++)
					{
						byte[] outB=callerReadBuffer.get(i);
								aV.add(outB);
					}
					if (myListener != null){
						new Thread(new Runnable(){
							public void run() {
								myListener.readyToRead(aV);	
							}}).start();
					} 							
				}										
			} while (!stopFlag);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		void callServerQuitListener()
		{
			if (instructionQ.size() < 1) return;
			Vector<byte[]> leftOver=new Vector<byte[]>();
			for (int i=0; i<instructionQ.size(); i++)
			{
				leftOver.add(instructionQ.poll());
			}
			myListener.onServerQuit(leftOver);
		}
		
		@Override
		public void close()
		{
			callServerQuitListener();
			Log.i("DAEM_SKT", "I am closing");
			if (openList != null)
			openList.remove(myTag);
				
			if (instructionQ!=null)
			instructionQ.clear();
			instructionQ=null;
			if (responseQ!=null)
				responseQ.clear();	
			responseQ=null;
			//mResponseLock=new ReentrantLock();
			if (callerReadBuffer != null) callerReadBuffer.clear();
			super.close();			
		}
}
