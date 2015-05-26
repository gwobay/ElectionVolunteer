package com.example.volunteerhandbook;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.android.gms.maps.model.LatLng;
import com.kou.utilities.AsyncSocketTask;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfilePage extends FormFragment 
		implements LoadBigImage.FinishLoadingListener
{

	byte[] photo;
static Activity mActivity=null;	
	public ProfilePage()
	{
		super();
		setConstants();
	}
	
	@Override
	protected void restoreInstanceState(Bundle oldState) {
	       Iterator<String> itr=oldState.keySet().iterator();
			while (itr.hasNext())
			{
				String key=itr.next();
				if (workingCopy == null)
					workingCopy=new HashMap<String, String>();
				workingCopy.put(key, oldState.getString(key));
			}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        //setRetainInstance(true);
        if (savedInstanceState != null)
        restoreInstanceState(savedInstanceState);
    }
    @Override
    protected void setConstants()
    {
    	mActivity=getActivity();
    	workingCopy=null;
    	
       	tableName="profile";
	
    	page_tags=new Object[][]{
    			{170,"profile"},{ 55,"regid"},{186, "citizen_id"},
				{49,"last_name"},{ 50,"first_name"}, { 51,"nick_name"},
				{176,"street_and_number"},{166,"address_city"},
				{167,"latitude"},{168,"longitude"},
				// max lat=25.15 min = 24.98 (outside will belong to same team)
				// max long=121.63 min=121.45
				{75,"birth_date"},{69, "sex"},{181,"mobile_number"},
				{178, "profile_photo_uri"}, {113, "managed_head_count"},
				{111,"rank"},{112, "badge_id"},{177,"team_id"}
			};

    	pageFields=new Object[][]{
    			{R.id.first_name, "first_name", "名字"}, 
    			{R.id.last_name, "last_name","姓"}, 
    			{R.id.nick_name, "nick_name","綽號"}, 
    			{R.id.street_and_number, "street_and_number","南京東路123巷0號"}, 
    			{R.id.address_city, "address_city","台北市"}, 
    			{R.id.mobile_number, "mobile_number","099999999"}, 
    			{R.id.birth_date, "birth_date", "1996/05/01"} , 
    			{R.id.managed_head_count, "managed_head_count", "0"} , 
    			{R.id.sex, "sex", "男女"},
    		    {R.id.no_noise_period_begin, ProfileActivity.NO_NOISE_START, "--"},	
    		    {R.id.no_noise_period_end, ProfileActivity.NO_NOISE_END, "--"}
    		    
    			//,{R.id.profile_photo, "profile_photo_uri", "null"} 
    	};
    
    }
    
    class ViewHolder
    {
    	TextView first_name;
    	TextView last_name;
    	TextView nick_name;
    	TextView street_and_number;
    	TextView address_city;
    	TextView mobile_number;
    	TextView birth_date;
    	TextView sex;  
    	TextView managed_head_count;  
    	TextView quite0;
    	TextView quite9;
       	TextView team_id;
    	TextView badge_id;
    	TextView rank;
     }
    
    @Override
    protected HashMap<String, String> getPageData()
	{
    	if (viewHolder == null) return super.getPageData();
		
		HashMap<String, String> aRow=new HashMap<String, String>();
			
				aRow.put("first_name", viewHolder.first_name.getText().toString() );
				aRow.put("last_name", viewHolder.last_name.getText().toString() );
				aRow.put("nick_name", viewHolder.nick_name.getText().toString() );
				aRow.put("street_and_number", viewHolder.street_and_number.getText().toString() );
				aRow.put("address_city", viewHolder.address_city.getText().toString() );
				aRow.put("mobile_number", viewHolder.mobile_number.getText().toString() );
				aRow.put("birth_date", viewHolder.birth_date.getText().toString() );
				aRow.put("sex", viewHolder.sex.getText().toString() );
				aRow.put("managed_head_count", viewHolder.managed_head_count.getText().toString() );
				aRow.put("quite0", viewHolder.quite0.getText().toString() );
				aRow.put("quite9", viewHolder.quite9.getText().toString() );
				aRow.put("team_id", viewHolder.team_id.getText().toString() );
				aRow.put("badge_id", viewHolder.badge_id.getText().toString() );
				aRow.put("rank", viewHolder.rank.getText().toString() );
				
		aRow.put("citizen_id", mCitizenId);
		addPageSpecialData(aRow);
        //addFixLine(aRow); taken care of by above
		return aRow;
	}

    static DecimalFormat dF=new DecimalFormat("#00");
    //static PositionLocator mPL=null;
    static boolean toClosePositionService=false;
    ViewHolder viewHolder;
    String myLatitude ;
    String myLongitude ;
    String myTeamId;
    String myBadgeId;
    public void onAddressReady(String newAddress)
    {
    	int id0=newAddress.indexOf("@");
    	if (id0 < 0) return;
    	String[] terms=newAddress.split("@");
    	String[] latLng=terms[terms.length-1].split(",");
    	myLatitude=latLng[0];
    	myLongitude=latLng[1];
    	int iLat=(int)(100*Double.parseDouble(myLatitude)) % 100;
    	int iLng=(int)(100*Double.parseDouble(myLongitude)) % 100;
    	myTeamId="T"+dF.format(iLat)+dF.format(iLng); 
    	if (viewHolder != null)
    	{
    		String nickName=(String) viewHolder.nick_name.getText().toString();
    		if (nickName==null) nickName=(String) viewHolder.last_name.getText().toString();
    		myBadgeId=myTeamId+nickName;
    		if (terms.length > 2 && viewHolder.street_and_number.getText().toString().length()<2)
    		{
    			viewHolder.street_and_number.setText(terms[0]);
    			viewHolder.address_city.setText(terms[1]);
    			viewHolder.street_and_number.invalidate();
    			viewHolder.address_city.invalidate();
    		}
    	}
    	//if (toClosePositionService) mPL.stopUpdates();
    	if (mySPF!=null){
    	SharedPreferences.Editor writer=mySPF.edit();
    	writer.putString("longitude", myLongitude);
    	writer.putString("latitude",  myLatitude);
    	writer.putString("badge_id", myBadgeId);
    	writer.putString("team_id",  myTeamId);
    	
    	writer.commit();
    	}
    }
    
	protected void readAdditionalData(SharedPreferences sharedPref,
			HashMap<String, String> oneRecord)
	{
		String noNoiseStart=sharedPref.getString(ProfileActivity.NO_NOISE_START, "--");
		String noNoiseEnd=sharedPref.getString(ProfileActivity.NO_NOISE_END, "--");
		oneRecord.put(ProfileActivity.NO_NOISE_START, noNoiseStart);
		oneRecord.put(ProfileActivity.NO_NOISE_END, noNoiseEnd);		
	}
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	
    	HashMap<String, String> aRow=getPageData();
    	Iterator<String> itr=aRow.keySet().iterator();
    	while (itr.hasNext())
    	{
    		String key=itr.next();
    		outState.putString(key, aRow.get(key));
    	}
    	
        super.onSaveInstanceState(outState);
        
    }
    
    void sendDataToServer(byte[] toSend)
    	//make sure this is called from a thread but not the main ui thread
    {
	 	final byte[] sendBytes=toSend;
		new Thread(new Runnable(){
			public void run(){
		    	AsyncSocketTask aTask=new AsyncSocketTask();
		        	//aTask.setVectorStore(responseStore);
		    	boolean wait4Response=(false);
		    		aTask.needResponse(wait4Response);
		        	//Vector<byte[]> resp=new Vector<byte[]>();
		        	//aTask.setDataBox(inBox);
		    		final byte[] host="220.134.85.189".getBytes();
		    		final byte[] port="9696".getBytes();
 
		    		aTask.execute(host, port, sendBytes);
		    	int waitTime=sendBytes.length/10000;
		    	if (waitTime<1)waitTime=1;
		    	try {
						Thread.sleep(waitTime*1000);
				} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
		        aTask.cancel(true);	
    		}
    	}).start();
    }
 @Override    
	protected void sendDataToServer(HashMap<String, String> data)
 	{
     SharedPreferences sharedPref = getActivity().getSharedPreferences(MainActivity.class.getSimpleName(),
             Context.MODE_PRIVATE);//getSharedPreferences(fileName, Context.MODE_PRIVATE);
     String regid = sharedPref.getString("registration_id", "--");
     	if (regid != null) data.put("regid", regid);
     	data.remove(getResources().getString(R.string.fix_line_key));
     	data.remove("profile_photo_uri");
     	if (photoBytes != null) {
     		data.put("photo_size", ""+photoBytes.length);
     	}
     	String fixLine=getFixLine(data);
     	byte[] asciiBytes=AsyncSocketTask.convertStringToBytes(fixLine);
     	byte[] sendBytes=new byte[asciiBytes.length+photoBytes.length];
     	System.arraycopy(asciiBytes, 0, sendBytes, 0, asciiBytes.length);
     	System.arraycopy(photoBytes, 0, sendBytes, asciiBytes.length, photoBytes.length);
     	Log.i("PHOTO", ":"+photoBytes.length);
     	sendDataToServer(sendBytes);
    }
    
	@Override    
	protected void addPageSpecialData(HashMap<String, String> aRow)
	{
		if (photo_uri != null) 
			aRow.put("profile_photo_uri", photo_uri.toString() );
		aRow.put("citizen_id", mCitizenId);
	    String fixL=getFixLine(aRow);
		String key=getResources().getString(R.string.fix_line_key);
		aRow.put(key, fixL);
		String street=aRow.get("street_and_number");
		String city=aRow.get("address_city");
		if (street != null && city != null)
		{
			aRow.put("latitude", myLatitude);			
			aRow.put("longitude", myLongitude);	
			aRow.put("badge_id", myBadgeId);
			aRow.put("team_id", myTeamId);
		}
	}
	
	@Override    
	protected void setPageSpecialValue(View v, HashMap<String, String> savedRecord)
    {
		String uriString=savedRecord.get("profile_photo_uri");
		if (uriString==null) return;
		ImageView img=(ImageView)(v.findViewById(R.id.profile_photo));
		photo_uri = Uri.parse(uriString);
		if (photo_uri != null)
		{
			v.findViewById(R.id.wait_4_photo).setVisibility(View.VISIBLE);
			getPhotoBitmap(img);
		}
		myLatitude=savedRecord.get("latitude");	
		
		if (myLatitude!=null) myLongitude=savedRecord.get("longitude");
		else {
			String street=savedRecord.get("street_and_number");
			String city=savedRecord.get("address_city");
			if (street != null && city != null)
				getLocation(street+" "+city);			
		}
		myBadgeId=savedRecord.get("badge_id");
		if (viewHolder !=null && myBadgeId !=null) viewHolder.badge_id.setText(myBadgeId);
		myTeamId=savedRecord.get("team_id");
		if (viewHolder !=null && myBadgeId !=null) viewHolder.team_id.setText(myTeamId);					
		//myTeamId=savedRecord.get("team_id");
    }
	String mCheckAddress;
	void getLocation(String addr)
	{
		if (mCheckAddress != null && mCheckAddress.equalsIgnoreCase(addr))
			return;
		mCheckAddress=addr;
		//if (mPL==null) mPL=new PositionLocator(mActivity, 1);
		//mPL.getPosition(getActivity(), this, addr);
		toClosePositionService=true;		
	}
	
	void getLocation()
	{
		if (mRootView==null) return;
		ViewHolder holder=(ViewHolder)mRootView.getTag();
		String street=(String) (holder.street_and_number.getText().toString());
		String city=(String) (holder.address_city.getText().toString());
		if (street != null && city != null)
		{
			String addr=street+" "+city;
			getLocation(addr);
		}
	}
	
	@Override
	protected String getSharedFileName()
	{
		//String pg=getArguments().getString(PAGE_TITLE);
		//String fileName=getString(R.string.candidate_logo)+pg;
		return MainActivity.getFileHeader()+"profile";
	}
	
	public static String getTableName()
	{
		return "profile";
	}
	
	void getPhotoBitmap(ImageView v)
	{
		Bitmap oMap=LoadBigImage.getCached(photo_uri);
		if (oMap != null)
		{
			v.setImageBitmap(oMap);
			return;
		}
		if (v.getWidth() > mWidth) mWidth = v.getWidth();
		if (v.getHeight() > mHeight) mHeight=v.getHeight();
		LoadBigImage lg=LoadBigImage.getInstance((Activity)v.getContext());
		//lg.loadBitMap(v, photo_uri, mWidth, mHeight);
		if (lg!=null) 
			{ 
				lg.addFinishLoadingListener(this);
				if (mWidth == 0) mWidth=80;
				if (mHeight == 0) mHeight=60;
				lg.loadBitMap(v, photo_uri, mWidth, mHeight);
			}	
	}

	static View mRootView=null;
	static ImageView profilePhoto=null;
	
	public ImageView getProfilePhoto()
	{
		return profilePhoto;
	}
	static int mWidth=-1;
	static int mHeight=-1;

	@Override
    protected View showPage(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) //add new and update personal data (show name and rank and update button
    {
        View rootView = null;
        int page=R.layout.profile_form;
        rootView=inflater.inflate(page, container, false); 
        
        mActivity=getActivity();
       //(rootView.findViewById(R.id.citizen_id)).setVisibility(View.INVISIBLE);
       mRootView=rootView;
       ImageView img=(ImageView)(rootView.findViewById(R.id.profile_photo));
       //img.setClickable(false);
	   if (updatingPhoto==true)
       {
    	   //img.setClickable(true);
    	   rootView.findViewById(R.id.wait_4_photo).setVisibility(View.VISIBLE);
       }
       else
    	   rootView.findViewById(R.id.wait_4_photo).setVisibility(View.GONE);	   
       profilePhoto=img;
       if (photo_uri != null)
		{
			getPhotoBitmap(img);
		}	
		else
		{
			img.setImageResource(R.drawable.ic_launcher);
			if (mWidth <= 0 && mHeight <= 0){
				mWidth=img.getWidth();
				mHeight=img.getHeight();
			}
			ImageView img1=(ImageView)(rootView.findViewById(R.id.image_candidate));
			img1.setBackgroundResource(R.drawable.kp_gif_frames);
			kpAnimation = (AnimationDrawable) img1.getBackground();
		 // Start the animation (looped playback by default).
			kpAnimation.start();
		}
       
       if (viewHolder==null){
       viewHolder = new ViewHolder();
       viewHolder.first_name=(TextView)(rootView.findViewById(R.id.first_name));
	   	viewHolder.last_name=(TextView)(rootView.findViewById(R.id.last_name));
	   	viewHolder.nick_name=(TextView)(rootView.findViewById(R.id.nick_name)); 
	   	viewHolder.street_and_number=(TextView)(rootView.findViewById(R.id.street_and_number));
	   	viewHolder.address_city=(TextView)(rootView.findViewById(R.id.address_city));
	   	viewHolder.address_city.setOnFocusChangeListener(
	   			new View.OnFocusChangeListener() {				
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
	   					if (!hasFocus)
	   					{
	   						getLocation();
	   					}
	   				}
	   			});// onEndBatchEdit();
	   	viewHolder.mobile_number=(TextView)(rootView.findViewById(R.id.mobile_number));
	   	viewHolder.birth_date=(TextView)(rootView.findViewById(R.id.birth_date));
	   	viewHolder.sex=(TextView)(rootView.findViewById(R.id.sex));
	   	viewHolder.managed_head_count=(TextView)(rootView.findViewById(R.id.managed_head_count));
		 viewHolder.quite0=(TextView)(rootView.findViewById(R.id.no_noise_period_begin));
		 viewHolder.quite9=(TextView)(rootView.findViewById(R.id.no_noise_period_end));
		 viewHolder.team_id=(TextView)(rootView.findViewById(R.id.team_id));
		 viewHolder.badge_id=(TextView)(rootView.findViewById(R.id.badge_id));
		 viewHolder.rank=(TextView)(rootView.findViewById(R.id.rank));

	   	mRootView.setTag(viewHolder);
	   		if (savedInstanceState==null){
		   	//mPL=new PositionLocator(mActivity, 1);
		   	//mPL.getMyAddress(rootView, this);
		   	}
	   	}
        return rootView;
    }
    
	public void updateProfilePhoto(ImageView v)
	{
		if (photo_uri == null) return;
		((View)(v.getParent())).findViewById(R.id.wait_4_photo).setVisibility(View.GONE);
		getPhotoBitmap(v);
		v.invalidate();
	}
    @Override
    protected void openSubSequentPage()
    {
    	((ProfileActivity)getActivity()).done();
    }

	static AnimationDrawable kpAnimation=null;
	static AnimationDrawable iJoinAnimation=null;
	static Uri photo_uri=null;
	static String photo_file_path=null;
	static byte[] photoBytes=null;
	public void onImageLoaded(ImageView v, Bitmap bmp)
	{
		v.setImageBitmap(bmp);
        v.invalidate(); 
       // int size = bmp.getByteCount();
      //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
      //int bytes = b.getWidth()*b.getHeight()*4; 

      /*ByteBuffer buffer = ByteBuffer.allocate(size); //Create a new buffer
      bmp.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
      byte[] array = buffer.array(); */
      
      ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
      bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);//png don't care qty so put 100
      photoBytes = byteArrayBitmapStream.toByteArray();
	}
	public void replaceProfilePhoto(ImageView v)
	{
		if (kpAnimation != null)
		{
			kpAnimation.stop();
		}
		if (photo_uri != null)
		{
			getPhotoBitmap(v);
		}	
		//v.invalidate();
	}
	
	public void setPhotoUri(Uri aUri)
	{
		photo_uri=aUri;
	}

		
	static final int REQ_PICK_IMAGE=1;
	static final int REQ_LOGIN=10;
	static ImageView mProfilePhotoImage=null;
	static boolean updatingPhoto=false;
	public void getPicture(View v)
	{
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
			updatingPhoto=true;
			startActivityForResult(chooserIntent, REQ_PICK_IMAGE);
		}
		//getLocation();
	}	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{			
	//String photo_file_path;
	Uri photo_uri=null;
		if (data != null) photo_uri=data.getData();

		super.onActivityResult(requestCode, resultCode, data);
		updatingPhoto=false;
		if (requestCode == REQ_PICK_IMAGE && photo_uri != null)
		{			
			//PersonalPage aPP=(PersonalPage)(globalParameters.get("volunteer"));
			setPhotoUri(photo_uri);
			replaceProfilePhoto(mProfilePhotoImage);  
		}
	}
	
    
}
