package transToClass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import op.globalArguments;

public class ClassFile {
    u4 magic = new u4();// 魔数 0xCAFEBABE
    u2 minor_version = new u2();// class文件副版本号
    u2 major_version = new u2();// class文件主版本号
    u2 constant_pool_count = new u2();// 常量池计数器
    cp_info[] constant_pool;// 常量池
    //u2 access_flags = new u2();// 访问标志
    String access_flags = "0020";
    u2 this_class = new u2();// 类索引
    u2 super_class = new u2();// 父类索引
    u2 interfaces_count = new u2();// 接口计数器
    u2[] interfaces;// 接口表
    u2 fields_count = new u2();// 字段计数器
    field_info[] fields;// 字段表
    u2 method_count = new u2();// 方法计数器
    method_info[] methods;// 方法表
    u2 attributes_count = new u2((short) 0);// 属性计数器
    attribute_info[] attributes;// 属性表
    
    ArrayList<String> byteCodeData = new ArrayList<>();
    
    public ClassFile() throws IOException {
    	readByteCodeFile();
    	magic.set(-889275714);
    	minor_version.set((short) 0);
    	major_version.set((short) 50);
    	constant_pool_count.set((short) globalArguments.const_id);
    	constant_pool = new cp_info[constant_pool_count.get() - 1];
    	fill_constant_pool();
    	access_flags = set_access_flags();
    	this_class.set((short) 1);
    	super_class.set((short) 2);
    	interfaces_count.set((short) globalArguments.inter_count);
    	interfaces = new u2[interfaces_count.get()];
    	fill_interfaces();
    	fields_count.set((short) globalArguments.field_count);
    	fields = new field_info[fields_count.get()];
    	fill_fields();
    	method_count.set((short) globalArguments.method_count);
    	methods = new method_info[method_count.get()];
    	fill_methods();
    }

    @Override
    public String toString() {
        String constant_pool_string = "";
        for(int i = 0; i < constant_pool_count.get()-1; i++)
            constant_pool_string += constant_pool[i].toString();
        String interfaces_string = "";
        for(int i = 0; i < interfaces_count.get(); i++)
            interfaces_string += interfaces[i].toString();
        String fields_string = "";
        for(int i = 0; i < fields_count.get(); i++)
            fields_string += fields[i].toString();
        String methods_string = "";
        for(int i = 0; i < method_count.get(); i++) {
            methods_string += methods[i].toString();
        }
        String attributes_string = "";
        for(int i = 0; i < attributes_count.get(); i++)
            attributes_string += attributes[i].toString();
        return magic.toString()
                + minor_version.toString()
                + major_version.toString()
                + constant_pool_count.toString()
                + constant_pool_string
                + access_flags
                + this_class.toString()
                + super_class.toString()
                + interfaces_count.toString()
                + interfaces_string
                + fields_count.toString()
                + fields_string
                + method_count.toString()
                + methods_string
                + attributes_count.toString()
                + attributes_string;
    }

