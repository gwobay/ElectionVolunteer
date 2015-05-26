



package com.kou.utilities;



import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author eric
 *
 * use 0x01 as field separator and = as key-value separator
 * 
 
 */
public class MessageParser {

	static String[] tagName=new String[200];
	static HashMap<String, Integer> tagNumber=new HashMap<String, Integer>();
	String[] tagData;
	String tableName;
	String db_command;
	Vector<String> outMessageList;
	Vector<RowStruct > outTable;
	byte[] outData;
	byte[] inData;
	int dataLength;
	int db_clientId;
	RowStruct readRow;
	
	void init()
	{
		tagData=new String[200];
		tableName=null;
		db_command=null;
		outMessageList=null;
		outTable=null;
		outData=null;
		inData=null;
		dataLength=-1;
		db_clientId=-1;
		readRow=null;
	}
	public MessageParser() {
		//super(arg0, arg1);
		//buildTags();
		init();
		
	}
	public MessageParser(byte[] msg, int buf_len)
	{
		if (msg == null) return; //for dummy purpose
		init();
		inData=new byte[buf_len+1];
		//for (int i=0; i<data.size(); i++) inData[i]=data.get(i);
		System.arraycopy(msg, 0, inData, 0, buf_len);
		if (tagData == null) tagData=new String[200];
		outMessageList=new Vector<String>();	
		//buildTags();
		dataLength=buf_len;
	}
	
	void setDbClientId(int id)
	{
		db_clientId=id;
	}
	
	static final Object[][][] page_tags={{
		{170,"volunteer"},
	{35,"msgType"},{ 11,"vid"},{ 55,"regid"},{186, "citizen_id"},
	{49,"last_name"},{ 50,"first_name"},{176,"address_street_and_number"},{166,"address_city"},
		{75,"birth_date"},{181,"mobile_number"},{111,"rank"},{177,"team_id"}},
		
		{{170,"plan_reminder"},{35,"msgType"},{ 11,"vid"},
			{171, "activity_title"},{172, "activity_date"},
			{173, "activity_time"},{175, "contact_number"},
			{174, "url"}},
			
		{{170,"commitment"},{35,"msgType"},{ 11,"vid"},
			{151, "visits_per_week"},{152, "hours_per_visit"},
			{153, "raises_per_week"},{154, "hours_per_raise"},
			{155, "events_per_week"},{156, "hours_per_event"},
			{157, "supports_per_week"},{158, "hours_per_support"},
			{159, "specialty_list"},{75,"starting_date"}},

	{{170,"visited"},{35,"msgType"},{ 11,"vid"},{186, "citizen_id"},{75,"act_date"},
			{179,"fullname"},{181,"mobile_number"},{64,"next_schedule_date"},
				{ 178,"voter_rating"}},

	{{170,"fund_raised"},{35,"msgType"},{ 11,"vid"},{186, "citizen_id"},{75,"act_date"},
			{179,"fullname"},{181,"mobile_number"},{186,"receipt_number"},
				{ 119,"amount"}},

	{{170,"agenda"},{35,"msgType"},{ 11,"aid"},{75,"act_date"},{76,"act_time"},
			{179,"title"},{58,"description"},{176,"location_street"},{166,"location_city"},
			{ 149,"url"},{148,"event_host"},{181,"contact_number"}},

	{{170,"candidate_vocal"},{35,"msgType"},{ 11,"audio_id"},{75,"act_date"},
			{179,"title"},{58,"description"},{ 149,"url"}},
			
		{{170,"team"},{35,"msgType"},{ 55,"mentor_id"},{177,"team_id"}, {11,"member_id"}},
		
					{{170,"api_regid"}, {179, "api_name"},{ 55,"regid"}}	
					
	};

	static int getTableId(String tblName)
	{
		for (int i=0; i<page_tags.length; i++)
		{
			if (tblName.equals(page_tags[i][0][1])) return i;
		}
		return -1;
	}
	
