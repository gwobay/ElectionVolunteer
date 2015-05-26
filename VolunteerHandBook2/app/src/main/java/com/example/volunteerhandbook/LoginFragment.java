package com.example.volunteerhandbook;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

public class LoginFragment extends Fragment {
	public static final String BKP_PASSWORD="BKP_PASSWORD";
	
	public LoginFragment() {
		// TODO Auto-generated constructor stub
	}
	
	static LinearLayout.LayoutParams llParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
			LinearLayout.LayoutParams.WRAP_CONTENT);
	static TableLayout.LayoutParams tbParams=new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
			LinearLayout.LayoutParams.WRAP_CONTENT);
	static LinearLayout mRoot=null;
	static Activity mActivity=null;
	static Context mContext=null;
	public void setActivity(Activity mv)
	{
		mActivity=mv;
		mContext=mv.getApplicationContext();
	}
	static TextView mAlpha=null;
	static TextView mCurrentText=null;
	static Button mOK=null;
	static int mColumn=-1;
	static int mRow=0; // 0 for id, 1 for password;
	static TextView[] mIdDigits=null;
	static TextView[] mPasswords=null;
	static TableLayout mTable=null;
	static char[] myCodes=new char[4];
	static String mSavedCode="";
	static String mBkpCode="";
	static boolean isDigitPad=false;
	static boolean newUser=false;
	void showFocusColor(TextView v)
	{
		v.setTextColor(Color.MAGENTA);
		v.invalidate();
	}
	void showNormalColor(TextView v)
	{
		v.setTextColor(Color.BLACK);
		v.invalidate();
	}

	class LL implements Button.OnClickListener
	{
		int which;
		public LL(int i)
		{
			which=i;
		}
		public void onClick(View v)
		{
			if (mCurrentText!=null) showNormalColor(mCurrentText);
			mRow=0;
			showFocusColor(mIdDigits[which]);
			mCurrentText=mIdDigits[which];
			mColumn=which-1;
			
			getDigits();
		}
	}
	
	class LP implements Button.OnClickListener
	{
		int which;
		public LP(int i)
		{
			which=i;
		}
		public void onClick(View v)
		{
			if (mCurrentText!=null) showNormalColor(mCurrentText);
			mRow=1;
			showFocusColor(mPasswords[which]);
			mCurrentText=mPasswords[which];
			mColumn=which-1;
			getDigits();
		}
	}
	
	private class ButtonInBox extends LinearLayout {
		Button mButton;
		
		public ButtonInBox(Context cx, String text, boolean isLarge)
		{
			super(cx);
			Drawable shape=null;
			if (!isLarge) shape = getResources().getDrawable(R.drawable.shape_oval_orange);
			else shape = getResources().getDrawable(R.drawable.shape_oval_orange_large);
			mButton=new Button(cx);
			mButton.setText(text);
			int txtSize=(isLarge)?50:40;
			mButton.setTextSize(txtSize);
			int h=shape.getMinimumHeight();
			int w=shape.getMinimumWidth();
			//mButton.setWidth(w+4);
			//mButton.setHeight(h+4);
			mButton.setBackground(shape);
			LinearLayout.LayoutParams aParams=new LinearLayout.LayoutParams(w+4, h+4);
			setLayoutParams(aParams);
			setGravity(17);
			addView(mButton);
		}
		
		public Button getButton()
		{
			return mButton;
		}
	}
	Pair<Integer, Integer> getOvalSize(boolean isLarge)
	{
		Drawable shape=null;
		if (!isLarge) shape = getResources().getDrawable(R.drawable.shape_oval_orange);
		else shape = getResources().getDrawable(R.drawable.shape_oval_orange_large);
		return new Pair<Integer, Integer>(shape.getMinimumHeight(),shape.getMinimumWidth());
	}
	void setOvalAttr(Button mButton, String text, boolean isLarge)
	{
		//View rootView = getActivity().getLayoutInflater().inflate(R.layout.oval_shape, null,	false);
		
		//int rId=(isLarge)?R.id.oval_large_button:R.id.oval_button;
		//mButton = (Button)rootView.findViewById(rId);
		//TransitionDrawable drawable = (TransitionDrawable) button.getDrawable();
		//drawable.startTransition(500);
		Drawable shape=null;
		if (!isLarge) shape = getResources().getDrawable(R.drawable.shape_oval_orange);
		else shape = getResources().getDrawable(R.drawable.shape_oval_orange_large);
		mButton.setText(text);
		int txtSize=(isLarge)?50:40;
		mButton.setTextSize(txtSize);
		//int h=shape.getMinimumHeight();
		//int w=shape.getMinimumWidth();
		//mButton.setWidth(w+4);
		//mButton.setHeight(h+4);
		mButton.setBackground(shape);
		
		//return button;
	}
	
	LinearLayout idLabel()
	{
		LinearLayout id=new LinearLayout(mContext);
		id.setLayoutParams(llParams);
		id.setOrientation(0);//0HORIZONTAL, 1Vertical);
	 	id.setGravity(1);
	 	TextView idLabel=new TextView(mContext);
	 	String cid=getResources().getString(R.string.citizen_id);
		idLabel.setText(cid);
		idLabel.setTextColor(Color.BLACK);
		idLabel.setTextSize(50);
		id.addView(idLabel);
		
		return id;
	}
		
	LinearLayout idLine()
	{
		LinearLayout id=new LinearLayout(mContext);
		id.setLayoutParams(llParams);
		id.setOrientation(0);//0HORIZONTAL, 1Vertical);
	 	id.setGravity(1);
	 	TextView idLabel=new TextView(mContext);
		idLabel.setText("  ");
		idLabel.setTextSize(50);
		id.addView(idLabel);

		mAlpha=new TextView(mContext);
		mAlpha.setWidth(40);
		mAlpha.setText("A");
		mAlpha.setTextSize(40);
		mAlpha.setClickable(true);
		mAlpha.setOnClickListener(new Button.OnClickListener(){
				public void onClick(View v)
				{
					mRow=0;
					getAlpha();
				}
			});
		id.addView(mAlpha);
		
		mIdDigits=new TextView[9];
		for (int i=0; i<9; i++)
		{
			mIdDigits[i]=new TextView(mContext);
			mIdDigits[i].setWidth(40);
			mIdDigits[i].setTextSize(40);
			mIdDigits[i].setText("0");
			mIdDigits[i].setClickable(true);
			mIdDigits[i].setOnClickListener(new LL(i));
			id.addView(mIdDigits[i]);			
		}
		return id;
	}
	
	LinearLayout passwdLabel()
	{
		LinearLayout id=new LinearLayout(mContext);
		TextView idLabel=new TextView(mContext);
		id.setLayoutParams(llParams);
		id.setOrientation(0);//0HORIZONTAL, 1Vertical);
	 	id.setGravity(1);
	 	String pwd=getResources().getString(R.string.password);
		idLabel.setText(pwd);
		idLabel.setTextColor(Color.BLACK);
		idLabel.setTextSize(50);
		id.addView(idLabel);
		return id;
	}
	LinearLayout passwordLine()
	{
		LinearLayout id=new LinearLayout(mContext);
		id.setLayoutParams(llParams);
		id.setOrientation(0);//0HORIZONTAL, 1Vertical);
	 	id.setGravity(17);//setGravity(1);

		
		mPasswords=new TextView[4];
		for (int i=0; i<4; i++)
		{
			mPasswords[i]=new TextView(mContext);
			mPasswords[i].setWidth(50);
			mPasswords[i].setTextSize(50);
			mPasswords[i].setText("0");
			mPasswords[i].setShadowLayer(20, 2, 2, Color.YELLOW);
			mPasswords[i].setInputType(16); //password
			mPasswords[i].setClickable(true);
			mPasswords[i].setOnClickListener(new LP(i));
			id.addView(mPasswords[i]);			
		}
		return id;
	}
	
	void getAlpha()
	{
		if (mCurrentText != null)
			showNormalColor(mCurrentText);	
		mRoot.removeView(mTable);
		mRoot.invalidate();
		mTable=showNButton(3, 'A');
		mRoot.addView(mTable);
		showFocusColor(mAlpha);
		mRoot.invalidate();
		mCurrentText=mAlpha;
		isDigitPad=false;
	}
	
	void getAlpha(String abc)
	{
		mRoot.removeView(mTable);
		mRoot.invalidate();
		mTable=showNButton(1, abc.charAt(0));
		mRoot.addView(mTable);
		mRoot.invalidate();
		isDigitPad=false;
	}

	void getDigits()
	{
		if (mColumn++ >= 0 && mCurrentText != null)
			showNormalColor(mCurrentText);
		if (mRow==1 && mColumn==4)
		{
			showFocusColor(mOK);
			return;
		}
		if (mColumn > 8) { mRow=1; mColumn=0;}
		if (mRow==0)
				mCurrentText=mIdDigits[mColumn %= 9];		
		else mCurrentText=mPasswords[mColumn %= 4];
		showFocusColor(mCurrentText);
		if (!isDigitPad)
		{
			mRoot.removeView(mTable);
			mRoot.invalidate();
			
			mTable=showNButton(3,'1');
		 	mRoot.setGravity(1);
			mRoot.addView(mTable);
			mRoot.invalidate();
		}
		isDigitPad=true;
	}
	
	LinearLayout oneRow(char a, int digits)
	{
		LinearLayout aRow=new LinearLayout(mContext);
		aRow.setOrientation(0);//0HORIZONTAL, 1Vertical);
		aRow.setGravity(17);
		boolean isLarge=true;
		if (a < 'A' || digits < 3)
		isLarge=false;
		//Pair<Integer, Integer> aPair=getOvalSize(isLarge);
		//LinearLayout.LayoutParams rowParam=new LinearLayout.LayoutParams(3*aPair.first+2, 3*aPair.second+2);
		//aRow.setLayoutParams(rowParam);
		for (int i=0; i<3; i++)
		{
			//Button aBt=new Button(mContext);
			String btText="";
			if (digits != 3)
				btText += (char)(a+i);
			else
			{
				btText += (char)(a+3*i);
				btText +=(char)(a+3*i+1);
				btText +=(char)(a+3*i+2);
			}
			
			//setOvalAttr(aBt, btText, isLarge);
			ButtonInBox aBt=new ButtonInBox(getActivity(),btText, isLarge);
			aBt.getButton().setOnClickListener(new Button.OnClickListener(){
				public void onClick(View v)
				{
					showTransition(v);
					String s=((Button)v).getText().toString().toUpperCase();
					if (s.length()==1)
					{
						if (mRow==1) 
							{
								myCodes[mColumn % 4]=s.charAt(0);
								if (!newUser)
									s="*";
							}
						if (mCurrentText!=null)
						mCurrentText.setText(s);
						if (s.charAt(0) > '9')
						{
							mAlpha.setText(s);
							showNormalColor(mAlpha);
							showFocusColor(mIdDigits[0]);
							mRow=0;
							mColumn=-1;
						}
							getDigits();
					}
					else
						getAlpha(s);
				}
			});
			aRow.addView(aBt);
		}
		return aRow;
	}
	
	void showTransition(View v)
	{
		TransitionDrawable drawable = (TransitionDrawable) getResources().getDrawable(R.drawable.transition);
		Button button = (Button)v;
		//button.setText("9");
		//button.setTextSize(60);
		button.setBackground(drawable);
		drawable.startTransition(300);
	}
	TableLayout showNButton(int N_row, char a)
	{
		TableLayout keyPad=new TableLayout(mContext);
		keyPad.setLayoutParams(tbParams);
		//keyPad.setOrientation(0);//0HORIZONTAL, 1Vertical);
	 	keyPad.setGravity(17);
		if (N_row==1)
		{
		
			keyPad.addView(oneRow(a, 1));
			return keyPad;
		}
		int k=3;
		if (a < 'A') k=1;
		for (int i=0; i<3; i++)
			{
				int m=3;
				if (a > '9') m=9;
				char c=(char)(a+m*i);
				keyPad.addView(oneRow(c, k));
			}
		if (a < 'A') //add last row in number key pad
		{
			LinearLayout aRow=new LinearLayout(mContext);
			aRow.setOrientation(0);//0HORIZONTAL, 1Vertical);
			aRow.setGravity(17);
			ButtonInBox aBt=new ButtonInBox(getActivity(),"OK", false);
			mOK=aBt.getButton();
			//setOvalAttr(aBt, "ok", false);
			aBt.getButton().setOnClickListener(new Button.OnClickListener(){
				public void onClick(View v)
				{
					saveData();
				}
			});
			aRow.addView(aBt);
			aBt=new ButtonInBox(getActivity(),"0", false);
			//setOvalAttr(aBt, "0", false);
			aBt.getButton().setOnClickListener(new Button.OnClickListener(){
				public void onClick(View v)
				{
					String s0="0";
					if (mRow==1) 
					{
						myCodes[mColumn % 4]='0';
						if (!newUser) s0="*";
					}
					if (mCurrentText != null)
						mCurrentText.setText(s0);						
					getDigits();
				}
			});
			aRow.addView(aBt);
			aBt=new ButtonInBox(getActivity(),"<-", false);
			//setOvalAttr(aBt, "<-", false);
			aBt.getButton().setOnClickListener(new Button.OnClickListener(){
				public void onClick(View v)
				{
					mColumn--;
					if (mColumn < 0) mColumn=0;
					if (mRow==0) mIdDigits[mColumn].setText("_");
					else mPasswords[mColumn].setText("_");
					mColumn--;
					getDigits();
				}
			});
			aRow.addView(aBt);
			keyPad.addView(aRow);			
		}
		return keyPad;
	}

	static AnimationDrawable kpAnimation=null;
	
	void saveData()
	{
		if(kpAnimation!=null) kpAnimation.stop();
		if (!newUser)
		{
			for (int i=0; i<4; i++)
			{
				if (myCodes[i] > '9' || myCodes[i] < '0') myCodes[i]='0';
			}
			String cc=new String (myCodes);
			int ic=Integer.parseInt(cc);
			int is = Integer.parseInt(mSavedCode);
			int ib = Integer.parseInt(mBkpCode);
			if (ic != is && ic!= ib  )
				mActivity.setTitle(getResources().getString(R.string.wrong_password));//"Wrong password, try again";
			else
			{
				MainActivity.setNewParameter("PASSWORD", cc);
				((LoginActivity)mActivity).done();
			}
			return;
		}
		String id=mAlpha.getText().toString();
		for (int i=0; i<9; i++)
		{
			id += mIdDigits[i].getText().toString();
		}
		String iw="";
		for (int i=0; i<4; i++)
		{
			String a=mPasswords[i].getText().toString();
			iw += a;
		}
		for (int i=0; i<4; i++)
		{
			myCodes[i]=iw.charAt(i);
			if (myCodes[i] > '9' || myCodes[i] < '0') myCodes[i]='0';			
		}
		iw=new String(myCodes);
		
    	String fileName=MainActivity.getFileHeader()+getResources().getString(R.string.login_page);
        SharedPreferences mSPF = getActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSPF.edit();//prefs.edit();
        String pwd=getResources().getString(R.string.pass_word_key);
        editor.putString(pwd, iw);
        editor.putString(MainActivity.CITIZEN_ID, id);
        editor.commit();
        getActivity().getIntent().putExtra(MainActivity.CITIZEN_ID, id);
        MainActivity.setNewParameter(MainActivity.CITIZEN_ID, id);
        ((LoginActivity)mActivity).done();
	}
	

	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) 
	 {
		 ScrollView sv=new ScrollView(mContext);
		 LinearLayout id=new LinearLayout(mContext);
		 	id.setOrientation(1);//0HORIZONTAL, 1Vertical);
		 	id.setGravity(1);//center_horizontal
			 mColumn=0;
			 mRow=1;
			 String pwd=getResources().getString(R.string.pass_word_key);       
			 mSavedCode=getArguments().getString(pwd);
			 mBkpCode=getArguments().getString(BKP_PASSWORD);
			 char a='1';
			 String pgTitle=getResources().getString(R.string.enter_password);//"Enter Password to Log in";
			 newUser = false;
			 if (mSavedCode == null || mSavedCode.length() < 4)
		 	{
		 		id.addView(idLabel());
		 		id.addView(idLine());
		 		a='A';
		 		mRow=0;
		 		pgTitle=getResources().getString(R.string.setup_new_account);
		 		newUser=true;
		 	}
			 
		 	id.addView(passwdLabel());

		 	id.addView(passwordLine());
		 	LinearLayout tbHome=new TableLayout(mContext);//LinearLayout(mContext);
		 	tbHome.setOrientation(0);//0HORIZONTAL, 1Vertical);
		 	//tbHome.addView(passwordLine());
		 	tbHome.setGravity(17);
		 	mTable=showNButton(3,a);
		 	tbHome.addView(mTable);
		 	id.addView(tbHome);
		 	//GifViewer aGif=new GifViewer(getActivity(), R.drawable.ninja_turtle);
		 	View rootView = null;
		    int page=R.layout.gif_view_port;
		        rootView=inflater.inflate(page, id, false); 
			ImageView img1=(ImageView)(rootView.findViewById(R.id.gif_view));
			img1.setBackgroundResource(R.drawable.nija_frames);
			kpAnimation = (AnimationDrawable) img1.getBackground();
		 // Start the animation (looped playback by default).
			kpAnimation.start();

		 	sv.addView(id);
		 	if (mRow==1)
		 	mCurrentText=mPasswords[0];
		 	else
		 		mCurrentText=mAlpha;
			showFocusColor(mCurrentText);
		 	mRoot=tbHome;
		 	mActivity.setTitle(pgTitle);
	        return sv;
	 }
}
