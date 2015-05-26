package com.example.volunteerhandbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class GetParticipantNumberDialog extends Activity {

	public GetParticipantNumberDialog() {
		// TODO Auto-generated constructor stub
	}
	EditText dataView=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.number_dialog);
		dataView = (EditText)findViewById(R.id.number);
		
	}

	public View CreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View rootView = inflater.inflate(R.layout.number_dialog, container, false);
	         return rootView;
	    }
	    

	public void setHome(View v)
	{
		Intent retInt=getIntent();
		retInt.putExtra("participant", " ");
		this.setResult(AgendaActivity.REQ_GET_PARTICIPANTS, retInt);
		
    	finish();
	}
	
	public void setData(View v)
	{
		Intent retInt=getIntent();
		retInt.putExtra("participant", dataView.getText().toString());
		this.setResult(AgendaActivity.REQ_GET_PARTICIPANTS, retInt);
		finish();
	}
}
