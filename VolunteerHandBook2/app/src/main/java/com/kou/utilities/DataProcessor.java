package com.kou.utilities;



import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;


/*implementing database linkage
 * 
 */
public final class DataProcessor extends Thread
{

	static class Cust_Instruction
	{
		int cid;
		DB_Instruction instruction;
		Cust_Instruction(int c, DB_Instruction d)
		{
			cid=c;
			instruction=d;
		}
	};
	static Charset chrSet=Charset.forName("UTF-8");
	static Locale tw=Locale.TRADITIONAL_CHINESE;
	static int client_registration_id=0;
	static ArrayBlockingQueue<Cust_Instruction> instructionQ=new ArrayBlockingQueue<Cust_Instruction>(100,true);
	static int dataLength=0;
	static Vector<Vector<RowStruct > > outData=new Vector<Vector<RowStruct > >();
	static boolean has_driver=false;
	private static HashMap<String, Vector<RowStruct > > allData= //tablename, field, value
			   new HashMap<String, Vector<RowStruct > >(); //Vec has all rows
	private java.sql.Connection conn;

	private static int instance_count=0;
	private long mStopTime=0;
	static boolean hasNewInstruction=false;
	static Lock writeLock=new ReentrantLock();
	static Lock responseLock=new ReentrantLock();
	
	static public DataProcessor getInstance()
	{
		if (instance_count > 0) return null;
		return (new DataProcessor());
	}
	
