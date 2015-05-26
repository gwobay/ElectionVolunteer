package com.kou.utilities;

import java.net.*;
import java.util.Date;
import java.io.*;
import java.lang.Thread;



public class MainServer extends Thread
{
int mPort;
long mStopTime;
	public MainServer()
	{
		super();
	}
	
	public void setPort(int p)
	{
		mPort=p;
	}
	public void setStopTime(long t)
	{
		mStopTime=t;;
	}

    public void run()
    {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(mPort);
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+mPort);
            System.exit(1);
        }

        do
        {
        	Socket clientSocket=null;
        	try {
        			clientSocket = serverSocket.accept();
            } catch (IOException e) {
                	System.err.println("Accept failed.");
                	if (clientSocket==null) continue;
                }
        
        System.out.println("Got call from "+clientSocket.getRemoteSocketAddress().toString());
        	
        	ProcessingSocket aProcess=new ProcessingSocket(clientSocket);
        	aProcess.start();
        } while ((new Date()).getTime() < mStopTime || mStopTime==0);

                try {
                        serverSocket.close();
                } catch (IOException e) {}
        return ;
    }

    public static void main(String[] args)
    {
    	int iPort=9696;
        if (args.length > 0)
                {
                	iPort=Integer.parseInt(args[0]);
                }
        long stopAt=(new Date()).getTime()+60*1000*60*8;
            	stopAt=0;
            	
        DataProcessor aDbProcessor=DataProcessor.getInstance();
            	aDbProcessor.setStopTime(stopAt);
            	aDbProcessor.start();
            	
        MainServer aServer=new MainServer();
                aServer.setPort(iPort);
                aServer.setStopTime(stopAt);
                aServer.start();
                
            	try {
            		
            		aServer.join();
            		
					aDbProcessor.join();
            		Thread.sleep(stopAt - (new Date()).getTime());
            	}catch(InterruptedException e){}
            	aDbProcessor.interrupt();
            	aServer.interrupt();
    }
}




