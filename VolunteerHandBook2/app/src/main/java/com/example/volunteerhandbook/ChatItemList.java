package com.example.volunteerhandbook;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.example.volunteerhandbook.ChatRoomActivity.BodyInfo;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ChatItemList extends ListFragment 
		implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String MY_LAST_SELECTION = "LAST_SELECTION";
	public static final int FORUM_NUMBER = 1;
	public static final int BODY_NUMBER = 9;
	public static final int CHAT_ROOM_NUMBER = 2;
	
    public static interface ItemListCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        public void onItemListItemSelected(Object obj, int from_which);
    }
    
    ItemListCallbacks listener;
    
   public static class Forum{
    	String anchor;
    	String subject;
    	String abstract_line;
    	public Forum(String a, String s, String l)
    	{
    		anchor=a; subject=s; abstract_line=l;
    	}
    }

   public static class RoomTitle{
   	String room_number;
   	String openBy;
	public RoomTitle(String a, String s)
	{
		room_number=a; openBy=s;
	}
   }
   
   public static class Body{
		byte[] photo;
		String name;
		String citizen_id;
	}
	
	class BodyListAdapter extends ArrayAdapter<Body>
	{
		Context mOwner;
		int resourceId;
		
		public BodyListAdapter(Context context, int resource, ArrayList<Body> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
			mOwner=context;
			resourceId=resource;
		}
		class ViewHolder{
			ImageView photo;
			TextView name;
		}
		@Override
		public View getView(int pos, View convertV, ViewGroup contV)
		{
			ViewHolder vHolder;
			if (convertV==null)
			{
				convertV=LayoutInflater.from(mOwner).inflate(resourceId, contV, false);
				vHolder=new ViewHolder();
				vHolder.photo=(ImageView)convertV.findViewById(R.id.body_photo);
				vHolder.name=(TextView)convertV.findViewById(R.id.body_name);
				convertV.setTag(vHolder);
			}
			else vHolder=(ViewHolder)convertV.getTag();
			Body aBody=(Body)getItem(pos);
			vHolder.photo.setImageBitmap(BitmapFactory.decodeByteArray(aBody.photo, 0, aBody.photo.length));
			vHolder.name.setText(aBody.name);
			return convertV;
		}		
	}

	static final String[] forumColumns={"anchor", "subject", "abstract_line"};
	static final int[] forumFieldId={R.id.anchor, R.id.forum_subject, R.id.forum_abstract};
	static final String[] chatRoomColumns={"room_number", "open_by"};
	static final int[] roomFieldId={R.id.room_number, R.id.open_by};
	      

    static ArrayList<Body> myBodyList=new ArrayList<Body>();
    static ArrayList<Forum> myForumList=new ArrayList<Forum>();
    static ArrayList<RoomTitle> myRoomList=new ArrayList<RoomTitle>();
    static ArrayList<HashMap<String, Object> > myDataList=new ArrayList<HashMap<String, Object> >();
    Activity mActivity;
    int myLastSelection=0;
	int mMainMenuPosition=-1;
	boolean mFromSavedInstanceState=false;
	
	static class ShowPhotoBinder implements SimpleAdapter.ViewBinder
	{
		public boolean setViewValue(View v, Object data, String s)
		{
			int vid=v.getId();
			if (vid != R.id.body_photo) return false;
			Body aBody=(Body)data;
			((ImageView)v).setImageBitmap(BitmapFactory.decodeByteArray(aBody.photo, 0, aBody.photo.length));
							
			return true;			
		}
	}
	
	void findAdapter(Bundle savedInstanceState){
        if (mMainMenuPosition<0)
        {
        	if (getArguments()==null) return;
        mMainMenuPosition=getArguments().getInt(ChatRoomActivity.MAIN_MENU_POSITION);
        }
        // Select either the default item (0) or the last selected item.
       ShowPhotoBinder theBinder=null;
       int row_layout_id=0;
       //ArrayList<?> data=null;
       String[] showColumns=null;
       int[] fldsId=null;
        switch (mMainMenuPosition)
        {
        case ChatRoomActivity.CheckOpenForum:
        	if (myDataList.size() <1) updateForumList();
        	row_layout_id=R.layout.forum_row;
             //data = (ArrayList<?>)myForumList;
             showColumns=new String[]{"anchor", "subject", "abstract_line"};
             fldsId=new int[]{R.id.anchor, R.id.forum_subject, R.id.forum_abstract};
        	break;
        case ChatRoomActivity.CheckTeamChatRooms:
        	if (myDataList.size() <1) updateChatRoomList();
        	row_layout_id=R.layout.room_row;
        	//data = (ArrayList<?>) myRoomList;
            showColumns=new String[]{"room_number", "open_by"};
            fldsId=new int[]{R.id.room_number, R.id.open_by};

        	break;
        case ChatRoomActivity.ChatWithMember:
        case ChatRoomActivity.InviteMember:
        	if (myDataList.size() <1) updateMyBodyList();
        	row_layout_id=R.layout.body_row;
        	//data = (ArrayList<?>) myBodyList;
        	showColumns=new String[]{"photo", "name"};
            fldsId=new int[]{R.id.body_photo, R.id.body_name};
            theBinder=new ShowPhotoBinder();
        	break;
        }
        selectItem(mMainMenuPosition);
	    //if (data.isEmpty()) return;
	    ListAdapter adp=new SimpleAdapter(mActivity, 
	    									myDataList,//data, 
	    									row_layout_id, 
	    									showColumns, 
	    									fldsId); 
	    if (theBinder != null)
	    ((SimpleAdapter)adp).setViewBinder(theBinder);
	   // v.setAdapter(adp);
	    myAdapter=(SimpleAdapter)adp;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //add code here to check the preference when was the last time to update the body list
        // and the room list

        if (savedInstanceState != null) {
        	myLastSelection = 
            		savedInstanceState.getInt(MY_LAST_SELECTION);
            mFromSavedInstanceState = true;
        }       
    }
    
    private DrawerLayout mainLayout;
    private ListView mSublistListView;
    private View mFragmentContainerView;
    ListAdapter myAdapter=null;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mSublistListView = (ListView) inflater.inflate(R.layout.forum_room_list, container, false);
    	myAdapter=new ArrayAdapter<String>(
                getActivity().getActionBar().getThemedContext(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                new String[]{"1","2","3"});
    	//findAdapter(savedInstanceState);
    	mSublistListView.setAdapter(myAdapter);
    	mSublistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        //mSublistListView.setItemChecked(myLastSelection, true);
        return mSublistListView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
    	super.onActivityCreated(savedInstanceState);

    	//setListAdapter(myAdapter);       
    }
    
    public void setItemListCallBack(ItemListCallbacks al)
    {
    	listener=al;
    }
    private void selectItem(int position) {
        mMainMenuPosition = position;
        if (mSublistListView != null) {
            mSublistListView.setItemChecked(position, true);
        }
        if (mainLayout != null) {
            mainLayout.closeDrawer(mFragmentContainerView);
        }
        if (listener != null) {
        	Object obj=null;
       
            switch (mMainMenuPosition)
            {
            case ChatRoomActivity.CheckOpenForum:
    	        	obj=myForumList.get(position);
    	        	break;
            case ChatRoomActivity.CheckTeamChatRooms:
            	obj=myRoomList.get(position);
            	break;
            case ChatRoomActivity.ChatWithMember:
            case ChatRoomActivity.InviteMember:
            	obj=myBodyList.get(position);
            }
        	listener.onItemListItemSelected(obj, mMainMenuPosition);
        }
    }
    
    void updateForumList()
    {
    	myDataList.clear();
    	//TODO start async task	
    	for (int i=0; i<10; i++)
    	{
    		HashMap<String, Object> aRow=new HashMap<String, Object> ();
    		aRow.put("anchor", "anchor"+i); aRow.put("subject", "subject"+i);
    		aRow.put("abstract_line",  "abstract"+i);
    		myDataList.add(aRow);
    	}
    }
    
    void updateChatRoomList()
    {
    	myDataList.clear();
    	//TODO 
    	for (int i=0; i<10; i++)
    	{
    		HashMap<String, Object> aRow=new HashMap<String, Object> ();
    		aRow.put("room_number", "Room"+i); aRow.put("open_by", "Opened By "+i);
    		myDataList.add(aRow);
    	} 	
    }
    void updateMyBodyList()
    {
    	myDataList.clear();
        HashMap<String, BodyInfo> bodyList=((ChatRoomActivity)getActivity()).getBodyList();
        if (bodyList == null) return;
        Iterator<BodyInfo> itr=bodyList.values().iterator();
    	while (itr.hasNext())
    	{
    		BodyInfo aBody=itr.next();   		
    		HashMap<String, Object> aRow=new HashMap<String, Object>();
    		aRow.put("citizen_id", aBody.citizen_id); aRow.put("name", aBody.name);
    		aRow.put("photo",  aBody.photo);
    		myDataList.add(aRow);
    	} 	
    }


    public void setUp(Activity ctx, int for_which_menu)
    {
    	mActivity=ctx;
    	mMainMenuPosition=for_which_menu;  
        switch (mMainMenuPosition)
        {
        case ChatRoomActivity.CheckOpenForum:
	        	updateForumList();
	        	break;
        /*case ChatRoomActivity.OpenNewForum:
        	break;*/
        case ChatRoomActivity.CheckTeamChatRooms:
        	updateChatRoomList();
        	break;
        /*case ChatRoomActivity.OpenTeamChatRoom:
        	break;*/
        case ChatRoomActivity.ChatWithMember:
        case ChatRoomActivity.InviteMember:
        	updateMyBodyList();
        	break;
        }
        findAdapter(null);
        mSublistListView.setAdapter(myAdapter);
    }
	public ChatItemList() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	

}