	static String[] getUniqueKeyField(String tblName)
	{
		switch (getTableId(tblName))
		{
			case 0://"volunteer":
				return new String[]{ "citizen_id"};
				
			case 1://"commitment":
				return new String[]{"vid"};

			case 2://"visited":
				return new String[]{"act_date","fullname"};

			case 3://"fund_raised":
				return new String[]{"vid", "citizen_id","act_date",
					"fullname","receipt_number","amount"};

			case 4://"agenda" :
				return new String[]{"act_date","title"};

			case 5://"candidate_vocal" :
				return new String[]{"act_date","title"};
				
			case 6://"team" :
				return new String[]{"member_id"};
			default:
				break;
		}
		return null;
	}
	
	void setTagRefByName(String tblName)
	{
		int i=0;
		for (i=0; i< page_tags.length; i++)
		{
			String pageName=(String)page_tags[i][0][1];
		if (tblName.toUpperCase().equals(pageName.toUpperCase()))
				break;
		}
		tagNumber.clear();
		for (int k=0; k<page_tags[i].length; k++)
			tagNumber.put((String)page_tags[i][k][1], 
					       (Integer)page_tags[i][k][0]);
		tagNumber.put("memo",  58);
		tagNumber.put("table_name",  170);
	}
	
	void addDBData(RowStruct dbData, int idx)
	{
		if (tagData[idx].length() > 0) dbData.put(tagName[idx], tagData[idx]);
	}
	String getDBData(RowStruct dbData, String key)
	{
		return (String)dbData.get(key);
	}

	void saveDBData(RowStruct dbData)
	{
		
	}
	void readDBData(String criteria, RowStruct dbData)
	{
		
	}
	
	public Vector<RowStruct > getResponseTable()
	{
		return outTable;
	}

	public String convertRowToLine(RowStruct aRow)
	{
		String fixLine="";
		Iterator<String> itr=aRow.keySet().iterator();
		while (itr.hasNext())
		{
			String fld=itr.next();
			String value=(String)aRow.get(fld); 
			if (value.length() < 1) continue;
			fixLine += tagNumber.get(fld);
			fixLine += "=";
			fixLine += value;
			fixLine += "|";
		}
		return fixLine;		
	}
	
	public String composeFixText(RowStruct aRow, String tbName)
	{
		setTagRefByName(tbName);
		aRow.put("table_name", tbName);
		return convertRowToLine(aRow)+"170="+tbName+"|";
	}
	void processNewRegistration() 
	{ 
		RowStruct dbData=readRow;
		
		RowStruct criteria=new RowStruct();
		//to create criteria \' should used to quote the tag-data
		criteria.put("citizen_id", "='"+tagData[186]+"'"); 
		int iType=0;
		String sType=(String)readRow.get("msgType");
		iType=Integer.parseInt(sType);
		//need to add \' for where clause
		String command="select volunteer";
		if (readRow.get("vid")!=null || iType==1) command="update volunteer";
		DataProcessor.putInstruction(db_clientId, new DB_Instruction(command, dbData, criteria));
		
		Vector<RowStruct > resp=DataProcessor.clientReadResponse(db_clientId);
		RowStruct dataRow=resp.get(0);
		String none="NO DATA";
		if (dataRow != null) none=(String)dataRow.get(dataRow.keySet().iterator().next());
		
		if (none.indexOf("NO DATA") > -1 ) //new one
		{
			dbData.remove("vid");
			dbData.put("regid", tagData[55]);
		DataProcessor.putInstruction(db_clientId, new DB_Instruction("insert volunteer", dbData, null));
		resp=DataProcessor.clientReadResponse(db_clientId);
		dataRow=resp.get(0);
		none=(String)dataRow.get(dataRow.keySet().iterator().next());
		}
		if (none.indexOf("NO DATA") < 0)
		{ //this should have vid	
		//DataProcessor.putInstruction(db_clientId, new DB_Instruction("select volunteer", null, criteria));
		//resp=DataProcessor.clientReadResponse(db_clientId);			 
		}
		setTagRefByName("volunteer");
		for (int i=0; i<resp.size(); i++)
		{
			outMessageList.add(convertRowToLine(resp.get(i)));
		}
	}
	
