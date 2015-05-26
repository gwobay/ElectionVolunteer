package com.example.volunteerhandbook;
	import java.util.HashMap;
import java.util.Iterator;

	import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


	public class FormFragment extends Fragment implements MenuItem.OnMenuItemClickListener {

		public FormFragment() {
			// TODO Auto-generated constructor stub
			
		}
	    public static final String ARG_PAGE_NUMBER = "which_draw";
	    public static final String PAGE_ACTION = "new_show_mod";
	    public static final String PAGE_TITLE = "page_title";
	    //public static final String FIX_MSG="FIX_MSG";
		//public static final String CITIZEN_ID = "CITIZEN_ID";
	    
	    protected static String mCitizenId=null;
		protected static String tableName="profile";
		
		protected static Object[][] page_tags=null;

	    protected static Object[][] pageFields=null;
	    
	    protected static HashMap<String, String> workingCopy=new HashMap<String, String>();
	    protected void setConstants()
	    {
	    	
	    }
	    
	    protected HashMap<String, String> parseFixLine(String aLine)
	    {
	    	HashMap<String, String> oneRecord=new HashMap<String, String>();
	    	for (int i=0; i<page_tags.length; i++)
	    	{
	    		int i0=aLine.indexOf(""+(int)page_tags[i][0]+"=");
	    		if (i0 < 0) continue;
	    		i0=aLine.indexOf("=", i0);
	    		int iB=aLine.indexOf("|", ++i0);
	    		if (iB<0) iB=aLine.length();
	    		oneRecord.put((String)page_tags[i][1], aLine.substring(i0, iB));
	    	}
	    	return oneRecord;
	    }
	    
	    protected String getFixLine(HashMap<String, String> oneRecord)
		{
	    	String fixLine="170="+tableName+"|";
	    	for (int i=0; i<page_tags.length; i++)
	    	{
	    		String v=oneRecord.get(page_tags[i][1]);
	    		if (v==null) continue;
	    		fixLine += (""+page_tags[i][0]);
	    		fixLine += "=";
	    		fixLine += v;
	    		fixLine += "|";
	    	}
			return fixLine; //need to work with it's fix page tags
		}

	    protected void setDefaultValue(View v)
		{
			Object[][] oTxt=pageFields;
			for (int i=0; i<oTxt.length; i++)
			{
				if (((String)oTxt[i][2]).equalsIgnoreCase("null")) continue;
				EditText txV = (EditText) v.findViewById((Integer)(oTxt[i][0]));
				if (txV==null) continue;
				txV.setText((String)oTxt[i][2]);
			}
			//TextView txV = (TextView) v.findViewById(R.id.is_modify);
			//if (txV != null) txV.setText("0");
	   	}
		
	    protected void setPageSpecialValue(View v, HashMap<String, String> savedRecord)
	    {
	    	
	    }
		protected void setSavedValue(View v, HashMap<String, String> savedRecord)
		{
			Object[][] oTxt=pageFields;
			for (int i=0; i<oTxt.length; i++)
			{
				if (((String)oTxt[i][2]).equalsIgnoreCase("null")) continue;
				
				TextView txV = (TextView) v.findViewById((Integer)(oTxt[i][0]));
				if (txV==null) continue;
				String value=savedRecord.get((String)oTxt[i][1]);
				if (value != null)
				txV.setText(value);
			}
			setPageSpecialValue(v, savedRecord);
			workingCopy=savedRecord;
		}
		
		protected void setSavedValue(View v, Bundle savedRecord)
		{
			if (savedRecord==null || savedRecord.isEmpty()) return;
	        
			HashMap<String, String> mp=new HashMap<String, String>();
			Iterator<String> itr=savedRecord.keySet().iterator();
			while (itr.hasNext())
			{
				String key=itr.next();
				mp.put(key, savedRecord.getString(key));
			}
			workingCopy=mp;
			setSavedValue(v, mp);
		}
		
		protected void readAdditionalData(SharedPreferences sharedPref,
				HashMap<String, String> oneRecord)
		{
			
		}
		protected HashMap<String, String> readSavedValues(SharedPreferences sharedPref)
		{
			HashMap<String, String> oneRecord=new HashMap<String, String>();
			Object[][] oTxt=pageFields;
			for (int i=0; i<oTxt.length; i++)
			{
				TextView txV = (TextView) getActivity().findViewById((Integer)(oTxt[i][0]));
				if (txV==null) continue;
				String value=sharedPref.getString((String)oTxt[i][1], "--");
				if (value.charAt(0)!= '-')
				oneRecord.put((String)oTxt[i][1], value);
			}
			readAdditionalData(sharedPref, oneRecord);
			return oneRecord;
	   	}
		
		protected String getSharedFileName()
		{
			//String pg=getArguments().getString(PAGE_TITLE);
			//String fileName=getString(R.string.candidate_logo)+pg;
			return null;
		}
		
		protected void addPageSpecialData(HashMap<String, String> aSet)
		{
			
		}
		
		protected SharedPreferences mySPF;
	    protected void saveData(Context cxt, HashMap<String, String> aSet)
	    {
	    	if (mySPF==null){
	    	String fileName=getSharedFileName();
	        mySPF = cxt.getSharedPreferences(fileName, Context.MODE_PRIVATE);
	    	}
	    	SharedPreferences.Editor editor=mySPF.edit();
	        editor.putString("citizen_id", mCitizenId);
	        Iterator<String> itr=aSet.keySet().iterator();
	        while (itr.hasNext())
	        {
	        	String key=itr.next();
	        	editor.putString(key, (String)aSet.get(key));
	        }
	        editor.commit();   	
	    }

	    protected void addFixLine(HashMap<String, String> aRow)
	    {
	    	
	    }
		
	    protected HashMap<String, String> getPageData()
		{
			HashMap<String, String> aRow=new HashMap<String, String>();
			for (int i=0; i<pageFields.length; i++)
			{
				TextView txV = (TextView) (getActivity().findViewById((Integer)(pageFields[i][0])));
				if (txV==null) continue;
				String txt=txV.getText().toString();
				if (txt != null && txt.length() > 0)
				aRow.put((String)pageFields[i][1], txt);
			}
			//TextView txV = (TextView) getActivity().findViewById(R.id.is_modify);
			//if (txV != null) aRow.put("msgType", (String)txV.getText());
			aRow.put("citizen_id", mCitizenId);
			addPageSpecialData(aRow);
	        //addFixLine(aRow); taken care of by above
			return aRow;
		}

	    protected void openSubSequentPage()
	    {
	    	((MainActivity)getActivity()).openCommitmentPage(null);
	    }
	    
	    protected 
	    void sendDataToServer(HashMap<String, String> data)
	    {
	    	
	    }
	    void saveFormData()
	    {
    		HashMap<String, String> aRow=getPageData();
    		saveData(getActivity(), aRow);
    		sendDataToServer(aRow);
    		
	    }
	    protected boolean actOnMenuItemClick (MenuItem item)
	    {	    	
	    	switch (item.getItemId())
	    	{
	    	case	R.id.action_save:
	    		saveFormData();
	    		Toast.makeText(getActivity(), "Thank you for your participation!!", Toast.LENGTH_LONG).show();
	    		openSubSequentPage();
	    			break;
	    	default:
	    		break;
	    	}
	    	return true;
	    }
	    
    
    public boolean onMenuItemClick (MenuItem item)
    {
    	if (item.getItemId()==R.id.action_save)
    	{
    		saveFormData();
//Toast.makeText(getActivity(), "Thank you for your participation!!", Toast.LENGTH_LONG).show();
    	}
    	openSubSequentPage();
			
    	return true;//actOnMenuItemClick (item);
    }


	protected  View showPage(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) //add new and update personal data (show name and rank and update button
	    {
	        View rootView = null;
	        int page=R.layout.profile_form;
	        rootView=inflater.inflate(page, container, false); 
	        	
	       //(rootView.findViewById(R.id.citizen_id)).setVisibility(View.INVISIBLE);

	        return rootView;
	    }
	    
	
	protected void restoreInstanceState(Bundle savedInstanceState) {
	}
	
	    @Override
	    public void onCreate(Bundle oldState) {
	    	 super.onCreate(oldState);
	    	 setRetainInstance(true);	
	    	if (oldState==null || oldState.isEmpty()) return;
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
	    public void onSaveInstanceState(Bundle outState) {
	    	
	        super.onSaveInstanceState(outState);
	        //getCurrentFormData(outState);
	        //
	    }
	    
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	    	View rootView=null;
	    	String cidKey=getResources().getString(R.string.citizen_id_key);
	        mCitizenId = getArguments().getString(cidKey);
	    	String fixKey= getResources().getString(R.string.fix_line_key);
	    	String fixLine=getArguments().getString(fixKey);
	    	rootView=showPage(inflater, container, savedInstanceState);
    		
	    	if (fixLine != null)
	    	{	    		
	    		setSavedValue(rootView, parseFixLine(fixLine));
	    	} 
	    	else if (savedInstanceState != null){
	    		
	    		setSavedValue(rootView, savedInstanceState);
	    	}
	    	else
	    	{
	    		if (mySPF==null) {
	    	 String fileName=getSharedFileName();
    			mySPF = getActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);
	    		}
	    		String mSaved = mySPF.getString(cidKey, "--");
	        	if (mSaved.charAt(0) != '-')
	    		{
	        		if (mCitizenId==null) mCitizenId=mSaved;
	        		setSavedValue(rootView, readSavedValues(mySPF));
	    		}
	    		else setDefaultValue(rootView);
    				    		
	    	}
		   
		   // int[] menuItems={R.id.action_save};
	    	//((MainActivity)getActivity()).activateMenuExclusive(menuItems);
			return rootView;
	    }
}
