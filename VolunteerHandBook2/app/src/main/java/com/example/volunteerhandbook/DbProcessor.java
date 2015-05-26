package com.example.volunteerhandbook;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

public class DbProcessor extends SQLiteOpenHelper {

	private static final int DB_VERSION=1;
	private static int iOpen=0;
	private static SQLiteDatabase sDB=null;
	private static String tableNames=null;
	public static HashMap<String, SQLiteDatabase > dbToClose=new HashMap<String, SQLiteDatabase >();
	
	
	String mSql;
	Context mContext;
	SQLiteDatabase mDb;
	
	public DbProcessor(Context context, String name) throws SQLiteException
	{
		super(context, name, null, DB_VERSION);
		mContext=context;
		
		mDb= getWritableDatabase ();
		if (mDb!=null)
		{
			Log.i("DBOPEN", "opened No."+(++iOpen)+" by "+context.getPackageName());
		}
	}

	public DbProcessor(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
		mContext=context;
		mDb= getWritableDatabase ();
	}

	@Override
	public void onCreate(SQLiteDatabase db) throws SQLiteException {
		// TODO Auto-generated method stub
		if (mSql==null) return;
		db.execSQL(mSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		Toast.makeText(mContext, "Database Got Upgraded from "+arg1+" to "+arg2, Toast.LENGTH_SHORT).show();
		onCreate(db);
	}
	
	public SQLiteDatabase getDb()
	{
		return mDb;
	}
	public void setSQL(String sql)
	{
		mSql=sql;
	}
	
	public static int selectCount(Context cx, String table, String whereClause)throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return -1;
		String sSql="select count(*) from "+table+ " ";
		if (whereClause != null) sSql += whereClause;
		SQLiteStatement stm=sDB.compileStatement(sSql);
		int iC=(int)stm.simpleQueryForLong();
		stm.close();
		aDb.close();
		return iC;
	}
	
	private static SQLiteDatabase getInstance(Context cx)
	{
		DbProcessor aDp=new DbProcessor(cx, "kop_volunteer");
		if (dbToClose==null)
			dbToClose=new HashMap<String, SQLiteDatabase >();

		return aDp.mDb;
		
	}
	
	public static void closeDbByCursor(Cursor csr)
	{
		SQLiteDatabase db=dbToClose.get((""+csr.hashCode()));
		if (db!=null) db.close();
		csr.close();
	}
	
