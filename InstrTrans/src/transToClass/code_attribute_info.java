package transToClass;

public class code_attribute_info {
	    u2 attribute_name_index = new u2();
	    u4 attribute_length = new u4();  //后面这些东西的总长
	    u2 max_stack = new u2();
	    u2 max_locals = new u2();
	    u4 code_length = new u4();
	    String code ;
	    u2 exception_table_length = new u2((short) 0);
	    exception_table_inf[] eti; 
	    u2 attribute_count = new u2((short) 2);
	    LNT_attribute_info LineNumTab_attribute;
	    LVT_attribute_info LocalVarTab_attribute;
	    String eti_str = "";
	    
	    int method_id = 0;
	    

	    public code_attribute_info(int method_id,int attribute_name_index, int max_stack, int max_locals,int code_length, String code,int exception_table_length) {
	        this.method_id = method_id;
	    	this.attribute_name_index.set((short) attribute_name_index);
	        this.max_stack.set((short) max_stack);
	        this.max_locals.set((short) max_locals);
	        this.code_length.set(code_length);
	        this.code = code;
	        this.exception_table_length.set((short) exception_table_length);
	        int i = 0;
	        for(i=0;i<exception_table_length;i++){
	        	eti[i]=new exception_table_inf();
	        	eti_str+=eti[i].toString();
	        }
	        LineNumTab_attribute = new LNT_attribute_info(method_id);
	        LocalVarTab_attribute = new LVT_attribute_info(method_id);
	        int sum = 2+2+4+code.length()/2 +2 + eti_str.length()/2 + 2 + LineNumTab_attribute.toString().length()/2 + LocalVarTab_attribute.toString().length()/2;
	        this.attribute_length.set(sum);
	        
	       // print();
	    }

	    @Override
	    public String toString() {  
	        return attribute_name_index.toString()
	                + attribute_length.toString()
	                + max_stack.toString()
	                + max_locals.toString()
	                + code_length.toString()
	                + this.code
	        		+ exception_table_length.toString()
	        		+ eti_str
	        		+ attribute_count.toString()
	        		+ LineNumTab_attribute.toString()
	        		+ LocalVarTab_attribute.toString();
	    }
	    
	    public void print(){
	    	String str;
	    	int i = 0;
	    	str = code;
	    	System.out.println(code);
	    	for(i=0;i<str.length();i+=2){
	    		System.out.print(str.substring(i, i+2) + " ");
	    	}
	    	System.out.println("");
	    }
}
