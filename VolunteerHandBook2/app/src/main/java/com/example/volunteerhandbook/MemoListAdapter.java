package com.example.volunteerhandbook;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class MemoListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<WorkMemoRecord.CurrentMemo> allData;

	public MemoListAdapter() {
		// TODO Auto-generated constructor stub
		super();
	}

	public MemoListAdapter(Context ct, ArrayList<WorkMemoRecord.CurrentMemo> s) {
		// TODO Auto-generated constructor stub
		super();
		mContext=ct;
		allData=s;
	}


	public void setDataSource(ArrayList<WorkMemoRecord.CurrentMemo> s) {
		// TODO Auto-generated constructor stub
		allData=s;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return allData.size();
	}

	@Override
	public Object getItem(int i) {
		// TODO Auto-generated method stub
		return allData.get(i);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private class RowView
	{
		TextView author;
		TextView outSideMessage;
		TextView myMessage;
		ImageView myPhoto;
		TextView dateV;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		RowView vRow;
		if (convertView==null)
		{
			vRow=new RowView();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.memo_row, parent, false);
			vRow.author=(TextView)convertView.findViewById(R.id.author);
			vRow.outSideMessage=(TextView)convertView.findViewById(R.id.yourMessage);
			vRow.myMessage=(TextView)convertView.findViewById(R.id.myMessage);
			vRow.myPhoto=(ImageView)convertView.findViewById(R.id.myImage);
			vRow.dateV=(TextView)convertView.findViewById(R.id.memo_date);
			convertView.setTag(vRow);
		}
		else vRow=(RowView)convertView.getTag();
		
		LayoutParams youTextLp = (LayoutParams) vRow.outSideMessage.getLayoutParams();
		LayoutParams myTextLp = (LayoutParams) vRow.myMessage.getLayoutParams();
		
		WorkMemoRecord.CurrentMemo rowData=allData.get(position);
		if (rowData.imAuthor())
		{
			vRow.author.setVisibility(View.GONE);
			vRow.outSideMessage.setText(" ");
			youTextLp.weight=1;
			vRow.myMessage.setText(rowData.subject);
			vRow.myPhoto.setVisibility(View.VISIBLE);
		}
		else
		{
			vRow.author.setText(rowData.author);
			vRow.outSideMessage.setText(rowData.subject);
			myTextLp.weight=1;
			vRow.myMessage.setText(" ");
			vRow.myPhoto.setVisibility(View.GONE);
		}
		vRow.dateV.setText(rowData.date);
		vRow.outSideMessage.setLayoutParams(youTextLp);
		vRow.myMessage.setLayoutParams(myTextLp);

		return convertView;
	}
	
}
