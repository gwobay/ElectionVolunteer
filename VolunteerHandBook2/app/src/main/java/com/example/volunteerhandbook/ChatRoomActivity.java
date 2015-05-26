package com.example.volunteerhandbook;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;

public class ChatRoomActivity extends Activity
        		implements ChatItemList.ItemListCallbacks
{

	public static final String MAIN_MENU_POSITION = "MM_POS";
	
	public static final int CheckOpenForum=0, OpenNewForum=1, CheckTeamChatRooms=2,
			OpenTeamChatRoom=3, ChatWithMember=4, InviteMember=9;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private ListView mainMenu;
    private String[] mMenuInDrawer ;
    
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static String mHost=null;
    public static int iPort;

    private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_forum);
		mHost=getResources().getString(R.string.data_server_host);
		String port=getResources().getString(R.string.data_server_port);
		iPort=Integer.parseInt(port);
        readBodyList();
        mDrawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        mTitle = getTitle();
        mMenuInDrawer = getResources().getStringArray(R.array.chat_room_array);
        if (savedInstanceState != null)
        {
        	//mainMenu=(ListView) findViewById(R.id.main_menu);//
        	//(MainMenuFragment)getFragmentManager().findFragmentByTag("MAINMENU");
        	mOptionListPage=(ChatItemList)getFragmentManager().findFragmentByTag("SUBMENU");
        }
       // if (mainMenu==null)
        	{
        	mainMenu = (ListView) findViewById(R.id.main_menu);
        	//(MainMenuFragment)getFragmentManager().findFragmentById(R.id.main_menu);
        // Set up the drawer.
        mainMenu.setAdapter(new ArrayAdapter<String>(
                getActionBar().getThemedContext(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                mMenuInDrawer));
        //setAdapter(new ArrayAdapter<String>(this, R.layout.main_menu, mMenuInDrawer));
        mainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        	}
    }

    ChatItemList mOptionListPage=null;
    
    public void selectItem(int main_menu_selection) {
    	switch (main_menu_selection)
    	{
    	case 1: 
    		//join forum 
    		break;
    	case 3:
    		//join team discuss room
    		break;
    	case 0:
    		//open forum list;
    	case 2:
    		//open chat room list;
    	case 4: 
    		//open body list to pick
        	mOptionListPage = (ChatItemList)getFragmentManager().findFragmentById(R.id.option_menu);
		    mTitle = getTitle();
		    mOptionListPage.setUp(this, main_menu_selection);		    
		    mDrawerLayout.closeDrawer(mainMenu);
		    mDrawerLayout.openDrawer(mOptionListPage.getListView());
    		break;
    	case 5:
    		//report abuse;
    		//name and when screen snap shot
    		break;
    	case 6:
    		pickBackgroundPicture();
    		break;
    	default:
    			break;
    	}

        /*// update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();*/
    }
    
    @Override
    public void onPostCreate(Bundle savedInstanceState)
    {
    	super.onPostCreate(savedInstanceState);
    	if (mOptionListPage!=null) 
    		getFragmentManager().beginTransaction().add(mOptionListPage, "SUBMENU").commit();
    	if (savedInstanceState!=null) return;
    	mDrawerLayout.openDrawer(Gravity.LEFT);   	
    }
 
    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState){
        if (savedInstanceState != null)
        {
        	ChatItemList mOptionListPage1=(ChatItemList)getFragmentManager().findFragmentByTag("SUBMENU");
        	if (mOptionListPage1 != null) mOptionListPage=mOptionListPage1;       	
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
    	if (mOptionListPage!=null) 
    		getFragmentManager().beginTransaction().add(mOptionListPage, "SUBMENU").commit();
    }

    public void onSectionAttached(int number) {
        mTitle=mMenuInDrawer[number];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void setChatRoomTitle(String ttl)
    {
    	getActionBar().setTitle(ttl);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mDrawerLayout.isDrawerOpen(mainMenu)) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void joinForum(ChatItemList.Forum aForm)
    {
    	//connect to server and open the chatroom
    }
    
    void joinChatRoom(String chatRoomNumber)
    {
    	//connect to server and open the chatroom
    }
    
    void chatWithBody(ChatItemList.Body aBody)
    {
       ChatRoomFragment bodyChat=new ChatRoomFragment();
       bodyChat.init(this, aBody.citizen_id);
       bodyChat.openNewChatWith(aBody.citizen_id);
       Fragment fgm=(Fragment)bodyChat;
       FragmentManager fragmentManager = getFragmentManager();
       fragmentManager.beginTransaction()
                .replace(R.id.container, fgm)
                .commit();
    	//connect to server and open the chatroom
    }
    public static class BodyInfo{
    	String citizen_id;
    	String name;//last_name+first_name
    	String badge_id;
    	byte[] photo;
    	public BodyInfo(String cid, String nm, String bgid, byte[] fto)
    	{
    		citizen_id=cid; name=nm; badge_id=bgid; photo=fto;
    	}
    }
    
    HashMap<String, BodyInfo> bodyList;
    public HashMap<String, BodyInfo> getBodyList()
    {
    	if (bodyList==null || bodyList.size() < 1) readBodyList();
    	return bodyList;
    }
    
    public void readBodyList()
    {
    	if (!DbProcessor.ifTableExists(this, "bodylist"))
    	{
    		ChatRoomFragment.createBodyListTable();
    	}
    	else
    	{
    		if (bodyList==null)
    			bodyList=new HashMap<String, BodyInfo>();
    		String sql = "select citizen_id, last_name+first_name as name, badge_id, photo from bodylist ;";
    		byte[] photo=null;
    		Cursor csr=null;
    		try {
    			csr=DbProcessor.getRecordsFromSql(this, sql);
    			if (csr == null)  return;
    			csr.moveToFirst();
    			while(!csr.isAfterLast()) 
    			{ 
    				String cid=csr.getString(0);
    				String name=csr.getString(1);
    				String badge_id=csr.getString(2);
    				photo=csr.getBlob(3);
    				bodyList.put(cid, new BodyInfo(cid, name, badge_id, photo));
    			}					 			
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		finally{
    			DbProcessor.closeDbByCursor(csr);
    		}
    	}
    }
    public void onItemListItemSelected(Object obj, int from_which)
    {
    	
    	switch (from_which)
    	{
    	case ChatItemList.FORUM_NUMBER:
    		joinForum((ChatItemList.Forum)obj);
    		break;
    	case ChatItemList.CHAT_ROOM_NUMBER:
    		joinChatRoom((String)obj); //obj is room number
    		break;
    	case ChatItemList.BODY_NUMBER:
    		chatWithBody((ChatItemList.Body)obj);
    		break;
    		default:
    			break;
    	}
    	return;
    }
    
    static final int REQ_PICK_IMAGE=5;
	public void pickBackgroundPicture()
	{
		Intent pickIntent = new Intent();
			pickIntent.setType("image/*");
			pickIntent.setAction(Intent.ACTION_GET_CONTENT);

			Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
			Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
			chooserIntent.putExtra
			(
			  Intent.EXTRA_INITIAL_INTENTS, 
			  new Intent[] { takePhotoIntent }
			);
			startActivityForResult(chooserIntent, REQ_PICK_IMAGE);
	}	

	public Bitmap getBitmapFromUri(Uri uri) throws IOException {
		return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
/*	    ParcelFileDescriptor parcelFileDescriptor =
	            getContentResolver().openFileDescriptor(uri, "r");
	    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
	    Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
	    parcelFileDescriptor.close();
	    return image;*/
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{			
	//String photo_file_path;
	Uri photo_uri=null;
		if (data != null) photo_uri=data.getData();

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_PICK_IMAGE && photo_uri != null)
		{			
			SharedPreferences ram=PreferenceManager.getDefaultSharedPreferences(this);
			ram.edit().putString("PERSONAL_ROOM_DECOR", photo_uri.toString());
			ram.edit().commit();
			try {
				ImageView bg=(ImageView)findViewById(R.id.container_view);
				bg.setImageBitmap(getBitmapFromUri(photo_uri));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