	void processNewCommitment()
	{
	/*
	 * 	38 OrderQty (committed number of participate, visits or donations)
40 OrdType (participate, visit or donation)
59 TimeInForce (per week or per month)
	 */
		RowStruct dbData=new RowStruct();
		
		addDBData(dbData, 11);
		addDBData(dbData, 55);	
		addDBData(dbData, 40);
		addDBData(dbData, 38);
		addDBData(dbData, 59);
		DataProcessor.putInstruction(db_clientId, new DB_Instruction("insert commitment", dbData, null));
		Vector<RowStruct > resp=DataProcessor.clientReadResponse(db_clientId);
		setTagRefByName("commitment");
		for (int i=0; i<resp.size(); i++)
		{
			outMessageList.add(convertRowToLine(resp.get(i)));
		}
	}

		
void processNewInterview()
{

		RowStruct dbData=new RowStruct();
		
		addDBData(dbData, 11); //vid
		addDBData(dbData, 55);	//regid
		addDBData(dbData, 179); //fullname
		addDBData(dbData, 152); //head count
		addDBData(dbData, 176); //street
		addDBData(dbData, 177); //number
		addDBData(dbData, 181); //phone
		addDBData(dbData, 178); //rating
		addDBData(dbData, 166); //city
		addDBData(dbData, 64);  //next schedule date
		addDBData(dbData, 75);  //date
		DataProcessor.putInstruction(db_clientId, new DB_Instruction("insert visited", dbData, null));
		Vector<RowStruct > resp=DataProcessor.clientReadResponse(db_clientId);
		setTagRefByName("visited");
		for (int i=0; i<resp.size(); i++)
		{
			outMessageList.add(convertRowToLine(resp.get(i)));
		}
}

	void processResendRequest()
	{
		RowStruct dbData=new RowStruct();
		
		addDBData(dbData, 11); //vid
		addDBData(dbData, 55);	//regid
		
		addDBData(dbData, 75);  //date; from this date on
	}
	
	void processNewRaisedFund()
	{
		RowStruct dbData=new RowStruct();
		
		addDBData(dbData, 11); //vid
		addDBData(dbData, 55);	//regid
		addDBData(dbData, 179); //fullname
		//addDBData(dbData, 152); //head count
		addDBData(dbData, 176); //street
		addDBData(dbData, 177); //number
		addDBData(dbData, 181); //phone
		addDBData(dbData, 166); //city
		addDBData(dbData, 186);  //receipt
		addDBData(dbData, 75);  //date
		addDBData(dbData, 119);  //amount
		DataProcessor.putInstruction(db_clientId, new DB_Instruction("insert raised_fund", dbData, null));
		//thread will be block between put and read here
		Vector<RowStruct > resp=DataProcessor.clientReadResponse(db_clientId);
		setTagRefByName("raised_fund");
		for (int i=0; i<resp.size(); i++)
		{
			outMessageList.add(convertRowToLine(resp.get(i)));
		}		
	}
	
	void processNewVoiceRequest()
	{
		RowStruct dbData=new RowStruct();

		addDBData(dbData, 11); //vid
		addDBData(dbData, 55);	//regid
		addDBData(dbData, 75);  //date; from this date on
	}
	
	void processMessage()
	{
		tableName=tagData[170].toLowerCase();	
		
		switch(getTableId(tableName))
		{
		case 0://"volunteer":
			processNewRegistration();
			break;
		case 1://"commitment":
			processNewCommitment();
			break;
		case 2://"visited": //new Exec
			processNewInterview();
			break;
		case 3://"agenda":
			processResendRequest();
			break;
		case 4://"raised_fund":
			processNewRaisedFund();
			break;
		case 5://"candidate_volca":
			processNewVoiceRequest();
			break;
		case 6://"team":
			processNewVoiceRequest();
			break;
		case 7://"blue_pint":
			processNewVoiceRequest();
			break;
		default:
			break;
		//new: J (advertise) will be used to send new agenda back
			//tableName="event_list
		}
	}
	
