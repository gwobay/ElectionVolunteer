package com.example.volunteerhandbook;

import java.util.HashMap;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class FundRaiseRecord extends ListRecordBase {

	public FundRaiseRecord() {
		super();
    	/* set up the following 
    	protected String[] mShowColumns; //filled by child page class
    	protected int[] rListColumnTextViews; //filled by child page class
    	protected int layout_list_row_by_page;
    	*/
		mShowColumns=new String[]{"fund_raised_date", "full_name", "mobile_number"};
		rListColumnTextViews=new int[]{R.id.fund_raised_date, R.id.full_name, R.id.mobile_number};
		mListPage=R.layout.visit_list;
		mRecordFormLayout = R.layout.fund_raised_form;
		
	}
	public static final String tableName="fund_raised";
	static Object[][] page_tags={
		{170,"fund_raised"},{35,"msgType"},{ 11,"vid"},{ 186,"citizen_id"},
		{151, "fund_raised_date"}, 
		{152, "full_name"}, 
		{153, "street_address"}, 
		{154, "address_city"}, 
		{155, "mobile_number"}, 			 
		{156, "amount"}, {158, "contributor_citizen_id"}, 
		{157, "receipt_number"}
	};

    static Object[][] pageFields={
		{R.id.fund_raised_date, "fund_raised_date", "0"}, 
		{R.id.full_name, "full_name","0"}, 
		{R.id.contributor_citizen_id, "contributor_citizen_id","0"}, 
		{R.id.street_address, "street_address","0"}, 
		{R.id.address_city, "address_city", "0"}, 
		{R.id.mobile_number, "mobile_number","0"}, 
		{R.id.amount, "amount","0"},
		{R.id.receipt_number, "receipt_number","0"}
	};
    

    @Override
	protected String createTableSql()
	{
		String sql="create table if not exists fund_raised";
			sql += "(citizen_id char(12) primary key not null,";
			sql += " fund_raised_date date  not null default '2014-05-01',";
			sql += " full_name text not null,"; 
			sql += " address_street text,";				
			sql += " address_city char(12),";				
			sql += " mobile_number char(12),";				
			sql += " amount char(12),";				
			sql += " contributor_citizen_id char(12),";		//0-10 scale		
			sql += " receipt_number char(12)');";
			return sql;
	}		
    
    @Override
	protected String getFixLine(HashMap<String, String> oneRecord)
	{
    	String fixLine="170=fund_raised|186="+mCitizenId+"|";
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
    	aRow.put("table_name",  "fund_raised");
		return;
	}

    @Override
    protected void getAllRecords()
	{
    	String sql="select * from fund_raised";	    	
		//mAllRecords=getAllListRecord(selectRecords(sql));
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
		
		return aRow;
	}
	

    @Override
	protected View setFormDefaultValues(View v)
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
		return v;
   	}
 
	protected void setFormValues(View v, HashMap<String, String> oneRecord)
	{
		Object[][] oTxt=pageFields;
		for (int i=0; i<oTxt.length; i++)
		{
			EditText txV = (EditText) v.findViewById((Integer)(oTxt[i][0]));
			if (txV==null) continue;
			String val=(String)oneRecord.get((String)oTxt[i][1]);
			if (val.length() > 0)
			txV.setText(val);
		}
		return ;
	}


	@Override
	protected View openForm(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fund_raised_form, container, false);
		//setupSpinner();
		return rootView;
	}

}
