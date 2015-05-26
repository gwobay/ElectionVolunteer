package com.example.volunteerhandbook;


	import java.util.Locale;

	import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

	public class VisitFragment extends Fragment 
	{
		public VisitFragment() {
				// TODO Auto-generated constructor stub
			}	    
		public static final String ARG_PLANET_NUMBER = "planet_number";
		
		static Activity mActivity;
		static Context mContext;
		public void setActivity(Activity av)
		{
			mActivity=av;
			mContext=av.getApplicationContext();
		}

		public static Object[][] tagNames={
			{170,"visited"},{35,"msgType"},{ 11,"vid"},{186, "citizen_id"},{75,"visit_date"},
			{179,"full_name"},{171, "address_street"}, {172, "address_city"},
			{181,"mobile_number"},{64,"next_schedule_date"},
				{ 178,"voter_rating"}
		};
		static String[] listColumns={"rowid _id", "visit_date", "full_name"};
		
		static String createVisitedTable()
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
				sql += " next_schedule_date date default '2014-05-01');";

				return sql;
		}		
		
		static DbProcessor mDb=null;
		static ContentValues mContentValues=null;
		static int iVid=10001;
		
		static ContentValues setValue(String visit_date, String full_name, 
								String address_street, String address_city,
								String mobile_number, String voter_rating, 
								String next_schedule_date)
		{
			ContentValues aC=new ContentValues();
			aC.put("vid", iVid++);
			aC.put("citizen_id",  "0123456789");
			aC.put("visit_date", visit_date); aC.put("full_name", full_name);
			aC.put("address_street", address_street); aC.put("address_city", address_city);
			aC.put("mobile_number", mobile_number); aC.put("voter_rating", voter_rating);
			aC.put("next_schedule_date", next_schedule_date);
			return aC;
			
		}
		public void startDb()
		{
			if (mDb != null) return;
			mDb=new DbProcessor(mContext, "kp_volunteer_db");
			//DbProcessor.createTable(createVisitedTable());			
		}
		
		static void closeDb()
		{
			if (mDb!=null) mDb.close();
			mDb=null;
		}
		
		void doInsert(SQLiteDatabase db)
		{
			String eMsg="";
			try {
				db.insert("visited", null, mContentValues);
			} catch (SQLiteConstraintException e){ eMsg=e.getMessage();}
			catch (SQLiteException e){ eMsg=e.getMessage();}
				if (eMsg.length() >0)
			{Toast.makeText(mContext, eMsg, Toast.LENGTH_SHORT).show();}
		}
		public void doTest()
		{
			mDb=new DbProcessor(mContext, "volunteer_db");
			try {
				DbProcessor.createTable(getActivity(), createVisitedTable());
			} catch (SQLiteException e){
				if (e.getMessage().indexOf("already exists")<0)
			{Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();}
				}
			

			SQLiteDatabase db=mDb.getDb();
			mContentValues=setValue("2014-04-05", "f.y. Kou", "20402 26 Ave.,", "Bayside", "0123456789", "fans", "2014-10-10");
			doInsert(db);
			mContentValues=setValue("2014-04-05", "f.f. Gou", "20402 30 Ave.,", "Flushing", "0123456789", "Will Vote", "2014-10-10");
			doInsert(db);
			mContentValues=setValue("2014-04-06", "k.y. Mohamod", "21402 6 Ave.,", "NYC", "0123456789", "not interested", "2014-10-10");
			doInsert(db);
			mContentValues=setValue("2014-04-07", "I.G. Mou", "10402 26 Street,", "Queens", "0123456789", "refused", "2014-");
			doInsert(db);
			mContentValues=setValue("2014-04-10", "H.y. Ciou", "2402 20 Ave.,", "Bayside", "0123456789", "fans", "2014-10-10");
			doInsert(db);
		}
		int getRowCount()
		{
			return DbProcessor.selectCount(getActivity(), "visited", null);
		}
		Cursor getRecords(String[] columns, String whereC, String[] forWhere, String gBy, String having, String oBy, String lmt)
		{
			if (mDb==null) return null;
			SQLiteDatabase db=mDb.getDb();
			return db.query("visited", columns, whereC, forWhere, gBy, having, oBy, lmt);
		}
		
		boolean mAddNew;
		public void addingNew(boolean T_F)
		{
			mAddNew=T_F;
		}
		
		View forCreateNew(LayoutInflater inflater, ViewGroup container,
		        Bundle savedInstanceState) 
			{
		    	return inflater.inflate(R.layout.list_view, container, false);
			}
	@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) 
		{
			if (mAddNew) return forCreateNew(inflater, container, savedInstanceState);
	    	View rootView=null;
	    	startDb();
	    	if (getRowCount() < 1 ) { 
	    		closeDb(); 
	    	return forCreateNew(inflater, container, savedInstanceState);
	    	}
	 	
	    	rootView = inflater.inflate(R.layout.list_view, container, false);
	    	/*
	    	//int i = getArguments().getInt(ARG_PLANET_NUMBER);
	    	
	    		Button aBt= (Button)(mActivity.findViewById(R.id.modify_visit));
	    		if (aBt != null)
	    		aBt.setVisibility(0);
	    	Cursor testCursor=getRecords(listColumns, null, null, null, null, null, null);
	    	String[] showColumn={"visit_date", "full_name"};
	    	if (testCursor != null)
	    	{
	    		//int[] listDataView={R.id.visiting_date, R.id.visiting_date, R.id.full_name};
	    		int[] listDataView={R.id.visiting_date, R.id.full_name};
		    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(mContext, 
		    			 //R.layout.visit_record, testCursor, listColumns, listDataView, 0);
		    	 		R.layout.visit_record, testCursor, showColumn, listDataView, 0);
		    	ListView lvw=(ListView)(rootView.findViewById(R.id.visit_record));
	    		lvw.setAdapter(adapter);
	    	}
	    	*/
	    	return rootView;
		}
	}


