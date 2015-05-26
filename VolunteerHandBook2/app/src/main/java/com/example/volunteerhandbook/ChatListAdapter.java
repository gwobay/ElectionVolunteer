package com.example.volunteerhandbook;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
/**

 *
 */
public class ChatListAdapter extends BaseAdapter{

	
	private Context mContext;
	private ArrayList<ChatMessage> mChatMessages;

	public ChatListAdapter(Context context, ArrayList<ChatMessage> yourTexts) {
		super();
		this.mContext = context;
		this.mChatMessages = yourTexts;
	}
	
	public static class ChatMessage {

		String message;
		boolean isMine;

		public ChatMessage(String msg, boolean isMine) {
			this.message = msg;
			this.isMine = isMine;
		}

		public String getMessage() {
			return message;
		}
		public void setMessage(String msg) {
			message = msg;
		}
		public boolean isMine() {
			return isMine;
		}
		public void setMine(boolean yesOrNo) {
			isMine = yesOrNo;
		}	
	}


	@Override
	public int getCount() {
		return mChatMessages.size();
	}
	@Override
	public Object getItem(int position) {		
		return mChatMessages.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatMessage message = (ChatMessage) this.getItem(position);
		//if(message.isStatusChatMessage()) return convertView;
		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.talk_bubble, parent, false);
			holder.yourPhoto = (ImageView) convertView.findViewById(R.id.yourImage);
			holder.yourText = (TextView) convertView.findViewById(R.id.yourMessage);
			holder.myText = (TextView) convertView.findViewById(R.id.myMessage);
			holder.myPhoto = (ImageView) convertView.findViewById(R.id.myImage);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		
		//if (fromOld) return convertView;
		LayoutParams youTextLp = (LayoutParams) holder.yourText.getLayoutParams();
		LayoutParams myTextLp = (LayoutParams) holder.myText.getLayoutParams();
		if(message.isMine()) 
			{
				holder.yourPhoto.setVisibility(View.INVISIBLE);				
				holder.yourText.setText(" ");//R.color.textColor);
				holder.yourText.setVisibility(View.INVISIBLE);
				youTextLp.weight = 1;
				holder.myText.setText(message.getMessage());
				holder.myText.setBackgroundResource(R.drawable.speech_bubble_green);
				holder.myText.setTextColor(Color.WHITE);//R.color.textColor);
				holder.myText.setVisibility(View.VISIBLE);
				myTextLp.gravity = Gravity.RIGHT;
				holder.myPhoto.setVisibility(View.VISIBLE);				
			}
			else
			{
				holder.yourPhoto.setVisibility(View.VISIBLE);				
				holder.yourText.setText(message.getMessage());
				//holder.yourText.setBackgroundResource(R.drawable.speech_bubble_orange9);//speech_bubble_orange);
				holder.yourText.setTextColor(Color.BLUE);//R.color.textColor);
				holder.yourText.setVisibility(View.VISIBLE);
				youTextLp.gravity = Gravity.LEFT;
				holder.myText.setText(" ");
				holder.myText.setVisibility(View.INVISIBLE);
				myTextLp.weight = 1;
				holder.myPhoto.setVisibility(View.INVISIBLE);							
			}
			holder.yourText.setLayoutParams(youTextLp);
			holder.myText.setLayoutParams(myTextLp);

		Log.i("ADAPTER", "at "+position+" on "+convertView.hashCode());
	
		return convertView;
	}
	
	private static class ViewHolder
	{
		ImageView yourPhoto;
		TextView yourText;
		TextView myText;
		ImageView myPhoto;
	}

	@Override
	public long getItemId(int position) {
		//Unimplemented, because we aren't using Sqlite.
		return position;
	}

}
