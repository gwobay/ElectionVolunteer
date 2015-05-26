/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.volunteerhandbook;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    //public static final int NOTIFICATION_ID = 1;
    public static int NOTIFICATION_ID;
    
    public static final ReentrantLock fileLock=new ReentrantLock(); 
    public static Ringtone noise=null;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                for (int i = 0; i < 5; i++) {
                    Log.i(TAG, "Working... " + (i + 1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                sendNotification("Received: " + extras.toString());
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    int saveData(String tblName, String fixLine)
    {
    	String fileName="notification"+tblName;
    	int iPending=0;
    	Set<String> nullSet=null;
    	synchronized(fileLock){
    	SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
    	Set<String> pendingData=sharedPref.getStringSet("notification", nullSet);
    	if (pendingData==null)
    		pendingData=new HashSet<String>();
    	pendingData.add(fixLine);
    	SharedPreferences.Editor adder=sharedPref.edit();
    	adder.putStringSet("notification", pendingData);
    	adder.commit();
    	iPending=pendingData.size();
    	}
    	return iPending;
    }
    void makeNoise()
    {
    	boolean toRing=false;
    	String fileName=MainActivity.getFileHeader()+ProfilePage.getTableName();
        SharedPreferences mem = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String sRing=mem.getString(ProfileActivity.CURRENT_RINGTON, "--");//, String);
        if (sRing.charAt(0) == '-') return;
        Uri ringUri=Uri.parse(sRing);//, String);
        String t0=mem.getString(ProfileActivity.NO_NOISE_START, "--");//, noNoiseStart);
        String t1=mem.getString(ProfileActivity.NO_NOISE_END, "--");//, noNoiseEnd);
        String[] sT0=t0.split(":");
        int h0=Integer.parseInt(sT0[0]);
        int m0=Integer.parseInt(sT0[1]);
        sT0=t1.split(":");
        int h1=Integer.parseInt(sT0[0]);
        int m1=Integer.parseInt(sT0[1]);
        Time tm=new Time();
        int iNow=tm.hour*60+tm.minute;
        int iU=h1*60+m1;
    	int iL=h0*60+m0;
    	if (h1 < h0) 
        	{
        		toRing=(iNow > iU && iNow < iL );
        	}
        else
        	toRing=(iNow > iU || iNow < iL);
    	//android.os.Debug.waitForDebugger();
        if (toRing)
        {
        	noise=RingtoneManager.getRingtone(getApplicationContext(), ringUri);
        	noise.play();
        }   else noise=null;           
    }
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    //static //String header=MainActivity.getFileHeader();
    
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        makeNoise();
        String msgShow=msg;
        int i0=msg.indexOf("170=");
        String extraString=null;
        if (i0 > 0)
        {
        	extraString=msg.substring(i0);
        	msgShow="";
        	int i9=0;
        	while (i9< msg.length() && msgShow.length() < 20)
        	{
        		i0=msg.indexOf("=", i0);
        		if (i0 < 0) break;
        		i9=msg.indexOf("|", ++i0);
        		if (i9<0) i9=msg.length();
        		msgShow += msg.substring(i0, i9);
        		msgShow += ",";
        	}
        }
        Intent jobIntent=null;//=new Intent(this, MainActivity.class);

        jobIntent=new Intent(this, MainActivity.class);//main will invoke other activities
        String tblName="";
        int iPending=1;
        
        if (extraString != null)
        {
        	int ix0=extraString.indexOf("170=");
        	int ix9=extraString.indexOf("|", ix0+4);
        	
        	tblName=extraString.substring(ix0+4, ix9);
        	if (tblName.equalsIgnoreCase("agenda"))
        	{
        		iPending=saveData(tblName, extraString);
        		//jobIntent=new Intent(this, AgendaActivity.class);//MainActivity.class);
        	}
        	/*else if (tblName.equalsIgnoreCase("chatroom"))
        		jobIntent=new Intent(this, ChatRoomActivity.class);*/
            String fixKey=getResources().getString(R.string.fix_line_key);
        	jobIntent.putExtra(fixKey, "GCM>>"+extraString);   
        }
        
        String header=getResources().getString(R.string.candidate_name);
        String MSGs=getResources().getString(R.string.msg_for_you);

        //DisplayMetrics metrics = new DisplayMetrics();
        //((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        //.setLargeIcon(Bitmap.createBitmap(metrics,80, 80, Bitmap.Config.ARGB_4444))//
        .setSmallIcon(R.drawable.save_icon)//kp37)//ic_stat_gcm)
        .setContentTitle(header+"("+iPending+")")
        .setStyle(new NotificationCompat.BigTextStyle().bigText(msgShow))
        .setContentText(tblName+" "+iPending+MSGs)
        .setLights(0xFF0000, 2000, 5000)
        .setAutoCancel(true);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, jobIntent, Intent.FLAG_ACTIVITY_NEW_TASK);//PendingIntent.FLAG_ONE_SHOT); //the intent to open when clicked
        
        mBuilder.setContentIntent(contentIntent); //so MainActivity will be opened when notification is clicked
        mNotificationManager.notify(tblName.hashCode(), mBuilder.build());
        
        if (noise != null)
        {
        	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	noise.stop();
        	noise=null;
        }
    }
    
    public void localNotification(String msg)
    {
    	sendNotification(msg);
    }
}
