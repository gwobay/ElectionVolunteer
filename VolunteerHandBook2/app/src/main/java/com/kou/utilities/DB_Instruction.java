package com.kou.utilities;

import java.util.HashMap;


public class DB_Instruction {

		String command; //can be select tableName or whole sql : select ... join ...
		RowStruct data;
	 	RowStruct criteria;
	 	public DB_Instruction (String cmd, RowStruct d, RowStruct c)
	 	{
	 		command=cmd;
	 		data=d;
	 		criteria=c;
	 	}


}
