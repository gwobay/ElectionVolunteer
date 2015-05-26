package com.example.volunteerhandbook;


import java.text.DecimalFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;



public class DatePickerFragment extends DialogFragment
						implements DatePickerDialog.OnDateSetListener 
{
	static TextView mDateView;
	//static Activity mActivity=null;

	public DatePickerFragment()
	{
		super();
	}
	
	public void forView(TextView v)
	{
		mDateView=v;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(),
				this, my, mm, md);
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) 
	{
		DecimalFormat dF=new DecimalFormat("00");
		String dateData=dF.format(year)+"-"+dF.format(month+1)+"-"+dF.format(day);	
		mDateView.setText(dateData);
	}
	int my;
	int mm;
	int md;
	public void setDay0(int y, int m, int d)
	{
		my=y; mm=m; md=d;
	}
	public void pickDate(View view, Activity aK)
	{	
		mDateView=(TextView)view;
		FragmentManager manager=aK.getFragmentManager();
		show(manager,  "datePicker");		
	}	

}
