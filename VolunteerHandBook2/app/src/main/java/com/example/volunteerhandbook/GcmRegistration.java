package com.example.volunteerhandbook;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kou.utilities.AsyncSocketTask;
import com.kou.utilities.RowStruct;

public class GcmRegistration {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "992881316154";
    String API_key="AIzaSyCW6L-ZazshSY4u70Pbd6afeG_G29TLBEk";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Client";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context mContext;
    Activity mActivity;
    SharedPreferences mSPF;
    String regid;
    boolean confirmed;

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable((Context)mActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
               // finish();
            }
            return false;
        }
        return true;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return mActivity.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
   
    private void storeRegistrationId(Context context, String regId) {
        //final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = mSPF.edit();//prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    private void storeConfirmed(Context context, String regId, boolean T_F) {
        //final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = mSPF.edit();//prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        int i=(T_F)?1:0;
        editor.putInt("CONFIRMED", i);
        editor.commit();
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = mSPF;//getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        if (prefs.getInt("CONFIRMED", 0)==1) confirmed=true;
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regid = gcm.register(SENDER_ID);
                    //msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    storeRegistrationId(mContext, regid);
                    
                    sendRegistrationIdToBackend();

                    // Persist the regID - no need to register again.
                    //
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() 
    {
    	AsyncSocketTask aTask=new AsyncSocketTask();
    	//aTask.setVectorStore(responseStore);
    	aTask.needResponse(false);
    	byte[] sendBytes=AsyncSocketTask.convertStringToBytes("170=api_regids|179=kop_volunteer|55="+regid+"|");
    	aTask.execute("220.134.85.189".getBytes(), "9696".getBytes(), sendBytes);
    	try {
			Vector<byte[]> response=aTask.get(10000, TimeUnit.MILLISECONDS);
			confirmed=false;
			if (response != null) confirmed=true;
			storeConfirmed(mContext, regid, confirmed) ;
		} catch (InterruptedException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			aTask.cancel(true);
		}catch ( ExecutionException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            aTask.cancel(true);
        }catch ( TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            aTask.cancel(true);
        }
    }
    
    public void doIt()
    {
    	mContext=mActivity.getApplicationContext();
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(mContext);
            regid = getRegistrationId(mContext);

            if (regid.isEmpty()) {
                registerInBackground();
            } else if (!confirmed) sendRegistrationIdToBackend();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    
	public GcmRegistration(Activity this1, SharedPreferences idFile) {
		mActivity=this1;
		mSPF=idFile;
		confirmed=false;
		//init();
	}

}
