package com.example.volunteerhandbook;

import android.support.v4.app.FragmentActivity;


import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

	public class CommitmentActivity extends FragmentActivity {

		public CommitmentActivity() {
			// TODO Auto-generated constructor stub
		}
		
		static final String COMMITMENT_TAG="COMMITMENT_TAG";
		static CommitmentForm mFragment=null;
		static String mPageTitle=null;
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
		    mPageTitle=intent.getExtras().getString(CommitmentForm.PAGE_TITLE);
		    mCitizenId=intent.getExtras().getString(MainActivity.CITIZEN_ID);
		    if (mCitizenId==null){
	    	String fileName=MainActivity.getFileHeader()+getResources().getString(R.string.login_page);
	        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
	        key=getResources().getString(R.string.pass_word_key);
	        String savedPassword = sharedPref.getString(key, "--");
	            if (savedPassword.charAt(0)!='-')
	            {
	            	mCitizenId=sharedPref.getString(MainActivity.CITIZEN_ID, "--");
	            }
		    }   
		    FragmentManager fragmentManager = getFragmentManager();
	        mFragment=(CommitmentForm) fragmentManager.findFragmentByTag(COMMITMENT_TAG);
		    if (mFragment==null || fixLine!=null)
		    {
		    	openCommitForm(fixLine);
		    }
		    else if (mFragment!=null)
			{
				fragmentManager.beginTransaction().add(R.id.login_content_frame, mFragment, COMMITMENT_TAG).commit();
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

		public void pickDate(View view)
		{
			DatePickerFragment pFrg=new DatePickerFragment();
			pFrg.pickDate(view, this);
		}
	    
		void openCommitForm(String fixLine)
		{
			String pLine=fixLine;
			String key=getResources().getString(R.string.fix_line_key);
			if (fixLine==null)
			{
		    	String fileName=MainActivity.getFileHeader()+CommitmentForm.getTableName();
		        SharedPreferences sharedPref = getSharedPreferences(fileName, Context.MODE_PRIVATE);
		        pLine=sharedPref.getString(key, "--");
			}
	        Bundle args = new Bundle();
	        CommitmentForm aPP=new CommitmentForm();
	        args.putString(CommitmentForm.PAGE_TITLE, mPageTitle);
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
	        fragmentManager.beginTransaction().add(R.id.login_content_frame, mFragment, COMMITMENT_TAG).commit();
	        //viewToRefresh=aPP.getProfilePhoto();
	        //setTitle(mPageTitle);
	return;
		}
		
		static final int REQ_PICK_IMAGE=1;
		static ImageView mProfilePhotoImage=null;
		public void refreshPhoto(View v)
		{
			//mFragment.updateProfilePhoto((ImageView)v);
		}
		
		public void getPicture(View v)
		{
			if (mFragment==null) return;
			//mFragment.getPicture(v);
			/*
			Button vB=(Button)v;
			mProfilePhotoImage=(ImageView)((View)vB.getParent()).findViewById(R.id.profile_photo);
			if (vB.getId()==R.id.get_profile_photo)
			{
				Intent pickIntent = new Intent();
				pickIntent.setType("image/*");
				pickIntent.setAction(Intent.ACTION_GET_CONTENT);

				Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
				Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
				chooserIntent.putExtra
				(
				  Intent.EXTRA_INITIAL_INTENTS, 
				  new Intent[] { takePhotoIntent }
				);
				startActivityForResult(chooserIntent, REQ_PICK_IMAGE);
			}
			*/
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
	    	//Intent it=new Intent();
	    	this.setResult(MainActivity.REQ_COMMITMENT);
	    	finish();
	    }
	

}
