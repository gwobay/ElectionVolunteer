package com.example.volunteerhandbook;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
public class ListRecordBase extends ListFragment implements MenuItem.OnMenuItemClickListener {
	public static final int ACTION_PROCESS_NOTIFICATION=2;
	
	public static final String TABLE_NAME="table_name";
	public static final String ACTION_TYPE="ACTION_TYPE";
	public static final String SELECTED_RECORD="SELECTED_RECORD";
	public static final String PASSDOWN_MSG="PASSDOWN_MSG";
	
	static Bundle myLastBundle=null;
		
	protected Context mContext;
	protected Activity mActivity;
	protected ViewGroup formFrame;
	protected ViewGroup listFrame;
	protected ListView mViewOfList=null;
	protected View mFormView=null;
	protected ListAdapter myAdapter=null;
	protected static ContentValues firstNew=null;
	protected static String FOCUS_FIELD_TAG="focus_this";
	protected static String mCitizenId;
	protected ListViewAutoScrollHelper listScroller=null;
	protected HashMap<String, String> defaultValues;
	protected HashMap<String, String> selectedValues;
	protected Vector<ContentValues> mAllRecords;

	protected DbProcessor mDbExcutor;
	protected SQLiteDatabase mDb;
	protected ContentValues mContentValues;
	// for ContentValues
	protected String fieldNames; //delimited by ", "
	protected String criteria;   // for rawQuery you can use ... where abc > ? and efg < ? and those ? can be replaced by
	protected String[] criteria_values;
	//for Cursor
	protected Vector<String> listColumns;//={"rowid _id", "visit_date", "full_name"}; id is mandatory for simpleCursorAdapter 
	protected Vector<String> showColumns;//={"visit_date", "full_name"};
	protected String orderBy; //default is date 
	protected String groupBy; //
	
	public static String mToday;
	public static String dbTableName;
	
	protected static HashMap<String, String> tagNames=null;
	protected static HashMap<String, String> nameTags=null;
	protected static Object[][] tableColumns=null;
	
	public ListRecordBase() {
		// TODO Auto-generated constructor stub
		super();

	}
	
	protected void checkForServerData(int last_record, SQLiteDatabase aDb)
	{
		
	}

	public static String getToday()
	{
		DecimalFormat dF=new DecimalFormat("00");
		Calendar aC=Calendar.getInstance();
		String today=dF.format(aC.get(Calendar.YEAR));
		today += ("-"+dF.format(aC.get(Calendar.MONTH)+1)+"-");
		today += dF.format(aC.get(Calendar.DATE));
		return today;
	}
	
	protected void init()
	{

	}

	public static void setCitizenId(String id)
	{
		mCitizenId=id;
	}

	public static String createWhereClause(HashMap<String, String> specifiedFields)
	{
		if (specifiedFields==null || specifiedFields.size()<1) return "";
		String criteria=" where ";
		Set<String> keys=specifiedFields.keySet();
		Iterator<String> itr=keys.iterator();
		boolean once=true;
		while (itr.hasNext())
		{
			if (!once){ criteria += " and ";} else once=false;
			String key= itr.next();
			criteria += key;
			criteria += "='";
			criteria += specifiedFields.get(key);
			criteria += "'";			
		}
		return criteria;
	}
	
	protected HashMap<String, String> fromContentValuesToMap(ContentValues aC)
	{
		if (aC==null || aC.size()<1) return null;
		HashMap<String, String> aMap=new HashMap<String, String>();
		Set<String> keys=aC.keySet();
		Iterator<String> itr=keys.iterator();
		while (itr.hasNext())
		{
			String key= itr.next();
			aMap.put(key, aC.getAsString(key));
		}
		return aMap;
	}
	
	public HashMap<String, String> getValidColumns(HashMap<String, String> aRow)
	{
	 return aRow;
	}
	protected ContentValues fromMapToContentValues(HashMap<String, String> aRow)
	{
		return getValidData(aRow);
		/*if (aRow==null || aRow.size()<1) return null;
		ContentValues oC=new ContentValues();
		Set<String> keys=aRow.keySet();
		Iterator<String> itr=keys.iterator();
		while (itr.hasNext())
		{
			String key= itr.next();
			if (key.compareToIgnoreCase("table_name")==0) continue;
			oC.put(key, aRow.get(key));
		}
		return oC;*/
	}
	
