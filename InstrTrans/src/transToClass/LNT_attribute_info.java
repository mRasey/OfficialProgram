package transToClass;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import op.globalArguments;

public class LNT_attribute_info {
	    u2 attribute_name_index = new u2((short) 6);
	    u4 attribute_length = new u4();  //后面这些东西的总长
	    u2 table_count = new u2();
	    u2 [] table;
	    int method_id = 0;
	    HashMap<Integer, Integer> LineNumTable;

	    public LNT_attribute_info(int method_id) {
	    	this.method_id = method_id;
	    	LineNumTable = globalArguments.lineNumberTables.get(method_id);
	    	table_count.set((short) LineNumTable.size());
	    	table = new u2[LineNumTable.size() * 2];
	    	setTable();
	    	attribute_length.set(2+ LineNumTable.size()*4);
	    	
	    }

	    @Override
	    public String toString() {  
	    	int i = 0;
	    	String table_str = "";
	    	for(i=0;i<table.length;i++){
	    		table_str+=table[i].toString();
	    	}
	        return attribute_name_index.toString()
	                + attribute_length.toString()
	                + table_count.toString()
	        		+ table_str;
	    }
	    
	    public void setTable(){
	    	int i = 0;
	    	int LineNumber = 0 , byteCodeNumber = 0;
	    	for(Entry<Integer, Integer> entry : LineNumTable.entrySet()) {
	    		table[i] = new u2();
	    		table[i+1] = new u2();
	    		LineNumber = entry.getKey();
	    		byteCodeNumber = entry.getValue();
	    		table[i].set((short) byteCodeNumber);
	    		table[i+1].set((short) LineNumber);
	    		i+=2;
	    	}
	    
	    }
}
