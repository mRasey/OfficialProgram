package transToClass;

import java.util.ArrayList;
import java.util.HashMap;

import op.globalArguments;

public class LVT_attribute_info {
	    u2 attribute_name_index = new u2((short) 7);
	    u4 attribute_length = new u4();  //后面这些东西的总长
	    u2 attribute_count = new u2();
	    u2 [] table;
	    
	    int method_id = 0;
	    ArrayList<Integer> local_name_index;
	    ArrayList<Integer> local_type_index;
	    ArrayList<String> local_line;
	    HashMap<Integer, Integer> line_table;
	    int end_num; //最后一个line的标号
	    public LVT_attribute_info(int method_id) {
	    	this.method_id = method_id;
	    	
	    	local_name_index = globalArguments.local_name_index.get(method_id);
	    	local_type_index = globalArguments.local_type_index.get(method_id);
	    	local_line = globalArguments.method_line.get(method_id);
	    	line_table = globalArguments.lineNumberTables.get(method_id);
	    	end_num = globalArguments.method_end_num.get(method_id);
	    	init();
	    }
	    
	    public void init(){
	    	int order = 0; 
	    	int tbc = 0;
	    	int i = 0;
	    	attribute_count.set((short) local_name_index.size());
	    	attribute_length.set(10*local_name_index.size() +2);
	    	
	    	table = new u2[5*local_name_index.size()];
	    	table[0] = new u2((short) 0);
	    	table[1] = new u2((short) (end_num+1));
	    	table[2] = new u2((short) 8);
	    	table[3] = new u2((short) 9);
	    	table[4] = new u2((short) order);
	    	tbc = 5;
	    	order ++;
	    	int lineNum;
	    	int t;
	    	int ni,ti;
	    	for(i=1;i<local_name_index.size();i++){
	    		lineNum = Integer.parseInt(local_line.get(i).split(" ")[1]);
	    		t = line_table.get(lineNum);
	    		table[tbc++] = new u2((short) t);
	    		table[tbc++] = new u2((short) (end_num - t + 1));
	    		ni = local_name_index.get(i);
	    		ti = local_type_index.get(i);
	    		table[tbc++] = new u2((short) ni);
	    		table[tbc++] = new u2((short) ti);
	    		table[tbc++] = new u2((short) order++);
	    	}
	    }

	    @Override
	    public String toString() {  
	    	String str="";
	    	int i = 0;
	    	for(i=0;i<table.length;i++){
	    		str+=table[i].toString();
	    	}
	        return attribute_name_index.toString()
	                + attribute_length.toString()
	                + attribute_count.toString()
	                + str;
	               
	    }
}
