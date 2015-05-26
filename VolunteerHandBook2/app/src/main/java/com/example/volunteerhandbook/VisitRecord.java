package com.example.volunteerhandbook;


import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class VisitRecord extends ListRecordBase
{

	public VisitRecord() {
		// TODO Auto-generated constructor stub
		super();
    	/* set up the following 
    	protected String[] mShowColumns; //filled by child page class
    	protected int[] rListColumnTextViews; //filled by child page class
    	protected int layout_list_row_by_page;
    	*/
		mShowColumns=new String[]{"visiting_date", "full_name", "mobile_number"};
		rListColumnTextViews=new int[]{R.id.visit_list_date, R.id.visit_list_name, R.id.visit_list_phone};
		mListPage=R.layout.visit_list;
		mRecordFormLayout = R.layout.visit_form;
		dbTableName="visited";
	}
	 
    	static Object[][] page_tags={
			{170,"Visited"},{35,"msgType"},{ 11,"vid"},{ 186,"citizen_id"},
			{151, "visiting_date"}, 
			{152, "full_name"}, 
			{153, "street_address"}, 
			{154, "address_city"}, 
			{155, "mobile_number"}, 			 
			{156, "rating"}, 
			{157, "next_visiting_date"}
		};

    	@Override
    	protected String getTagName(int iTag)
    	{
    		for (int i=3; i<page_tags.length; i++)
    		{
    			if (iTag != (int)page_tags[i][0]) continue;
    			return (String)page_tags[i][1];
    		}
    		return null;
    	}
    	
	    static Object[][] pageFields={
			{R.id.visit_date, "visit_date", "0"}, 
			{R.id.full_name, "full_name","0"}, 
			{R.id.street_address, "street_address","0"}, 
			{R.id.address_city, "address_city", "0"}, 
			{R.id.mobile_number, "mobile_number","0"}, 			 
			{R.id.rating_spinnner, "rating","0"},
			{R.id.next_visiting_date, "next_visiting_date", "2014-11-30"}
		};
	    

	    @Override
		protected String createTableSql()
		{
			String sql="create table if not exists visited";
				sql += "( vid int ,";
				sql += " citizen_id char(12) primary key not null,";
				sql += " visit_date date  not null default '2014-05-01',";
				sql += " full_name text not null,"; 
				sql += " address_street text,";				
				sql += " address_city char(12),";				
				sql += " mobile_number char(12),";				
				sql += " voter_rating text not null,";		//0-10 scale		
				sql += " next_schedule_date date default '2014-12-01');";
				return sql;
		}		
	    
	    @Override
		protected String getFixLine(HashMap<String, String> oneRecord)
		{
	    	String fixLine="170=visited|186="+mCitizenId+"|";
	    	for (int i=0; i<page_tags.length; i++)
	    	{
	    		String value=oneRecord.get((String)page_tags[i][1]);
	    		if (value == null) continue;
	    		fixLine += (""+(int)page_tags[i][0]);
	    		fixLine += "=";
	    		fixLine += value;
	    		fixLine += "|";	    		
	    	}
			return fixLine; //need to work with it's fix page tags
		}
	    
	    @Override
		protected void addTableName(HashMap<String, String> aRow)
		{
	    	aRow.put("table_name",  "visited");
			return;
		}

	    @Override
	    protected void getAllRecords()
		{
	    	String sql="select * from visited";	    	
	    	if (mAllRecords != null) mAllRecords.clear();
	    	else mAllRecords=new Vector<ContentValues>();
	    	Cursor csr=DbProcessor.getRecordsFromSql(mActivity, sql);
	    	if (csr.getCount() < 1) {return;}
	    	csr.moveToFirst();
	    	while (!csr.isAfterLast())
			{
	    		ContentValues aC=new ContentValues();
	    		for (int k=0; k<csr.getColumnCount(); k++)
	    		{
	    			aC.put(csr.getColumnName(k), csr.getString(k));
	    		}
	    		mAllRecords.add(aC);
	    		csr.moveToNext();
			}
		}
	    
	    
	    
	    @Override
		protected ContentValues getUserInput() //put inside the mContentValues
		{
	    	ContentValues aRow=new ContentValues();
			for (int i=0; i<pageFields.length; i++)
			{
				EditText txV = (EditText) getActivity().findViewById((Integer)(pageFields[i][0]));
				if (txV==null) continue;
				String txt=txV.getText().toString();
				if (txt != null && txt.length() > 0)
				aRow.put((String)pageFields[i][1], txt);
			}
			aRow.put("voter_rating", mRating);
			
			return aRow;
		}
	    

		static void setDefaultValues(View v)
		{
			Object[][] oTxt=pageFields;
			for (int i=0; i<oTxt.length; i++)
			{
				EditText txV = (EditText) v.findViewById((Integer)(oTxt[i][0]));
				if (txV==null) continue;
				String val=(String)oTxt[i][2];
				if (val.length() > 0)
				txV.setText(val);
			}
	   	}

	    @Override
		protected View setFormDefaultValues(View v)
		{
	    	mRating="not decided yet";
	    	setDefaultValues(v);
			return v;
		}

	    static final String [][] voter_rating={
    		{"Join As Volunteer", "10"},
    		{"Will Contribute", "9"},
    		{"Will Participate", "7"},
    			{"Fan", "8"},
    				{"Will Recommand", "6"},
    					{"Shall Vote For", "5"},
    					{"Probably", "4"},
						{"will look into it", "3"},
						{"Not Decided Yet", "2"},
						{"Not Interested", "1"},
						{"Refused", "0"}
    };
    
	    String mRating;

		public void setupSpinner()
		{
			Spinner spinner = (Spinner) getActivity().findViewById(R.id.rating_spinnner);
		       //建立一個ArrayAdapter物件，並放置下拉選單的內容
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
					getActivity(), R.array.voter_rating, android.R.layout.simple_spinner_item);

		       //ArrayAdapter<string> adapter = new ArrayAdapter<string>(this,android.R.layout.simple_spinner_item,new String[]{"紅茶","奶茶","綠茶"});
		       //設定下拉選單的樣式
		        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		        spinner.setAdapter(adapter);
		       //設定項目被選取之後的動作
		       spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
		            public void onItemSelected(AdapterView adapterView, View view, int position, long id){
		               mRating=voter_rating[position][0];
		            }
		            public void onNothingSelected(AdapterView arg0) {
		                mRating="Not Decided Yet";
		           }
		        });
		}

		@Override
		public void setFormValues(View v, HashMap<String, String> oneRecord)
		{
			for (int i=0; i<pageFields.length; i++)
			{
				((EditText)(v.findViewById((int)pageFields[i][0]))).setText(oneRecord.get((String)pageFields[i][1]));
			}
			setupSpinner();
			return ;
		}
		
		@Override
		protected View openForm(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.visit_form, container, false);
			setupSpinner();
			return rootView;
		}

}