	void setTagRefByNumber(String tblName)
	{
		int i=0;
		for (i=0; i< page_tags.length; i++)
		{
			String pageName=(String)page_tags[i][0][1];
		if (tblName.toUpperCase().equals(pageName.toUpperCase()))
				break;
		}
		//tagNumber.clear();
		for (int k=0; k<page_tags[i].length; k++)
		{
			tagName[(Integer)page_tags[i][k][0]]=(String)page_tags[i][k][1]; 
		}	
		tagName[58]="memo";
	}
	
	int[] parserFixMessage(String inText)
	{
		int[] tags=new int[200];
		tags[0]=0;
		for (int i=0; i<200; i++)
		{	tagData[i]=""; }
		int k=1; // first element reserved for 170 -> table_name
		if (inText.charAt(inText.length()-1) != '|') inText += '|';	
		int i0=0, iEq=0;
		for (int ix=0; ix < inText.length(); ix++)
		{
			if (inText.charAt(ix)=='=') iEq=ix;
			else if (inText.charAt(ix)== '|')
			{
				if (iEq > i0)
				{
				try {
					String tagNo=inText.substring(i0, iEq);
						i0=Integer.parseInt(tagNo);
						tagData[i0]=inText.substring(++iEq, ix);
						if (i0==170) tags[0]=170;
						else
							tags[k++]=i0;				
					} catch(NumberFormatException e){return null;}
				}
				i0=ix+1;
			}
		}
		return tags;	
	}

	public Vector<String> getDbResponseToClient()
	{
		return outMessageList;
	}
	
	public RowStruct lineToRow(String aLine)
	{
		int[] tags=parserFixMessage(aLine);
		if (tags == null ) return null;
		if (tags[0]==0) return null;
		setTagRefByNumber(tagData[170]);
		tagName[170]="table_name";
		RowStruct data=new RowStruct();
		int i=0; 
		do
		{
			data.put(tagName[tags[i]], tagData[tags[i]]);
		}while (tags[++i]> 0);
		return data;
	}
	
	public RowStruct parseFixMessage(String inText)
	{
		if (inText.charAt(inText.length()-1) != '|') inText += "|";
		return lineToRow(inText); //no data check here, checked in client
	}
	
	void startJob()
	{
		String inText=new String(inData, 0, dataLength, Charset.forName("UTF-8"));
		readRow=parseFixMessage(inText);
		processMessage();
		for (int i=0; i<outMessageList.size(); i++)
			System.out.println("My id "+db_clientId+" : "+outMessageList.get(i));
	}
	
	public static void main(String[] args)
	{
		DataProcessor aProcess=DataProcessor.getInstance();
		long stopAt=(new Date()).getTime()+60*10*1000;
		aProcess.setStopTime(stopAt);
		aProcess.start();
		MessageParser aP=new MessageParser(null, 0);
		//aP.startJob();
		final String testData="35=0|55=sdafabdjads1536|49=?|50=??|176=???621?1?|166=???|186=K120009376|181=0986056745|170=volunteer";
		
		//t1.setData(testData);
		Thread t1=new Thread(new Runnable(){
			public void run()
			{
				byte[] data=testData.getBytes();
				MessageParser aP=new MessageParser(data, data.length);
				aP.startJob();
			}
		});
		t1.start();
		try { t1.join();} catch (InterruptedException e){}
		final String testLine="35=1|186=K120009376|75=1955-11-24|170=volunteer|";
		Thread t2=new Thread(new Runnable(){
			public void run()
			{
				byte[] data=testLine.getBytes();
				MessageParser aP=new MessageParser(data, data.length);
				aP.startJob();
			}
		});
		t2.start();
		try {
			//t1.join();
			t2.join(); } catch (InterruptedException e){}
		
		aProcess.interrupt();
	}
}
