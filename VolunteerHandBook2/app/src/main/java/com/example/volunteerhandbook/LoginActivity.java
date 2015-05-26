package com.example.volunteerhandbook;

import java.text.DecimalFormat;

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
public class LoginActivity extends FragmentActivity {
	static boolean justPassby=false;
	static LoginActivity mActivity=null;
    	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Intent intent = getIntent();
		mActivity=this;
		setContentView(R.layout.login_activity);//activity_login);
		checkPassword();
		// Set up the login form.
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}*/

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	
	public static class FistPagePopup extends DialogFragment {
		
		public FistPagePopup()
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
	
	void checkIfNoSignIn()
	{
		FistPagePopup aPP=new FistPagePopup();
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
