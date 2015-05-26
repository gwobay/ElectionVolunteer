package com.example.volunteerhandbook;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AgendaListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<HashMap<String, String> > allData;

	public AgendaListAdapter() {
		// TODO Auto-generated constructor stub
		super();
	}

	Drawable iGo=null;
	Drawable iNew=null;
	Drawable iLove=null;
	Drawable iXX=null;
	
	public AgendaListAdapter(Context ct, ArrayList<HashMap<String, String> > s) {
		// TODO Auto-generated constructor stub
		super();
		mContext=ct;
		allData=s;
		iGo=ct.getResources().getDrawable(R.drawable.i_go);
		iNew=ct.getResources().getDrawable(R.drawable.new_one);
		iLove=ct.getResources().getDrawable(R.drawable.i_love);
		iXX=ct.getResources().getDrawable(R.drawable.ic_action_x);		
	}


	public void setDataSource(ArrayList<HashMap<String, String> > s) {
		// TODO Auto-generated constructor stub
		allData=s;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int iCount=allData.size();
		return iCount;
	}

	@Override
	public HashMap<String, String>  getItem(int i) {
		//public HashMap<String, String>  getItem(int i) {
				// TODO Auto-generated method stub
		return allData.get(i);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean areAllItemsEnabled ()
	{
		return true;
	}
	
	private class RowView
	{
		TextView event_date;
		TextView event_title;
		TextView location;
		ImageView action;
		TextView participants;		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		RowView vRow;
		if (convertView==null)
		{
			vRow=new RowView();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.agenda_row, parent, false);
			vRow.event_date=(TextView)convertView.findViewById(R.id.event_date);
			vRow.event_title=(TextView)convertView.findViewById(R.id.event_title);
			vRow.location=(TextView)convertView.findViewById(R.id.event_location);
			vRow.action=(ImageView)convertView.findViewById(R.id.event_status);
			vRow.participants=(TextView)convertView.findViewById(R.id.event_people);
			convertView.setTag(vRow);
		}
		else vRow=(RowView)convertView.getTag();
		
		HashMap<String, String> rowData=(HashMap<String, String>)allData.get(position);
		
		vRow.event_date.setText(rowData.get("event_date"));
		vRow.event_title.setText(rowData.get("event_title"));
		vRow.location.setText(rowData.get("event_location"));
		Drawable img=null;
		String status=rowData.get("status");
		if (status.equalsIgnoreCase("JOIN")) img=iGo;
		else if (status.equalsIgnoreCase("NEW")) img=iNew;
		else if (status.equalsIgnoreCase("DELETED")) img=iXX;
		else img=iLove;
		vRow.action.setBackground(img);
		
		vRow.participants.setText(rowData.get("participants"));
		if (status.equalsIgnoreCase("JOIN")) vRow.participants.setVisibility(View.VISIBLE);
		else vRow.participants.setVisibility(View.INVISIBLE);

		return convertView;
	}
	
}
