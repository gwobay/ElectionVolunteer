package com.example.volunteerhandbook;


import java.util.HashMap;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class CommitmentForm extends FormFragment
{

		public CommitmentForm()
		{
			super();
			setConstants();
		}
		
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        // retain this fragment
	        setRetainInstance(true);
	    }	
		
	@Override
	protected void setConstants()
	{

    	tableName="commitment";
    	page_tags=new Object[][]{
			{170,"commitment"},{35,"msgType"},{ 11,"vid"},
			{186, "citizen_id"},
			{151, "visits_per_week"}, 
			{152, "hours_per_visit"}, 
			{153, "raises_per_week"}, 
			{154, "hours_per_raise"}, 
			{155, "events_per_week"}, 			 
			{156, "hours_per_event"}, 
			{157, "supports_per_week"}, 
			{158, "hours_per_support"}, 
			{159, "specialty_list"},{75,"starting_date"}, {58, "specialty"}
		};

	    pageFields=new Object[][]{
			{R.id.visits_per_week, "visits_per_week", "0"}, 
			{R.id.hours_per_visit, "hours_per_visit","0"}, 
			{R.id.raises_per_week, "raises_per_week","0"}, 
			{R.id.hours_per_raise, "hours_per_raise", "0"}, 
			{R.id.events_per_week, "events_per_week","0"}, 			 
			{R.id.hours_per_event, "hours_per_event","0"}, 
			{R.id.supports_per_week, "supports_per_week","0"}, 
			{R.id.hours_per_support, "hours_per_support","0"}, 
			{R.id.specialty_list, "specialty_list",""}
		};
	}
	
	    
	    @Override
	    protected void openSubSequentPage()
	    {
	    	((CommitmentActivity)getActivity()).done();
	    }


		@Override
		protected String getSharedFileName()
		{
			//String pg=getArguments().getString(PAGE_TITLE);
			//String fileName=getString(R.string.candidate_logo)+pg;
			return MainActivity.getFileHeader()+"commitment";
		}
		
		public static String getTableName()
		{
			return "commitment";
		}
		
		@Override    
		protected void addPageSpecialData(HashMap<String, String> aRow)
		{
			String fixL=getFixLine(aRow);
			String key=getResources().getString(R.string.fix_line_key);
			aRow.put(key, fixL);
		}
				
		static AnimationDrawable ttAnimation=null;
		static AnimationDrawable iJoinAnimation=null;

	    @Override
	    protected View showPage(LayoutInflater inflater, ViewGroup container,
		            Bundle savedInstanceState) //add new and update personal data (show name and rank and update button
		{
		        View rootView = null;
		        int page=R.layout.commitment_form;
		        rootView=inflater.inflate(page, container, false); 
				ImageView img1=(ImageView)(rootView.findViewById(R.id.nija_gif));
				//Drawable nija=getResources().getDrawable(R.drawable.nija_frames);
				img1.setBackgroundResource(R.drawable.nija_frames);//nija);
				ttAnimation = (AnimationDrawable) img1.getBackground();
			 // Start the animation (looped playback by default).
				ttAnimation.start();		        		        	
		        return rootView;
		}
		
}