	public static void createTable(Context cx, String sSql)
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return;
		SQLiteStatement stm=null;
		try {
			stm=aDb.compileStatement(sSql);
			stm.execute();
			stm.close();
			stm=null;
		} catch (SQLiteException e){
			e.printStackTrace();
		}
		finally{
			if (stm != null) stm.close();
			aDb.close();	
		}
			return;		
	}
	
	public static void dropTable(Context cx, String sSql)
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return;
		try {
		SQLiteStatement stm=aDb.compileStatement(sSql);
		stm.execute();
		stm.close();
		aDb.close();
		} catch (SQLiteException e)
		{
			e.printStackTrace();
			return;
		}
		readTableNames(cx);
	}
	
	public static void execSQL(Context cx, String sSql)throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return;
		aDb.execSQL(sSql);
		aDb.close();
	}
	
	public static void emptyTable(Context cx, String sSql)throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return;
		SQLiteStatement stm=aDb.compileStatement(sSql);
		stm.execute();
		stm.close();
		aDb.close();
	}
	
	public static int insertTable(Context cx, String table, String nullColumnHack, ContentValues values )
			throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return -1;
		int iC=(int)aDb.insert(table, nullColumnHack, values);
		aDb.close();
		return iC;
	}
	
	public static int insertTable(Context cx, String sSql)throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return -1;
		SQLiteStatement stm=aDb.compileStatement(sSql);
		int iC=(int)stm.executeInsert();

		stm.close();
		aDb.close();
		return iC;
	}
	
	public static int updateTable(Context cx,  String table, ContentValues values, String sWhere, String[] toSet)throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return -1;
		
		//SQLiteStatement stm=aDb.compileStatement(sSql);
		int iC=aDb.update(table, values, sWhere, toSet);//(int)stm.executeUpdateDelete();

		//stm.close();
		aDb.close();
		return iC;
	}
	
	public static int modifyTable(Context cx, String sSql)throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return -1;
		SQLiteStatement stm=aDb.compileStatement(sSql);
		int iC=(int)stm.executeUpdateDelete();

		stm.close();
		aDb.close();
		return iC;
	}
	
	@Override 
	protected void finalize()
	{
		Log.w("DBProc", "finalize is called");
		if (dbToClose != null && dbToClose.size() > 0)
		{
			Iterator<String> itr=dbToClose.keySet().iterator();
			while (itr.hasNext()){
				String key=itr.next();
				SQLiteDatabase db=dbToClose.get(key);
				if (db!=null && db.isOpen()) db.close();
			}
			dbToClose.clear();
			dbToClose=null;
		}
	}
	public static Cursor getAllRecords(Context cx, String tableName) throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return null;
		String sql="select rowid _id, * from "+tableName+" ;"; //cursor needs the rowid shows up but 
		Cursor csr=aDb.rawQuery(sql, null);
		dbToClose.put(""+csr.hashCode(), aDb);														//just neglects it
		
		return csr;
	}
	
	public static Cursor getRecordsFromSql(Context cx, String sql)throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return null;
		
		Cursor csr=aDb.rawQuery(sql, null);
		if (csr==null){
				Log.w("DbPROC", "csr is NULL for "+sql);
				aDb.close();
				return null;
		} else	try {	
		dbToClose.put(""+csr.hashCode(), aDb);	
		} catch (NullPointerException e){
			Log.w("DBProc", "something wrong for "+sql);
			if (dbToClose==null) Log.w("DBProc", "dbToClose is null ");
			else if (csr==null) Log.w("DBProc", "csr is null ");
			else if (aDb==null) Log.w("DBProc", "aDb is null ");
		}
		return csr;
	}
	
	static void readTableNames(Context cx)throws SQLiteException
	{
		SQLiteDatabase aDb=getInstance(cx);
		if (aDb==null) return ;
		
		String sql="SELECT tbl_name FROM sqlite_master WHERE type='table' ;";
		Cursor c=aDb.rawQuery(sql, null);;
		if (c== null) return;
		//String tblName="";
		c.moveToFirst();
		tableNames=" ";
		while (!c.isAfterLast())
		{
			tableNames += (c.getString(0)+", ");
			c.moveToNext();
		}
		c.close();
		aDb.close();
		SharedPreferences sharedPref = cx.getSharedPreferences("data_last_check_time", Context.MODE_PRIVATE);
		SharedPreferences.Editor writer=sharedPref.edit();
		writer.putLong("TABLES", new Date().getTime());
		writer.commit();
		sharedPref = cx.getSharedPreferences("global_references", Context.MODE_PRIVATE);
		writer=sharedPref.edit();
		writer.putString("TABLE_NAMES", tableNames);
		writer.commit();		
	}
	
	public static boolean ifTableExists(Context cx, String tblName)
	{
		if (tblName == null) return false;
		SharedPreferences sharedPref = cx.getSharedPreferences("data_last_check_time", Context.MODE_PRIVATE);
		long lTime=sharedPref.getLong("TABLES", 0);
		long timeNow=new Date().getTime();
		if (lTime ==0 || lTime+3000 <  timeNow){
			readTableNames(cx);
		}
		sharedPref = cx.getSharedPreferences("global_references", Context.MODE_PRIVATE);
		String allNames=sharedPref.getString("TABLE_NAMES", "--");
		if (allNames.toLowerCase().indexOf(tblName.toLowerCase()) <0) return false;
		return true;
	}
	

}