    public void print() throws IOException{
    	
    	File byteCodeFile = new File("res/check.txt");
		FileWriter fw = new FileWriter(byteCodeFile.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
    	
    	int i=0,j=0;
    	bw.write("magic "+ magic.toString()+"\n");
    	bw.write("version: "+ minor_version.toString()+major_version.toString()+"\n");
    	bw.write("constant_pool_count: "+ constant_pool_count.toString()+"\n");
    	bw.write("constant_pool:"+"\n");
    	while(!byteCodeData.get(j).equals("Constant pool:")){
    		j++;
    	}
    	j++;
    	for(i = 0; i < constant_pool_count.get()-1; i++){
    		bw.write(byteCodeData.get(j++)+"\n");
    		bw.write(constant_pool[i].toString()+"\n");
    	}
    		
    	bw.write("access_flags: "+access_flags+"\n");
    	bw.write("this_class: "+this_class.toString()+"\n");
    	bw.write("super_class: "+super_class.toString()+"\n");
    	
    	
    	
    	bw.write("interfaces_count: "+ interfaces_count.toString()+"\n");
    	bw.write("interfaces:"+"\n");
    	for(i = 0; i < interfaces_count.get(); i++){
    		bw.write(globalArguments.inter_name.get(i)+"\n");
    		bw.write("inter_conpool_number:"+globalArguments.inter_conpool_number.get(i)+"\n");
    		bw.write(interfaces[i].toString()+"\n");
    	}
    		
    	
    	bw.write("fields_count: "+ fields_count.toString()+"\n");
    	bw.write("fields:"+"\n");
    	for(i = 0; i < fields_count.get(); i++){
    		bw.write(globalArguments.field_info.get(i)+"\n");
    		bw.write("fieldName_conpool_number:"+globalArguments.fieldName_conpool_number.get(i)+"\n");
    		bw.write("fieldType_conpool_number:"+globalArguments.fieldType_conpool_number.get(i)+"\n");
    		String temp = fields[i].toString();
    		for(j=0;j<temp.length();j+=4){
    			bw.write(temp.substring(j,j+4)+" ");
    		}
    		bw.write("\n");
    	}
    		
    	
    	bw.write("method_count: "+ method_count.toString()+"\n");
    	bw.write("method:"+"\n");
    	for(i = 0; i < method_count.get(); i++){
    		bw.write(globalArguments.method_info.get(i)+"\n");
    		bw.write("methodName_conpool_number:"+globalArguments.methodName_conpool_number.get(i)+"\n");
    		bw.write("methodType_conpool_number:"+globalArguments.methodType_conpool_number.get(i)+"\n");
    		String temp = methods[i].toString();
    		for(j=0;j<16;j+=4){
    			bw.write(temp.substring(j,j+4)+" ");
    		}
    		for(;j<temp.length();j+=2){
    			bw.write(temp.substring(j,j+2)+" ");
    		}
    		bw.write("\n");
    	}
    		
    	
    	bw.write("attributes_count: "+ attributes_count.toString()+"\n");
    	bw.write("attributes:"+"\n");
    	for(i = 0; i < attributes_count.get(); i++)
    		bw.write(attributes[i].toString()+"\n");
    	
    	bw.close();
    }
    
    public void readByteCodeFile(){
    	String byteCodeFilePath = "res/result.txt";
    	File file = new File(byteCodeFilePath);
    	FileInputStream fis = null;
    	InputStreamReader isr = null;
    	BufferedReader br = null; 
    	try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		isr = new InputStreamReader(fis);
		br = new BufferedReader(isr);
		
		String str;
		try {
			while((str = br.readLine()) != null){
				if(!str.equals("")){
					byteCodeData.add(str);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void fill_constant_pool(){
    	int i = 0;
    	int j = 0;
    	while(!byteCodeData.get(i).equals("Constant pool:")){
    		i++;
    	}
    	i++;
    	while(!byteCodeData.get(i).equals("{")){
    		constant_pool[j] = new cp_info(byteCodeData.get(i));
    		i++;
    		j++;
    	}
    }

	public void fill_interfaces() {
		int i = 0, temp = 0;
		for(i=0;i<globalArguments.inter_count;i++){
			interfaces[i] = new u2();
			temp = globalArguments.inter_conpool_number.get(i);
			interfaces[i].set((short)temp);
		}
	}

	public void fill_fields(){
		int i = 0;
		for(i=0;i<globalArguments.field_count;i++){
			fields[i] = new field_info();
			fields[i].set_info(i);
		}
	}
	
	public void fill_methods() throws IOException{
		new Matchup().buildTransCode();
		int i = 0;
		for(i=0;i<globalArguments.method_count;i++){
			methods[i] = new method_info();
			 methods[i].set_info(i);
		}
	}
	
	
	public String set_access_flags(){
		 String acc_flag = "0020";
		 char[] bstr = "0000000000100000".toCharArray();
		 int i = 0;
		 for(i=0;i<globalArguments.classProPerty.size();i++){
			 switch(globalArguments.classProPerty.get(i)){
	    		case "public":
	    			bstr[15]='1';
	    			break;
	    		case "final":
	    			bstr[11]='1';
	    			break;
	    		case "super":
	    			bstr[10]='1';
	    			break;
	    		case "interface":
	    			bstr[6]='1';
	    			break;
	    		case "abstact":
	    			bstr[5]='1';
	    			break;
	    		case "synthetic":
	    			bstr[3]='1';
	    			break;
	    		case "annotation":
	    			bstr[2]='1';
	    			break;
	    		case "enum":
	    			bstr[1]='1';
	    			break;
	    		default:
	    			System.err.println("error in set_access_flags");
	    			break;
	    	}
		 }
		 
		StringBuffer htmp = new StringBuffer(); 
		String temp = "";
	    for(i=0;i<16;i++){
	    	temp+=bstr[i];
	    }
	    int iTmp = 0;  
		for (i = 0; i < temp.length(); i += 4) {
			iTmp = 0;
			for (int j = 0; j < 4; j++) {
				iTmp += Integer.parseInt(temp.substring(i + j, i + j + 1)) << (4 - j - 1);
			}
			htmp.append(Integer.toHexString(iTmp));
		}
	    acc_flag=htmp.toString();
		 
	    return acc_flag;
	   }

}
