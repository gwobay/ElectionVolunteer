package com.example.volunteerhandbook;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.kou.utilities.AsyncSocketTask;
import com.kou.utilities.DaemonCallSocket;
import com.kou.utilities.FixDataBundle;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class WorkMemoRecord extends ListRecordBase 
			implements 
			//View.OnClickListener, 
			DaemonCallSocket.Listener 
{
	static LinearLayout.LayoutParams llParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
			LinearLayout.LayoutParams.WRAP_CONTENT);
	
	static Thread sockThread=null;
	static ArrayList<byte[]> msgQ=null;//new Vector<String>();
	ArrayList<byte[]> pendingQ;
	static ReentrantReadWriteLock rLock=new ReentrantReadWriteLock();
	
	static String myLastName=null;
	static String myFirstName=null;
	static String myName=null;
	static HashMap<String, String> authors=new HashMap<String, String>();
	static MemoListAdapter myAdapter=null;
	public WorkMemoRecord() {
		// TODO Auto-generated constructor stub
			// TODO Auto-generated constructor stub
		super();
    	
		if (msgQ==null)
			msgQ=new ArrayList<byte[]>();
			checkServer=false;
	}
	
	boolean checkServer;
	public void setActivity(Activity av)//, String mId	)//for update from main on the start up
	{
		mActivity=av;
		mContext=mActivity.getApplicationContext();
		//mCitizenId=mId;
		//imBackground=background_process;
	}
	
	static ArrayList<CurrentMemo> dataSink=new ArrayList<CurrentMemo>();
	public static class CurrentMemo
	{
		String date;
		String subject;
		String author;
		private String author_cid;
		private String detail;
		private String recv_time;
		private ArrayList<CurrentMemo> children;
		public CurrentMemo(String date1, String subj, String auth, String cid, String desc, String rcvtm)
		{
			date=date1; subject=subj; author=auth; author_cid=cid; detail=desc; recv_time=rcvtm;
		}
		public boolean imAuthor()
		{
			return (mCitizenId.equalsIgnoreCase(author_cid));
		}
		public void fillMemoData(HashMap<String, String> store)
		{
			store.put("memo_date", date);
			store.put("memo_title", subject);
			store.put("memo_author", author);
			store.put("memo_description", detail);
			store.put("recv_time", recv_time);
			store.put("author_citizen_id", author_cid);
		}
		//------------------ following will be used for open sub listview in dialog
		//                                                    or replace this one
		//                                                  using backstack
		public ArrayList<CurrentMemo> getChild()
		{
			return children;
		}
		public boolean hasChildren()
		{
			return (children!=null && children.size()>0);
		}
	}
	
	public void checkServer(boolean T_F)
	{
		checkServer=T_F;
	}
	@Override
	protected void init()
	{
		setConstants();
		mDbExcutor=null;
		mShowColumns=new String[]{"memo_date", "memo_title", "memo_location", "status", "participants"};
		rListColumnTextViews=new int[]{R.id.memo_date, R.id.memo_title};
		mListPage=R.layout.memo_list;
		mRecordFormLayout = R.layout.memo_form;
		if (mActivity==null) mActivity=getActivity();
		if (myLastName==null || myFirstName==null)
		{
			SharedPreferences sharedPref = mActivity.getSharedPreferences(
						MainActivity.getFileHeader()+"profile", Context.MODE_PRIVATE);
			myLastName=sharedPref.getString("last_name", "--");
			if (myLastName.charAt(0)!= '-')
			{
				myFirstName=sharedPref.getString("first_name", "--");
				myName=myLastName+myFirstName;
			}	    	
		}
	    if (!DbProcessor.ifTableExists(mActivity, "workmemo")){
	    	String sql=createTableSql();
	    	DbProcessor.createTable(mActivity, sql);//doCreateTable();
	    	checkForServerData(1, mDb);
	    	checkServer=true;
	    }
	    else
	    {
	    	//select data from agenda into memo
	    	
	    	String sql="insert into workmemo (memo_date, memo_title, memo_location, memo_description)"; 
	    	sql += " select event_date, event_title, event_location+', '+event_city as location, event_description";
	    	sql += " from agenda where status='JOIN' and event_date > '"+getToday()+"'";
	    	runThisSql(sql);
	       	//checkServer=true;
	    }
	    if (checkServer)
	    {
	    	SharedPreferences sharedPref = mActivity.getSharedPreferences("memo_last_check_time", Context.MODE_PRIVATE);
	    	long lTime=sharedPref.getLong("memo_last_check_time", 0);
	    	long timeNow=new Date().getTime();
	    	if (lTime ==0 || lTime+8*3600*1000 <  timeNow)
	    	{
	    		DbProcessor dp=new DbProcessor(mActivity, "kp_volunteer_db");
	    		SQLiteDatabase aDb=dp.getDb();
	    		if (aDb != null) {
			    checkForServerData(1, aDb);			   
			    SharedPreferences.Editor writer=sharedPref.edit();
			    writer.putLong("memo_last_check_time", timeNow);
			    writer.commit();
	    		}
	    	}
		    return;
	    }
	    
	    if (mActivity==null) mActivity=(WorkMemoActivity)getActivity();
		iXX=mActivity.getResources().getDrawable(R.drawable.ic_action_x);
		iGo=mActivity.getResources().getDrawable(R.drawable.i_go);
		iNew=mActivity.getResources().getDrawable(R.drawable.new_one);
		iLove=mActivity.getResources().getDrawable(R.drawable.i_love);
	}
	
	public void initMe()
	{
		init();
	}
	

	Object[][] table_tags;
	@Override
	protected void setConstants()
	{	
    	dbTableName="workmemo";
    	table_tags=new Object[][]{
			{170,"workmemo"},{35,"msgType"},{ 186,"citizen_id"},
			{181, "memo_date"}, 			 
			{182, "memo_title"},
			{183, "recv_time"}, 
			{184, "memo_author"}, 
			{185, "author_citizen_id"}, 
			{188, "memo_description"},
			{189, "memo_location"}, 			
			{187, "status"}
		};
    	
    	page_tags=table_tags;

	    pageFields=new Object[][]{
			{R.id.memo_date, "memo_date", " "}, 			 
			{R.id.memo_title, "memo_title"," "}, 
			{R.id.memo_author, "memo_author", "台北市"}, 
			{R.id.memo_description, "memo_description"," "}
		};	    
	}
	@Override
	protected String[] getValidFields()
	{
		return new String[]{
			"memo_date", "recv_time", 
			"memo_title", "author_citizen_id",
				"memo_location", "memo_author", "memo_status", "memo_description"
				};
	}
	public static String getTableName()
	{
		return "workmemo";
	}
	
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
	
    @Override
	protected String createTableSql()
	{
		String sql="drop table if exists workmemo; ";
		runThisSql(sql);
			sql ="create table if not exists workmemo";
			sql += "( memo_date date not null default '2014-05-01',";
			sql += " recv_time char(8) not null default '00000000',"; 
			sql += " memo_title char(40) not null,"; 
			sql += " memo_location char(40),";				
			sql += " memo_author char(12) not null default 'myself',";	
			sql += " author_citizen_id char(10),";	
			sql += " status char(12),";  //new, opened or cancelled
			sql += " memo_description text );";//,";
			//sql += " constraint date_title_time unique ";
			//sql += " (memo_title, memo_date, recv_time) );";
		//runThisSql(sql);
			////sql += " status char(8) not null default 'new',";
			//sql += " participants char(8) default '0' );";//new, cancle, read, join
			return sql;
	}		

    protected void createMyTable()
	{
		String sql="drop table if exists workmemo; ";
		runThisSql(sql);
			sql ="create table if not exists workmemo";
			sql += "( memo_date date not null default '2014-05-01',";
			sql += " recv_time char(8) not null default '00000000',"; 
			sql += " memo_title char(40) not null,"; 
			sql += " memo_location char(40),";				
			sql += " memo_author char(12) not null default 'myself',";	
			sql += " author_citizen_id char(10),";	
			sql += " status char(12),";  //new, opened or cancelled
			sql += " memo_description text );";
		runThisSql(sql);
			////sql += " status char(8) not null default 'new',";
			//sql += " participants char(8) default '0' );";//new, cancle, read, join
			return ;
	}

    void saveFormDataToDb()
    {
    	String columns="(";
    	String values=" values (";
    	Iterator<String> itr=viewHolder.data.keySet().iterator();
    	while (itr.hasNext())
    	{
    		String key=itr.next();
    		columns += (key+", ");
    		values += ("'"+viewHolder.data.get(key)+"', ");
    	}
    	String aId=authors.get(viewHolder.data.get("author"));
    	if (aId != null)
    	{
    		columns += "author_citizen_id,";
    		values += ("'"+aId+"', ");
    	}
		columns += "recv_time)";
		values += ("'"+(new Date().getTime() % 10000000)+"') ; ");
		String sql="insert into workmemo "+columns+values;
		runThisSql(sql);

    }
	HashMap<String, String> defaultRow()
	{
		HashMap<String, String> mp=new HashMap<String, String>();
		for (int i=0; i<pageFields.length; i++)
		{
			mp.put((String)pageFields[i][1], (String)pageFields[i][2]);
		}
		mp.put("status", "null");
		return mp;
	}
	
	void runThisSql(String sql)
	{
    	final String sSQL=sql;
    		new Thread(new Runnable(){
    			public void run()
    			{
    				DbProcessor.execSQL(mActivity, sSQL);
    			}
    		}).start();	    		 
	}
	void processServerResponse(Vector<byte[]> resp)
	{
		HashSet<String> newData=new HashSet<String>();
		for (int i=0; i<resp.size(); i++)
		{
			FixDataBundle aFDB=new FixDataBundle(resp.get(i));
			newData.add(aFDB.getFixLine());
		}
		saveFIXDataSet(newData);
		Log.i("MEMO", "GOT SERVER DATA "+resp.size());
		newData.clear();
		newData=null;
	}
	
	String getLastUpdateDate()
	{
		String sql="select max(memo_date) from workmemo;";
		String today=null;
		Cursor c1=DbProcessor.getRecordsFromSql(mActivity, sql);
		if (c1!= null && c1.getCount() > 0)
		{
			c1.moveToFirst();
			today=c1.getString(0);
			c1.close();
			c1=null;
		}
		return today;
	}
	
	void sendDataToServer(String command, Vector<byte[]> inBox, SQLiteDatabase aDb)
	//make sure this is called from a thread but not the main ui thread
	{
		final AsyncSocketTask aTask=new AsyncSocketTask();
    	//aTask.setVectorStore(responseStore);
		boolean wait4Response=(inBox != null);
		aTask.needResponse(wait4Response);
    	//Vector<byte[]> resp=new Vector<byte[]>();
    	aTask.setDataBox(inBox);
		
		/*if (today==null){
	    	Calendar aC=Calendar.getInstance();
	    	today=dF.format(aC.get(Calendar.YEAR));
	    	today += ("-"+dF.format(aC.get(Calendar.MONTH)+1)+"-");
	    	today += dF.format(aC.get(Calendar.DATE));
    	}
    	String command="35=RESEND|170=memo|151="+today+"|";*/
    	byte[] sendByts=AsyncSocketTask.convertStringToBytes(command);
    	aTask.execute("220.134.85.189".getBytes(), "9696".getBytes(), sendByts, "120".getBytes());
    	
    	//final boolean processed=false;
    	if (!wait4Response) return;
    	//get a thread wait for the result
    	final SQLiteDatabase sDb=aDb;
    	final Vector<byte[]> boxToCheck=inBox;
    	new Thread(new Runnable(){
    		public void run(){
    	try {
			Vector<byte[]> response=aTask.get(2*60000, TimeUnit.MILLISECONDS);
			if (response != null) 
			{
				if(!readyAndSave(response, sDb))
				processServerResponse(response) ;
				response.clear();
				//processed=true;
			}
		} catch (InterruptedException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch ( TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	aTask.cancel(true);	
    		}
    	
    	}).start();
    	
    	//if (!processed && inBox.size() > 0)
    		//processServerResponse(inBox);
	}
	
	@Override
	protected void checkForServerData(int last_record, SQLiteDatabase aDb)
	{
		String today=null;
		if (last_record > 0) 
			today=getLastUpdateDate();
		DecimalFormat dF=new DecimalFormat("00");
		//send fixLine with 35=RESEND, memo_date >= today;
    	Vector<byte[]> resp=new Vector<byte[]>();
    	if (today==null){
	    	Calendar aC=Calendar.getInstance();
	    	today=dF.format(aC.get(Calendar.YEAR));
	    	today += ("-"+dF.format(aC.get(Calendar.MONTH)+1)+"-");
	    	today += dF.format(aC.get(Calendar.DATE));
    	}
    	String command="35=RESEND|170=memo|151="+today+"|";
    	sendDataToServer(command, resp, aDb);
	}
	

	public boolean readyAndSave(Vector<byte[]> outQ, SQLiteDatabase aDb)
	{
		if (aDb == null ||  !aDb.isOpen() )
			return false;
		for (int i=0; i<outQ.size(); i++)
		{
			//have to convert from bytes back to string (w/ header removed)
			FixDataBundle data=new FixDataBundle(outQ.get(i));
			String fixLine=data.getFixLine();
			Log.d("MEMO", "got server response "+fixLine);
			ContentValues aC=fromMapToContentValues(parseFixLine(fixLine));
			
			aDb.insert("workmemo", null, aC);
			
			if (data.getStream() != null)
			{
				
			}
		}
		outQ.clear();
		aDb.close();
		return true;
	}
	
	
	public HashMap<String, String> getDataForMap(View v1) //form on page
	{
		//get address
		View v=mFormView;//(View)v1.getParent();
		TextView txV = (TextView) v;//.findViewById(R.id.memo_location);
		if (txV==null) return null;
		String street=txV.getText().toString();
		txV = (TextView) v.findViewById(R.id.memo_author);
		if (txV==null) return null;
		HashMap<String, String> data=new HashMap<String, String>();
		String city=txV.getText().toString();
    	data.put(MapActivity.LOCATION, street+" "+city);
    	txV=(TextView) v.findViewById(R.id.memo_title);
    	if (txV!=null)
    	{
    		String info=(txV.getText().toString()+"(");
    		txV=(TextView) v.findViewById(R.id.memo_date);
    		if (txV!=null) info += txV.getText().toString();
    		if (txV!=null) info += (", "+txV.getText().toString()+")");
    		//info += (street+", "+city);
    		String fixKey=getResources().getString(R.string.fix_line_key);
    		data.put(fixKey, info);
    	}
    	return data;   
		//show info window with mark
	}
	
	
	static boolean try_already=true;

	

	    @Override
		protected String getFixLine(HashMap<String, String> oneRecord)
		{
	    	String fixLine="170=memo|";
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
	    	aRow.put("table_name",  "workmemo");
			return;
		}

	    @Override
	    protected Vector<ContentValues> getAllListRecord(Cursor aCursor)
		{
	    	if (aCursor.getCount()<1)
	    	{
	    		aCursor.close();
	    		Toast.makeText(mActivity,  "NO RECORD TO SHOW", Toast.LENGTH_LONG).show();
	    		if (!IamBoss) {
	    			if (aCursor !=null)
	    			aCursor.close();
	    			((WorkMemoActivity)mActivity).done();	    			
	    			return null;
	    		}
	    	}
	    	Log.d("dbDATA", "CALLED BY"+this.getId());
			Vector<ContentValues> allRows=new Vector<ContentValues>();
			firstNew=null;
			String[] heads=aCursor.getColumnNames();
			aCursor.moveToFirst();
			while (!aCursor.isAfterLast())
			{
				ContentValues aC= new ContentValues();
				for (int k=0; k<aCursor.getColumnCount(); k++)
				{
					switch (aCursor.getType(k))
					{
					case Cursor.FIELD_TYPE_NULL:
						aC.put(heads[k], "0"); break;
					case Cursor.FIELD_TYPE_INTEGER:
						aC.put(heads[k], ""+aCursor.getInt(k) ); break;
					case Cursor.FIELD_TYPE_FLOAT:
						aC.put(heads[k], ""+aCursor.getFloat(k) ); break;
					case Cursor.FIELD_TYPE_STRING:
						aC.put(heads[k], aCursor.getString(k) ); break;
					case Cursor.FIELD_TYPE_BLOB:
						aC.put(heads[k], new String(aCursor.getBlob(k)) ); break;
					default:
						aC.put(heads[k],  "0");
							break;
					}
				}

				allRows.add(aC);
				aCursor.moveToNext();
			}
			aCursor.close();
			return allRows;
		}
		
	    @Override
	    protected void getAllRecords()
		{
	    	String sql="select * from workmemo order by memo_date desc";
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
	    	ContentValues mContentValues=new ContentValues();
	    	getFormData();
	    	Iterator<String> itr=viewHolder.data.keySet().iterator();
			
	    	while (itr.hasNext())
	    	{
	    		String key=itr.next();
	    		mContentValues.put(key, viewHolder.data.get(key));
			}
			//mContentValues.put("status", "new");
			return mContentValues;
		}
		
	    
	    @Override
		protected void getCurrentFormData(Bundle outState)
		{
	    	getFormData();
	    	Iterator<String> itr=viewHolder.data.keySet().iterator();
			
	    	while (itr.hasNext())
	    	{
	    		String key=itr.next();
			outState.putString(key, viewHolder.data.get(key));
			}
			//saveMsgQ(outState);
			//myLastBundle=new Bundle(outState);
			return ;
		}
		
	    @Override
		protected HashMap<String, String> getFromSavedInstance(Bundle sb)
		{
	    	if (viewHolder==null) return null;
	    	
	    	Iterator<String> itr=sb.keySet().iterator();
			
	    	while (itr.hasNext())
	    	{
	    		String key=itr.next();
	    		viewHolder.data.put(key, sb.getString(key));
	    	}
	    	setFormData();
			return viewHolder.data;
		}
	    
	    void sendToServer()
	    {
	    	String fixLine=getFixLine(fromContentValuesToMap(mContentValues));
	    	fixLine += "201=broadCastToAll|";	
	    	//((MemoActivity)getActivity()).sendDataToServer(fixLine);
	    	msgQ.add(fixLine.getBytes());
	    	//processMsgQ();
	    }
	    	    
	    void confirmCancelPopup(String memo_date, String memo_title)
	    {
	    	
	    }
	    @Override
	    public boolean onMenuItemClick (MenuItem item)
	    {
	    	//ContentValues dataRow;
	    	boolean noAction=false;
	    	switch (item.getItemId())
	    	{
	    	case R.id.action_reply:
	    		//when send back to reply                    35=reply
	    		viewHolder.memo_description.setText(getResources().getString(R.string.reply_hint));
	    		// after finish send the reply the reply will become a new memo with title+(replied)
	    		setMenuActionItem("Create");
	    		break;
	    	case R.id.action_cancel:
	    		if (viewHolder==null)
	    		{
	    			if (mRootView==null) return false;
	    			getViewHolder(mRootView);
	    			getFormData();
	    		}
	    		String sql="update workmemo set status='CANCELLED' where memo_date='";
	    			sql += viewHolder.data.get("memo_date")+"' and memo_title='";
	    			sql += viewHolder.data.get("memo_title")+"' and recv_time='";
	    			sql += viewHolder.data.get("recv_time")+"'; ";
	    		runThisSql(sql);
	    		if (!viewHolder.data.get("author").equalsIgnoreCase(myName))
		    	{
		    		String msg=getFixLine(viewHolder.data);
		    		String fixLine="35=void|186="+mCitizenId+"|"+msg+"|220='"+authors.get(viewHolder.data.get("author"));
		    		sendDataToServer(fixLine, null, null);
	    		}
	    		//selectNewListItem();
	    		getListView().setSelection(dataSink.size()-1);
	    		noAction=true;
	    		//setMenuActionItem("Create");
	    		//revoke previous reply
	    		//                                            35=void
	    		//get the author_citizen_id and send the content back indicate cancel the reply
/*	    		if (selectedValues == null) break;
	    			setMenuActionItem("READ");
	    			
	    			String status=selectedValues.get("status");	    			
	    			if (status.toUpperCase().indexOf("JOIN")<0) break;
	    			selectedValues.put("status", "READ");    				
	    			selectedValues.put("paticipants", "0");    				
		    		tt=selectedValues.get("memo_title");//tx.getText().toString();
					dd=selectedValues.get("memo_date");//tx.getText().toString();				
					sql="update memo set status='READ', participants='0' where memo_date='"+dd+"' and "; 
					sql += "memo_title='"+tt+"'";
		    		if (doDbSql(sql))
		    		{
		        		final String fixLine="35=CANCEL|186="+mCitizenId+"|"+getFixLine(selectedValues);
		        			new Thread(new Runnable(){
		        				public void run()
		        				{
		        					sendDataToServer(fixLine, null, null);
		        				}
		        			}).start();
		    
		    			postFormValueToList(selectedValues);
		    		}
		    		((MemoActivity)mActivity).redrawList();
*/	    		
	    		break;
	    	case R.id.action_new:
	    		createNewMemo();
	    		//the send to button within the form will be activated and 
	    		//list of receiver will be shown (from the body list)
	    		setMenuActionItem("Create");	    		
	    		break;
	    	case R.id.action_modify:
	    		modifyMemo();
	    		setMenuActionItem("Create");	    		
	    		break;	    		
	    	case R.id.action_send:
	    		//saved to local db and send to server
	    		//((MemoActivity)getActivity()).connectToServer();
	    		boolean isNew=true;
	    		String command=null;
	    		if (viewHolder.data.size()>1)
	    			isNew=false;
	    		getFormData();
	    		saveFormDataToDb();
	    		String author=viewHolder.data.get("author");
	    		if (author!=null && author.length() > 1 && !author.equalsIgnoreCase(myName))
		    	{
		    		String msg=getFixLine(viewHolder.data);
		    		String fixLine="35=reply|";
		    		if (isNew) fixLine="35=New|";
		    		fixLine += ("186="+mCitizenId+"|"+msg+"|220='"+authors.get(viewHolder.data.get("author")));
		    		sendDataToServer(fixLine, null, null);
	    		} 
	    		setMenuActionItem("Open");
	    		break;
	    	case R.id.action_close:
	    		mMenuItemId=null;
	    		((WorkMemoActivity)getActivity()).done();
	    		break;
	    	case R.id.action_home:
	    		mMenuItemId=null;
	    		((WorkMemoActivity)getActivity()).done();
	    		break;
	    	default:
	    		noAction=true;
	    		break;
	    	}
	    	if (!noAction)
	    	viewHolder.data.put("CURRENT_ACTION", ""+item.getItemId());
	    	else
	    		viewHolder.data.put("CURRENT_ACTION", ""+R.id.action_retry);
	    	return true;
	    }
	    

	    public void setMenuItems(Menu menu)
	    {
	    	if (viewHolder != null && viewHolder.data != null && viewHolder.data.size() > 0)
	    	{
	    		setMenuActionItem(viewHolder.data.get("CURRENT_ACTION"));
	    	}
	    	else setMenuActionItem(""+R.id.action_new);
	    		return;
	    	
	    }
		
		@Override
		protected void setMenuActionItem(String status)
		{
			
			boolean isMine=true;
			String authorId=viewHolder.data.get("author");
			if (authorId!=null){
				String id=authors.get(authorId);
				if (id != null && !id.equalsIgnoreCase(mCitizenId))
					isMine=false;
			}
			int actionId=0;
			try {
				actionId=Integer.parseInt(status);
			} catch (NumberFormatException e){}
			int[] menu=new int[10];
			int i=0;
	    	switch (actionId)
	    	{
	    	case R.id.action_modify:    		
		    case R.id.action_new:    		
		    case R.id.action_reply:
	    		menu[i++]=R.id.action_retry; menu[i++]=R.id.action_send;
	    		break;
		    case R.id.action_send:
		    case R.id.action_retry:		    	
	    	case R.id.action_cancel:
	    		if (!isMine)
	    		menu[i++]=R.id.action_reply;
	    		else {
	    			menu[i++]=R.id.action_modify;
	    			menu[i++]=R.id.action_cancel;
		    	}
	    		menu[i++]=R.id.action_new;
	    		
	    		break;
	    	default:
	    		break;
	    	}
	    	menu[i++]=R.id.action_home;
	    	menu[i++]=0;
			activateMenu(menu);
		}
		
		void activateMenu(int[] aMenu)
		{
			Menu menu=
					((WorkMemoActivity)getActivity()).getMenuBar();
			if (menu==null) return;
			mMenuItemId=aMenu;
			for (int i=0; i<menu.size(); i++)
	    	{
	    		menu.getItem(i).setVisible(false);
	    	}
	    	for (int i=0; i<mMenuItemId.length; i++)
	    	{	 
	    		if (mMenuItemId[i]==0) break;
	    		menu.findItem(mMenuItemId[i]).setVisible(true);
	    		menu.findItem(mMenuItemId[i]).setOnMenuItemClickListener(this);
	    	}
		}
		
		public int[] getMenuItems()
				{
				return mMenuItemId;
				}


		void setTextAttr(TextView v, int size, int xCr, int bCr)
		{
			v.setTextSize(size);
			v.setTextColor(xCr);
			v.setBackgroundColor(bCr);
			v.setPadding(4, 0, 2, 0);
		}
		
		View mRootView=null;
		public static DecimalFormat dF=new DecimalFormat("00");
		protected View createNewMemo()
		{
			if (viewHolder==null){
			LayoutInflater inflater=getActivity().getLayoutInflater();			
			View tmp=inflater.inflate(R.layout.memo_form_list, (ViewGroup) mRootView, false);
			View formView=tmp.findViewById(R.id.memo_form);
			getViewHolder(formView);
			}
			//HashMap<String, String> data=viewHolder.data;
			GregorianCalendar calendar=new GregorianCalendar();			
			viewHolder.memo_title.setText(" ");			
			viewHolder.memo_date.setText(dF.format(calendar.get(Calendar.YEAR))+"-"+
								dF.format(calendar.get(Calendar.MONTH))+"-"+
								dF.format(calendar.get(Calendar.DATE)));				
			//viewHolder.memo_time.setText(data.get("memo_time"));				
			viewHolder.memo_location.setText(" ");	
		    viewHolder.memo_author.setText(" ");
			viewHolder.memo_description.setText(" ");
			viewHolder.data.put("status",  "pending");
			viewHolder.data.put("author_citizen_id",  mCitizenId);
			viewHolder.data.put("memo_author",  myLastName+" "+myFirstName);
			
			mMenuItemId=new int[]{R.id.action_retry, R.id.action_send, R.id.action_home};
			activateMenu(mMenuItemId);
			
			return mFormView;
		}	

		protected View modifyMemo()
		{
			return mFormView;
		}	
		
		static class ViewHolder{
			View parent;
			TextView  memo_title ;			
			TextView memo_date;				
			TextView memo_time;				
			TextView memo_location;	
		    TextView memo_author;
			TextView memo_description;	
			HashMap<String, String> data;
		}
		static ViewHolder viewHolder=new ViewHolder();
		void getViewHolder(View formView)
		{
			viewHolder.memo_title=(TextView)(formView.findViewById(R.id.memo_title));			
			viewHolder.memo_date=(TextView)(formView.findViewById(R.id.memo_date));				
			//viewHolder.memo_location=(TextView)(formView.findViewById(R.id.memo_location));	
		    viewHolder.memo_author=(TextView)(formView.findViewById(R.id.memo_author));
			viewHolder.memo_description=(TextView)(formView.findViewById(R.id.memo_description));
			if (viewHolder.data==null)
				viewHolder.data=new HashMap<String, String>();
			viewHolder.parent=formView;
		}
		void getFormData()
		{
			if (viewHolder == null ) return;
			if (viewHolder.data==null)
				viewHolder.data=new HashMap<String, String>();
			viewHolder.data.put("memo_title", viewHolder.memo_title.getText().toString());			
			viewHolder.data.put("memo_date", viewHolder.memo_date.getText().toString());					
			//viewHolder.data.put("memo_time", viewHolder.memo_time.getText().toString());					
			//viewHolder.data.put("memo_location", viewHolder.memo_location.getText().toString());	
			viewHolder.data.put("memo_author", viewHolder.memo_author.getText().toString());	
			viewHolder.data.put("memo_description", viewHolder.memo_description.getText().toString());
			return ;
		}
		
		void setFormData()
		{
			if (viewHolder == null || viewHolder.data==null) return;
			HashMap<String, String> data=viewHolder.data;
			viewHolder.memo_title.setText(data.get("memo_title"));			
			viewHolder.memo_date.setText(data.get("memo_date"));				
			//viewHolder.memo_time.setText(data.get("memo_time"));				
			//viewHolder.memo_location.setText(data.get("memo_location"));	
		    viewHolder.memo_author.setText(data.get("memo_author"));
			viewHolder.memo_description.setText(data.get("memo_description"));
			setMenuActionItem(""+R.id.action_retry);
		}
		boolean IamBoss=false;
		@Override
		protected View setTopFormContent(ViewGroup v, HashMap<String, String> data)
		{
			//sendTestLine();
			
			boolean openNew=false;
			mMenuItemId=new int[]{R.id.action_modify, R.id.action_new, R.id.action_home};
			if (myLastBundle != null)
			{
				selectedValues=getFromSavedInstance(myLastBundle);
			}
			else if (data != null)
				selectedValues=data;
			
			if (selectedValues==null)
			{
				if (firstNew != null)
				selectedValues=fromContentValuesToMap(firstNew);
				else
				{
					openNew=true;
				selectedValues=defaultRow();
				selectedValues.put("status",  "CREATE");
				}
			}
					
			LayoutInflater inflater=getActivity().getLayoutInflater();
			if (openNew)
				mFormView=createNewMemo();
			else
			mFormView=openForm(inflater, getFormFrame(), null);
			getViewHolder(mFormView);
			//((ListView)v).addHeaderView(mFormView);
			//activateMenuItems();
			//((MemoActivity)mActivity).redrawForm();
			//mFormView.postInvalidate();
			return mFormView;
		}
		
		void deleteRow(String memo_date, String memo_title)
		{
			if (mDb==null || !mDb.isOpen())
			{
				Log.d("MEMO","no db to work on delete");
				return;
			}
			String sql="delete from workmemo ";
			sql += (" where memo_date='"+memo_date+"' ");
			sql += (" and memo_title='"+memo_title+"' ;");
			final String mySQL=sql;
			//final SQLiteDatabase aDb=mDb;
			new Thread(new Runnable(){
				public void run(){
					mDb.execSQL(mySQL);					
				}
			}).start();
			return;
		}
		void modifyRow(ContentValues aRow)
		{
			if (mDb==null || !mDb.isOpen())
			{
				Log.d("MEMO","no db to work on modify");
				return;
			}
			String memo_date=aRow.getAsString("memo_date");
			String memo_title=aRow.getAsString("memo_title");
			deleteRow(memo_date,  memo_title);
			final ContentValues aC=aRow;
			new Thread(new Runnable(){
				public void run(){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mDb.insert("workmemo",  null, aC);					
				}
			}).start();
		}
		
		@Override
		protected void setDeletedStatus(ContentValues aRow)
		{

		}
		void saveNotifyDataToTable(HashSet<String> data)
		{
			//MemoRecord aAg=new MemoRecord();
			//aAg.initMe();
			saveFIXDataSet(data);
			/*Iterator<String> itr=data.iterator();
			while (itr.hasNext())
			{
				String fixLine=itr.next();
				int ix9=fixLine.indexOf("|120=");
				if (ix9 < 0) ix9=fixLine.length()-1;
				
				//aAg.saveFIXData(fixLine.substring(0, ix9+1), this);
			}*/
			
			return;
		}
		

		void getPendingNotificationMsg()
		{
	    	String fileName="notification"+"workmemo";
	    	Set<String> pendingData=null;
	    	Set<String> nullSet=null;
	    	synchronized(GcmIntentService.fileLock){
	    	SharedPreferences sharedPref = getActivity().getSharedPreferences(fileName, Context.MODE_PRIVATE);
	    	pendingData=sharedPref.getStringSet("notification", nullSet);
	    		if (pendingData != null)
	    		{
	    			SharedPreferences.Editor rm=sharedPref.edit();
	    			rm.clear();
	    			rm.commit();    			
	    		}
	    	}
	    	if (pendingData==null) return;
	    	saveNotifyDataToTable((HashSet<String>)pendingData);
	    	return;
		}
		
		@Override
		protected void checkIfNotification(String fixLine)
		{
			if ( fixLine.indexOf("GCM>>")==0){
			getPendingNotificationMsg();
			//fixLine=fixLine.substring(5);
			}
		}
		
		void setViewData(CurrentMemo this1)
		{
			if (viewHolder == null) return;
			if (viewHolder.data==null) viewHolder.data=new HashMap<String, String>();
			HashMap<String, String> data=viewHolder.data;
			this1.fillMemoData(viewHolder.data);
			setFormData();
			viewHolder.parent.invalidate();
		}
		@Override 
		public void onListItemClick(ListView lv, View tv, int where, long rowId)
		{
			CurrentMemo cm=dataSink.get((int) rowId);
			setViewData(cm);
			
		}
		
		View openMyForm(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState)
		{
			View aForm=null;
			aForm = inflater.inflate(R.layout.memo_form_list, container, false);
			aForm.setBackgroundColor(0xFFF380);
			return aForm;
		}
		
		public static ContentValues getContentValues(Cursor aCursor)
		{
			String[] heads=aCursor.getColumnNames();
			ContentValues aC= new ContentValues();
				for (int k=0; k< heads.length; k++)
				{
					switch (aCursor.getType(k))
					{
					case Cursor.FIELD_TYPE_NULL:
						aC.put(heads[k], "0"); break;
					case Cursor.FIELD_TYPE_INTEGER:
						aC.put(heads[k], ""+aCursor.getInt(k) ); break;
					case Cursor.FIELD_TYPE_FLOAT:
						aC.put(heads[k], ""+aCursor.getFloat(k) ); break;
					case Cursor.FIELD_TYPE_STRING:
						aC.put(heads[k], aCursor.getString(k) ); break;
					case Cursor.FIELD_TYPE_BLOB:
						aC.put(heads[k], new String(aCursor.getBlob(k)) ); break;
					default:
						aC.put(heads[k],  "0");
							break;
					}
				}

			return	aC;
		}
		//database -> cursor -> cycle through 1 record by record (via thread)
		void updateListFromRecordSet(Cursor csr)
		{
			ContentValues aC= getContentValues(csr);
			addNewMemo(new CurrentMemo(aC.getAsString("memo_date"), aC.getAsString("memo_subject"),
					aC.getAsString("memo_athor"), aC.getAsString("author_citizen_id"),
					aC.getAsString("memo_description"), aC.getAsString("recv_time")));
			//csr.moveToNext();
		}
		void addNewMemo(CurrentMemo m)
		{
			dataSink.add(m);
			myAdapter.notifyDataSetChanged();
			//getListView().setSelection(dataSink.size()-1);
		}
		
		void refreshData()
		{
			String sql=" select * from workmemo order by memo_date+recv_time desc;";
		   	final String sSQL=sql;
	    		new Thread(new Runnable(){
	    			public void run()
	    			{
	    				Cursor cr=DbProcessor.getRecordsFromSql(mActivity, sSQL);
	    				cr.moveToFirst();
	    				while (!cr.isAfterLast())
	    				{
	    					updateListFromRecordSet(cr);
	    					cr.moveToNext();
	    				}
	    				cr.close();
	    			}
	    		}).start();	    		
	    	
		}
		
		void setListData()
		{
			refreshData();
		}
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
	    	
	    	mRootView=openMyForm(inflater, container, savedInstanceState);
			
	    	View formView=mRootView.findViewById(R.id.memo_form);
			getViewHolder(formView);
			
			ListView lv=(ListView) mRootView.findViewById(android.R.id.list);
			
	    	if (myAdapter==null) myAdapter=new MemoListAdapter(getActivity(), dataSink);
	    	lv.setAdapter(myAdapter);
	    	setListData();
	    	
	    	return mRootView;
	    }
		
		@Override
		public void readyToRead(Vector<byte[]> outQ) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onServerQuit(Vector<byte[]> outQ) {
			// TODO Auto-generated method stub
			
		}
		

}

