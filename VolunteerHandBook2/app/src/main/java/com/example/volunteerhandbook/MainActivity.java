/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.volunteerhandbook;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kou.utilities.AsyncSocketTask;
import com.kou.utilities.MessageParser;
import com.kou.utilities.RowStruct;



public class MainActivity extends FragmentActivity implements MediaPlayer.OnCompletionListener
{
	public static final String FROM_SERVER="FROM_SERVER";
	public static final String CITIZEN_ID="citizen_id";
	
    static private DrawerLayout mDrawerLayout;
    static private ListView mDrawerList;
    private static ActionBarDrawerToggle mDrawerToggle;

    private static Menu mActionMenu;
    private static CharSequence mDrawerTitle;
    private static CharSequence mTitle;
    private static String[] mPageTitles, mPages;
    private static String mPassword=null;
    private static String mFixKey=null;

    static HashMap<String, Object> globalParameters=new HashMap<String, Object>();
    private static MediaPlayer mSpeaker=null;
    private static boolean firstTime=true;
    private static String mCitizenId=null;
    private static String mRegid=null;
    private static boolean noDrawYet=true;
    
    private static boolean imBoss=false;
    public static Context globalContext=null;
    public static final String PAGE_TITLE = "page_title";
    public static String getFileHeader()
    {
    	return "kp.gif";//av.getResources().getString(R.string.candidate_logo);
    }
    public static Context getPackageContext()
    {
    	return globalContext;
    }
    public static String getCitizenId()
    {
    	return mCitizenId;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (globalContext==null) globalContext=getApplicationContext();
        mFixKey=getResources().getString(R.string.fix_line_key);
        
        mTitle = mDrawerTitle = getTitle();
        mPageTitles = getResources().getStringArray(R.array.page_array);
        mPages = getResources().getStringArray(R.array.page_array_EN);
 
        String sBoss="NO";//getResources().getString(R.string.im_boss);
		imBoss=false;//(sBoss.charAt(0)=='Y');
		//if (!imBoss) init();
		//else noDrawYet=false;
        //if (!noDrawYet)
        {
	        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
	        mDrawerList = (ListView) findViewById(R.id.main_left_drawer);
	
	        // set a custom shadow that overlays the main content when the drawer opens
	        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	        // set up the drawer's list view with items and click listener
	        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
	                					R.layout.drawer_list_item, mPageTitles));
	        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	
	        // enable ActionBar app icon to behave as action to toggle nav drawer
	        getActionBar().setDisplayHomeAsUpEnabled(true);
	        getActionBar().setHomeButtonEnabled(true);
	
