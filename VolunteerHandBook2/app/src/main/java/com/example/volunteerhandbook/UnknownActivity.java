package com.example.volunteerhandbook;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.widget.ImageView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class UnknownActivity extends FragmentActivity {
	
	static final String AGENDA_TAG="AGENDA_TAG";
	static AgendaRecord mFragment=null;
	static String mPageTitle=null;
	static String mCitizenId=null;

	static boolean justPassby=false;
	static UnknownActivity mActivity=null;
    
	void saveDataToTable(HashSet<String> data)
	{
		return;
	}
		
	void getPendingNotificationMsg()
	{
    	String fileName="notification"+"chat";
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
    	saveDataToTable((HashSet<String>)pendingData);
    	return;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Intent intent = getIntent();
		mActivity=this;
		//if (!firstTime) return;
		getPendingNotificationMsg();
		Intent intent = getIntent();
		String key=getResources().getString(R.string.fix_line_key);
	    String fixLine=null;
	    if (intent!=null && intent.hasExtra(MainActivity.PAGE_TITLE))
	    {
	    		fixLine=intent.getExtras().getString(key);
	    
	    mPageTitle=intent.getExtras().getString(MainActivity.PAGE_TITLE);
	    mCitizenId=intent.getExtras().getString(MainActivity.CITIZEN_ID);
	    }/* for test purpose
	    fixLine="170=agenda|151=2014-07-04|152=09:00|153=軟體或網站3|154=信義路5段20號|155=台北市|156=杜名玫|157=0951357456|158=特許執照可使用期限自即日起至民國119年12月31日止。身為全台唯一取得最寬35 MHz頻譜的電信業者奠基於4G最大頻寬優勢，高、中、低價位帶完整產品線，滿足市場全方位需求，目前已推出30多款4G智慧型手機及平板選擇|";
	    */
	    if (mCitizenId==null){
	    	mCitizenId=MainActivity.getCitizenId(this);
	    }   
	   
	    FragmentManager fragmentManager = getFragmentManager();
	    if (mFragment==null){ mFragment=(AgendaRecord) fragmentManager.findFragmentByTag(AGENDA_TAG);}
	    if (mFragment==null || fixLine!=null)
	    {
	    	showChatRoom(fixLine);
	    }
	    else if (mFragment!=null)
		{
			fragmentManager.beginTransaction().add(R.id.login_content_frame, mFragment, AGENDA_TAG).commit();
	        //if (viewToRefresh!=null) viewToRefresh.invalidate();
		}
	
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
    		if (mActionMenu.getItem(i).getItemId()==R.id.action_check ||
    				mActionMenu.getItem(i).getItemId()==R.id.action_join)
    			continue;
    		mActionMenu.getItem(i).setVisible(false);
    			//setOnMenuItemClickListener(aListener);
    	}
		menu.findItem(R.id.action_join).setVisible(true);
		menu.findItem(R.id.action_join).setOnMenuItemClickListener(mFragment);
		menu.findItem(R.id.action_check).setVisible(true);
		menu.findItem(R.id.action_check).setOnMenuItemClickListener(mFragment);

		return true;
	}
	
	
	public static class TalkToPopup extends DialogFragment {
		
		public TalkToPopup()
		{
			super();
		}
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	ImageView bg=new ImageView(getActivity());
			bg.setImageResource(R.drawable.first_page);
			
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setView(bg)//setMessage(R.string.dialog_volunteer)
	               .setPositiveButton(R.string.dialog_volunteer, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   justPassby=false;
	                   }
	               })
	               .setNegativeButton(R.string.dialog_passby, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   justPassby=true;
	                	   mCid="ZZZZZ99999";
	       			 		mActivity.done();
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
	
	void showChatRoom(String fixLine)
	{
		
	}
	void checkIfNoSignIn()
	{
		TalkToPopup aPP=new TalkToPopup();
		FragmentManager manager=getFragmentManager();
		FragmentTransaction trans = manager.beginTransaction();
		aPP.show(trans, "To Start");
		//manager.beginTransaction().replace(R.id.login_content_frame, aPP).commit();   	
		return ;
	}

	static String mSavedPassword=null;
	static String mBackCode=null;
	static String mCid=null;
    void checkPassword()
    {
    	String fileName=MainActivity.getFileHeader()+getResources().getString(R.string.login_page);
        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String pwd=getResources().getString(R.string.pass_word_key);
    	 mSavedPassword = sharedPref.getString(pwd, "--");
    	 if (mSavedPassword.charAt(0) == '-')
    	 {
    		 checkIfNoSignIn();
    	 }
    	 else   //if (mSavedPassword.charAt(0)!='-')
            {
            	mCid=sharedPref.getString(MainActivity.CITIZEN_ID, "--");
            	MainActivity.setNewParameter(pwd, mSavedPassword);
            	MainActivity.setNewParameter(MainActivity.CITIZEN_ID, mCid);
            	int i=Integer.parseInt(mCid.substring(4));
            	int k=i % 9999;
            	mBackCode=(new DecimalFormat("0000")).format(k);            	
            }
           
    	LoginFragment fragment = new LoginFragment();
    	fragment.setActivity(this);
    	Bundle args = new Bundle();
    	args.putString(pwd, mSavedPassword);
    	args.putString(LoginFragment.BKP_PASSWORD, mBackCode);
        fragment.setArguments(args);
    	FragmentManager fragmentManager = getFragmentManager();
    	fragmentManager.beginTransaction().replace(R.id.login_content_frame, fragment).commit();
    	mSavedPassword=null;
    }
    
    public void done()
    {
    	Intent it=getIntent();
    	if (mCid==null)
    	{
    		mCid=getIntent().getStringExtra(MainActivity.CITIZEN_ID);
    	}
    	it.putExtra(MainActivity.CITIZEN_ID, mCid);
    	setResult(MainActivity.REQ_LOGIN, it);
    	finish();
    }
	
}
