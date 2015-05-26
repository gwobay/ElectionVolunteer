package com.example.volunteerhandbook;

import android.os.Environment;

public class DataStorage {

	public DataStorage() {
		// TODO Auto-generated constructor stub
	}

	public static String[][] table_of_page={
        /* update the main content by replacing fragments
        0.Candidate Blue-Print <  1.New Agenda   2.Candidate Photos   3.Candidate Broadcasting
       4.Candidate Current Position     5.Messaging Team-mates 6.Visited History   
        7. Fund Raise History, 8.My Calendar Reminder  9.My Commitment 
        10.Personal Information
       */
		{"Candidate Blue-Print","blue_print"},{"New Agenda","agenda"},
		{"Candidate Photos","candidate_photos"},
		{"Candidate Broadcasting","candidate_vocal"},
		{"Candidate Location",""}, 
		 {"Messaging Team-mates","team"},{"Visiting Cases","visited"},
 		{"Fund-Raise Cases","fund_raised"},{"My Calendar Reminder","plan_reminder"},
   		{"My Commitment","commitment"},
       	{"Personal Information","profile"}
    };	
	
	public static String getTableName(String fixLine)
	{
		int i0=fixLine.indexOf("170=");
		if (i0 < 0) return null;
		int iB=fixLine.indexOf("|", i0);
		if (iB < 0) iB=fixLine.length();
		return fixLine.substring(i0+4, iB);
	}
	
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	
}