	        // ActionBarDrawerToggle ties together the the proper interactions
	        // between the sliding drawer and the action bar app icon
	        mDrawerToggle = new ActionBarDrawerToggle(
	                this,                  /* host Activity */
	                mDrawerLayout,         /* DrawerLayout object */
	                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
	                R.string.drawer_open,  /* "open drawer" description for accessibility */
	                R.string.drawer_close  /* "close drawer" description for accessibility */
	        ) 
	        {
	            public void onDrawerClosed(View view) 
	            {
	                getActionBar().setTitle(mTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	
	            public void onDrawerOpened(View drawerView) 
	            {
	                getActionBar().setTitle(mDrawerTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	        };
	        mDrawerLayout.setDrawerListener(mDrawerToggle);
	        //loop_what_I_thought();//
	        //show_what_I_thought();
	    }

        mCitizenId="K123456789";
        mPassword="1234";//(String) globalParameters.get("PASSWORD");
        if (savedInstanceState == null) {
        	//if (mCitizenId==null && globalParameters.get(CITIZEN_ID)==null)
        	//checkPassword();
        }
        firstTime=false;
        String fixKey=getResources().getString(R.string.fix_line_key);
        if (getIntent().hasExtra(fixKey))
    	{
        	//if (GcmIntentService.noise != null) GcmIntentService.noise.stop();
    		String fixLine=getIntent().getStringExtra(getResources().getString(R.string.fix_line_key));
    		String tableName=DataStorage.getTableName(fixLine);
    		if (tableName.equalsIgnoreCase("profile"))
    			openPersonalPage(fixLine);
    		else if (tableName.equalsIgnoreCase("commitment"))
    			openCommitmentPage(fixLine);
    		else if (tableName.equalsIgnoreCase("agenda"))
    		{
    			openAgendaPage(fixLine);
    		}
    		else if (tableName.equalsIgnoreCase("chatroom"))
    			openForumChatPage(fixLine);
        		//openChatRoomPage(fixLine);
    	}
       // else
       // openPersonalPage(null);
       // mSpeaker=null;
       // showProgress("test");
    }
    float endY ,h, hBox=0;
    FrameLayout container;;

    void show_what_I_thought()
    {
    	//thoughts = getResources().getStringArray(R.array.what_I_thought);
        
        //container = (FrameLayout) findViewById(R.id.main_content_frame);
        return;
        /*
        container.setClipChildren(false);
        container.removeAllViews();
        LinearLayout textBox=new LinearLayout(this);
        textBox.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textBox.setOrientation(1);
        for (int i=3; i<thoughts.length; i++)
        {
        	TextView aLine = new TextView(this);
        	aLine.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        	aLine.setText(thoughts[i]);
        	aLine.setTextSize(24);
        	aLine.setTypeface(Typeface.DEFAULT_BOLD, 1);
        	aLine.setTextColor(Color.parseColor("#99FF66"));
	        textBox.addView(aLine);
	    }
        //toMove.setTranslationY(600);
        container.addView(textBox);
        textBox.setX(20);
        textBox.setY(40);
        float startY = textBox.getY();
        endY = container.getHeight() - 100f;
        h = (float)container.getHeight();
        int duration = (int)(20000);
        ValueAnimator bounceAnim = ObjectAnimator.ofFloat(textBox, "y", startY, endY);
        bounceAnim.setDuration(duration);
        bounceAnim.setInterpolator(new AccelerateInterpolator());
        //bounceAnim.start();
        */
    }
    String[] thoughts;       
    TextView[] toMove;
    int iTh=0, iTx=0, iLoop=0;
    int lastone=3;
    void loopNewBox(LinearLayout iBox)
    {
    	if (lastone<=1)
    	{
    		show_what_I_thought();
    		return;
    	}
    	if (iTh > 3 *thoughts.length+2) 
    		{
    			lastone--;
    		return;
    		}
    	float bH=(int)(1.2*iBox.getHeight());
    	iBox.removeAllViews();
    	for (int i=0; i<thoughts.length/4; i++)
    	{
    		iBox.addView(toMove[iTh++ % thoughts.length]);       		
    	}      
    //toMove.setTranslationY(600);
    //container.addView(textBox);
    	iBox.setX(20);
    	iBox.setY(2*bH);
    	//hBox += iBox.getHeight();
    	float startY = iBox.getY();
    	endY=(-1)*bH;
    	int duration = 20000;           
    ValueAnimator bounceAnim = ObjectAnimator.ofFloat(iBox, "y", startY, endY);
    	bounceAnim.setDuration(duration);
    	bounceAnim.setInterpolator(new LinearInterpolator());
    	final LinearLayout fBox=iBox; 
    	bounceAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loopNewBox(fBox);
            }
        });
    	bounceAnim.start();
    }
    void loop_what_I_thought()
    {
    	thoughts = getResources().getStringArray(R.array.what_I_thought);       
        int lines=thoughts.length/4;
        container = (FrameLayout) findViewById(R.id.main_content_frame);
        LinearLayout[] textBox=new LinearLayout[3];
        endY = container.getHeight() - 100f;
        h = (float)container.getHeight();
        hBox=0;
        /*
        for (int k=0; k<3; k++){
        	textBox[k]=new LinearLayout(this);
        	textBox[k].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textBox[k].setOrientation(1);
        }
        */
        //LinearLayout tmp=(LinearLayout) getLayoutInflater().inflate(R.layout.one_line, null);
        //TextView  tmpTxt=(TextView)tmp.findViewById(R.id.oneLine);
        //tmp.removeView(tmpTxt);
        int[] color=new int[4];
        color[3]=Color.parseColor("#66FF66");
        color[1]=Color.parseColor("#FF9900");
        color[2]=Color.parseColor("#0099FF");
        color[0]=Color.parseColor("#00FF00");
        toMove=new TextView[thoughts.length];
        int textSize=24;
        for (int i=0; i<thoughts.length; i++)
        {
        	toMove[i] = new TextView(this);
            toMove[i].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	        toMove[i].setText(thoughts[i]);
	        toMove[i].setTextSize(textSize);
	        toMove[i].getPaint().setFakeBoldText(true);
	        toMove[i].setTextColor(color[1]-0x20202*i);
	        //textBox.addView(toMove);
	    }
        int boxHeight=(lines+1)*(int)(textSize*1.5);
        for (int k=0; k<3; k++)
        {
        	textBox[k].removeAllViews();
        	for (int i=0; i<thoughts.length/4; i++)
        	{
        		TextView v=toMove[iTh++ % thoughts.length];
        		v.setTextColor(color[k]);
        		textBox[k].addView(v);       		
        	}      
        //toMove.setTranslationY(600);
        //container.addView(textBox);
        	container.addView(textBox[k]);        
            textBox[k].setX(20);
        	textBox[k].setY(2*boxHeight+hBox);
        	hBox += boxHeight; //textBox[k].getHeight();
        	float startY = textBox[k].getY();
        	endY=(-1)*boxHeight;
        	int duration = (int)(((startY-endY)/(3*boxHeight))*20000);           
        ValueAnimator bounceAnim = ObjectAnimator.ofFloat(textBox[k], "y", startY, endY);
        	bounceAnim.setDuration(duration);
        	bounceAnim.setInterpolator(new LinearInterpolator());
        	final LinearLayout iBox=textBox[k]; 
        	bounceAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loopNewBox(iBox);
                }
            });
        	bounceAnim.start();
        } 
        
    }
    void checkPassword()
    {
    	Intent logIntent=new Intent(this, LoginActivity.class);
    	startActivityForResult(logIntent, REQ_LOGIN);    	
    }
    static Fragment mCurrentFragment;
    
    static int mCurrentPage;
  
    private void registerNow(SharedPreferences idFile)
    {
    	GcmRegistration toReg=new GcmRegistration(this, idFile);
    	toReg.doIt();
    }
    private boolean checkIfRegistered()
    {
        //String fileName=getResources().getString(R.string.app_name)+"register_id";
        SharedPreferences sharedPref = getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);//getSharedPreferences(fileName, Context.MODE_PRIVATE);
        mRegid = sharedPref.getString("registration_id", "--");//REGISTRATION_ID", "--");
        if (mRegid.charAt(0)=='-') registerNow(sharedPref);
        return true;
    }
    
    
    public static String getCitizenId(Activity mAty)
    {
    	String fileName=MainActivity.getFileHeader()+mAty.getResources().getString(R.string.login_page);
    	SharedPreferences sharedPref = mAty.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    	return sharedPref.getString(MainActivity.CITIZEN_ID, "--");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        mActionMenu=menu;
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) 
    {
    	if (!noDrawYet && mDrawerLayout!=null) 
        mDrawerLayout.isDrawerOpen(mDrawerList);
        for (int i=0; i<menu.size(); i++)
        {
        	menu.getItem(i).setVisible(false);
        }
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        mActionMenu=menu;
        return super.onPrepareOptionsMenu(menu);
    }

    public void activateMenuExclusive(int[] menu_R_id)
    {
    	if (mActionMenu==null) return;
        for (int i=0; i<mActionMenu.size(); i++)
        {
        	mActionMenu.getItem(i).setVisible(false);
        }
    	for (int i=0; i<menu_R_id.length; i++)
    	{
    		mActionMenu.findItem(menu_R_id[i]).setVisible(true);
    	}
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if (noDrawYet) return true;
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) 
        {
            return true;
        }
        // Handle action buttons
        closeSpeaker();
        ListRecordBase record=(ListRecordBase)mCurrentFragment;
        int itemId = item.getItemId();
        switch (itemId)
        {
        case R.id.action_new:        	
        case R.id.action_modify:
        case R.id.action_delete:
        case R.id.action_check:
        case R.id.action_save:
        	record.getArguments().putInt(ListRecordBase.ACTION_TYPE, itemId);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_content_frame, mCurrentFragment).commit();
        	break;
        case R.id.action_join:
        	break;
        case R.id.action_websearch:
			// create intent to perform web search for this planet
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			// catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) 
                startActivity(intent);
            else 
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
		default:
			super.onOptionsItemSelected(item);
		}
        return true;
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener 
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
            selectItem(position);
        }
    }

    private void selectItem(int position) 
    {
    	if (mPassword==null || mPassword.length() < 4) 
		{
			checkPassword();
			return;
		}
    	if (noDrawYet) return;
    	if (position == mPageTitles.length-1) 
    		{
    		closeSpeaker();
    		finish();
    		}
        /*
        <item>0候選人施政藍圖</item>
        <item>1新議題及活動通知</item>
        <item>2候選人最近的活動照片</item>
        <item>3候選人現在位置</item>
        <item>4申請主辦社區活動</item>
        <item>5工作交代與討論</item>        
        <item>6主題討論與志工對話室</item>
        <item>7我的拜訪記錄</item>
        <item>8我的募款記錄</item>
        <item>9我的近期工作備忘錄</item>
        <item>10我自訂的工作目標</item>
        <item>11個人資料</item>
        <item>12關閉結束</item>
        */
    	MenuItem.OnMenuItemClickListener aListener;
        Fragment fragment = null; 
        ListRecordBase aList=null;
        Bundle args = new Bundle();
        switch (position)
        {
        case 0:
        case 1:
        	openAgendaPage(null);
        	return;
        case 2:
        case 3:
        case 4:
        	setUpMyAgenda("Create New");
        	return;
        case 6:
        	openForumChatPage(null);
        	return;
        case 10:
        	openCommitmentPage(null);
        	return;
        case 11:
        	openPersonalPage(null);
        	return;
        
        case 9:
        case 8:
        case 5:
        	openMemoPage(null);
        	return;

        case 7:
        	aList=new VisitRecord();
        	break;

             default:
            	 return;
        }
        	
        	args.putString(PAGE_TITLE, mPages[position]);   
        	aListener = aList;
        	//String name=getResources().getStringArray(R.array.page_array_EN)[position];
        	args.putInt(ListRecords.PARENT_PASSDOWN_KEY, position);
        	fragment=(Fragment)aList;

        fragment.setArguments(args);
        mCurrentFragment=fragment;
        mCurrentPage=position;
        for (int i=0; i<mActionMenu.size(); i++)
        {
        	mActionMenu.getItem(i).setOnMenuItemClickListener(aListener);
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content_frame, fragment).commit();
        
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPageTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public static void setNewParameter(String key, String value)
    {
    	globalParameters.put(key, value);
    }
    
    public String getParameter(String key)
    {
    	return globalParameters.get(key).toString();
    }
    
    public void openChatRoomPage(String fixMsg)
    {
    	noDrawYet=false;
    	//startSpeaker();
    	Intent pIntent=new Intent(this, UnknownActivity.class);
    	pIntent.putExtra(CITIZEN_ID, mCitizenId);
    	pIntent.putExtra(PAGE_TITLE, mPageTitles[5]);
    	pIntent.putExtra(mFixKey, fixMsg);
    	startActivityForResult(pIntent, REQ_CHATROOM);    	
    }

    //static final String AgendaFragmentTag="AGEND";
    public void openMemoPage(String fixMsg)
    {
    	noDrawYet=false;
    	//startSpeaker();
    	Intent pIntent=new Intent(this, WorkMemoActivity.class);
    	pIntent.putExtra(CITIZEN_ID, mCitizenId);
    	pIntent.putExtra(PAGE_TITLE, mPageTitles[8]);
    	pIntent.putExtra(mFixKey, fixMsg);
    	startActivity(pIntent);    	
    }

    void openForumChatPage(String fixMsg)
    {
    	noDrawYet=false;
    	//startSpeaker();
    	Intent pIntent=new Intent(this, ChatRoomActivity.class);
    	pIntent.putExtra(CITIZEN_ID, mCitizenId);
    	pIntent.putExtra(PAGE_TITLE, mPageTitles[5]);
    	pIntent.putExtra(mFixKey, fixMsg);
    	startActivityForResult(pIntent, REQ_CHATROOM);   
    }
    //static final String AgendaFragmentTag="AGEND";
    public void setUpMyAgenda(String fixMsg)
    {
    	noDrawYet=false;
    	//startSpeaker();
    	Intent pIntent=new Intent(this, AgendaActivity.class);
    	pIntent.putExtra(CITIZEN_ID, mCitizenId);
    	pIntent.putExtra(PAGE_TITLE, mPageTitles[4]);
    	pIntent.putExtra(mFixKey, fixMsg);
    	startActivityForResult(pIntent, REQ_AGENDA);    	
    }

    public void openAgendaPage(String fixMsg)
    {
    	noDrawYet=false;
    	//startSpeaker();
    	Intent pIntent=new Intent(this, AgendaActivity.class);
    	pIntent.putExtra(CITIZEN_ID, mCitizenId);
    	pIntent.putExtra(PAGE_TITLE, mPageTitles[1]);
    	pIntent.putExtra(mFixKey, fixMsg);
    	startActivityForResult(pIntent, REQ_AGENDA);    	
    }

    public void openPersonalPage(String fixMsg)
    {
    	if (imBoss) return;
    	startSpeaker();
    	Intent pIntent=new Intent(this, ProfileActivity.class);
    	pIntent.putExtra(CITIZEN_ID, mCitizenId);
    	pIntent.putExtra(mFixKey, fixMsg);
    	startActivityForResult(pIntent, REQ_PROFILE);    	
    }
    
    public void openCommitmentPage(String fixMsg)
    {
    	if (imBoss) return;
    	
    	startSpeaker();
    	Intent pIntent=new Intent(this, CommitmentActivity.class);
    	pIntent.putExtra(CITIZEN_ID, mCitizenId);
    	pIntent.putExtra(PAGE_TITLE, mPageTitles[10]);
    	pIntent.putExtra(mFixKey, fixMsg);
    	startActivityForResult(pIntent, REQ_COMMITMENT); 
    }
    
    @Override
    public void setTitle(CharSequence title) 
    {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) 
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (!noDrawYet && mDrawerToggle !=null)
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (!noDrawYet && mDrawerToggle != null)
        	mDrawerToggle.onConfigurationChanged(newConfig);
    }
