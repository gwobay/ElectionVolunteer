package com.example.volunteerhandbook;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GetParticipantNumber extends DialogFragment {

	public GetParticipantNumber() {
		// TODO Auto-generated constructor stub
	}
	public static GetParticipantNumber newInstance(Activity av, HashMap<String, String> note) {
		GetParticipantNumber frag = new GetParticipantNumber();
		mActivity=av;
		mNote=note;
        /*Bundle args = new Bundle();
        args.putIntegerArrayList(key, value);("data", (Parcelable) note);
        frag.setArguments(args);*/
        return frag;
	}
	
	static Activity mActivity=null;
	static HashMap<String, String> mNote=null;
	
		@Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
			//@SuppressWarnings("unchecked")
			//final HashMap<String, String> note=(HashMap<String, String>)getArguments().getParcelable("data");
	        
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        
	     // Get the layout inflater
	        LayoutInflater inflater = mActivity.getLayoutInflater();
	        View v=inflater.inflate(R.layout.number_dialog, null);
	        final EditText txt=(EditText)(v.findViewById(R.id.number));
	        

	        // Inflate and set the layout for the dialog
	        // Pass null as the parent view because its going in the dialog layout
	        builder.setView(v)
	        		.setPositiveButton(R.string.confirm_to_send, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       mNote.put("participant", ((TextView)txt).getText().toString());
	                   }
	               })
	               .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // User cancelled the dialog
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();		
	}
}