	public void saveFIXData(String fixLine)
	{
		selectedValues=parseFixLine(fixLine);
		ContentValues aRow=fromMapToContentValues(selectedValues);
		String tableName=DataStorage.getTableName(fixLine);
		if (tableName != null)
		DbProcessor.insertTable(mActivity, tableName, null, aRow);
	}
	
	public void saveFIXData(String fixLine, Activity av)
	{
		//init();
		
		HashMap<String, String> aRowValue=parseFixLine(fixLine);
		ContentValues aRow=fromMapToContentValues(aRowValue);
		String tableName=DataStorage.getTableName(fixLine);
		if (tableName != null)
		{
			if (tableName.equalsIgnoreCase("agenda") && aRow.getAsString("status")==null)
					aRow.put("status", "new");
			
			if (mActivity != null)
			DbProcessor.insertTable(mActivity, tableName, null, aRow);
		}
		//because it is opened by Context not by bounded activity
	}

	protected void setDeletedStatus(ContentValues aRow)
	{
		return;
	}
	public void saveFIXDataSet(HashSet<String> data)//, Activity av)
	{
		Iterator<String> itr=data.iterator();
		while (itr.hasNext())
		{
			String fixLine=itr.next();
			int ix9=fixLine.indexOf("|201=");
			if (ix9 < 0) ix9=fixLine.length()-1;
		
		HashMap<String, String> nRow=parseFixLine(fixLine.substring(0, ix9+1));
		ContentValues aRow=fromMapToContentValues(nRow);
			if (fixLine.indexOf("2039=CANCEL") > 0) 
			//aRow.put("status", "DELETED");
			{
				setDeletedStatus(aRow);
				continue;
			}
		String tableName=DataStorage.getTableName(fixLine);
			if (tableName != null)
			{
				if (tableName.equalsIgnoreCase("agenda") && aRow.getAsString("status")==null)				
						aRow.put("status", "new");			
				DbProcessor.insertTable(mActivity, tableName, null, aRow);
				
			}
		}
		//closeDb(); //because it is opened by Context not by bounded activity
	}
	
	protected Vector<ContentValues> getAllListRecord(Cursor aCursor)
	{
		//please override it
		aCursor.close();
		return null;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
        if (savedInstanceState != null)
        	restoreInstanceState(savedInstanceState);        	
    }
	
	protected int getCurrentRowId()
	{
		return 0;
	}
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getCurrentFormData(outState);
        //
        int iOpen=getArguments().getInt(SELECTED_RECORD, -1);
        if (iOpen >=0) outState.putInt(SELECTED_RECORD, iOpen);
        else outState.putInt(SELECTED_RECORD, getCurrentRowId());
    	
