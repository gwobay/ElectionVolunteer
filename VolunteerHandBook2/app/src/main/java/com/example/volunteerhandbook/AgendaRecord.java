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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.kou.utilities.AsyncSocketTask;
import com.kou.utilities.DaemonCallSocket;
import com.kou.utilities.FixDataBundle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class AgendaRecord extends ListRecordBase 
			implements 
			//View.OnClickListener, 
			DaemonCallSocket.Listener 
{
	static LinearLayout.LayoutParams llParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
			LinearLayout.LayoutParams.WRAP_CONTENT);
	static final String STATUS="status", PARTICIPANTS="participants";
	static Thread sockThread=null;
	static ArrayList<byte[]> msgQ=null;//new Vector<String>();
	ArrayList<byte[]> pendingQ;
	static ReentrantReadWriteLock rLock=new ReentrantReadWriteLock();
	public AgendaRecord() {
		// TODO Auto-generated constructor stub
			// TODO Auto-generated constructor stub
		super();
    	/* set up the following 
    	protected String[] mShowColumns; //filled by child page class
    	protected int[] rListColumnTextViews; //filled by child page class
    	protected int layout_list_row_by_page;
    	*/
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
	
	public void checkServer(boolean T_F)
	{
		checkServer=T_F;
	}
	
    public void checkServerForUpdate()
    {
    	if (mActivity==null) return;
    	SharedPreferences sharedPref = mActivity.getSharedPreferences("agenda_last_check_time", Context.MODE_PRIVATE);
    	long lTime=sharedPref.getLong("agenda_last_check_time", 0);
    	long timeNow=new Date().getTime();
    	if (lTime ==0 || lTime+8*3600*1000 <  timeNow)
    	{
    		DbProcessor dp=new DbProcessor(mActivity, "kp_volunteer_db");
    		SQLiteDatabase aDb=dp.getDb();
    		if (aDb != null) {
		    checkForServerData(1, aDb);			   
		    SharedPreferences.Editor writer=sharedPref.edit();
		    writer.putLong("agenda_last_check_time", timeNow);
		    writer.commit();
    		}
    	}
	    return;
    }
	@Override
	protected void init()
	{
		setConstants();
		//dataSink=new ArrayList<HashMap<String, String> >();
		mShowColumns=new String[]{"event_date", "event_title", "event_location", STATUS, PARTICIPANTS};
		rListColumnTextViews=new int[]{R.id.event_date, R.id.event_title, R.id.event_location, R.id.event_status, R.id.event_people};
		mListPage=R.layout.agenda_row;
		mRecordFormLayout = R.layout.agenda_form;
		if (mActivity==null) mActivity=getActivity();
		if (mActivity == null) return;
		
		//int i=checkIfHasTable(dbTableName);
	    if (!DbProcessor.ifTableExists(mActivity, "agenda")){
	    	String sql=createTableSql();
	    	DbProcessor.createTable(mActivity, sql);//doCreateTable();
	    	checkServer=true;
	    }
		
	    checkServerForUpdate();
	    
	    //doTest();
	    mMenuItemId=new int[]{R.id.action_home, R.id.action_join};
	    //refreshData();
		   
	    if (mActivity==null) mActivity=(AgendaActivity)getActivity();
		iXX=mActivity.getResources().getDrawable(R.drawable.ic_action_x);
		iGo=mActivity.getResources().getDrawable(R.drawable.i_go);
		iNew=mActivity.getResources().getDrawable(R.drawable.new_one);
		iLove=mActivity.getResources().getDrawable(R.drawable.i_love);
	}

	void doTest()
	{
		HashSet<String> set=new HashSet<String>();
		set.add("170=agenda|151=2014-07-21|152=17:20|153=新三民主義：庶民、鄉民、公民|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「台北市長參選人柯文哲日前提出「新三民主義：庶民、鄉民、公民」，依此概念招募競選團隊成員，包括發言人、隨行秘書等職務都開放徵選，24日將在華山文創園區辦理「海選」活動|");

		set.add("170=agenda|151=2014-07-23|152=17:20|153=在野整合辯論|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「今天是我們要去拜託別人，所以對方開的條件 我們都會接受，每個人按照他的想法，努力的去做 至於有沒有辦法，|");


		set.add("170=agenda|151=2014-07-24|152=17:20|153=青年海選計畫|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「徵求發言人與隨行祕書。因選戰繁忙，徵選時還要測驗體能，薪資則優於國科會助理標準，大學畢業約32K、碩士36K起跳|");
	
		set.add("170=agenda|151=2014-07-24|152=17:20|153=看透新聞|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=鄭弘儀|158=「但是我們真的不要小看國民黨，畢竟是百年老店，它在基層的組織其實是滿完整的，只要資源下去，它的動員是非常快的」。須以一個謹慎態度來面對二○一四的選舉|");
		set.add("170=agenda|151=2014-07-21|152=17:20|153=新三民主義：庶民、鄉民、公民2|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「台北市長參選人柯文哲日前提出「新三民主義：庶民、鄉民、公民」，依此概念招募競選團隊成員，包括發言人、隨行秘書等職務都開放徵選，24日將在華山文創園區辦理「海選」活動|");

		set.add("170=agenda|151=2014-07-23|152=17:20|153=在野整合辯論2|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「今天是我們要去拜託別人，所以對方開的條件 我們都會接受，每個人按照他的想法，努力的去做 至於有沒有辦法，|");


		set.add("170=agenda|151=2014-07-24|152=17:20|153=青年海選計畫2|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=柯文哲|158=「徵求發言人與隨行祕書。因選戰繁忙，徵選時還要測驗體能，薪資則優於國科會助理標準，大學畢業約32K、碩士36K起跳|");
	
		set.add("170=agenda|151=2014-07-24|152=17:20|153=看透新聞2|154=中正區忠孝西路1段72號62樓|155=台北市|157=0986056845|156=鄭弘儀|158=「但是我們真的不要小看國民黨，畢竟是百年老店，它在基層的組織其實是滿完整的，只要資源下去，它的動員是非常快的」。須以一個謹慎態度來面對二○一四的選舉|");
		Iterator<String> itr=set.iterator()	;
		while (itr.hasNext())
		{
			HashMap<String, String> aRow=parseFixLine(itr.next());
			ContentValues aC=getValidData(getValidColumns(aRow));
			DbProcessor.insertTable(mActivity, "agenda",  null, aC);
		}
	}
	
	static final String DATE="event_date", TIME="event_time", TITLE="event_title",
			LOCATION="event_location", CITY="event_city", HOST="event_host", 
			DESCRIPTION="event_description";

	@Override
	protected void setConstants()
	{	
		mToday=getToday();
    	
    	page_tags=new Object[][]{
			{170,"agenda"},{35,"msgType"},{ 186,"citizen_id"},
			{151, DATE}, 			 
			{152, TIME}, 
			{153, TITLE}, 
			{154, LOCATION}, 
			{155, CITY}, 
			{156, HOST}, 
			{157, "contact_number"}, 
			{158, DESCRIPTION},
			{159, STATUS}, 
			{160, PARTICIPANTS}
		};

    	dbTableName="agenda";
    	
    	tableColumns=page_tags;
    	tagNames=new HashMap<String, String>();
    	nameTags=new HashMap<String, String>();
    	for (int i=0; i<tableColumns.length; i++)
    	{
    		tagNames.put(""+(int)tableColumns[i][0], (String)tableColumns[i][1]);
    		nameTags.put((String)tableColumns[i][1], ""+(int)tableColumns[i][0]);
    	}
    	
	    pageFields=new Object[][]{
			{R.id.event_date, "event_date", " "}, 			 
			{R.id.event_time, "event_time", " "}, 
			{R.id.event_title, "event_title"," "}, 
			{R.id.event_location, "event_location"," "}, 
			{R.id.event_city, "event_city", "台北市"}, 
			{R.id.event_host, "event_host", " "}, 
			{R.id.contact_number, "contact_number"," "}, 
			{R.id.event_description, "event_description"," "},
			{R.id.event_people, PARTICIPANTS, "0"}
		};	    
	}
	
	@Override
	protected String[] getValidFields()
	{
		return new String[]{
			"event_date", "event_time", "event_title", 
				"event_location", "event_city", "event_host", 
				"contact_number", "event_description",
				STATUS, PARTICIPANTS};
	}
	public static String getTableName()
	{
		return "agenda";
	}
	
	@Override
	public HashMap<String, String> getValidColumns(HashMap<String, String> aRow)
	{
		HashMap<String, String> mp=new HashMap<String, String>();
		for (String s:getValidFields())
			mp.put(s, aRow.get(s));
		if (mp.get(STATUS)==null) mp.put(STATUS, "NEW");
		return mp;
	}
	HashMap<String, String> defaultRow()
	{
		HashMap<String, String> mp=new HashMap<String, String>();
		for (int i=0; i<pageFields.length; i++)
		{
			mp.put((String)pageFields[i][1], (String)pageFields[i][2]);
		}
		mp.put(STATUS, "null");
		return mp;
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
		Log.i("AGENDA", "GOT SERVER DATA "+resp.size());
		newData.clear();
		newData=null;
	}
	
	String getLastUpdateDate()
	{
		String sql="select max(event_date) from agenda;";
		String today="2000-01-01";
		Cursor c1=DbProcessor.getRecordsFromSql(mActivity, sql);
		if (c1!= null && c1.getCount() > 0)
		{
			c1.moveToFirst();
			today=c1.getString(0);
			DbProcessor.closeDbByCursor(c1);
		}
		return today;
	}
	
	public static AsyncSocketTask sendServerData(String command, Vector<byte[]> inBox)
	{
		final AsyncSocketTask aTask=new AsyncSocketTask();
    	
		boolean wait4Response=(inBox != null);
		aTask.needResponse(wait4Response);
    	
    	aTask.setDataBox(inBox);
		
    	byte[] sendByts=AsyncSocketTask.convertStringToBytes(command);
    	aTask.execute("220.134.85.189".getBytes(), "9696".getBytes(), sendByts, "120".getBytes());
    	
    	return aTask;
    	//final boolean processed=false;
	}
	public void sendDataToServer(String command, Vector<byte[]> inBox)
	//make sure this is called from a thread but not the main ui thread
	{
		boolean wait4Response=(inBox != null);
		final AsyncSocketTask aTask=sendServerData(command, inBox);
    	//final boolean processed=false;
    	if (!wait4Response) return;
    	//get a thread wait for the result
    	final Vector<byte[]> boxToCheck=inBox;
    	new Thread(new Runnable(){
    		public void run(){
    	try {
			Vector<byte[]> response=aTask.get(2*60000, TimeUnit.MILLISECONDS);
			if (response != null) 
			{
				if(!readyAndSave(response))
				processServerResponse(response) ;
				response.clear();
				//processed=true;
			}
		} catch (InterruptedException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}catch (ExecutionException  e) {
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
		//send fixLine with 35=RESEND, event_date >= today;
    	Vector<byte[]> resp=new Vector<byte[]>();
    	if (today==null){
	    	Calendar aC=Calendar.getInstance();
	    	today=dF.format(aC.get(Calendar.YEAR));
	    	today += ("-"+dF.format(aC.get(Calendar.MONTH)+1)+"-");
	    	today += dF.format(aC.get(Calendar.DATE));
    	}
    	String command="35=RESEND|170=agenda|151="+today+"|";
    	sendDataToServer(command, resp);
/*    	AsyncSocketTask aTask=new AsyncSocketTask();
    	//aTask.setVectorStore(responseStore);
    	aTask.needResponse(true);
    	aTask.setDataBox(resp);
    	aTask.execute("220.134.85.189", "9696", command, "120");
    	try {
			Vector<byte[]> response=aTask.get(2*60000, TimeUnit.MILLISECONDS);
			if (response != null) 
			{
				processServerResponse(response) ;
				response.clear();
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
    	aTask.cancel(true);
    	//if (checkServer) finish();
    	if (resp.size() > 0) processServerResponse(resp) ;*/
	}
	
	@SuppressWarnings("unchecked")
	void processMsgQ()
	{
		synchronized (rLock){
			if (msgQ.size() < 1) return;
			pendingQ=(ArrayList<byte[]>)msgQ.clone();

			final String host=getActivity().getResources().getString(R.string.host_name);
			final String sPort=getActivity().getResources().getString(R.string.host_port);
			final DaemonCallSocket.Listener l=this;
			final ExecutorService thd=Executors.newSingleThreadExecutor();
			thd.execute(new Runnable(){
					public void run(){
						try {
						DaemonCallSocket aCkt=DaemonCallSocket.getInstance(host, Integer.parseInt(sPort), pendingQ,"agenda",  l);
						new Thread(aCkt).start();
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
					}
				});
			msgQ.clear();
		}
	}
	
	public boolean readyAndSave(Vector<byte[]> outQ)
	{
		for (int i=0; i<outQ.size(); i++)
		{
			//have to convert from bytes back to string (w/ header removed)
			FixDataBundle data=new FixDataBundle(outQ.get(i));
			String fixLine=data.getFixLine();
			Log.d("AGENDA", "got server response "+fixLine);
			ContentValues aC=getValidData(parseFixLine(fixLine));
			
			if (aC.getAsString(STATUS)==null)
				aC.put(STATUS, "NEW");
			DbProcessor.insertTable(mActivity, "agenda", null, aC);
			
			if (data.getStream() != null)
			{
				Log.w("AGENDA", "server send extra stream data "+data.getStream().length);
			}
		}
		outQ.clear();
		return true;
	}
	
	public void readyToRead(Vector<byte[]> outQ)
	{
		for (int i=0; i<outQ.size(); i++)
		{
			String fixLine=new String(outQ.get(i));
			Log.d("AGENDA", "got server response "+fixLine);
		}
		outQ.clear();
	}
	
	public void onServerQuit(Vector<byte[]> outQ)
	{
		for (int i=0; i<outQ.size(); i++)
		{
			String fixLine=new String(outQ.get(i));
			Log.d("AGENDA", "unconfirmed data:"+fixLine);
		}
		outQ.clear();
	}
/*	public void onClick(View v1) //for location map
	{
		//get address
		View v=mFormView;//(View)v1.getParent();
		TextView txV = (TextView) v.findViewById(R.id.event_location);
		if (txV==null) return;
		String street=txV.getText().toString();
		txV = (TextView) v.findViewById(R.id.event_city);
		if (txV==null) return;
		String city=txV.getText().toString();
    	Intent pIntent=new Intent(getActivity(), MapActivity.class);
    	pIntent.putExtra(MapActivity.LOCATION, street+" "+city);
    	txV=(TextView) v.findViewById(R.id.event_title);
    	if (txV!=null)
    	{
    		String info=(txV.getText().toString()+"(");
    		txV=(TextView) v.findViewById(R.id.event_date);
    		if (txV!=null) info += txV.getText().toString();
    		txV=(TextView) v.findViewById(R.id.event_time);
    		if (txV!=null) info += (", "+txV.getText().toString()+")");
    		//info += (street+", "+city);
    		String fixKey=getResources().getString(R.string.fix_line_key);
    		pIntent.putExtra(fixKey, info);
    	}
    	startActivity(pIntent);   
		//show info window with mark
	}
	
*/	public HashMap<String, String> getDataForMap(View v1) //form on page
	{
		//get address
		View v=mFormView;//(View)v1.getParent();
		TextView txV = (TextView) v.findViewById(R.id.event_location);
		if (txV==null) return null;
		String street=txV.getText().toString();
		txV = (TextView) v.findViewById(R.id.event_city);
		if (txV==null) return null;
		HashMap<String, String> data=new HashMap<String, String>();
		String city=txV.getText().toString();
    	data.put(MapActivity.LOCATION, street+" "+city);
    	txV=(TextView) v.findViewById(R.id.event_title);
    	if (txV!=null)
    	{
    		String info=(txV.getText().toString()+"(");
    		txV=(TextView) v.findViewById(R.id.event_date);
    		if (txV!=null) info += txV.getText().toString();
    		txV=(TextView) v.findViewById(R.id.event_time);
    		if (txV!=null) info += (", "+txV.getText().toString()+")");
    		//info += (street+", "+city);
    		String fixKey=getResources().getString(R.string.fix_line_key);
    		data.put(fixKey, info);
    	}
    	return data;   
		//show info window with mark
	}
	
	
	static boolean try_already=true;
/*	void insertTestData()
	{
		if (try_already) return;
		try_already=true;
		//reviseTable();
		String sql="update agenda set participants='0' where status != 'JOIN';";
		doDbSql(sql);
		if (try_already) return;
		String fixLine="170=agenda|151=2014-05-26|152=14:20|153=基層動員關心公共的事務|154=中正區忠孝西路2段11號62樓|155=台北市|";
    	fixLine +=  "156=古民雄|157=0989898989|";
    	fixLine +=  "158=「但是我們真的不要小看國民黨，它在基層的組織其實是滿完整的，只要資源下去，它的動員是非常快的」。須以一個謹慎態度來面對二○一四的選舉。|";
    	
		HashMap<String, String> mp=parseFixLine(fixLine);
		saveUserData("agenda", mp);
		mp.put("event_title", "提名大會動員1");
		mp.put("event_date", "2014-06-13");
		saveUserData("agenda", mp);
		mp.put("event_title", "大會提名動員1");
		mp.put("event_date", "2014-05-27");
		saveUserData("agenda", mp);
		mp.put("event_title", "登記大會師動員1");
		mp.put("event_date", "2014-09-25");
		saveUserData("agenda", mp);	
		
		mp.put("event_title", "黨辨論動員2");
		mp.put("event_date", "2014-06-10");
		saveUserData("agenda", mp);
		mp.put("event_title", "辨論大會黨提名動員2");
		mp.put("event_date", "2014-05-29");
		saveUserData("agenda", mp);
		mp.put("event_title", "登記黨大會師動員2");
		mp.put("event_date", "2014-09-16");
		saveUserData("agenda", mp);	
		sql="update agenda set status='NEW' where status != 'JOIN';";
		doDbSql(sql);
		
		//getAllRecords();
	}
	
	void add30MoreRecords()
	{
		HashMap<String, String> mp=defaultRow();
		for (int i=0; i<30; i++)
		{			
			String tt=mp.get("event_title");
			tt += i;
			String dd="2014-08-";
			dd += (i+1);
			mp.put("event_title", tt);
			mp.put("event_date", dd);
			String dsc=mp.get("event_description");
			dsc += i;
			mp.put("event_description", dsc);
			saveUserData("agenda", mp);
		}
		getAllRecords();
	}

	void removeAll()
	{
		String dT="delete from table agenda;";
		doDbSql(dT);
		//doCreateTable();
	}
	
	void reviseTable()
	{
		String dT="drop table agenda;";
		doDbSql(dT);
		doCreateTable();
	}
	
	*/
	
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
			String sql="create table if not exists agenda";
				sql += "( event_date date  not null default '2014-05-01',";
				sql += " event_time char(8) not null,"; 
				sql += " event_title text unique not null,"; 
				sql += " event_location text,";				
				sql += " event_city char(12),";	
				sql += " event_host char(8),";	
				sql += " contact_number char(12),";
				sql += " event_description text,";
				sql += " status char(8) not null default 'new',";
				sql += " participants char(8) default '0' );";//new, cancle, read, join
				return sql;
		}		

	    void sendTestLine()
	    {
	    	String fixLine="170=agenda|151=2014-05-22|152=14:20|153=基層關心公共的事務|154=中正區忠孝西路2段11號62樓|155=台北市|";
	    	fixLine +=  "156=顧真雄|157=0986056745|";
	    	fixLine +=  "158=「但是我們真的不要小看國民黨，它在基層的組織其實是滿完整的，只要資源下去，它的動員是非常快的」。須以一個謹慎態度來面對二○一四的選舉。|";
	    	fixLine += "201=broadCastToAll|";	
	    	msgQ.add(fixLine.getBytes());
		    	processMsgQ();
	    }
	    

	    @Override
		protected String getFixLine(HashMap<String, String> oneRecord)
		{
	    	String fixLine="170=agenda|";
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
	    	aRow.put("table_name",  "agenda");
			return;
		}

	    @Override
	    protected Vector<ContentValues> getAllListRecord(Cursor aCursor)
		{
	    	if (aCursor==null) {
	    		DbProcessor.closeDbByCursor(aCursor);
	    		return null;
	    	}
	    	if (aCursor.getCount()<1)
	    	{
	    		DbProcessor.closeDbByCursor(aCursor);
	    		Toast.makeText(mActivity,  "NO RECORD TO SHOW", Toast.LENGTH_LONG).show();
	    		if (!IamBoss) {
	    			if (aCursor !=null)
	    				DbProcessor.closeDbByCursor(aCursor);
	    			((AgendaActivity)mActivity).done();	    			
	    			return null;
	    		}
	    	}
	    	Log.d("dbDATA", "CALLED BY"+this.getId());
			Vector<ContentValues> allRows=new Vector<ContentValues>();
			firstNew=null;
			int selectedId=-1;
			int iShow=-1;
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
				String dd=aC.getAsString("event_date");
				String tt=aC.getAsString("event_title");
				String status=aC.getAsString(STATUS);
				Log.d("dbDATA", dd+tt+aC.getAsString(STATUS));
				if (selectedValues != null && selectedId<0){
					if (dd.equalsIgnoreCase(selectedValues.get("event_date")) &&
							tt.equalsIgnoreCase(selectedValues.get("event_title")))
						selectedId=aCursor.getPosition();
				}

				if (status == null || status.equalsIgnoreCase("NEW") || status.charAt(0)==' ')
				{
					
					aC.put(STATUS,  "NEW");
					firstNew=aC;
					iShow=aCursor.getPosition();
					//foundFirstNew=true;
				}
				allRows.add(aC);
				aCursor.moveToNext();
			}
			DbProcessor.closeDbByCursor(aCursor);
			if (firstNew==null && allRows.size() >1)
			{	firstNew=allRows.get(0); iShow=0; }
			Log.d("dbDATA", firstNew.getAsString("event_date")+firstNew.getAsString("event_title")+firstNew.getAsString(STATUS));
			if (selectedId >= 0) getArguments().putInt(SELECTED_RECORD, selectedId);
			else getArguments().putInt(SELECTED_RECORD, iShow);
			return allRows;
		}
		
	    @Override
	    protected void getAllRecords()
		{
/*	    	String sql="select * from agenda order by event_date desc";	    	
			mAllRecords=getAllListRecord(selectRecords(sql));*/
		}
	    
	    @Override
		protected ContentValues getUserInput() //put inside the mContentValues
		{
	    	ContentValues mContentValues=new ContentValues();
/*			for (int i=0; i<pageFields.length; i++)
			{
				EditText txV = (EditText) mFormView.findViewById((Integer)(pageFields[i][0]));
				if (txV==null) continue;
				String txt=txV.getText().toString();
				if (txt != null && txt.length() > 0)
				mContentValues.put((String)pageFields[i][1], txt);
			}
			//mContentValues.put(STATUS, "new");
*/	    	
	    	Iterator<String> itr=viewHolder.data.keySet().iterator();
	    	while (itr.hasNext())
	    	{
	    		String key=itr.next();
	    		mContentValues.put(key, viewHolder.data.get(key));
	    	}
			return mContentValues;
		}
		
	    void saveMsgQ(Bundle outState)
	    {
	    	DecimalFormat dF=new DecimalFormat("000000");
	    	if (pendingQ == null) pendingQ=msgQ;
	    	else
	    	if (pendingQ.size() > 0)
	    	{
	    		//for  (int i=0; i<msgQ.size(); i++)
	    			pendingQ.addAll(msgQ);//.get(i));
	    	}
	    	for  (int i=0; i<pendingQ.size(); i++)
	    		{
	    			outState.putByteArray("MSGQ"+dF.format(i), pendingQ.get(i)); 
	    		}
	    }
	    void recoverMsgQ(Bundle savedBdl)
	    {
	    	DecimalFormat dF=new DecimalFormat("000000");
	    	int i=0;
	    	if (msgQ==null) msgQ=new ArrayList<byte[]>();
	    	do
	    	{
	    		byte[] got=savedBdl.getByteArray("MSGQ"+dF.format(i++));
	    		if (got == null) break;
	    		msgQ.add(got);
	    	} while (true);
	    	if (msgQ.size() > 0) processMsgQ();
	    }
	    
	    @Override
		protected void getCurrentFormData(Bundle outState)
		{
/*			for (int i=0; i<pageFields.length; i++)
			{
				TextView txV = (TextView) mFormView.findViewById((Integer)(pageFields[i][0]));
				if (txV==null) continue;
				String txt=txV.getText().toString();
				if (txt != null && txt.length() > 0)
					outState.putString((String)pageFields[i][1], txt);
			}
			//saveMsgQ(outState);
			*/
	    	getFormData();
	    	//if (viewHolder.data==null) return;
	    	Iterator<String> itr=viewHolder.data.keySet().iterator();
	    	while (itr.hasNext())
	    	{
	    		String key=itr.next();
	    		outState.putString(key, viewHolder.data.get(key));
	    	}
	    	
	    	
/*	    	if (dataSink.size() > 0)
	    	{
	    		int iSize=dataSink.size();
	    		HashMap<String, String> aRec=(HashMap<String, String>) dataSink.get(0);
	    		Iterator<String> itr=aRec.keySet().iterator();
	    		while (itr.hasNext())
	    		{
	    			String key=itr.next();
	    			String[] sArray=new String[iSize];
	    			for (int i=0; i<iSize; i++)
	    			{
	    				sArray[i]=((HashMap<String, String>) dataSink.get(i)).get(key);
	    			}
	    			outState.putStringArray(key, sArray);
	    		}
	    	}*/
	    	myLastBundle=new Bundle(outState);
			return ;
		}
		
	    @Override
		protected HashMap<String, String> getFromSavedInstance(Bundle sb)
		{
	    	if (pageFields==null) return null;
			
	    	int iOpen=sb.getInt(SELECTED_RECORD);
	    	if (iOpen>=0)
	    	{
	    		if (dataSink.size()>0)
	    		{
	    			if (iOpen >= dataSink.size()) iOpen=dataSink.size()-1;
	    			mCurrentRowId=iOpen;
	    			setSelectedValue(iOpen);
	    			return viewHolder.data;
	    		}
	    	}
	    	HashMap<String, String> lastValues=new HashMap<String, String>();
	    	for (int i=0; i<pageFields.length; i++)
			{
				String key=(String)pageFields[i][1];
				String txt=sb.getString(key);
				if (txt != null && txt.length() > 0)
					lastValues.put(key, txt);
			}
			//recoverMsgQ(sb);
			return lastValues;
		}
	    
	    void sendToServer()
	    {
	    	String fixLine=getFixLine(fromContentValuesToMap(mContentValues));
	    	fixLine += "201=broadCastTooAll|";	
	    	//((AgendaActivity)getActivity()).sendDataToServer(fixLine);
	    	//msgQ.add(fixLine.getBytes());
	    	//processMsgQ();
	    }
	    
	   
	    public void setParticipantData(String numb)
	    {
	    	viewHolder.data.put(PARTICIPANTS, numb);
	    	viewHolder.data.put(STATUS, "JOIN");
	    	updateDbStatus(viewHolder.data);
	    	updateListRecord(viewHolder.data);
	    	mMenuItemId=new int[]{R.id.action_home, R.id.action_cancel};
    			setMenuActionItem("JOIN");
    			if (mCitizenId==null)
    			mCitizenId=MainActivity.getCitizenId(getActivity());
    			final String fixLine="35=REPORT|186="+mCitizenId+"|"+getFixLine(viewHolder.data);
    			new Thread(new Runnable(){
    				public void run()
    				{
    					sendDataToServer(fixLine, null);
    				}
    			}).start();    		
	    }
	    
	    void setDeleteAndInformServer()
	    {
	    	setDeletedStatus(viewHolder.data);
	    	String command=("35=delete|186="+mCitizenId+"|");
	    	final String fixLine=command+getFixLine(viewHolder.data)+"|201=broadcast|2039=DELETE|";
			new Thread(new Runnable(){
				public void run()
				{
					sendDataToServer(fixLine, null);
				}
			}).start();	    		    	
	    }
	    
	    void confirmCancelPopup(String event_date, String event_title, boolean for_participants)
	    {
	    	String cfm=getResources().getString(R.string.confirm_cancel);
	    	String ag=getResources().getString(R.string.agenda);
	    	String pp=getResources().getString(R.string.number_participants);
	    	final String date=cfm+" "+event_date;
	    	final String title=(" ["+event_title+"] "+pp+" "+ag);
	    	final boolean cancel_participants=for_participants;
	    	DialogFragment pop=new DialogFragment() {
	    	    @Override
	    	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	        // Use the Builder class for convenient dialog construction
	    	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    	        builder.setTitle(date)
	    	        	.setMessage(title)
	    	               .setPositiveButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {
	    	                   public void onClick(DialogInterface dialog, int id) {
	    	                	   if (cancel_participants)
	    	                		   cancelParticipants();
	    	                		   else
	    	                	   setDeleteAndInformServer();
	    	                   }
	    	               })
	    	               .setNegativeButton(R.string.forget, new DialogInterface.OnClickListener() {
	    	                   public void onClick(DialogInterface dialog, int id) {
	    	                       // User cancelled the dialog
	    	                   }
	    	               });
	    	        // Create the AlertDialog object and return it
	    	        return builder.create();
	    	    }
	    	};
	    	pop.show(getFragmentManager(), title);
	    }
	    
	    void cancelParticipants()
	    {
    		getFormData();
    		viewHolder.data.put(STATUS, "OPEN");
    		viewHolder.data.put(PARTICIPANTS, "0");
    		final String fixLine="35=CANCEL|186="+mCitizenId+"|"+getFixLine(viewHolder.data);
    		new Thread(new Runnable(){
    			public void run()
    			{
    				sendDataToServer(fixLine, null);
    			}
    		}).start();
    		updateDbStatus(viewHolder.data);
    		updateListRecord(viewHolder.data);
    		/*getMyListView().setSelection(mCurrentRowId);
    		if (viewHolder.recordViewHolder!= null)
    			viewHolder.recordViewHolder.postInvalidate();
    		invalidateViews();	*/    	    		
	    }
	    
	    @Override
	    public boolean onMenuItemClick (MenuItem item)
	    {
	    	ContentValues dataRow;
	    	String tt, dd, sql;
	    	
	    	switch (item.getItemId())
	    	{
	    	case R.id.action_join:
	    		//if (selectedValues == null) break;
	    		((AgendaActivity)mActivity).getParticipantNumber(viewHolder.data);
	    		//selectedValues.put(STATUS, "JOIN");    				
	    		//setMenuActionItem("JOIN");
	    		//data updated after the dialog for participants
	    		break;
	    	case R.id.action_cancel:
	    		String date=viewHolder.data.get("event_date");
	    		String title=viewHolder.data.get("event_title");
	    		confirmCancelPopup(date, title, true);
	    		//cancelParticipants();
		    			//postFormValueToList(selectedValues);
		    		//}
		    		//((AgendaActivity)mActivity).redrawList();
	    		setMenuActionItem("OPEN");
	    		break;
	    	case R.id.action_delete:
	    		date=viewHolder.data.get("event_date");
	    		title=viewHolder.data.get("event_title");
	    		confirmCancelPopup(date, title, false);
	    		setMenuActionItem("NEW");	    		
	    		break;	
	    	case R.id.action_new:
	    		createNewAgenda();
	    		setMenuActionItem("Create");	    		
	    		break;
	    	case R.id.action_modify:
	    		modifyAgenda();
	    		setMenuActionItem("Create");	    		
	    		break;	    		
	    	case R.id.action_send:
	    		//saved to local db and send to server
	    		//((AgendaActivity)getActivity()).connectToServer();
	    		dataRow=getUserInput();
	    		boolean isModify=false;
	    		String command=null;
	    		if (viewHolder.data.size()>1)
	    			isModify=true;
	    		if (isModify){
	    			command="35=update|186="+mCitizenId+"|";
	    			modifyRow(dataRow);
	    		}
	    		else {
		    		DbProcessor.insertTable(mActivity, dbTableName, null, dataRow);	
		    		command="35=CREATE|186="+mCitizenId+"|";
	    		}
	    		getFormData();
	    		final String fxLine=command+getFixLine(viewHolder.data)+"|201=broadcast|";
    			new Thread(new Runnable(){
    				public void run()
    				{
    					sendDataToServer(fxLine, null);
    				}
    			}).start();

	    		//((AgendaActivity)getActivity()).done();
	    		break;
	    	case R.id.action_close:
	    		mMenuItemId=null;
	    		((AgendaActivity)getActivity()).done();
	    		break;
	    	case R.id.action_home:
	    		mMenuItemId=null;
	    		((AgendaActivity)getActivity()).done();
	    		break;
	    	default:
	    		break;
	    	}
	    	((AgendaActivity)getActivity()).refreshMe(getListView());
	    	return true;
	    }
	    
		static void setDefaultValue(View v)
		{
			Object[][] oTxt=pageFields;
			for (int i=0; i<oTxt.length; i++)
			{
				EditText txV = (EditText) (v.findViewById((Integer)(oTxt[i][0])));
				if (txV==null) continue;
				String val=(String)oTxt[i][2];
				if (val.length() > 0)
				txV.setText(val);
			}
	   	}
/*
	   protected View setRateDefaultValues(View v)
		{
	    	mRating="not decided yet";
	    	setDefaultValue(v);
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

		}
*/
		@Override
		protected void setFormValues(View v, HashMap<String, String> savedRecord)
		{
			Object[][] oTxt=pageFields;for (int i=0; i<oTxt.length; i++){
				TextView txV = (TextView)( v.findViewById((Integer)(oTxt[i][0])));
				if (txV==null) continue;
				String value=savedRecord.get((String)oTxt[i][1]);
				if (value != null)
				txV.setText(value);
			}
			changeNewRecordToOpen();
			return ;
		}
		
		static int mCurrentRowId=0;
		View clickedView;
		@Override 
		public void onListItemClick(ListView lv, View rv, int where, long rowId)
		{
			mCurrentRowId=(int) rowId;
			clickedView=rv;
			HashMap<String, String> aRec=(HashMap<String, String>)(dataSink.get((int)rowId));
			if (viewHolder.data == null)
				viewHolder.data=new HashMap<String, String>();
			Iterator<String> itr=aRec.keySet().iterator();
			while (itr.hasNext())
			{
				String key=itr.next();
				viewHolder.data.put(key,  aRec.get(key));
			}
			if (viewHolder.parent != null)
			{
				viewHolder.recordViewHolder=rv;
				setFormData();
				((AgendaActivity)getActivity()).refreshMe(viewHolder.parent);
			}
			setMenuActionItem(viewHolder.data.get(STATUS));
		}
		
/*		View openMyForm(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState)
		{
			View aForm=null;
			//aForm = inflater.inflate(R.layout.agenda_form, container, false);
			int rotation=getArguments().getInt("ROTATION");
			if (rotation==Surface.ROTATION_90)
				aForm = inflater.inflate(R.layout.agenda_form_list_landscape, container, false);
			  else
			aForm = inflater.inflate(R.layout.agenda_form_list, container, false);
			aForm.setBackgroundColor(0xFFF380);
			return aForm;
		}*/
		
		void showTransition(View v, boolean bGo)
		{
			TransitionDrawable drawable = (TransitionDrawable) getResources().getDrawable(R.drawable.to_join);
			if (bGo) drawable = (TransitionDrawable) getResources().getDrawable(R.drawable.to_cancel);
			//Button button = (Button)v;
			//button.setText("9");
			//button.setTextSize(60);
			v.setBackground(drawable);
			
			drawable.startTransition(1000);
			/*else
				drawable.reverseTransition(1000);*/
		}
/*		@SuppressWarnings("unchecked")
		@Override
		protected void postFormValueToList(HashMap<String, String> aRow)
		{
			if (mAllRecords==null || mAllRecords.size()<1) return;
			int pos=getArguments().getInt(SELECTED_RECORD, -1);
			if (pos < 0) return;
			ContentValues c= mAllRecords.get(pos);
	    	if (c==null) return;
	    	if (c.getAsString(STATUS).equalsIgnoreCase("deleted")) return;
	    	
	    	if (c.getAsString(STATUS).equalsIgnoreCase(aRow.get(STATUS))) return;
	    	c.put(STATUS, aRow.get(STATUS));
	    	c.put(PARTICIPANTS, aRow.get(PARTICIPANTS));
	    	
			boolean bGo=aRow.get(STATUS).equalsIgnoreCase("JOIN");
			ListView lv=(ListView)mViewOfPage;
			TreeMap<String, Object> rRow=null;
			if (rowAdapter != null)
			{
				rRow=(TreeMap<String, Object>)rowAdapter.getItem(pos);	    	
			}
			else
			{
		    	if (mViewOfPage==null) return;
		    	lv=(ListView)mViewOfPage;
		    	if (lv==null) return;
					rRow=(TreeMap<String, Object>)lv.getItemAtPosition(pos);
			}
	    	if (rRow==null) return;
	    	if (!aRow.get("event_date").equalsIgnoreCase((String)(rRow.get("event_date"))) ||
	    			!aRow.get("event_date").equalsIgnoreCase((String)(rRow.get("event_date"))))
	    		return;
	    	if (bGo)
	    		{ rRow.put(STATUS, iGo);
	    		rRow.put(PARTICIPANTS, aRow.get(PARTICIPANTS));
	    		}
	    	else {
	    		rRow.put(STATUS, iLove);
	    		rRow.put(PARTICIPANTS, aRow.get(" "));
	    	}	
	    	View v=lv.getChildAt(pos-lv.getFirstVisiblePosition());
	    	if (v!=null){
	    		ImageView change= (ImageView)(v.findViewById(R.id.event_status));
	    		TextView numb=(TextView)(v.findViewById(R.id.event_people));
	    		try {
	    		if (bGo) {numb.setText(aRow.get(PARTICIPANTS));change.setBackground(iGo);}
	    		else 
	    		{ numb.setText(" "); change.setBackground(iLove);}	    		
	    		//showTransition(change, bGo);
	    		}catch (NullPointerException e){}
	    		v.invalidate();	    	
	    	}
	    	else
	    	mViewOfPage.invalidate();
	    	//((AgendaActivity)mActivity).redrawList();
	    	setMenuActionItem(aRow.get(STATUS));
		}
		
*/
		
		@Override
		protected void setMenuActionItem(String sts)
		{
			String status=sts;
		
			if (status == null){
				if (viewHolder.data==null) getFormData();
				if (viewHolder.data!=null) {
					status=viewHolder.data.get(STATUS);
					if (status == null) return;
				} else return;
			}
			if (status.equalsIgnoreCase("READ")||
					status.equalsIgnoreCase("OPEN")	)
			{
				mMenuItemId=new int[]{R.id.action_home, R.id.action_join};   				
			}
			else if (status.equalsIgnoreCase("JOIN"))
			{
				mMenuItemId=new int[]{R.id.action_home, R.id.action_cancel};   				
			}
			else if (status.equalsIgnoreCase("CREATE") ||
					status.equalsIgnoreCase("MODIFY"))
			{
				mMenuItemId=new int[]{R.id.action_home, R.id.action_send};   				
			}
			activateMenu();
		}
		
		void activateMenu()
		{
			Menu menu=
					((AgendaActivity)getActivity()).getMenuBar();
			if (menu==null) return;
			
			for (int i=0; i<menu.size(); i++)
	    	{
	    		menu.getItem(i).setVisible(false);
	    	}
	    	for (int i=0; i<mMenuItemId.length; i++)
	    	{	    		
	    		menu.findItem(mMenuItemId[i]).setVisible(true);
	    		menu.findItem(mMenuItemId[i]).setOnMenuItemClickListener(this);
	    		//menu.findItem(mMenuItemId[i]).getActionView().invalidate();
	    	}
		}
		
		public int[] getMenuItems()
				{
				return mMenuItemId;
				}
		@Override
		protected void modifyRowByJob(TreeMap<String, Object> aRow)
		{
/*			String status=(String)(aRow.get(STATUS));
			Log.d("LISTREC", (String)aRow.get("event_date")+(String)aRow.get("event_title")+status);
			aRow.put(STATUS, iLove);
				if (!status.equalsIgnoreCase("JOIN"))
					aRow.put(PARTICIPANTS, " ");
				else aRow.put(STATUS, iGo);
				if (status.equalsIgnoreCase("NEW"))
					aRow.put(STATUS, iNew);
				if (status.substring(0,3).equalsIgnoreCase("DEL"))
					aRow.put(STATUS, iXX);
				return;*/
		}
		
		@Override 
		protected void changeNewRecordToOpen()
		{
/*			String status=selectedValues.get(STATUS);
			if (!status.equalsIgnoreCase("NEW")) return;
				
			selectedValues.put(STATUS, "READ");
				String tt=selectedValues.get("event_title");//tx.getText().toString();
				String dd=selectedValues.get("event_date");//tx.getText().toString();
				if (tt != null){
				String sql="update agenda set status='READ' where event_date='"+dd+"' and "; 
					sql += "event_title='"+tt+"'";
					if (doDbSql(sql))
					{
						postFormValueToList(selectedValues);
					}
				}*/
		}
		
/*		@Override
		protected View openForm(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState)
		{
			if (mFormView==null)
			{
				if (formFrame != null)
					mFormView = inflater.inflate(R.layout.agenda_form, formFrame, true);
				else
			mFormView = inflater.inflate(R.layout.agenda_form, container, false);
			}
			mFormView.setBackgroundColor(0xFFF380);
			getViewHolder(mFormView);
			
			return mFormView;
		}*/

		protected View createNewAgenda()
		{
			if (viewHolder.data != null)
				getFormData();
			LayoutInflater inflater=getActivity().getLayoutInflater();
			View vP=viewHolder.parent;
			View tmp=inflater.inflate(R.layout.create_agenda_form, (ViewGroup) vP, false);
			ViewGroup vG=(ViewGroup) vP;//AgendaActivity.formFrame;
			vG.removeAllViews();
			vG.addView(tmp);
			
			mFormView = tmp;
			mFormView.setBackgroundColor(0xFFF380);
			getViewHolder(tmp);
			viewHolder.data.clear();//
			//((ListView)mViewOfPage).addHeaderView(mFormView);
			mMenuItemId=new int[]{R.id.action_send, R.id.action_home};
			activateMenu();
			//((AgendaActivity)getActivity()).activateMenu();
			return mFormView;
		}	

		protected View modifyAgenda()
		{
			getFormData();
			
			LayoutInflater inflater=getActivity().getLayoutInflater();
			
			View vP=viewHolder.parent;
			View tmp=inflater.inflate(R.layout.create_agenda_form, (ViewGroup) vP, false);
			ViewGroup vG=(ViewGroup) vP;//AgendaActivity.formFrame;
			vG.removeAllViews();
			vG.addView(tmp);
			
			mFormView = tmp;
			mFormView.setBackgroundColor(0xFFF380);
			//
			//((ListView)mViewOfPage).addHeaderView(mFormView);
			getViewHolder(tmp);
			setFormData();
			viewHolder.data.put(STATUS, "MODIFY");
			mMenuItemId=new int[]{R.id.action_send, R.id.action_home};
			activateMenu();
			//((AgendaActivity)getActivity()).activateMenu();
			return mFormView;
		}	
		
	public void setFormFrame(ViewGroup vG)
		{
			formFrame=vG;
		}
		
		@Override
		ViewGroup getFormFrame()
		{
			return ((AgendaActivity)getActivity()).formFrame;
		}
		
		@Override
		ViewGroup getListFrame()
		{
			return ((AgendaActivity)getActivity()).listFrame;
		}
		
		static class ViewHolder{
			View parent;
			View recordViewHolder;
			TextView  event_title ;			
			TextView event_date;				
			TextView event_time;				
			TextView event_location;	
		    TextView event_city;
			TextView event_host;				
			TextView contact_number;
			TextView event_description;
			String status;
			HashMap<String, String> data;
		}
		final static ViewHolder viewHolder=new ViewHolder();
		void getViewHolder(View formView)
		{
			viewHolder.parent=formView;
			viewHolder.event_title=(TextView)(formView.findViewById(R.id.event_title));			
			viewHolder.event_date=(TextView)(formView.findViewById(R.id.event_date));				
			viewHolder.event_time=(TextView)(formView.findViewById(R.id.event_time));				
			viewHolder.event_location=(TextView)(formView.findViewById(R.id.event_location));	
		    viewHolder.event_city=(TextView)(formView.findViewById(R.id.event_city));
			viewHolder.event_host=(TextView)(formView.findViewById(R.id.event_host));				
			viewHolder.contact_number=(TextView)(formView.findViewById(R.id.contact_number));
			viewHolder.event_description=(TextView)(formView.findViewById(R.id.event_description));									
		}
		void getFormData()
		{
			if (viewHolder.parent == null ) return;
			if (viewHolder.data==null)
				viewHolder.data=new HashMap<String, String>();
			viewHolder.data.put("event_title", viewHolder.event_title.getText().toString());			
			viewHolder.data.put("event_date", viewHolder.event_date.getText().toString());					
			viewHolder.data.put("event_time", viewHolder.event_time.getText().toString());					
			viewHolder.data.put("event_location", viewHolder.event_location.getText().toString());	
			viewHolder.data.put("event_city", viewHolder.event_city.getText().toString());	
			viewHolder.data.put("event_host", viewHolder.event_host.getText().toString());	
			viewHolder.data.put("contact_number", viewHolder.contact_number.getText().toString());	
			viewHolder.data.put("event_description", viewHolder.event_description.getText().toString());
			return ;
		}
		
		void setFormData()
		{
			if (viewHolder.data==null) return;
			HashMap<String, String> data=viewHolder.data;
			viewHolder.event_title.setText(data.get("event_title"));			
			viewHolder.event_date.setText(data.get("event_date"));				
			viewHolder.event_time.setText(data.get("event_time"));				
			viewHolder.event_location.setText(data.get("event_location"));	
		    viewHolder.event_city.setText(data.get("event_city"));
			viewHolder.event_host.setText(data.get("event_host"));				
			viewHolder.contact_number.setText(data.get("contact_number"));
			viewHolder.event_description.setText(data.get("event_description"));
			String status = viewHolder.data.get(STATUS);
			if (status == null || status.equalsIgnoreCase("NEW"))
			{
				viewHolder.data.put(STATUS, "OPEN");
				updateDbStatus(viewHolder.data);
				updateListRecord(viewHolder.data);
			}
			
		}
		boolean IamBoss=false;
		/*@Override
		protected View setTopFormContent(ViewGroup v, HashMap<String, String> data)
		{
			//sendTestLine();
			
			boolean openNew=false;
			//mMenuItemId=new int[]{R.id.action_join};
			String sBoss=getResources().getString(R.string.im_boss);
			IamBoss=(sBoss.charAt(0)=='Y');
			if (IamBoss) mMenuItemId=new int[]{R.id.action_modify, R.id.action_new, R.id.action_home};
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
				selectedValues.put(STATUS,  "CREATE");
				}
			}
					
			LayoutInflater inflater=getActivity().getLayoutInflater();
			if (openNew)
				mFormView=createNewAgenda();
			else
			mFormView=openForm(inflater, getFormFrame(), null);
			getViewHolder(mFormView);
			//((ListView)v).addHeaderView(mFormView);
			//activateMenuItems();
			//((AgendaActivity)mActivity).redrawForm();
			//mFormView.postInvalidate();
			return mFormView;
		}*/
		
		void deleteRow(String event_date, String event_title)
		{
			String sql="delete from agenda ";
			sql += (" where event_date='"+event_date+"' ");
			sql += (" and event_title='"+event_title+"' ;");
			final String mySQL=sql;
			//final SQLiteDatabase aDb=mDb;
			new Thread(new Runnable(){
				public void run(){
					DbProcessor.modifyTable(mActivity, mySQL);	
				}
			}).start();
			return;
		}
		
		void modifyRow(ContentValues aRow)
		{
			String event_date=aRow.getAsString("event_date");
			String event_title=aRow.getAsString("event_title");
			deleteRow(event_date,  event_title);
			final ContentValues aC=aRow;
			new Thread(new Runnable(){
				public void run(){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					DbProcessor.insertTable(mActivity, "agenda",  null, aC);
				}
			}).start();
		}
		

		protected void setDeletedStatus(HashMap<String, String> aRow)
		{
			//ContentValues always for database
			String event_date=aRow.get("event_date");
			String event_title=aRow.get("event_title");
			String sql="update agenda set status='DELETED' ";
			sql += (" where event_date='"+event_date+"' ");
			sql += (" and event_title='"+event_title+"' ;");
			final String mySQL=sql;
			//final SQLiteDatabase aDb=mDb;
			new Thread(new Runnable(){
				public void run(){
					DbProcessor.modifyTable(mActivity, mySQL);
				}
			}).start();
			return;
		}
		
		protected void updateDbStatus(HashMap<String, String> aRow)
		{
			//ContentValues always for database
			String event_date=aRow.get("event_date");
			String event_title=aRow.get("event_title");
			String status = aRow.get(STATUS);
			String sql="update agenda set status='"+status+"' ";
			if (!status.equalsIgnoreCase("JOIN"))
				sql += ", participants='0' ";
			else sql += (", participants='"+aRow.get(PARTICIPANTS)+"' ");
			sql += (" where event_date='"+event_date+"' ");
			sql += (" and event_title='"+event_title+"' ;");
			final String mySQL=sql;
			//final SQLiteDatabase aDb=mDb;
			new Thread(new Runnable(){
				public void run(){
					DbProcessor.modifyTable(mActivity,mySQL);		
				}
			}).start();
			return;
		}
		
		void updateListRecord(HashMap<String, String> aRow)
		{
/*			String event_date=aRow.get("event_date");
			String event_title=aRow.get("event_title");
			String status=aRow.get(STATUS);
			for (int i=0; i<dataSink.size(); i++)
			{*/
				HashMap<String, String> aRec=/*(HashMap<String, String>)(dataSink.get(i));
				if (!aRec.get("event_date").equalsIgnoreCase(event_date) ||
						!aRec.get("event_title").equalsIgnoreCase(event_title))
					continue;*/
						dataSink.get(mCurrentRowId);
				aRec.put(STATUS, aRow.get(STATUS));
				if (aRow.get(STATUS).equalsIgnoreCase("JOIN"))
					aRec.put(PARTICIPANTS, aRow.get(PARTICIPANTS));
				else aRec.put(PARTICIPANTS, "0");
				//break;
			//}
				if (rowAdapter != null)
				rowAdapter.notifyDataSetChanged();
			/*getListView().invalidate();
			if (clickedView != null) ((AgendaActivity)getActivity()).refreshMe(clickedView);
			ListView v=getMyListView();
			if (v!=null) ((AgendaActivity)getActivity()).refreshMe(v)*/;
		}
		
		void updateListFromRecordSet(Cursor csr)
		{
			if (csr==null) {
				DbProcessor.closeDbByCursor(csr);
				return;
			}
			if (csr.getPosition()==0) 
				{
					mCurrentRowId=0;
					dataSink.clear();
				}
			HashMap<String, String> aRow=new HashMap<String, String>();			
			for (int i=0; i<csr.getColumnCount(); i++)
			{
				aRow.put(csr.getColumnName(i), csr.getString(i));
			}
			String date=aRow.get("event_date");
			String status=aRow.get(STATUS);
			if (date != null && date.compareTo(mToday) > 0 && 
					   status != null && status.equalsIgnoreCase("NEW"))
				mCurrentRowId=dataSink.size();
			addNewRow(aRow);
			//csr.moveToNext();
		}
		
		void setSelectedValue(int iSelected)
		{
			mCurrentRowId=iSelected;
			confirmListData();
			if (viewHolder.data==null)
					viewHolder.data=new HashMap<String, String>();
			if (mCurrentRowId > dataSink.size() ) mCurrentRowId=dataSink.size()-1;
			HashMap<String, String> aRec=(HashMap<String, String>)(dataSink.get(mCurrentRowId));
			Iterator<String> itr=aRec.keySet().iterator();
			while (itr.hasNext())
			{
				String key=itr.next();
				viewHolder.data.put(key, aRec.get(key));
			}	
			setFormData();
		}
		
		static void copyRecordSetToArrayList(Cursor csr)
		{			
			if (csr==null) {
				DbProcessor.closeDbByCursor(csr);
				return;
			}
			HashMap<String, String>  aRow=new HashMap<String, String>();
			if (csr.getPosition()==0) 
				{
				mCurrentRowId=0;
				dataSink.clear();
				}
			for (int i=0; i<csr.getColumnCount(); i++)
			{
				aRow.put(csr.getColumnName(i), csr.getString(i));
			}
			dataSink.add(aRow);
			String date=aRow.get("event_date");
			String status=aRow.get(STATUS);
			if (date != null && date.compareTo(mToday) > 0 && 
					   status != null && status.equalsIgnoreCase("NEW"))
				mCurrentRowId=dataSink.size()-1;
			if (csr.getPosition()==csr.getCount()-1)
			if (mListView != null) mListView.setSelection(mCurrentRowId);
		}
		
		public static class agendaRecordCursorBinder implements SimpleCursorAdapter.ViewBinder
		{
			public boolean setViewValue(View v, Cursor csr, int iCol)
			{
				String colName=csr.getColumnName(iCol).toUpperCase();
				
				if (!colName.equalsIgnoreCase("STATUS")) return false;
				String status=csr.getString(iCol).toUpperCase();
				
				Drawable img=null;
				switch (status)
				{
				case "NEW":
					img=iNew;
					break;
				case "JOIN":
					img=iGo;
					
					break;
				case "DELETED":
					img=iXX;
					
					break;
				default:
					img=iLove;
					
					break;
				}
				((ImageView)v).setBackground(img);
				copyRecordSetToArrayList(csr);
				
				return true;
			}
		}
		
		@Override
		protected SimpleCursorAdapter.ViewBinder getMyCursorBinder(){
		
			return new agendaRecordCursorBinder();
		}
		
		@Override
		protected void fillListView(ListView v, ArrayList<HashMap<String, String>> data)
		{
			if (rowAdapter==null) rowAdapter=new AgendaListAdapter(getActivity(), dataSink);	    	
				getMyListView().setAdapter(rowAdapter);
		}
		
		void onFinishLoading()
		{
			if (rowAdapter == null) return;
			if (dataSink.size() > 0)
			{
				setSelectedValue(mCurrentRowId);
			}
			((AgendaActivity)getActivity()).refreshMe(viewHolder.parent);
			invalidateViews();
		}
		
		void addNewRow(HashMap<String, String> aRow)
		{
			dataSink.add(aRow);
			if (rowAdapter != null)
			rowAdapter.notifyDataSetChanged();
			//getListView().setSelection(dataSink.size()-1);
		}
		
		//getListView().setSelection(mRecorder.size()-1);
		void refreshData()
		{
			String sql=" select * from agenda order by event_date desc;";
		   	Cursor cr=DbProcessor.getRecordsFromSql(mActivity, sql);
		   	if (cr==null) {
		   		DbProcessor.closeDbByCursor(cr);
		   		return;
		   	}
			
		   	cr.moveToFirst();
		   	while (!cr.isAfterLast())
		   	{
		   		updateListFromRecordSet(cr);
		   		cr.moveToNext();
		   	}
		   	DbProcessor.closeDbByCursor(cr); 		
		}
		
		@Override
		protected void restoreInstanceState(Bundle savedInstanceState) {
			if (viewHolder.data != null) viewHolder.data=null;
				viewHolder.data=getFromSavedInstance(savedInstanceState);
			//else selectedValues=getFromSavedInstance(savedInstanceState);
	    }
		
		static ListView mListView;
		AgendaListAdapter rowAdapter;
		//View mRootView;
		static ArrayList<HashMap<String, String> > dataSink=new ArrayList<HashMap<String, String> >();
		
	
		/*
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {

			mRootView=super.onCreateView(inflater, container, savedInstanceState);
	
	    	init();
	    	//lastNew=0;
	    	String key=getResources().getString(R.string.fix_line_key);
	    	String fixLine=getArguments().getString(key);
	    	if (fixLine != null)
	    	{
	    		checkIfNotification(fixLine);	    		
	     	} 
	    	
	    	mRootView=openMyForm(inflater, container, savedInstanceState);
			
	    	View formView=mRootView.findViewById(R.id.agenda_form);
			getViewHolder(formView);

	    	if (savedInstanceState != null)
	    	restoreInstanceState(savedInstanceState);
	    	//data saved in viewHolder's store ContentValues dataRow;
	    	
	    	mListView=(ListView) mRootView.findViewById(android.R.id.list);
			
			if (dataSink==null) dataSink=new ArrayList<HashMap<String, String> >();
	    	if (rowAdapter==null) rowAdapter=new AgendaListAdapter(getActivity(), dataSink);
	    	//mListView.
	    	setListAdapter(rowAdapter);
	    	//mListView.setAdapter(rowAdapter);
	    	
	    	//refreshData();
	    	//getListView().setSelection(lastNew);
	    	String sBoss=getResources().getString(R.string.im_boss);
			IamBoss=(sBoss.charAt(0)=='Y');
			if (dataSink.size() > 0)
			{
				setSelectedValue(lastNew);
			}
	
	    	return mRootView;
	    }
			*/	
		
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {

	    	init();
	    	
	    	if (savedInstanceState != null)
	    	restoreInstanceState(savedInstanceState);
	    	
	    	String key=getResources().getString(R.string.fix_line_key);
	    	String fixLine=getArguments().getString(key);
	    	if (fixLine != null)
	    	{
	    		checkIfNotification(fixLine);
	    		/*
	    		selectedValues=parseFixLine(fixLine);
	    		
	    		 * should be done in activity already
	    		 * mContentValues=getValidData(selectedValues);
	    		String tableName=DataStorage.getTableName(fixLine);
	    		if (tableName != null)
	    		doInsert(tableName);*/
	     	} 

	    	getListData();
	    	View mRootView;
	    	
	    	WindowManager windowManager=(WindowManager)(mActivity.getSystemService(Context.WINDOW_SERVICE));
			  Display display=windowManager.getDefaultDisplay();
			  int rotation=display.getRotation();
			 
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
	    	mViewOfList.setBackgroundResource(R.drawable.list_background);*/
	    	
			//(mActivity).activateMenu();
			listScroller=new ListViewAutoScrollHelper((ListView)mViewOfList);
			
	/*		//might be overridden to use cursor
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
	*/    	
	    	return mRootView;
	    }
		void fillDummy(){
			dummyAgenda.put("event_date", "2014-01-01");
			dummyAgenda.put("event_time", "06:00");
			dummyAgenda.put("event_title", "Happy New Year");
			dummyAgenda.put("event_location", "信義區市府路1號");
			dummyAgenda.put("event_city", "臺北市");
			dummyAgenda.put("event_host", "柯文哲");
			dummyAgenda.put("contact_number", "(02)­2564-2900");
			dummyAgenda.put(STATUS, "open");
			dummyAgenda.put(PARTICIPANTS, "100000");
			String descp="若有任何疑問，請來電(02)­2564-2900, 謝謝大家的支持。";
			descp += "捐款的朋友請認明此帳號：";
			descp += "台北富邦松江分行 012­4704活期存款";
			descp += "103年臺北市市長擬參選人柯文哲政治獻金專戶";
			descp += "470-­102-­04812-­8";
			dummyAgenda.put("description", descp);
		}
		static HashMap<String, String> dummyAgenda=new HashMap<String, String>();
	    protected boolean confirmListData()
	    {
	    	if (dataSink.size() < 1) 
	    		{
	    		if (dummyAgenda.size()==0) fillDummy();
	    			dataSink.add(dummyAgenda);	    			
	    		}
	    	return true;
	    }
	    
	    protected ArrayList<HashMap<String, String> > getListData()
	    {
	    	if (dataSink.size() < 1) refreshData();
	    	return dataSink;
	    }
	    
/*	    @Override 
	    public ListView getListView()
	    {
	    	return (ListView) mViewOfList;
	    }*/
		
		protected int getCurrentRowId()
		{
			return mCurrentRowId;
		}
		
		void invalidateViews()
		{
			if (mFormView != null) ((AgendaActivity)getActivity()).refreshMe(mFormView);
			if (mViewOfList != null ) ((AgendaActivity)getActivity()).refreshMe(mViewOfList);
		}
		
	    @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	    	
	        super.onActivityCreated(savedInstanceState);

			//might be overridden to use cursor
			//mFormView=openForm(LayoutInflater.from(getActivity()), getFormFrame(), savedInstanceState);
			getViewHolder(mFormView);
			int iOpen=getArguments().getInt(SELECTED_RECORD, -1);
			if (iOpen < 0) iOpen=mCurrentRowId; 
			mCurrentRowId=iOpen;
			if (getListView() != null)
				getListView().setSelection(mCurrentRowId);
			//else
	    	setSelectedValue(mCurrentRowId);
	    	//setFormData();
	    	mFormView.postInvalidate();
	    }
	    
		void saveNotifyDataToTable(HashSet<String> data)
		{
			//AgendaRecord aAg=new AgendaRecord();
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
	    	String fileName="notification"+"agenda";
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
}

