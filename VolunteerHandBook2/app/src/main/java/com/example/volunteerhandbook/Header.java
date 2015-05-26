package com.example.volunteerhandbook;

public class Header {
    /* update the main content by replacing fragments
    0.Candidate Blue-Print <  1.New Agenda   2.Candidate Photos   3.Candidate Broadcasting
   4.Candidate Current Position     5.Messaging Team-mates 6.Visited History   
    7. Fund Raise History, 8.My Calendar Reminder  9.My Commitment 
    10.Personal Information
   */

	public static final String[] PAGES={"Candidate Blue-Print",  "New Agenda", "Candidate Photos", "Candidate Broadcasting",
				"Candidate Current Position", "Messaging Team-mates", "Visited History",
				"Fund Raise History", "My Calendar Reminder", "My Commitment",  
		    "Personal Information"};
	
	public static final Object[][] PAGE_FORM_LIST={{"visited", R.layout.visit_form, R.layout.visit_list},
		{"fund_raised", R.layout.fund_raised_form, R.layout.fund_raised_form}
	};
			

    public static String[][] PAGE_TABLE={
       	{"Personal Information","volunteer"},{"My Calendar Reminder","plan_reminder"},
   		{"My Commitment","commitment"},{"Visited History","visited"},
    		{"Fund Raise History","fund_raised"},{"New Agenda","agenda"},
    		{"Candidate Photos","candidate_photos"}, {"Candidate Blue-Print", "blue_print"},
    		{"Candidate Broadcasting","candidate_vocal"},{"Messaging Team-mates","team"}
    };	
    			
}