        if (mDb != null) mDb.close();
    }
	
	protected void restoreInstanceState(Bundle savedInstanceState) {
		selectedValues=getFromSavedInstance(savedInstanceState);
    }

	//following methods must be override
	
	protected void getCurrentFormData(Bundle outState)
	{
		return ;
	}
	
	protected HashMap<String, String> getFromSavedInstance(Bundle savedInstanceState)
	{
		return null;
	}
	protected void setConstants()
	{
		
	}
	
	protected int checkIfHasTable(String tblName)
	{
		DbProcessor.ifTableExists(mActivity, tblName);
		return 1;
	}
	
	protected void getAllRecords()
	{
		mAllRecords=null;
	}
	protected String createTableSql()
	{
		String sql="create table if not exists visited";
		/*
			sql += "( vid int ,";
			sql += " citizen_id char(12) primary key not null,";
			sql += " visit_date date  not null default '2014-05-01',";
			sql += " full_name text not null,"; 
			sql += " address_street text,";				
			sql += " address_city char(12),";				
			sql += " mobile_number char(12),";				
			sql += " voter_rating text not null,";		//0-10 scale		
			sql += " next_schedule_date date default '2014-05-01');";
		*/
			return sql;
	}		
	
	protected ContentValues setValue(String visit_date, String full_name, 
			String address_street, String address_city,
			String mobile_number, String voter_rating, 
			String next_schedule_date)
	{
		ContentValues aC=new ContentValues();
		//aC.put("vid", iVid++);
		aC.put("citizen_id",  "0123456789");
		aC.put("visit_date", visit_date); aC.put("full_name", full_name);
		aC.put("address_street", address_street); aC.put("address_city", address_city);
		aC.put("mobile_number", mobile_number); aC.put("voter_rating", voter_rating);
		aC.put("next_schedule_date", next_schedule_date);
		return aC;

	}

	protected static Object[][] page_tags=null;
    protected static Object[][] pageFields=null;

	protected String getTagName(int iTag)
	{
		for (int i=3; i<page_tags.length; i++)
		{
			if (iTag != (int)page_tags[i][0]) continue;
			return (String)page_tags[i][1];
		}
		return null;
	}
	
	protected String getFixLine(HashMap<String, String> oneRecord)
	{
		return null; //need to work with it's fix page tags
	}
	
	protected void addTableName(HashMap<String, String> aRow)
	{
		
	}
	
	protected String[] getValidFields()
	{
		return null;
	}
	protected ContentValues getValidData(HashMap<String, String> aRow)
	{
		ContentValues aC=new ContentValues();
		String[] valids=getValidFields();
		for (String key : valids)
			aC.put(key, aRow.get(key)); 
		return aC;
	}
	
	protected View setFormDefaultValues(View v)
	{
		return null;
	}
	
	protected void setFormValues(View v, HashMap<String, String> oneRecord)
	{
		return;
	}

	ContentValues getUserInput() //put inside the mContentValues
	{
		return null;
	}
	
	
	protected void saveUserData(String tableName, HashMap<String, String> aRow) //use mContentValues
	{
		//doInsert with table name and mContentValues
		ContentValues dRow=fromMapToContentValues(aRow);
		DbProcessor.insertTable(mActivity, tableName, null, dRow);
	}
	
	protected void saveUserData(String tableName, ContentValues aRow) //use mContentValues
	{
		//doInsert with table name and mContentValues
		DbProcessor.insertTable(mActivity, tableName, null, aRow);
	}
	
	protected void deleteUserData()
	{
		
	}
	
	protected int getRecordCount()
	{
		return -1;
	}
	
	
	public View takeAction(View v)
	{

		(new AlertDialog.Builder(getActivity()).setMessage("Bad Action")).show().setView(v);
		return v;
	}
	
	public static HashMap<String, String> parseFixLine(String aFixLine)
	{
		String[] pairs=aFixLine.split("\\|");
		HashMap<String, String> aRow=new HashMap<String, String>();
		for (int i=0; i< pairs.length; i++)
		{
			String[] tokens=pairs[i].split("=");
			if (tokens.length<2) continue;
			aRow.put(tagNames.get(tokens[0]), tokens[1]);			
		}
		aRow.put("table_name", dbTableName);
		return aRow;
	}
	
	protected HashMap<String, String> getDefaultFieldValues()
	{
		return null;
	}
	
	protected HashMap<String, String> getSelectedFieldValues()
	{
	    	if (mAllRecords==null || mAllRecords.size() < 1)
	    		getAllRecords();
	    	if (mAllRecords==null || mAllRecords.size() < 1) return null;
	    	ContentValues c= mAllRecords.get(getArguments().getInt(SELECTED_RECORD));
			return fromContentValuesToMap(c);		
	}
	
	protected void setMenuActionItem(String status)
	{
		return;
	}
	
	protected void postFormValueToList(HashMap<String, String> aRow)
	{
		return;
	}
	protected int mRecordFormLayout;
	@Override 
	public void onListItemClick(ListView lv, View tv, int where, long rowId)
	{
		//if (mAllRecords==null) getAllRecords();
		getArguments().putInt(SELECTED_RECORD, (int)rowId);
		selectedValues=fromContentValuesToMap(mAllRecords.get((int)rowId));
		int h=tv.getHeight()*(int)rowId;
		setFormValues(mFormView, selectedValues);
		postFormValueToList(selectedValues);
		
		setMenuActionItem(selectedValues.get("status"));
		//TextView vT=(TextView)(mFormView.findViewWithTag(FOCUS_FIELD_TAG));
		//mFormView.requestFocus();
		//((ScrollView)mFormView).fullScroll(View.FOCUS_UP);
		
/*		lv.invalidate();
		listScroller.scrollTargetBy(0, (-3)*h/2);*/
	}
	
	String mPageTitle;
    public boolean onMenuItemClick (MenuItem item)
    {
    	ContentValues dataRow;
    	switch (item.getItemId())
    	{
    	case R.id.action_save:
    		dataRow=getUserInput();
    		saveUserData(dbTableName, dataRow);
    		break;
    	case R.id.action_join:
    		//addToReminder(); // need a variable indicating the form record information
    		break;
    	default:
    		break;
    	}
    	return true;
    }
    
    protected int[] mMenuItemId=null;
    /*
    protected void activateMenuItems()
    {
    	(mActivity).activateMenuExclusive(mMenuItemId);
    }
    */
	protected String[] mShowColumns; //filled by child page class
	protected int[] rListColumnTextViews; //filled by child page class
	protected int mListPage;
	public static Drawable iXX=null;
	public static Drawable iGo=null;
	public static Drawable iNew=null;
	public static Drawable iLove=null;
	
	
	protected void fillListView(ListView v, Cursor csr)
	{
		    ListAdapter adp=new SimpleCursorAdapter(mActivity, 
		    									mListPage, //has the following listview for setting adapter
		    									csr, 
		    									mShowColumns, 
		    									rListColumnTextViews,
		    									0); 
		    ((SimpleCursorAdapter)adp).setViewBinder(getMyCursorBinder());
		    //v.setAdapter(adp);
		    //for listFragment use getListView
		    setListAdapter(adp);
		    myAdapter=adp;
		    return;    		
	}

	protected SimpleCursorAdapter.ViewBinder getMyCursorBinder()
	{		
		return null;
	}
	public static class ShowNewBinder implements SimpleAdapter.ViewBinder
	{
		public boolean setViewValue(View v, Object data, String s)
		{
			int vid=v.getId();
			if (vid == R.id.event_status) 
			{		
				//Log.d("LISTREC", s);
				((ImageView)v).setBackground((Drawable)data);
				/*if (((String)data).toUpperCase().indexOf("JOIN")==0)
				 { 
					((TextView)v).setText(s.substring(4));
					v.setBackground(iGo);
				}
				else if (((String)data).equalsIgnoreCase("NEW"))
					{
						((TextView)v).setText(" ");
						v.setBackground(iNew);
					}
				else
					{v.setVisibility(View.INVISIBLE);}*/
			
			return true;
			}
			
			return false;
			
		}
	}
	
	protected void modifyRowByJob(TreeMap<String, Object> aRow)
	{
		
	}
	
	protected void fillListView(ListView v, ArrayList<HashMap<String, String>> data)
	{
		//in agenda case use agenda list adapter 
	}
	protected void fillListView(ListView v, Vector<ContentValues> records)
	{
			ArrayList<Map<String, Object> > data=new ArrayList<Map<String, Object> >();
			for (int i=0; i<records.size(); i++)
			{
				TreeMap<String, Object> aRow=new TreeMap<String, Object>();
				for (int k=0; k<mShowColumns.length; k++)
				{
					String key=mShowColumns[k];
					String val=records.get(i).getAsString(mShowColumns[k]);
					aRow.put(key, val);
				}
				modifyRowByJob(aRow);
				
				data.add(aRow);
			}
		    //if (data.isEmpty()) return;
		    ListAdapter adp=new SimpleAdapter(mActivity, 
		    									data, 
		    									mListPage, //has the following listview for setting adapter
		    									mShowColumns, 
		    									rListColumnTextViews); 
		    ((SimpleAdapter)adp).setViewBinder(new ShowNewBinder());
		    v.setAdapter(adp);
		    myAdapter=(SimpleAdapter)adp;
		    return;    		
	}

	protected View openListPage(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.list_view, container, false);
		rootView.setBackgroundColor(0xC3FDB8);
		return rootView;
	}
	
	protected View openForm(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
	{
		return null;
	}
	
	protected View setTopFormContent(ViewGroup v, HashMap<String, String> data)
	{
		return null;
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
	}
	
	protected void setUpMyView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
	{
		
	}
	protected void changeNewRecordToOpen()
	{
		
	}
	ViewGroup getFormFrame()
	{
		return null;
	}
	
	ViewGroup getListFrame()
	{
		return null;
	}
	
	protected void checkIfNotification(String fixLine)
	{
		
	}
	
	protected void checkAndFillListArray()
	{
		
	}
	
	/*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

    	init();
    	if (savedInstanceState != null)
    	restoreInstanceState(savedInstanceState);
    	ContentValues dataRow;
    	
    	String key=getResources().getString(R.string.fix_line_key);
    	String fixLine=getArguments().getString(key);
    	if (fixLine != null)
    	{
    		checkIfNotification(fixLine);
    		
     	} 

    	WindowManager windowManager=(WindowManager)(mActivity.getSystemService(Context.WINDOW_SERVICE));
		  Display display=windowManager.getDefaultDisplay();
		  mCurrentRotation=display.getRotation();
		  
    	getListData();
    	View mRootView;
    	int rotation=getArguments().getInt("ROTATION");
		if (rotation==Surface.ROTATION_90)
			mRootView = inflater.inflate(R.layout.agenda_form_list_landscape, container, false);
		else
    	mRootView=inflater.inflate(R.layout.agenda_form_list, container, false);
    	mFormView=mRootView.findViewById(R.id.agenda_form);
    			//openForm(inflater, container, savedInstanceState);
		
    	//getAllRecords();
    	//if (mAllRecords==null) return null;
    	checkAndFillListArray();
    	mViewOfList=(ListView) mRootView.findViewById(android.R.id.list);
    			//openListPage(inflater, container, savedInstanceState); 
    	
    	/*ViewGroup lFrame=getListFrame();
    	lFrame.removeAllViews();
    	lFrame.addView(mViewOfList);
    	((ListView)mViewOfList).setHeaderDividersEnabled(true);
    	mViewOfList.setBackgroundResource(R.drawable.list_background);
    	
		//(mActivity).activateMenu();
		listScroller=new ListViewAutoScrollHelper((ListView)mViewOfList);
		
	//might be overridden to use cursor
		fillListView((ListView)mViewOfList, mAllRecords);
		
		mViewOfList.postInvalidate();
		
		
		int iOpen;
		if (selectedValues==null)
		{   	
			iOpen=getArguments().getInt(SELECTED_RECORD, -1);
    		if (iOpen>=0)    	
    	selectedValues=fromContentValuesToMap(mAllRecords.get(iOpen));
    	
		}		
		setFormValues(mFormView, selectedValues);
    	mFormView.postInvalidate();
    	//mFormView.setFocusable(true);  
   	
    	return mRootView;
    }
*/     
    protected boolean confirmListData()
    {
    	return false;
    }
    
    protected ArrayList<HashMap<String, String> > getListData()
    {
    	return null;
    }
    
    public ListView getMyListView()
    {
    	return (ListView) mViewOfList;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	
        super.onActivityCreated(savedInstanceState);

		//might be overridden to use cursor
        
        if (confirmListData())
		fillListView(getMyListView(), getListData());
		
        if (mViewOfList!=null)
		mViewOfList.postInvalidate();
		
		/*mFormView=openForm(LayoutInflater.from(getActivity()), getFormFrame(), savedInstanceState);
		
		int iOpen;
		if (selectedValues==null)
		{   	
			iOpen=getArguments().getInt(SELECTED_RECORD, -1);
    		if (iOpen>=0)    	
    	selectedValues=fromContentValuesToMap(mAllRecords.get(iOpen));
    	
		}		
		setFormValues(mFormView, selectedValues);
    	mFormView.postInvalidate();
    	//mFormView.setFocusable(true);  
*/
    }
}
