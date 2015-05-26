package com.example.volunteerhandbook;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import com.kou.utilities.DaemonCallSocket;
import com.kou.utilities.FixDataBundle;

import android.app.Activity;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.volunteerhandbook.ChatListAdapter.ChatMessage;

/**
 *
 */
public class ChatRoomFragment extends ListFragment {

	static String chatHostName=null;
	static String chatHostPort=null;
	ArrayList<ChatMessage> mRecorder;
	ChatListAdapter adapter;
	EditText text;
	HashMap<String, Bitmap> bodyPhotos;
	static Random rand = new Random();	
	static String sender;
	String mCitizenId=null;
	Activity mActivity=null;
	String firstFixLine=null;//for first fix line to open or to accept
	String currentRoomNumber;
	static String myName;
	String mHost;
	int iPort;

	ArrayList<String> chatRecords;
	DaemonCallSocket chatChannel=null;
	public ChatRoomFragment(){
		super();
		//init();
	}
	
	void selectPhoto(String cid)
	{
		String sql = "select citizen_id, photo from bodylist where citizen_id="+cid+";";
		byte[] photo=null;
		Cursor csr=null;
		try {
			csr=DbProcessor.getRecordsFromSql(mActivity, sql);
				if (csr == null)  return;
				csr.moveToFirst();
				while(!csr.isAfterLast()) 
				{ 
					photo=csr.getBlob(1);
					//if (photo != null) return photo;
/*					if (photo!=null)
					{
						int iSize=(int)photo.length();
						//Integer.parseInt((String)(aRow.get(blobField+"_size")));				
						byte[] photoBytes=new byte[(int) iSize];
						DataInputStream inStream=new DataInputStream(photo.getBinaryStream(1, iSize));
						inStream.readFully(photoBytes);
						return photoBytes;
					}*/
				}					 			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				photo=null;
			}
		finally{
			DbProcessor.closeDbByCursor(csr);
		}
		if (photo==null) return;
		else {
		if (bodyPhotos == null)
			bodyPhotos=new HashMap<String, Bitmap>();
		bodyPhotos.put(cid,  BitmapFactory.decodeByteArray(photo, 0, photo.length));
		}
	}

	static void insertPhoto(String bodyId, byte[] photo){
		//String sql="update bodylist set photo_size=?, photo=? where citizen_id=?";
		ContentValues aC=new ContentValues();
		aC.put("photo_size", photo.length);
		aC.put("photo", photo);
		String sWhere=" where citizen_id='"+bodyId+"' ";
		DbProcessor.updateTable(MainActivity.globalContext, "bodylist", aC, sWhere, null);
	}
	
	static void saveNewBody(FixDataBundle aFDB){
		String bodyId=aFDB.getCommand(286);
		if (bodyId==null) return;
		String photo=aFDB.getCommand(35);			
		int iC=DbProcessor.selectCount(MainActivity.globalContext, "bodylist", 
				" where citizen_id='"+bodyId+"'"); 
		if (iC > 0) {
			if (photo.equalsIgnoreCase("photo"))
				insertPhoto(bodyId, aFDB.getStream());
					return;
		}
		String sql="insert into bodylist ";
		sql += "(citizen_id, last_name, first_name, badge_id, photo_size) ";
		sql += "values ('"+bodyId+"', '"+aFDB.getCommand(49)+"', '";
		sql += aFDB.getCommand(50)+"', '"+aFDB.getCommand(112)+"', 0);";
		DbProcessor.insertTable(MainActivity.globalContext, sql);		
	}
	
	Vector<byte[]> resendQ=null;
	public static class BodyDataListener implements DaemonCallSocket.Listener
	{
		@Override
		public void readyToRead(Vector<byte[]> outQ) {
			// TODO Auto-generated method stub
			if (outQ.size() < 1) return;
			for (int i=0; i<outQ.size(); i++)
			{
				FixDataBundle aFDB=new FixDataBundle(outQ.get(i));
				saveNewBody(aFDB);
			}
		}

