/**
 * 
 */
package com.example.volunteerhandbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * @author eric
 *
 */
public class ListRecords extends ListRecordBase {
	
	public static final String PARENT_PASSDOWN_KEY = "which_draw";
	public static final String PASSDOWN_CHILD_KEY = "which_draw";
	private Activity mActivity;
	private HashMap<String, String> dataMap;
	public ListRecords()
	{
		
	}

	public void setActivity(Activity a)
	{
	     mActivity=a;	
	}

	@Override
	public
	void onListItemClick(ListView l, View v, int pos, long id)
	{
		TextView txtV=(TextView)(((RelativeLayout)v).getChildAt(0));
		String name=(String) txtV.getText();
		if (name!=null){
			String url=dataMap.get(name);
			if (url!=null) {
			//try {
				//String html=(url+name+"</a>");
				Intent openUrl= new Intent(mActivity, OpenUrlActivity.class);//Intent.ACTION_VIEW, Uri.parse(url));
				openUrl.putExtra(PASSDOWN_CHILD_KEY, url+")"+name);
				mActivity.startActivity(openUrl);
			//} catch (ActivityNotFoundException e){}
			}
		}
		
	}
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
    HashMap<String, String> setPreferenceData(SharedPreferences storage)
 	{
     	HashMap<String, String> map=new HashMap<String, String>();
 		String testHead="http://developer.android.com";
 	    String[] events=getResources().getStringArray(R.array.url_list);
 	    SharedPreferences.Editor writer=storage.edit();
 	    for (int i=0; i<events.length; i++)
 	    {
 	    	String[] where_name=events[i].split("zAz");
 	    	if (where_name.length < 2) continue;
 	    	String url=testHead+where_name[0];//+"\">";//+where_name[1]+"</a>";
 	    	writer.putString(where_name[1], url);
 	    	map.put(where_name[1], url);	    
 	    }
 	    writer.commit();
 	    	
 	    return map;
 	}
    
 	void updateListView(HashMap<String, String> map)
 	{
 			ArrayList<Map<String, String> > data=new ArrayList<Map<String, String> >();
 			Iterator<String> itr=map.keySet().iterator();
 			while (itr.hasNext())
 			{
 		    	TreeMap<String, String> aRow=new TreeMap<String, String>();
 		    	String event=itr.next();   		    	
 		    	aRow.put("z", event);
 		    	data.add(aRow);
 		    }
 		    if (data.isEmpty()) return;
 		    
 		    String[] key={"z"};
 		    int rID=R.id.event_list;
 		    int[] textViewId={rID};
 		    ListAdapter adp=new SimpleAdapter(mActivity, 
 		    									data, 
 		    									R.layout.list_row,
 		    									key, 
 		    									textViewId);    		    
 		    setListAdapter(adp);
 		    return;    		
 	}
 	
     View showActivitiesListPage(LayoutInflater inflater, ViewGroup cnr,
             Bundle savedInstanceState)
     {
         View rootView = inflater.inflate(R.layout.list_view, cnr, false);
         
         String fileName=getString(R.string.candidate_photo)+"event_list";
         
 		SharedPreferences myStorage=cnr.getContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
 		HashMap<String, String>event_url=(HashMap<String, String>)myStorage.getAll();
 	    if (event_url.size() < 1) event_url=setPreferenceData(myStorage);
 	    if (event_url.size() > 0) updateListView(event_url);
 	    dataMap=event_url;
         return rootView;
     }
 
     View showNewAgendaListPage(LayoutInflater inflater, ViewGroup cnr,
             Bundle savedInstanceState)
     {
    	 View rootView = inflater.inflate(R.layout.list_view, cnr, false);
    	 return rootView;
     }

     View showCaseHistoryPage(LayoutInflater inflater, ViewGroup cnr,
             Bundle savedInstanceState)
     {
    	 View rootView = inflater.inflate(R.layout.list_view, cnr, false);
    	 return rootView;    	 
     }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
	{
	View rootView=null;
	int position = getArguments().getInt(PARENT_PASSDOWN_KEY);
	String fileName=getResources().getStringArray(R.array.page_array_EN)[position];
	if (fileName.indexOf("Activities") >=0)
		rootView=showActivitiesListPage(inflater, container, savedInstanceState);
	else if (fileName.indexOf("Agenda") >=0)
		
		rootView=showNewAgendaListPage(inflater, container, savedInstanceState);
	else if (fileName.indexOf("History") >=0)
		
		rootView=showCaseHistoryPage(inflater, container, savedInstanceState);
	
		String ttl=getResources().getStringArray(R.array.page_array)[position];
		mActivity.setTitle(ttl);
	return rootView;
	}
}
