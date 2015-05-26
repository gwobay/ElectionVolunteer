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
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

public class WorkMemoActivity extends JobActivity
							//FragmentActivity 
					implements View.OnClickListener, TimePickerDialog.OnTimeSetListener
{

		public WorkMemoActivity() {
			// TODO Auto-generated constructor stub
		}
		
		static final String MEMO_TAG="MEMO_TAG";
		static WorkMemoRecord mFragment=null;
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
		
	void doTest()
		{
			HashSet<String> set=new HashSet<String>();
			set.add("170=memo|151=2014-05-21|152=17:20|153=新三民主義：庶民、鄉民、公民|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「台北市長參選人柯文哲日前提出「新三民主義：庶民、鄉民、公民」，依此概念招募競選團隊成員，包括發言人、隨行秘書等職務都開放徵選，24日將在華山文創園區辦理「海選」活動|");

			set.add("170=memo|151=2014-05-23|152=17:20|153=在野整合辯論|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「今天是我們要去拜託別人，所以對方開的條件 我們都會接受，每個人按照他的想法，努力的去做 至於有沒有辦法，|");


			set.add("170=memo|151=2014-05-24|152=17:20|153=青年海選計畫|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「徵求發言人與隨行祕書。因選戰繁忙，徵選時還要測驗體能，薪資則優於國科會助理標準，大學畢業約32K、碩士36K起跳|");
		
			set.add("170=memo|151=2014-05-24|152=17:20|153=看透新聞|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=鄭弘儀|158=「但是我們真的不要小看國民黨，畢竟是百年老店，它在基層的組織其實是滿完整的，只要資源下去，它的動員是非常快的」。須以一個謹慎態度來面對二○一四的選舉|");
			set.add("170=memo|151=2014-06-21|152=17:20|153=新三民主義：庶民、鄉民、公民2|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「台北市長參選人柯文哲日前提出「新三民主義：庶民、鄉民、公民」，依此概念招募競選團隊成員，包括發言人、隨行秘書等職務都開放徵選，24日將在華山文創園區辦理「海選」活動|");

			set.add("170=memo|151=2014-06-23|152=17:20|153=在野整合辯論2|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「今天是我們要去拜託別人，所以對方開的條件 我們都會接受，每個人按照他的想法，努力的去做 至於有沒有辦法，|");


			set.add("170=memo|151=2014-06-24|152=17:20|153=青年海選計畫2|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「徵求發言人與隨行祕書。因選戰繁忙，徵選時還要測驗體能，薪資則優於國科會助理標準，大學畢業約32K、碩士36K起跳|");
		
			set.add("170=memo|151=2014-06-24|152=17:20|153=看透新聞2|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=鄭弘儀|158=「但是我們真的不要小看國民黨，畢竟是百年老店，它在基層的組織其實是滿完整的，只要資源下去，它的動員是非常快的」。須以一個謹慎態度來面對二○一四的選舉|");
			//saveNotifyDataToTable(set);			
		}
		

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
			
			     
			//((WindowManager)
			WindowManager windowManager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
			Display display=windowManager.getDefaultDisplay();
			if (display.getRotation()==Surface.ROTATION_90)
				setContentView(R.layout.memo_form_list_landscape);
			else
			setContentView(R.layout.memo_form_list);//login_activity);//activity_login);
			
			formFrame=(ViewGroup)findViewById(R.id.memo_form);
			
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
		    	if (fixLine != null)
		    	{
		    		/*if ( fixLine.indexOf("GCM>>")==0){
		    			getPendingNotificationMsg();
		    			fixLine=fixLine.substring(5);
		    		}*/
			    	
		    	}
		    }/* for test purpose
		    fixLine="170=memo|151=2014-07-04|152=09:00|153=軟體或網站3|154=信義路5段20號|155=台北市|156=杜名玫|157=0951357456|158=特許執照可使用期限自即日起至民國119年12月31日止。身為全台唯一取得最寬35 MHz頻譜的電信業者奠基於4G最大頻寬優勢，高、中、低價位帶完整產品線，滿足市場全方位需求，目前已推出30多款4G智慧型手機及平板選擇|";
		    */
		    if (mCitizenId==null){
		    	String fileName=MainActivity.getFileHeader()+getResources().getString(R.string.login_page);
		    	SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
		    	mCitizenId=sharedPref.getString(MainActivity.CITIZEN_ID, "--");
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
		    if (mFragment==null){ mFragment=(WorkMemoRecord) fragmentManager.findFragmentByTag(MEMO_TAG);}
		    if (mFragment==null || fixLine!=null)
		    {
		    	showMemoList(fixLine);
		    }
		    else if (mFragment!=null)
			{
		    	//fragmentManager.beginTransaction().add(R.id.record_list_frame, mFragment, MEMO_TAG).commit();
		    	fragmentManager.beginTransaction().add(mFragment, MEMO_TAG).commit();
		        //if (viewToRefresh!=null) viewToRefresh.invalidate();
			}					
		}
		static Menu mActionMenu=null;
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			
/*			if (mFragment != null && mActionMenu!=null && ((MemoRecord)mFragment).getMenuItems()==null)
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
	    		if (mActionMenu.getItem(i).getItemId()==R.id.action_check ||
	    				mActionMenu.getItem(i).getItemId()==R.id.action_join)
	    			continue;
	    		mActionMenu.getItem(i).setVisible(false);
	    			//setOnMenuItemClickListener(aListener);
	    	}
			menu.findItem(R.id.action_join).setVisible(true);
			menu.findItem(R.id.action_join).setOnMenuItemClickListener(mFragment);
			if (mFragment != null)
			mFragment.setMenuItems(menu);
			/*if (getResources().getString(R.string.usr_type).charAt(0)=='b')
			{
				menu.findItem(R.id.action_new).setVisible(true);
				menu.findItem(R.id.action_new).setOnMenuItemClickListener(mFragment);
			}
			menu.findItem(R.id.action_home).setVisible(true);
			menu.findItem(R.id.action_home).setOnMenuItemClickListener(mFragment);*/
			
			return true;
		}
		
		public Menu getMenuBar()
		{
			return mActionMenu;
		}
		static ImageView viewToRefresh=null;

		public void activateMenu()
		{
			if (mActionMenu==null || mFragment==null) return;
			mFragment.setMenuItems(mActionMenu);

		}

		void showMemoList(String fixLine)
		{
			String pLine=fixLine;
			String key=getResources().getString(R.string.fix_line_key);
			Bundle args = new Bundle();
	        WorkMemoRecord aPP=new WorkMemoRecord();
	        args.putString(MainActivity.PAGE_TITLE, mPageTitle);
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
	        //fragmentManager.beginTransaction().add(R.id.record_list_frame, mFragment, MEMO_TAG).commit();
	        fragmentManager.beginTransaction().add( mFragment, MEMO_TAG).commit();
		        //viewToRefresh=aPP.getProfilePhoto();
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
			/*DecimalFormat dF=new DecimalFormat("00");
			String sTime=dF.format(h)+":"+dF.format(m);	
			View eventTime=formFrame.findViewById(R.id.memo_time);
			((EditText)eventTime).setText(sTime);*/
		}
		public void getEventTime() {
			TimePickerDialog pT=new TimePickerDialog(this, this, 12, 30, true);
			pT.setTitle("Event Time");
			pT.show();
		}
		static View eventDate;
		static View eventTime;
		public void getDate(View v)
		{
			pickDate(formFrame.findViewById(R.id.memo_date));
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
	    	//if (mFragment != null) mFragment.closeDb();
	    	if (!noQ())Toast.makeText(this, "PENDING "+mPendingQ.size()+" MSGs Q Please Wait ", Toast.LENGTH_LONG).show();
	    	stopConnection();
	    	Intent upIntent = NavUtils.getParentActivityIntent(this);
	    	NavUtils.navigateUpTo(this, upIntent);
	    	finish();
	    	return;

	    	//showMemoList(null);
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
