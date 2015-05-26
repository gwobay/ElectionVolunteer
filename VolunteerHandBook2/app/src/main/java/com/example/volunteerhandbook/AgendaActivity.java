package com.example.volunteerhandbook;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AgendaActivity extends JobActivity
							//FragmentActivity 
					implements View.OnClickListener, TimePickerDialog.OnTimeSetListener
{

		public AgendaActivity() {
			// TODO Auto-generated constructor stub
		}
		
		static final String AGENDA_TAG="AGENDA_TAG";
		static AgendaRecord mFragment=null;
		static ViewGroup formFrame=null;
		static ViewGroup listFrame=null;
		static String mPageTitle=null;
		static String mCitizenId=null;
		
		public void onClick(View v) //form on page
		{
			//mFragment.onClick(v);
		}
		public void showMap(View v) //form on page
		{
			HashMap<String, String> mp=mFragment.getDataForMap(v);
			if (mp.size()<1) return;
			
	    	Intent pIntent=new Intent(this, MapActivity.class);
	    	pIntent.putExtra(MapActivity.LOCATION, mp.get(MapActivity.LOCATION));
	    	String fixKey=getResources().getString(R.string.fix_line_key);
	    	String info=mp.get(fixKey);
	    	if (info != null)
	    	pIntent.putExtra(fixKey, info);	    	
	    	startActivity(pIntent);   
			//show info window with mark
		}

		public void refreshMe(View v)
		{
			findViewById(R.id.container).invalidate();
		}
		public void redrawForm()
		{
			if (formFrame!=null)
				formFrame.invalidate();
		}
		public void redrawList()
		{
			if (listFrame != null)
				listFrame.invalidate();
		}
	/*	void saveNotifyDataToTable(HashSet<String> data)
		{
			AgendaRecord aAg=new AgendaRecord();
			aAg.initMe();
			aAg.saveFIXDataSet(data, this);
			Iterator<String> itr=data.iterator();
			while (itr.hasNext())
			{
				String fixLine=itr.next();
				int ix9=fixLine.indexOf("|120=");
				if (ix9 < 0) ix9=fixLine.length()-1;
				
				//aAg.saveFIXData(fixLine.substring(0, ix9+1), this);
			}
			
			return;
		}
		
		void getPendingNotificationMsg()
		{
	    	String fileName="notification"+"agenda";
	    	Set<String> pendingData=null;
	    	Set<String> nullSet=null;
	    	synchronized(GcmIntentService.fileLock){
	    	SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
	    	pendingData=sharedPref.getStringSet("notification", nullSet);
	    		if (pendingData != null)
	    		{
	    			SharedPreferences.Editor rm=sharedPref.edit();
	    			rm.clear();
	    			rm.commit();    			
	    		}
	    	}
	    	if (pendingData==null) return;
	    	saveNotifyDataToTable((HashSet<String>)pendingData);
	    	return;
		}
		*/
		void toSend(View v)
		{
		    Object[][] formFields=new Object[][]{
					{R.id.event_date, "event_date", 151}, 			 
					{R.id.event_time, "event_time", 152}, 
					{R.id.event_title, "event_title",153}, 
					{R.id.event_location, "event_location",154}, 
					{R.id.event_city, "event_city", 155}, 
					{R.id.event_host, "event_host", 156}, 
					{R.id.contact_number, "contact_number",157}, 
					{R.id.event_description, "event_description",158},
					{R.id.need_man, "need_man", 163},
					{R.id.need_equipment, "need_equipment", 164},
					{R.id.need_money, "need_money", 165}};
		    HashMap<String, String> formData=new HashMap<String, String>();
		    String fixLine="35=Apply|170=agenda|186="+mCitizenId+"|";
		    for (int i=0; i<formFields.length; i++)
		    {
		    	TextView tx=(TextView)mEntryForm.findViewById((int)formFields[i][0]);
		    	if (tx==null) continue;
		    	formData.put((String)formFields[i][1], tx.getText().toString());
		    	fixLine += ""+(int)formFields[i][2]+"="+tx.getText().toString()+"|";
		    }
		    AgendaRecord.sendServerData(fixLine, null);
		}
		View mEntryForm;
		boolean noMenu=false;
		void createNewAgenda()
		{
			FrameLayout container=(FrameLayout)findViewById(R.id.container);
			ScrollView sv=(ScrollView)getLayoutInflater().inflate(R.layout.create_agenda_form, null);
			container.addView(sv);
			mEntryForm=container;
			noMenu=true;
		}
		int mCurrentRotation;
	@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			    
			         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
			                 .detectDiskReads()
			                 .detectDiskWrites()
			                 .detectNetwork()   // or .detectAll() for all detectable problems
			                 .penaltyLog()
			                 .build());
			         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
			                 .detectLeakedSqlLiteObjects()
			                 .detectLeakedClosableObjects()
			                 .penaltyLog()
			                 .penaltyDeath()
			                 .build());
			  WindowManager windowManager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
			  Display display=windowManager.getDefaultDisplay();
			  mCurrentRotation=display.getRotation();
			  //if (mCurrentRotation==Surface.ROTATION_90)
				  //setContentView(R.layout.form_list_landscape);//agenda_form_list_landscape);
			 // else			     			
			setContentView(R.layout.fragment_container);//form_list_compound);//agenda_form_list);////activity_login);
			//formFrame=(ViewGroup)findViewById(R.id.record_content_frame);
			//listFrame=(ViewGroup)findViewById(R.id.record_list_frame);
			
			//if (!firstTime) return;
			String bTest=getResources().getString(R.string.test_agenda_key);
			noMenu=false;
			if (bTest.charAt(0)=='Y')
			{
				
				showAgendaList(null);
				return;
			}
			Intent intent = getIntent();
			String key=getResources().getString(R.string.fix_line_key);
		    String fixLine=null;
		    if (intent != null)  // no extra back from map
		    {
		    	if (intent.hasExtra(key))
		    	fixLine=intent.getExtras().getString(key);
		    	if (intent.hasExtra(MainActivity.PAGE_TITLE))
		    	{
		    		mPageTitle=intent.getExtras().getString(MainActivity.PAGE_TITLE);
		    		mCitizenId=intent.getExtras().getString(MainActivity.CITIZEN_ID);
		    	}
		    	
		    }/* for test purpose
		    fixLine="170=agenda|151=2014-07-04|152=09:00|153=軟體或網站3|154=信義路5段20號|155=台北市|156=杜名玫|157=0951357456|158=特許執照可使用期限自即日起至民國119年12月31日止。身為全台唯一取得最寬35 MHz頻譜的電信業者奠基於4G最大頻寬優勢，高、中、低價位帶完整產品線，滿足市場全方位需求，目前已推出30多款4G智慧型手機及平板選擇|";
		    */
		    if (mCitizenId==null){
		    	String fileName=MainActivity.getFileHeader()+getResources().getString(R.string.login_page);
		    	SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
		    	mCitizenId=sharedPref.getString(MainActivity.CITIZEN_ID, "--");
		    }   
		    if (fixLine != null && fixLine.toUpperCase().indexOf("CREATE")==0)
	    	{
	    			createNewAgenda();
	    			return;
	    	}
	    	
		    refreshMe(fixLine);
		}
		
		public void refreshThis(View v)
		{
			v.invalidate();
		}
		public void refreshMe(String fixLine)
		{
		    FragmentManager fragmentManager = getFragmentManager();
		    if (mFragment==null){ mFragment=(AgendaRecord) fragmentManager.findFragmentByTag(AGENDA_TAG);}
		    if (mFragment==null || fixLine!=null)
		    {
		    	showAgendaList(fixLine);
		    }
		    else if (mFragment!=null)
			{
		    	//fragmentManager.beginTransaction().add(R.id.record_list_frame, mFragment, AGENDA_TAG).commit();
		    	fragmentManager.beginTransaction().add(R.id.container, mFragment, AGENDA_TAG).commit();
		        //R.id.login_content_frame,
			}					
		}
		static Menu mActionMenu=null;
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
/*			if (mFragment != null && mActionMenu!=null && ((AgendaRecord)mFragment).getMenuItems()==null)
			{
				getActionBar().setDisplayHomeAsUpEnabled(true);
				Intent upIntent = NavUtils.getParentActivityIntent(this);
		    	NavUtils.navigateUpTo(this, upIntent);
		    	finish();
		    	return true;
			}*/
			getMenuInflater().inflate(R.menu.main, menu);
			mActionMenu=menu;
			for (int i=0; i<mActionMenu.size(); i++)
	    	{
	    		mActionMenu.getItem(i).setVisible(false);
	    			//setOnMenuItemClickListener(aListener);
	    	}
			if (noMenu) return true;
			menu.findItem(R.id.action_join).setVisible(true);
			menu.findItem(R.id.action_join).setOnMenuItemClickListener(mFragment);
			if (mFragment != null)
			mFragment.setMenuActionItem(null);
			if (getResources().getString(R.string.usr_type).charAt(0)=='b')
			{
				menu.findItem(R.id.action_new).setVisible(true);
				menu.findItem(R.id.action_new).setOnMenuItemClickListener(mFragment);
			}
			menu.findItem(R.id.action_home).setVisible(true);
			menu.findItem(R.id.action_home).setOnMenuItemClickListener(mFragment);
			
			return true;
		}
		
		public Menu getMenuBar()
		{
			return mActionMenu;
		}
		static ImageView viewToRefresh=null;

		void showAgendaList(String fixLine)
		{
			String pLine=fixLine;
			String key=getResources().getString(R.string.fix_line_key);
			/* using tables for list fragments so no file opening
			if (fixLine==null)
			{
		    	String fileName=MainActivity.getFileHeader()+AgendaRecord.getTableName();
		        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
		        pLine=sharedPref.getString(key, "--");
			}
			*/
	        Bundle args = new Bundle();
	        AgendaRecord aPP=new AgendaRecord();
	        //aPP.setFormFrame(formFrame);
	        args.putString(MainActivity.PAGE_TITLE, mPageTitle);
	        if (pLine != null)
	        	args.putString(key, pLine);
	        key=getResources().getString(R.string.citizen_id_key);
	        args.putString(key, mCitizenId);
	        args.putInt("ROTATION", mCurrentRotation);
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
	        //fragmentManager.beginTransaction().add(R.id.record_list_frame, mFragment, AGENDA_TAG).commit();
	        fragmentManager.beginTransaction().add(R.id.container,  mFragment, AGENDA_TAG).commit();
		        //R.id.login_content_frame,
	        //setTitle(mPageTitle);
	return;
		}
		
		public void getParticipantNumber(HashMap<String, String> rec)
			{
				//GetParticipantNumber reader=GetParticipantNumber.newInstance(this, rec);
				//reader.show(getFragmentManager(), "READ_it");
				Intent numIntent=new Intent(this, GetParticipantNumberDialog.class);
				startActivityForResult(numIntent, REQ_GET_PARTICIPANTS);
			}
			
		static final int REQ_PICK_IMAGE=1;
		static final int REQ_GET_PARTICIPANTS=4;
		static ImageView mProfilePhotoImage=null;
		public void refreshPhoto(View v)
		{
			//mFragment.updateProfilePhoto((ImageView)v);
		}
		
		public void getPicture(View v)
		{
			if (mFragment==null) return;
			
		}	
		public void pickDate(View view)
		{
			DatePickerFragment pFrg=new DatePickerFragment();
			pFrg.setDay0(2014, 6, 1);
			pFrg.pickDate(view, this);;
		}
		public void onTimeSet(TimePicker view, int h, int m)
		{
			DecimalFormat dF=new DecimalFormat("00");
			String sTime=dF.format(h)+":"+dF.format(m);	
			View eventTime=formFrame.findViewById(R.id.event_time);
			((EditText)eventTime).setText(sTime);
		}
		public void getEventTime() {
			TimePickerDialog pT=new TimePickerDialog(this, this, 12, 30, true);
			pT.setTitle("Event Time");
			pT.show();
		}
		static View eventDate;
		static View eventTime;
		public void getDateTime(View v)
		{
			pickDate(formFrame.findViewById(R.id.event_date));
			getEventTime();
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
				//mFragment.setPhotoUri(photo_uri);
				//mFragment.replaceProfilePhoto(mProfilePhotoImage);  
			}
			if (requestCode == REQ_GET_PARTICIPANTS)
			{
				String number=data.getStringExtra("participant");
				if (number==null || number.charAt(0)==' ' || Integer.parseInt(number)<1 ||mFragment==null) return;
				mFragment.setParticipantData(number);
				//refreshMe(null);
			}
		}
		
	    public void done()
	    {
	    	if (mActionMenu != null) {
			for (int i=0; i<mActionMenu.size(); i++)
	    	{
	    		mActionMenu.removeItem(mActionMenu.getItem(i).getItemId());
	    	}
    		mActionMenu.clear();
    		mActionMenu.close(); 
    		}
    		getActionBar().setDisplayHomeAsUpEnabled(true);
	    	//Intent it=new Intent();
	    	this.setResult(RESULT_OK);
	    	
	    	if (!noQ())Toast.makeText(this, "PENDING "+mPendingQ.size()+" MSGs Q Please Wait ", Toast.LENGTH_LONG).show();
	    	stopConnection();
	    	Intent upIntent = NavUtils.getParentActivityIntent(this);
	    	NavUtils.navigateUpTo(this, upIntent);
	    	finish();
	    	return;

	    	//showAgendaList(null);
	    }
	    
		public void activateMenuExclusive(int[] mMenuItemId) {
	    	if (mActionMenu==null) return;
	        for (int i=0; i<mActionMenu.size(); i++)
	        {
	        	mActionMenu.getItem(i).setVisible(false);
	        }
	    	for (int i=0; i<mMenuItemId.length; i++)
	    	{
	    		mActionMenu.findItem(mMenuItemId[i]).setVisible(true);
	    	}
	    	mActionMenu.findItem(R.id.action_close).setVisible(true);
		}
}
