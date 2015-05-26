package com.kou.utilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class RowStruct extends HashMap<String, Object>{


	public HashMap<String, String> toHashMapString()
	{
		HashMap<String, String> aMap=new HashMap<String, String>();
		Set<String> keys=this.keySet();
		Iterator<String> itr=keys.iterator();
		while (itr.hasNext())
		{
			String key= itr.next();
			aMap.put(key, (String)this.get(key));
		}
		return aMap;
	}
}