		@Override
		public void onServerQuit(Vector<byte[]> outQ) {
			// TODO Auto-generated method stub
			if (outQ.size() > 0)
			{
				/*if (resendQ==null)
					resendQ=new Vector<byte[]>();
				Log.w("DAEMCKT", "ckt closed with "+outQ.size()+" unprocessed outbound data");
				for (int i=0; i<outQ.size(); i++)
				{
					resendQ.add(outQ.get(i));
				}
				outQ.clear();*/
			}
		}		
	}
	
	static long lastCheckBodyListTime=0;
	public static void getBodyDataFromServer(String fixLine, int processTimeInMilli)
	{
		SharedPreferences sharedPref = MainActivity.globalContext.getSharedPreferences("data_last_check_time", Context.MODE_PRIVATE);
		SharedPreferences.Editor writer=sharedPref.edit();
		long iLastTime=sharedPref.getLong("BODY_LIST", -1);
		if (iLastTime != -1 && iLastTime - (new Date().getTime()) < 2*60*60*1000)
			return;
		writer.putLong("BODY_LIST", new Date().getTime());
		writer.commit();
		final String host=ChatRoomActivity.mHost;
		final int port=ChatRoomActivity.iPort;
		final String command=fixLine;
		final int elapseTime=processTimeInMilli;
		new Thread(new Runnable(){
			public void run(){
				try {
					DaemonCallSocket aDaem = new DaemonCallSocket(host, port);
					aDaem.setElapseTime(elapseTime);
					aDaem.putInstruction(command);
					aDaem.setDataReadyListener(new BodyDataListener());
					aDaem.run();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}}).start();
	}
	
	public static void createBodyListTable()
	{
		String sql="create table if not exists bodylist";
		sql += "( citizen_id CHAR(12) unique not null,";
		sql += " last_name CHAR(10)  not null,";
		sql += " first_name CHAR(20)  not null,";
		sql += " badge_id CHAR(12) ,";
		sql += " photo_size int, ";
		sql += " photo blob );";
		
		final String s1=sql;
		new Thread(new Runnable(){
			public void run(){
				DbProcessor.createTable(MainActivity.globalContext, s1);				
			}
		}).start();
		final String command="35=get|170=bodylist|186="+MainActivity.getCitizenId()+"|";
		getBodyDataFromServer(command, 3000);
	}
	
	void getBodyIcon()
	{
		String sql="select citizen_id from bodylist where photo_size=0;";
		Cursor csr=DbProcessor.getRecordsFromSql(mActivity, sql);
		if (csr == null) return;
		csr.moveToFirst();
		while (!csr.isAfterLast())
		{
			String id=csr.getString(0);
			String command="35=photo|170=bodylist|286="+id+"|";
			getBodyDataFromServer(command, 30000);
		}
		DbProcessor.closeDbByCursor(csr);
	}
	public void init(Activity av,String citizenId)
	{
		mActivity=av;
		mCitizenId=citizenId;
		if (!DbProcessor.ifTableExists(getActivity(), "bodylist"))
			createBodyListTable();
		else
			{
			String command="35=get|170=bodylist|186="+mCitizenId+"|";
			getBodyDataFromServer(command, 3000);
			getBodyIcon();
			}
		chatRecords=new ArrayList<String>();
		SharedPreferences mem=mActivity.getSharedPreferences(MainActivity.getFileHeader()+"profile",Context.MODE_PRIVATE );
		myName=mem.getString("last_name", "--");
		if (myName.charAt(0) != '-')
		{
			String name=mem.getString("first_name", "--");
			if (name.charAt(0) != '-') myName += name;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRetainInstance(true);

		if (savedInstanceState != null)
		restoreData(savedInstanceState, chatRecords);
		
    	String key=getResources().getString(R.string.fix_line_key);
    	String fixLine=getArguments().getString(key);
    	if (fixLine != null)
    	{
    		acceptInvitation(fixLine);
    	}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
	
    	View mRootView=inflater.inflate(R.layout.chat_main, container, false);
		text = (EditText) mRootView.findViewById(R.id.text);		
		sender = null;//Utility.sender[rand.nextInt( Utility.sender.length-1)];
		getActivity().setTitle(sender);
		mRecorder = new ArrayList<ChatMessage>();
		adapter = new ChatListAdapter(getActivity(), mRecorder);
		setListAdapter(adapter);		
    	return mRootView;
    }
    
    void postRoomTitle(FixDataBundle aFDB)
    {
    	((ChatRoomActivity)getActivity()).setChatRoomTitle(aFDB.getCommand(34));
    }
	class ChatMessageListener implements DaemonCallSocket.Listener
	{
		@Override
		public void readyToRead(Vector<byte[]> outQ) {
			// TODO Auto-generated method stub
			if (outQ.size() < 1) return;
			for (int i=0; i<outQ.size(); i++)
			{
				FixDataBundle aFDB=new FixDataBundle(outQ.get(i));
				if (aFDB.getCommand(35).equalsIgnoreCase("new"))
					postRoomTitle(aFDB); //70 room number, 79=title(anchor)
				else
				showChatMessage(aFDB);
			}
		}
		@Override
		public void onServerQuit(Vector<byte[]> outQ) {
			// TODO Auto-generated method stub
			if (outQ.size() > 0)
			{
				outQ.clear();
			}
		}		
	}

	static public boolean goodInvitation(String fixLine)
	{
		int i49=fixLine.indexOf("|186=");//sender id
		int i55=fixLine.indexOf("|55=");//symbol name to shown    		
		int i56=fixLine.indexOf("|286"); //this should be my id
		int i34=fixLine.indexOf("|34"); //room number
		int i100=fixLine.indexOf("|100=");//host ip addr
		int i101=fixLine.indexOf("|101=");//host ip port
		if (i49 < 0 || i56<0 || i34<0 || i100<0 || i101 < 0)
			return false;
		else return true;
	}
	
	void acceptInvitation(String fixLine)
	{
		firstFixLine=fixLine;
		//find the inviter
		if (!goodInvitation(fixLine))
			return;	
		HashMap<String, String> info=new HashMap<String, String>();
		String[] pairs=fixLine.split("\\|");
		for (int i=0; i<pairs.length; i++)
		{
			String[] tag_name=pairs[i].split("=");
			info.put(tag_name[0], tag_name[1]);			
		}
		String host=info.get(""+100);
		if (host == null) return;
		String port=info.get(""+101);
		if (port == null) return;
		currentRoomNumber=info.get(""+34);
		if (currentRoomNumber== null) return;
		String rLine="35=accept|170=chatroom|186="+mCitizenId+"|34="+currentRoomNumber+"|"; 
		rLine += "55="+myName+"|50=Thanks for your invitation!";
		openChatChannel(rLine, host, Integer.parseInt(port), 10*60*1000);       		
	}

	public void openNewChatWith(String citizen_id)
	{
		String nLine="35=invite|170=chatroom|186="+mCitizenId+"|";
		if (currentRoomNumber != null) nLine += "34="+currentRoomNumber+"|"; 
		nLine += "55="+myName+"|286="+citizen_id+"|";
		openChatChannel(nLine, mHost, iPort, 10*60*1000);
	}
	
	void openChatChannel(String fixLn, String hostnm, 
									int iPort, int processTimeInMilli)
	{
		final String fixLine=fixLn;
		final int port=iPort;
		final String host=hostnm;
		final int elapseTime=processTimeInMilli;
		new Thread(new Runnable(){
			public void run(){
				try {
				chatChannel = new DaemonCallSocket(host, port);
				//line will shut down after mute for 10 mins
				chatChannel.setElapseTime(elapseTime);
				chatChannel.putInstruction(fixLine);
				chatChannel.setDataReadyListener(new ChatMessageListener());
				chatChannel.run();
				} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
			// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}}).start();
	}
	
	public void sendMessage(View v)
	{
		String newMessage = text.getText().toString().trim(); 
		if(newMessage.length() == 0) return;
		showChatMessage(newMessage);
		String fixLine="35=msg|170=chatroom|34="+currentRoomNumber+"|186="+mCitizenId+"|50="+newMessage+"|";
		if (chatChannel!= null)
			chatChannel.putInstruction(fixLine);
		text.setText("");
	}
	
	void showChatMessage(String msg)
	{
		chatRecords.add(msg);
		//this is from mine
		addNewMessage(new ChatMessage(msg, true));
	}
	void showChatMessage(FixDataBundle aFDB)
	{
		chatRecords.add(aFDB.getFixLine());
		//this is from outside
		String msg=aFDB.getCommand(50);
		if (msg != null)
		addNewMessage(new ChatMessage(msg, false));
	}
	void addNewMessage(ChatMessage m)
	{
		mRecorder.add(m);
		adapter.notifyDataSetChanged();
		getListView().setSelection(mRecorder.size()-1);
	}
	
	void addTestMessage(ArrayList<ChatMessage> recorder)
	{
		recorder.add(new ChatMessage("Hello", false));
		recorder.add(new ChatMessage("Hi!", true));
		recorder.add(new ChatMessage("Wassup??", false));
		recorder.add(new ChatMessage("nothing much, working on speech bubbles.", true));
		recorder.add(new ChatMessage("you say!", true));
		recorder.add(new ChatMessage("oh thats great. how are you showing them", false));
		
		String longLine="有意參選台北市長的台大醫師柯文哲，下月將與立委姚文智PK民調。";
		longLine += "柯文哲昨宣布將啟動「青年海選計畫」，徵求發言人與隨行祕書。因選戰繁忙，";
		longLine += "徵選時還要測驗體能，薪資則優於國科會助理標準，大學畢業約32K、碩士36K起跳。 ";
		longLine += "柯文哲宣布將啟動「青年海選計畫」，徵求發言人與隨行祕書。";
		recorder.add(new ChatMessage(longLine, true));
		recorder.add(new ChatMessage("mmm, well, using 9 patches png to show them.", true));
		
		longLine="過去所有職務他一肩扛，現要徵求男女發言人各1名、最多3名隨行祕書與文宣、";
		longLine += "新聞、活動等職務，至於確切徵才人數將依募款多寡而定。";
		longLine += "應徵發言人者面試時需準備3分鐘發言，而隨行祕書則需貼身處理各事物";
		recorder.add(new ChatMessage(longLine, false));		
	}
	void restoreData(Bundle inState, ArrayList<String> recorder)
	{
		String[] chats=inState.getStringArray("CHAT_RECORDS");
		if (recorder==null) recorder=new ArrayList<String>();
		for (int i=0; i<chats.length; i++)
		{
			recorder.add(chats[i]);
		}
	}
	@Override
	public void onSaveInstanceState (Bundle outState)
	{
		outState.putStringArray("CHAT_RECORDS", (String[])chatRecords.toArray());
		super.onSaveInstanceState(outState);
	}
	private class SendMessage extends AsyncTask<Void, String, String>
	{
		@Override
		protected String doInBackground(Void... params) {
			try {
				Thread.sleep(2000); //simulate a network call
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.publishProgress(String.format("%s started writing", sender));
			try {
				Thread.sleep(2000); //simulate a network call
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.publishProgress(String.format("%s has entered text", sender));
			try {
				Thread.sleep(3000);//simulate a network call
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
						
			return "Tester";//Utility.messages[rand.nextInt(Utility.messages.length-1)];
						
		}
		@Override
		public void onProgressUpdate(String... v) {
			
/*			if(mRecorder.get(mRecorder.size()-1).isStatusMessage)//check wether we have already added a status message
			{
				mRecorder.get(mRecorder.size()-1).setMessage(v[0]); //update the status for that
				adapter.notifyDataSetChanged(); 
				getListView().setSelection(mRecorder.size()-1);
			}
			else{
				addNewMessage(new Message(true,v[0])); //add new message, if there is no existing status message
			}*/
		}
		@Override
		protected void onPostExecute(String text) {
			//if(mRecorder.get(mRecorder.size()-1).isStatusMessage)//check if there is any status message, now remove it.
			//{
				//mRecorder.remove(mRecorder.size()-1);
			//}
			
			addNewMessage(new ChatMessage(text, false)); // add the orignal message from server.
		}
	}

}