/**
 * 
 */
package com.kou.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;


/**
 * @author eric
 *
 */
public class ProcessingSocket extends Thread 
{

	IOSocket mySocket;
	InputStream in;
	OutputStream out;
	Vector<Byte> data;
	byte[] inDataBuffer;
	boolean zipped_channel;
	String readPage;
	int totalRead;
	int myDatabaseId;
	/**
	 * 
	 */
	public ProcessingSocket(Socket clientSkt) {
		// TODO Auto-generated constructor stub
		if (clientSkt == null) return;
		mySocket = new IOSocket(clientSkt);
	}
	
	public void setZippedFlag(boolean T_F) { mySocket.setZippedFlag(T_F);}

	
	void processData()//byte[] readData)
	{
		byte[] readData=mySocket.readStreamData();
		MessageParser aParser=new MessageParser(readData, readData.length);
		aParser.setDbClientId(myDatabaseId);
		aParser.startJob();
		Vector<String> dV=aParser.getDbResponseToClient();
		for (int i=0; i<dV.size();i++)
		{
			mySocket.sendSocketText(dV.get(i));
		}
		
	}
	
	public void run()
	{
		//byte[] readBytes=mySocket.readStreamData();
		//if (readBytes!=null && readBytes.length > 1) 
		{
		myDatabaseId=DataProcessor.registerClient();
		//used as header of instruction and to read the response dat
			processData();//readBytes);
			if (mySocket!=null)
				mySocket.close();
		}
	}
	/**
	 * @param arg0
	 */
	public ProcessingSocket(Runnable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public ProcessingSocket(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ProcessingSocket(ThreadGroup arg0, Runnable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ProcessingSocket(ThreadGroup arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ProcessingSocket(Runnable arg0, String arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public ProcessingSocket(ThreadGroup arg0, Runnable arg1, String arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public ProcessingSocket(ThreadGroup arg0, Runnable arg1, String arg2,
			long arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

}