/*--------------------------------------------------- page related --------*/
    void closeSpeaker()
    {
    	if (mSpeaker == null) return;
    	mSpeaker.stop();
    	mSpeaker.release();
    	mSpeaker=null;
    }

    public void onCompletion(MediaPlayer mp)
    {
    	closeSpeaker();
    }
    
    private void startSpeaker()
    {
    	if (mSpeaker != null ) return;
    	//if (!firstTime) return;
    	mSpeaker = MediaPlayer.create(this, R.raw.kp_grateful);
    	mSpeaker.setOnCompletionListener(this);
    	mSpeaker.start();
    }
    public void init()//onWindowFocusChanged (boolean hasFocus)
    {
    	if (!firstTime) return;
    	//super.onWindowFocusChanged(hasFocus);
    	//startSpeaker();
    	checkIfRegistered();
    	firstTime=false;
    	//showProgress("Test");
    }

    ShowProgressDialog mDialog;
    AnimationDrawable mAnimation;
    View viewToRemove;
    FrameLayout ContainerOfProgressView;

	void processResponse(Vector<RowStruct> response)
	{
		if (response==null ) return;
		//GcmIntentService aNf=new GcmIntentService();
		for (int i=0; i<response.size(); i++)
		{
			RowStruct bRow=response.get(i);
			HashMap<String, String> aRow=bRow.toHashMapString();
			if (aRow==null) continue;
			String whichPage=(String)aRow.get("table_name");
			if (whichPage.equals("profile"))
			{
				ProfilePage p=new ProfilePage();
				//aNf.localNotification(p.getFixLine(aRow));
			} else if (whichPage.equals("commitment"))
			{
				CommitmentForm c=new CommitmentForm();
				//aNf.localNotification(c.getFixLine(aRow));
			}  else if (whichPage.equals("visited"))
			{
				
			}  else if (whichPage.equals("fund_raised"))
			{
				
			}  else if (whichPage.equals("agenda"))
			{
				
			}  else if (whichPage.equals("candidate_vocal"))
			{
				
			}  else if (whichPage.equals("blue_print"))
			{
				
			}
		}
	}

	void updateHomePageView(HashMap<String, String> aRow)
	{
		
	}
    String mPageName;
    void processResponse(String aLine)
    {
    	RowStruct aRow=(new MessageParser()).lineToRow(aLine);
    	if (aRow == null) return;
    	aRow.get("table_name");
    }
    


    String getFixLine(HashMap<String, String> aRow, String tblName)
    {
    	//always attach 186=mCitizenId
    	aRow.put("citizen_id",  mCitizenId);
    	RowStruct r=new RowStruct();
		Set<String> keys=aRow.keySet();
		Iterator<String> itr=keys.iterator();
		while (itr.hasNext())
		{
			String key= itr.next();
			r.put(key, aRow.get(key));
		}
    	String aLine=(new MessageParser()).composeFixText(r, tblName);
    	return "186="+mCitizenId+"|"+aLine;
    }
  

    public void doModify(View view)
    {
        // update the main content by replacing fragments
        selectItem(mCurrentPage);
    }
        

    public void doSend(View view)
    {
    	
    }

    void updateAgendaInBackground()
    {
    	final Activity me=this;
    	final AgendaRecord aPP=new AgendaRecord();
    	Thread agendaThd=new Thread(new Runnable(){
    		public void run()
    		{
    			//AgendaRecord.Parasite aPP=new AgendaRecord.Parasite(me);
    			aPP.setActivity(me);
    			aPP.checkServer(true);
    			aPP.checkServerForUpdate();
    			//aPP.updateData();   			
    		}
    	});   	
    	agendaThd.start();
    }

	static final int REQ_PICK_IMAGE=1;
	static final int REQ_LOGIN=11;
	static final int REQ_PROFILE=10;
	static final int REQ_COMMITMENT=9;
	static final int REQ_AGENDA=21;
	static final int REQ_CHATROOM=15;
	static ImageView mProfilePhotoImage=null;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{			
		
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQ_LOGIN)
		{
			updateAgendaInBackground();
			String id=data.getStringExtra(CITIZEN_ID);
			if (id != null) mCitizenId=id;
			if (mCitizenId.indexOf("ZZZ") == 0)
				openAgendaPage(null);
			else
			{				
				openPersonalPage(null);
			}
		}
		else if (requestCode == REQ_PROFILE)
		{
			openCommitmentPage(null);
		}
		else
		{
			openAgendaPage(null);
		}
	}

}