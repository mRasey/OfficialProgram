package optimize;

import op.globalArguments;

/*	
 * 没有考虑float、long、double为负数的情况
 * */

public class ReplaceLdc {
	/*
	 * ldc "asdas"  这种字符串赋值参数是以双引号开始的
	 * 字符串的双引号是在提取常量池时去掉的
	 * */
	String number = "";  //记录每条指令前的标号
	String regex = "\\d+:";
	
	
	public void replace(){
		int i = 0;
		String[] byteCode;
		for(i=0;i<globalArguments.traTabByteCodePC;i++){
			byteCode = globalArguments.traTabByteCode.get(i).split(" ");
			number = byteCode[0];
			if(byteCode[0].matches(regex) && byteCode[1].equals("ldc")){
				//不考虑字符串赋值
				if(!byteCode[2].startsWith("\"")){
					//不考虑float的情况
					if(byteCode[2].startsWith("0x")){
						if(byteCode[2].charAt(byteCode[2].length()-1) != 'F'){
							globalArguments.traTabByteCode.set(i, Hex_data(byteCode[2].substring(2)));
						}
					}
					else if(byteCode[2].startsWith("-0x")){
						if(byteCode[2].charAt(byteCode[2].length()-1) != 'F'){
							globalArguments.traTabByteCode.set(i, neg_Hex_data(byteCode[2].substring(3)));
						}
					}
					else{
						globalArguments.traTabByteCode.set(i, Dec_data(byteCode[2]));
					}
				}
			}
		}
	}
	
	public String Dec_data(String data){
		String new_code=number+" ";
		//System.out.println(data);
		int n = Integer.parseInt(data);
		if(n >= 0 && n <=127){
			String byte_str = Integer.toHexString(n);
			if (byte_str.length() == 1) {
				byte_str = "0" + byte_str;
			}
			new_code += "bipush" + " " + "0x" + byte_str;
		}
		else if(n >= 128 && n <= 32767){
			String short_str = Integer.toHexString(n);
			while (short_str.length() < 4) {
				short_str = "0" + short_str;
			}
			new_code += "sipush" + " " + "0x" + short_str;
		}
		else if(n < 0 && n >= -128){
			new_code+="bipush"+" "+"0x"+byte_to_Hex(n);
		}
		else if(n < -128 && n >= -32768){
			new_code+="bipush"+" "+"0x"+short_to_Hex(n);
		}
		else{
			System.err.println("error in replaceLdc/Dec_data");
		}
		
		return new_code;
		
	}
	
	
	/*
	 * 12   0xc(byte)
	 * -12   -0xc(byte)
	 * 128   0x80(short)
	 * -128   -0x80(byte)
	 * 3276   0xccc(short)
	 * */
	
	//正的18进制，先转换为10进制判断用什么指令，补全后在代替原来的指令
	//补全后数据:byte 2位  short 4位  int 8位
	public String Hex_data(String data){
		String new_code=number+" ";
		int n = toUnsignInt(data);
		if (n >= 0 && n <= 127) {
			String byte_str = Integer.toHexString(n);
			if (byte_str.length() == 1) {
				byte_str = "0" + byte_str;
			}
			new_code += "bipush" + " " + "0x" + byte_str;
		}
		else if (n > 127 && n <= 32767) {
			String short_str = Integer.toHexString(n);
			while (short_str.length() < 4) {
				short_str = "0" + short_str;
			}
			new_code += "sipush" + " " + "0x" + short_str;
		} 
		else if (n > 32767 && n< 2147483647) {
			String int_str = Integer.toHexString(n);
			while (int_str.length() < 8) {
				int_str = "0" + int_str;
			}
			new_code = "ldc" + " " + "0x" + int_str;
		} 
		else {
			System.err.println("error in replaceLdc/Hex_data");
		}
		return new_code;
	}
	
	
	public String neg_Hex_data(String data){
		String new_code=number+" ";
		int n = toUnsignInt(data);
		if (n >= 0 && n <= 128) {
			String byte_str = byte_to_Hex(-n);
			new_code += "bipush" + " " + "0x" + byte_str;
		}
		else if (n > 128 && n <= 32768) {
			String short_str = short_to_Hex(-n);
			new_code += "sipush" + " " + "0x" + short_str;
		} 
		else if (n > 32768) {
			String int_str = int_to_Hex(-n);
			new_code += "ldc" + " " + "0x" + int_str;
		} 
		else {
			System.err.println("error in replaceLdc/neg_Hex_data");
		}
		return new_code;
	}
	
	//将16进制字符串无符号扩展成10进制
	public  int toUnsignInt(String str) {  
	        if(str==null||str.length()<1){  
	            throw new RuntimeException("字符串不合法");  
	        }  
	          
	        int sum=0;  
	        int n=0;  
	        //System.out.println(str);
	        for(int i=0;i<str.length();i++){  
	            char c=str.charAt(str.length()-1-i);  
	            if(c>='a'&&c<='f'||c>='A'&&c<='F'){  
	                n=Character.toUpperCase(c)-'A';
	                n+=10;
	            }else if(c>='0'&&c<='9'){  
	                n=c-'0';  
	            }else{  
	                throw new RuntimeException("字符串不合法");  
	            }  
	            sum+=n*(1<<4*i);  
	        }  
	        return sum;  
	    } 
	
	//传进来一个负数，转换为16进制
	public String byte_to_Hex(int n){
		String hex = "";
		int m = 256 + n;
		hex = Integer.toHexString(m);
		while (hex.length() < 2) {
			hex = "f" + hex;
		}
		return hex;
	}
	
	public String short_to_Hex(int n){
		String hex = "";
		int m = 65536 + n;
		hex = Integer.toHexString(m);
		while (hex.length() < 4) {
			hex = "f" + hex;
		}
		return hex;
	}
	public String int_to_Hex(int n){
		String hex = Integer.toHexString(n);
		return hex;
	}
	
}
