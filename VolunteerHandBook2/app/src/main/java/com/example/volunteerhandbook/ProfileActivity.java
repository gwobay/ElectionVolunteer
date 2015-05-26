package com.example.volunteerhandbook;

import java.text.DecimalFormat;

import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

public class ProfileActivity extends FragmentActivity 
				implements TimePickerDialog.OnTimeSetListener 
{
	static final String PROFILE_TAG="PROFILE_TAG";
	static ProfilePage mFragment=null;
	static String mCitizenId=null;
	static boolean firstTime=true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);//activity_login);
		
		//if (!firstTime) return;
		Intent intent = getIntent();
		String key=getResources().getString(R.string.fix_line_key);
	    String fixLine=intent.getExtras().getString(key);
	    mCitizenId=intent.getExtras().getString(MainActivity.CITIZEN_ID);
	    if (mCitizenId==null){
    	String fileName=MainActivity.getFileHeader()+getResources().getString(R.string.login_page);
        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        key=getResources().getString(R.string.pass_word_key);
         String   savedPassword = sharedPref.getString(key, "--");
            if (savedPassword.charAt(0)!='-')
            {
            	mCitizenId=sharedPref.getString(MainActivity.CITIZEN_ID, "--");
            }
	    }   
	    FragmentManager fragmentManager = getFragmentManager();
        mFragment=(ProfilePage) fragmentManager.findFragmentByTag(PROFILE_TAG);
	    if (mFragment==null || fixLine!=null)
	    {
	    	showProfile(fixLine);
	    }
	    else if (mFragment!=null)
		{
			fragmentManager.beginTransaction().add(R.id.login_content_frame, mFragment, PROFILE_TAG).commit();
	        if (viewToRefresh!=null) viewToRefresh.invalidate();
		}
	    getActionBar().setHomeButtonEnabled(false);
		// Set up the login form.
	}
	static Menu mActionMenu=null;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		mActionMenu=menu;
		for (int i=0; i<mActionMenu.size(); i++)
    	{
    		if (mActionMenu.getItem(i).getItemId()==R.id.action_save ||
    				mActionMenu.getItem(i).getItemId()==R.id.action_check)
    			continue;
    		mActionMenu.getItem(i).setVisible(false);
    			//setOnMenuItemClickListener(aListener);
    	}
		menu.findItem(R.id.action_save).setVisible(true);
		menu.findItem(R.id.action_save).setOnMenuItemClickListener(mFragment);
		menu.findItem(R.id.action_check).setVisible(true);
		menu.findItem(R.id.action_check).setOnMenuItemClickListener(mFragment);

		return true;
	}
	
	static ImageView viewToRefresh=null;

    
	void showProfile(String fixLine)
	{
		String pLine=fixLine;
		String key=getResources().getString(R.string.fix_line_key);
		if (fixLine==null)
		{
	    	String fileName=MainActivity.getFileHeader()+ProfilePage.getTableName();
	        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
	        pLine=sharedPref.getString(key, "--");
		}
        Bundle args = new Bundle();
        ProfilePage aPP=new ProfilePage();
        String pgTitle=getResources().getString(R.string.create_profile);
        args.putString(ProfilePage.PAGE_TITLE, pgTitle);
        if (pLine != null)
        	args.putString(key, pLine);
        key=getResources().getString(R.string.citizen_id_key);
        args.putString(key, mCitizenId);
        aPP.setArguments(args);
        MenuItem.OnMenuItemClickListener aListener=aPP;
        if (mActionMenu != null)
        {
        	for (int i=0; i<mActionMenu.size(); i++)
        	{
        	mActionMenu.getItem(i).setOnMenuItemClickListener(aListener);
        	}
        }
        mFragment = aPP;
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(R.id.login_content_frame, mFragment, PROFILE_TAG).commit();
        viewToRefresh=aPP.getProfilePhoto();
        setTitle(pgTitle);
return;
	}
	
	static final int REQ_PICK_IMAGE=1;
	static final int FOR_RINGTON=2;
	static final int FOR_TIMESTART=3;
	static final int FOR_TIMEEND=4;
	static ImageView mProfilePhotoImage=null;
	public void refreshPhoto(View v)
	{
		mFragment.updateProfilePhoto((ImageView)v);
	}
	
	public void getPicture(View v)
	{
		if (mFragment==null) return;
		mFragment.getPicture(v);

	}	
	
	public static final String CURRENT_RINGTON="CURRENT_RINGTON";
	public static final String NO_NOISE_START="NO_NOISE_START";
	public static final String NO_NOISE_END="NO_NOISE_END";
	static String ringUriString=null;
	static String noNoiseStart=null;
	static String noNoiseEnd=null;
	static TextView noNoiseTextField0=null;
	static TextView noNoiseTextField1=null;
		
	void saveRingToneData()
	{
    	String fileName=MainActivity.getFileHeader()+ProfilePage.getTableName();
        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor sv=sharedPref.edit();
        sv.putString(CURRENT_RINGTON, ringUriString);
        sv.putString(NO_NOISE_START, noNoiseStart);
        sv.putString(NO_NOISE_END, noNoiseEnd);
        sv.commit();
	}
	public void getRingTone(View v) {
	    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
	    Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	    RingtoneManager.getRingtone(getApplicationContext(), ringUri).play();
	    intent.putExtra(CURRENT_RINGTON, ringUri);
	    if (intent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(intent, FOR_RINGTON);
	    }
	    noNoiseTextField0=(TextView)((View)v.getParent()).findViewById(R.id.no_noise_period_begin);	
	    noNoiseStart=null;
	    noNoiseTextField1=(TextView)((View)v.getParent()).findViewById(R.id.no_noise_period_end);	
	    noNoiseEnd=null;
	}
	
	public void onTimeSet(TimePicker view, int h, int m)
	{
		DecimalFormat dF=new DecimalFormat("00");
		String sTime=dF.format(h)+":"+dF.format(m);	
		if (noNoiseStart==null)
		{
			noNoiseStart=sTime;
			noNoiseTextField0.setText(sTime+" - ");
			getNoNoisePeriod(getResources().getString(R.string.no_noise_period_end));
		}
		else
		{
			noNoiseEnd=sTime;
			noNoiseTextField1.setText(sTime);
			saveRingToneData();
		}
	}
	
	public void getNoNoisePeriod(String title) {
		TimePickerDialog pT=new TimePickerDialog(this, this, 0, 0, true);
		//FragmentManager manager=getFragmentManager();
		pT.setTitle(title);
		pT.show();//manager,  "datePicker");	
	    //Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
	    //Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	    //RingtoneManager.getRingtone(getApplicationContext(), ringUri).play();
	    //intent.putExtra(CURRENT_RINGTON, ringUri);
	    //if (intent.resolveActivity(getPackageManager()) != null) {
	        //startActivityForResult(intent, FOR_RINGTON);
	    //}
	}

	public void pickDate(View view)
	{
		DatePickerFragment pFrg=new DatePickerFragment();
		View dateView=((View)view.getParent()).findViewById(R.id.birth_date);
		pFrg.setDay0(1994, 6, 1);
		pFrg.pickDate(dateView, this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{			
	//String photo_file_path;
	Uri photo_uri=null;
		if (data != null) photo_uri=data.getData();
		if(requestCode == REQ_PICK_IMAGE && photo_uri != null) 
		{
		/*
		        //User had pick an image.
			Cursor cursor = getContentResolver().query(photo_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
		        
			cursor.moveToFirst();

		        //Link to the image
		        photo_file_path = cursor.getString(0);
		        cursor.close();
		 */       	
		}
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_PICK_IMAGE && photo_uri != null)
		{			
			//PersonalPage aPP=(PersonalPage)(globalParameters.get("volunteer"));
			if (mFragment==null) return;
			mFragment.setPhotoUri(photo_uri);
			mFragment.replaceProfilePhoto(mProfilePhotoImage);  
		}
		if (requestCode==FOR_RINGTON)
		{
			Uri pickedRing=(Uri)data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			//Ringtone aRing=RingtoneManager.getRingtone(getApplicationContext(), pickedRing);
			ringUriString=pickedRing.toString();
			getNoNoisePeriod(getResources().getString(R.string.no_noise_period_begin));
		}
	}
	
    public void done()
    {
    	//Intent it=new Intent();
    	this.setResult(MainActivity.REQ_PROFILE);
    	finish();
    }
}
