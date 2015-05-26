package com.example.volunteerhandbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class ShowProgressDialog extends DialogFragment {

	Context mContext;
	GifViewer mViewImage;
	
	public ShowProgressDialog() {
		super();
		// TODO Auto-generated constructor stub		
	}
	
	public void setContext(Context context)
	{
		mContext=context;
	}
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
	AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
	//LayoutInflater inflater = getActivity().getLayoutInflater();
	mViewImage=new GifViewer(mContext,R.drawable.progress_circle);
	builder.setView(mViewImage);//inflater.inflate(R.layout.waiting_response, null));
	mViewImage.invalidate();
	return builder.create();
	}
	
	//public void show(FragmentManager manager, String tag)
	{
		//super.show(manager, tag);
		
	}
}