	void dropTables()
	{
		creatSql("drop table agenda;");
		creatSql("drop table candidate_vocal;");
		creatSql("drop table commitment;");
		creatSql("drop table fund_raised;");
		creatSql("drop table visited;");
		creatSql("drop table volunteer;");
		creatSql("drop table team;");
		creatSql("drop table blue_print;");		
	}
	void CreatTables()
	{
		dropTables();
		creatSql(CreateTableSql.createAgendaTable());
		creatSql(CreateTableSql.createCandidateVocalTable());
		creatSql(CreateTableSql.createCommitmentTable());
		creatSql(CreateTableSql.createFundRaisedTable());
		creatSql(CreateTableSql.createTeamTable());
		creatSql(CreateTableSql.createVisitedTable());
		creatSql(CreateTableSql.createVolunteerTable());
		creatSql(CreateTableSql.createBluePrintTable());
		
		creatSql(CreateTableSql.createAgendaDateIndex());
		creatSql(CreateTableSql.createCommitmentIdIndex());
		creatSql(CreateTableSql.createCandidateVocalIndex());
		creatSql(CreateTableSql.createVolunteerNameIndex());
		creatSql(CreateTableSql.createFundRaisedIdIndex());
		creatSql(CreateTableSql.createTeamIndex());
		creatSql(CreateTableSql.createVisitedIdIndex());
		creatSql(CreateTableSql.createBluePrintIndex());
	}
	private DataProcessor()
	{
		try {
		if (!has_driver)
		{
		
		Class.forName("com.mysql.jdbc.Driver").newInstance(); 
		
		}
		has_driver=true;
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		}
		catch (InstantiationException e){
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//readAllTables();
		for (int i=0; i<100; i++)
			outData.add(null);
		//dropTables();
		//CreatTables();
	}
	
	public void setStopTime(long st)
	{
		mStopTime=st;
	}
	public static int registerClient()
	{
		int id;
		synchronized (writeLock){
		id= ++client_registration_id;
		}
		return id;
	}
	
	public static Vector<RowStruct > clientReadResponse(int cid)
	{
		Vector<RowStruct > outTable;
		synchronized (responseLock)
		{
			while (outData.get(cid)==null || outData.get(cid).isEmpty())
			{
			try {
					responseLock.wait(100);
				} catch (InterruptedException e){}
			}
			outTable=new Vector<RowStruct >(outData.get(cid));
			outData.get(cid).clear();
		}
		return outTable;
	}
	
	public static void putInstruction(int cid, DB_Instruction instruction)
	{
		Cust_Instruction aInst=new Cust_Instruction(cid, instruction);
		try {
		instructionQ.put(aInst);
		} catch (InterruptedException e){}
		/*
		 * 
		 */
	}

	public void stopThread()
	{
		mStopTime=1;
	}
// Open new connection. 
	
 

/* To connect to the database, 
you need to use a JDBC url with the following format 
([xxx] denotes optional url components):
jdbc:mysql://[hostname][:port]/[dbname][?param1=value1][&param2=value2]... 
By default MySQL's hostname is "localhost." 
The database used here is called "mydb" and MySQL's default user is "root". 
If we had a database password we would add 
"&password=xxx" to the end of the url. 
*/ 
private final String host = "localhost";//"114.35.38.112";
private final String port = "3306";
private final String database = "kop_volunteer";
private final String user = "user=erickou";
private final String password = "&password=fatherkou";
private final String charSet = "&characterSetResults=UTF-8";
private final String charCode = "&characterEncoding=UTF-8";
private final String useUnicode = "&useUnicode=yes";
private String conn_command = "jdbc:mysql://";


private boolean getConn() throws SQLException
{
conn_command += (host+":"+port+"/"+database+"?"+user+password+charSet+charCode+useUnicode);
conn = DriverManager.getConnection(conn_command); 
if (conn == null) return false;
return true;
}

void creatSql(String sql)
{
	try {
	if (!getConn()) return;
	Statement sqlStatement = conn.createStatement(); 
//Generate the SQL query. 
	String query = sql; 
//Get the query results and display them. 
	//ResultSet sqlResult = 
			sqlStatement.execute(query); 

	//sqlResult.close(); 
	sqlStatement.close(); 
	conn.close(); 
		} catch (SQLException se){se.printStackTrace();}
		return;
}

class sqlThread extends Thread
{
	String sql;

	public sqlThread(String sql1)
	{
		sql=sql1;
	}
	public void run()
	{	
		try {
	if (!getConn()) {
		System.out.println("No connection to SQL");
		return;
	}
	Statement sqlStatement = conn.createStatement(); 
//Generate the SQL query. 
	String query = sql; 
//Get the query results and display them. 
	if (sql.toUpperCase().indexOf("SELECT")==0)
	{
		ResultSet sqlResult = sqlStatement.executeQuery(query); 
		sqlResult.close(); 
	}
	else 
		{
			int iCount=sqlStatement.executeUpdate(query);
			System.out.println("Processed "+iCount+ "records");
		}
	sqlStatement.close(); 
	conn.close(); 
		} catch (SQLException se){se.printStackTrace();}
		return;
	}	
}

public void sendSql(String sql) 
{
	sqlThread t=new sqlThread(sql);
	t.start();
}

void readAllTables()
{
	Vector<String> tables=new Vector<String>();
	try {
		if (!getConn()) return;
		Statement sqlStatement = conn.createStatement(); 
		//Generate the SQL query. 
		String query = "show tables"; 
		//Get the query results and display them. 
		ResultSet sqlResult = sqlStatement.executeQuery(query); 
		//RowSetMetaDataImpl rowInfo=;
		int iColumns=sqlResult.getMetaData().getColumnCount();
		while(sqlResult.next()) 
		{ 
			tables.add(sqlResult.getString(1));
		}
		sqlResult.close(); 
		for (int i=0; i<tables.size(); i++)
		{
			String table=tables.get(i);
			query="select * from "+table;
			sqlResult = sqlStatement.executeQuery(query);
			iColumns=sqlResult.getMetaData().getColumnCount();
			Vector<RowStruct > allRow=new Vector<RowStruct >();
			while(sqlResult.next()) 
			{ 
				RowStruct aRow=new RowStruct();
				for (int k=1; k<=iColumns; k++)
				{
					String colName=sqlResult.getMetaData().getColumnName(k);
					Object a = sqlResult.getObject(k);
					String value="";
					if (a != null) value=a.toString();
					aRow.put(colName, value);
				}
				allRow.add(aRow);
			}
			sqlResult.close();
			allData.put(table, allRow);
		}
		sqlStatement.close(); 
		
		//conn.close(); 
		} catch (SQLException se){se.printStackTrace();}
		
}

static final String[] INT_fields={"vid", "aid", "audio_id", "team_id", "mentor_vid", "member_id"};

static boolean isINTField(String fld)
{
	for (String intF : INT_fields)
	{
		if (fld.toLowerCase().equals(intF)) return true;
	}
	return false;
}

static final String comparator[]={">", ">=", "=", "<=", "<"};
static int getCompId(String eql)
{
	int i=0;
	for (String s : comparator)
	{
		if (s.equals(eql)) return i;
		i++;
	}
	return -1;
}
void filterTable(Vector<RowStruct >table, RowStruct criteria, Vector<Integer> found)
{
	//only filter out " AND " cases
	int[] good1=new int[table.size()+1];
	int i=0;
	while (i<table.size())
	{
		good1[i]=i++;
	}
	good1[i]=(-1);
	Iterator<String> itr=criteria.keySet().iterator();
	boolean ok=true;
	while (itr.hasNext())
	{
		String fld=itr.next();
		String cmp=((String)criteria.get(fld)).toUpperCase(tw);
		String cmpData;
		int iCmp=0;
		char ch=cmp.charAt(iCmp);
		while (iCmp<2 && ch != '=' && ch != '>' && ch != '<') {ch=cmp.charAt(+iCmp);}
		if (ch == 39) cmpData=cmp.substring(iCmp+1, cmp.length()-1);
		else cmpData=cmp.substring(iCmp, cmp.length()-1);
		String equal="=";
		if (iCmp> 0) equal=cmp.substring(0, iCmp);
		int k=0;
		boolean isInt=isINTField(fld);
		for (i =0; good1[i]> (-1); i++)
		{
			RowStruct aRow=table.get(good1[i]);
			String data=((String)aRow.get(fld)).toUpperCase(tw);
			
		try {
			switch ( getCompId(equal))
			{
			case 0: //">":
					if (isInt)
					{
						if (Integer.parseInt(data) > Integer.parseInt(cmpData)) good1[k++]=i;
					}
					else 
					{
						if (data.compareTo(cmpData) > 0) good1[k++]=i;
					}
				break;
			case 1://">=":
				if (isInt)
				{
					if (Integer.parseInt(data) >= Integer.parseInt(cmpData)) good1[k++]=i;
				}
				else 
				{
					if (data.compareTo(cmpData) >= 0) good1[k++]=i;
				}
				break;
			case 2://"=":
				if (isInt)
				{
					if (Integer.parseInt(data) == Integer.parseInt(cmpData)) good1[k++]=i;
				}
				else 
				{
					if (data.compareTo(cmpData) == 0) good1[k++]=i;
				}				
				break;
			case 3://"<=":
				if (isInt)
				{
					if (Integer.parseInt(data) <= Integer.parseInt(cmpData)) good1[k++]=i;
				}
				else 
				{
					if (data.compareTo(cmpData) <= 0) good1[k++]=i;
				}
				break;
			case 4://"<":
				if (isInt)
				{
					if (Integer.parseInt(data) < Integer.parseInt(cmpData)) good1[k++]=i;
				}
				else 
				{
					if (data.compareTo(cmpData) < 0) good1[k++]=i;
				}
				break;
			default:
				break;
			}
			}catch (NullPointerException e){}	
		}
		good1[k]=-1;
	}
	found.clear();for (i=0; good1[i]> -1; i++) found.add(good1[i]);
}

Vector<RowStruct > fillNotFound(String memo)
{
	Vector<RowStruct > aTbl=new Vector<RowStruct >();
	RowStruct aMp=new RowStruct();
	aMp.put("memo", memo);
	aTbl.add(aMp);	
	return aTbl;
}

void alterRow(RowStruct row, RowStruct setValues)
{
	Iterator<String> itrV=setValues.keySet().iterator();
		while (itrV.hasNext())
		{
			String fld=itrV.next();
			String data=(String)setValues.get(fld);
			row.put(fld, data);
		}
}

void processSelect(int cid, DB_Instruction instruction)
{
	Vector<RowStruct > oTbl=null;
	String sql=instruction.command;
	RowStruct criteria=instruction.criteria;
	
	String tblName=(String)criteria.get("table_name");
	
	Vector<RowStruct > aTbl=allData.get(tblName);
	if (aTbl == null || aTbl.size()==0) oTbl=fillNotFound("NO DATA");
	else 
	{
		oTbl=new Vector<RowStruct >();
		if (criteria == null) oTbl=aTbl;
		else {
			Vector<Integer> found=new Vector<Integer>();
			filterTable(oTbl, criteria, found);
			for (int i=0; i<found.size(); i++)
			{
				int idx=found.get(i);
				oTbl.add(aTbl.get(idx));
			}
		}
	}
	synchronized (responseLock){
		outData.set(cid, oTbl);
		responseLock.notifyAll();
	}
}

Vector<RowStruct> dbSelect(String table,String whereClause)
{
	Vector<RowStruct> foundList=null;
	try {
		if (!getConn()) return null;
		Statement sqlStatement = conn.createStatement(); 
		//Generate the SQL query. 
		String query = "select * from "+table+whereClause;
		//Get the query results and display them. 
		ResultSet sqlResult = sqlStatement.executeQuery(query);
		int iColumns=sqlResult.getMetaData().getColumnCount();
		foundList=new Vector<RowStruct>();
		while(sqlResult.next()) 
		{ 
			RowStruct aRow=new RowStruct();
			for (int k=1; k<=iColumns; k++)
			{
				String colName=sqlResult.getMetaData().getColumnName(k);
				Object a = sqlResult.getObject(k);
				String value="";
				if (a != null) value=a.toString();
				aRow.put(colName, value);
			}
			aRow.put("table_name", table);
			foundList.add(aRow);
		}
		sqlResult.close();	
		sqlStatement.close(); 	
	} catch (SQLException se){se.printStackTrace(); return null;}
	return foundList;
}


String buildEqualCriteria(RowStruct valueSet)
{
	String sql = " where ";

	Iterator<String> itr=valueSet.keySet().iterator();
	//boolean first=true;
	boolean first=true;
	while (itr.hasNext())
	{
		String key=itr.next();
		if (key.toLowerCase().equals("table_name") || 
				key.toLowerCase().equals("msgtype"))
		continue;
		if (!first) sql += " and ";
		sql += key;
		sql += "=";
		if (!isINTField(key)) sql += "'";
		sql += valueSet.get(key);
		if (!isINTField(key)) sql += "'";
		first = false;
	}	
	
	return sql;
}

void updateTableAfterInsert(int cid, String tblName, DB_Instruction instruction)
{
	//the criteria from unique not null flds should be provided by the caller
	String whereClause=buildEqualCriteria(instruction.criteria);
	if (whereClause== null) {
		System.out.println("Bad criteria :"); return;
	}
	Vector<RowStruct> oTbl=dbSelect(tblName, whereClause);
	Vector<RowStruct > aTable=allData.get(tblName);
	if (aTable==null) return;
	//only one row will be retrieved from return selection table
	aTable.add(oTbl.get(0));
	
	synchronized (responseLock){
		outData.set(cid, oTbl);
		responseLock.notifyAll();}
}

String criteriaLine(RowStruct criteria)
{
	String sql = " where ";

	Iterator<String> itr=criteria.keySet().iterator();
	boolean first=true;
	while (itr.hasNext())
	{
		String key=itr.next();		
		if (key.toLowerCase().equals("table_name") || 
				key.toLowerCase().equals("msgtype"))
			continue;
		if (!first) sql += " and ";
		sql += key;
		sql += criteria.get(key);
		first=false;	
	}
	return sql;
}

void dBUpdate(String tblName, DB_Instruction instruction)
{
String sql="Update "+tblName+" set ";
RowStruct valueSet=instruction.data;


Iterator<String> itr=valueSet.keySet().iterator();
boolean first=true;
	while (itr.hasNext())
	{
		String key=itr.next();		
		if (key.toLowerCase().equals("table_name") || 
				key.toLowerCase().equals("msgtype"))
			continue;
			
		if (!first) sql += ",";
		sql += key;
		sql += "=";
		if (!isINTField(key)) sql += "'";
		sql += valueSet.get(key);
		if (!isINTField(key)) sql += "'";

		first=false;	
	}
	sql += " where ";
	RowStruct criteria=instruction.criteria;
	
	
	criteria.remove("table_name");
	criteria.remove("msgType");

	itr=criteria.keySet().iterator();
	first=true;
	while (itr.hasNext())
	{
		String key=itr.next();		
		if (key.toLowerCase().equals("table_name") || 
				key.toLowerCase().equals("msgtype"))
			continue;
		if (!first) sql += " and ";
		sql += key;
		sql += criteria.get(key);
		first=false;	
	}	
	if (instruction.criteria == null && instruction.data == null) sql=instruction.command;
	sendSql(sql);
}
void processUpdate(int cid, DB_Instruction instruction)
{
	Vector<RowStruct > oTbl=null;
	//String sql=instruction.command;
	RowStruct criteria=instruction.criteria;
	
	String tblName=(String)criteria.get("table_name");
	if (tblName==null){
		tblName=(String)instruction.data.get("table_name");
		if (tblName==null) 
			tblName="no table";
	}
	Vector<RowStruct > aTbl=allData.get(tblName);
	if (aTbl == null) oTbl=fillNotFound("NO DATA");
	else 
	{
		dBUpdate(tblName, instruction);
		oTbl=dbSelect(tblName, criteriaLine(criteria));
		
			Vector<Integer> found=new Vector<Integer>();
			filterTable(aTbl, criteria, found);
			
			for (int i=0; i<found.size(); i++)
			{
				int idx=found.get(i);
				RowStruct aRow=aTbl.get(idx);
				alterRow(aRow, instruction.data);
				oTbl.add(aRow);
			}
		
	}
	synchronized (responseLock){
		outData.set(cid, oTbl);
		responseLock.notifyAll();
	}
}

void dBInsert(String tblName, DB_Instruction instruction)
{
	String sql="Insert "+tblName+" (";
	String values=" values (";
	RowStruct valueSet=instruction.data;
	
	Iterator<String> itr=valueSet.keySet().iterator();
	boolean first=true;
	while (itr.hasNext())
	{
		String key=itr.next();		
		if (key.toLowerCase().equals("table_name") || 
				key.toLowerCase().equals("msgtype"))
			continue;
		if (valueSet.get(key)==null) continue;
		if (!first) sql += ",";
		sql += key;
		
		if (!first) values += ",";
		if (!isINTField(key)) values += "'";
		values += valueSet.get(key);
		if (!isINTField(key)) values += "'";

		first=false;	
	}
	sql += ") ";
	values += ")";
	sql += values;

	sendSql(sql);
}

void processInsert(int cid, DB_Instruction instruction)
{
	Vector<RowStruct > oTbl=null;
	String sql=instruction.command;
	RowStruct data=instruction.data;
	
	String tblName=(String)data.get("table_name");
	Vector<RowStruct > aTbl=allData.get(tblName);
	if (aTbl != null) 
	{
		dBInsert(tblName, instruction);
		String[] criteriaFlds=MessageParser.getUniqueKeyField(tblName);
		RowStruct criteria=new RowStruct();
		for (int i=0; i<criteriaFlds.length; i++)
		criteria.put(criteriaFlds[i], data.get(criteriaFlds[i]));
		instruction.criteria=criteria;
		updateTableAfterInsert(cid, tblName, instruction);
		return;
	}
	oTbl=fillNotFound("NO TABLE");
	
	synchronized (responseLock){
		outData.set(cid, oTbl);
		responseLock.notifyAll();
	}
}

void dBDelete(String tblName, DB_Instruction instruction)
{
	String sql="Delete "+tblName+" ";
	
		sql += " where ";
		RowStruct criteria=instruction.criteria;
		
		Iterator<String> itr=criteria.keySet().iterator();
		boolean first=true;
		while (itr.hasNext())
		{
			String key=itr.next();		
			if (key.toLowerCase().equals("table_name") || 
					key.toLowerCase().equals("msgtype"))
				continue;
			if (!first) sql += " and ";
			
			sql += key;
			sql += criteria.get(key);
			first=false;	
		}	
	if (instruction.criteria == null) sql=instruction.command;
	sendSql(sql);
}
void processDelete(int cid, DB_Instruction instruction)
{
	Vector<RowStruct > oTbl=null;
	String sql=instruction.command;
	RowStruct criteria=instruction.criteria;
	
	String tblName=(String)criteria.get("table_name");
	Vector<RowStruct > aTbl=allData.get(tblName);
	if (aTbl == null || criteria == null) oTbl=fillNotFound("NO DATA");
	else 
	{
		oTbl=new Vector<RowStruct >();
		
			Vector<Integer> found=new Vector<Integer>();
			filterTable(oTbl, criteria, found);
			
			for (int i=0; i<found.size(); i++)
			{
				int idx=found.get(i);
				RowStruct aRow=aTbl.get(idx);
				aTbl.remove(idx);
				oTbl.add(aRow);
			}
		
	}

	synchronized (responseLock){
		outData.set(cid, oTbl);
		responseLock.notifyAll();
	}
	dBDelete(tblName, instruction);
}
void process(Cust_Instruction ins)
{
	DB_Instruction instruction=ins.instruction;
	int cid=ins.cid;
	String sql=instruction.command;
	if (sql.toUpperCase().indexOf("SELECT") == 0)
		processSelect(cid, instruction);
	else if (sql.toUpperCase().indexOf("UPDATE") == 0)
		processUpdate(cid, instruction);
	else if (sql.toUpperCase().indexOf("INSERT") == 0)
		processInsert(cid, instruction);
	else if (sql.toUpperCase().indexOf("DELETE") == 0)
		processDelete(cid, instruction);	
}

public void run()
{
	readAllTables();
	do
	{
		Cust_Instruction instruction=null;
		try {
			instruction=instructionQ.take();
		}catch (InterruptedException e){
			continue;
		}
		if (instruction != null)
			process(instruction);
		
	} while ((new Date()).getTime() < mStopTime || mStopTime ==0);
}

}

