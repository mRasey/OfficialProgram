package optimize;

import op.globalArguments;

/*
 * 将bipush,sipush,ldc2_w的数据补全并转换成16进制
 * ldc 的float负数
 * ldc2_w的long、double负数
 * smail里面float的负号  取反加1就是正确的16进制
 * */

public class CompleteData {
	
	String number = "";
	String regex = "\\d+:";
	

	public void complete(){
		int i = 0;
		String[] byteCode;
		for(i=0;i<globalArguments.traTabByteCodePC;i++){
			byteCode = globalArguments.traTabByteCode.get(i).split(" ");
			number = byteCode[0];
			if(byteCode[0].matches(regex) && byteCode[1].equals("bipush")){
				globalArguments.traTabByteCode.set(i, bipush_comp(byteCode[2]));
			}
			else if(byteCode[0].matches(regex) && byteCode[1].equals("sipush")){
				globalArguments.traTabByteCode.set(i, sipush_comp(byteCode[2]));
			}
			else if(byteCode[0].matches(regex) && byteCode[1].equals("ldc2_w")){
				//不考虑字符串赋值
				if(!byteCode[2].startsWith("\"")){
					//去掉末尾的L
					globalArguments.traTabByteCode.set(i, ldc2_w_comp(byteCode[2].replace("L", "")));
				}	
			}
			else if(byteCode[0].matches(regex) && byteCode[1].equals("ldc")){
				//不考虑字符串赋值
				if(!byteCode[2].startsWith("\"")){
					globalArguments.traTabByteCode.set(i, ldc_comp(byteCode[2]));
				}	
			}
		}
	}
	
	
	public String bipush_comp(String data){
		String new_code = number+" "+"bipush" + " ";
		if(data.startsWith("0x")){
			if(data.length() == 4){
				new_code+=data;
			}
			else{
				data = "0x0" + data.charAt(2);
				new_code+=data;
			}
		}
		else if(data.startsWith("-0x")){
			int n = toUnsignInt(data.substring(3));
			String byte_str = byte_to_Hex(-n);
			new_code +="0x" + byte_str;
		}
		else{
			//System.out.println(data);
			int n = Integer.parseInt(data);
			if(n >= 0){
				String byte_str = Integer.toHexString(n);
				if (byte_str.length() == 1) {
					byte_str = "0" + byte_str;
				}
				new_code += "0x" + byte_str;
			}
			else{
				new_code+="0x"+byte_to_Hex(n);
			}
		}
		return new_code;
	}
	
	public String sipush_comp(String data){
		String new_code = number+" "+"sipush" + " ";
		if(data.startsWith("0x")){
			if(data.length() == 6){
				new_code+=data;
			}
			else{
				while(data.length() < 6){
					data = "0x0" + data.substring(2);
				}
				new_code+=data;
			}
		}
		else if(data.startsWith("-0x")){
			int n = toUnsignInt(data.substring(3));
			String short_str = short_to_Hex(-n);
			new_code +="0x" + short_str;
		}
		else{
			int n = Integer.parseInt(data);
			if(n >= 0){
				String short_str = Integer.toHexString(n);
				while (short_str.length() < 4) {
					short_str = "0" + short_str;
				}
				new_code += "0x" + short_str;
			}
			else{
				new_code+="0x"+short_to_Hex(n);
			}
		}
		return new_code;
	}
	
	public String ldc2_w_comp(String data){
		String new_code = number+" "+"ldc2_w" + " ";
		if(data.startsWith("-0x")){
			while(data.length() < 19){
				data = "-0x0" + data.substring(3);
			}
			new_code += long_neg_to_pos(data.substring(3))+"L";
		}
		else{
			while(data.length() < 18){
				data = "0x0" + data.substring(2);
			}
			new_code+=data+"L";
		}
		return new_code;
	}
	
	public String ldc_comp(String data){
		String new_code = number+" "+"ldc" + " ";
		//float只处理负数的情况，float位数应该正好
		if(data.charAt(data.length()-1) == 'F' && data.startsWith("-0x")){
			//不传进去末尾的F和前面和前面的-0x
			new_code+=float_neg_to_pos(data.substring(3,data.length()-1))+"F";
		}
		else{
			new_code+=data;
		}
		return new_code;
	}
	
	//传进来一个负数，转换为16进制
	public String byte_to_Hex(int n) {
		String hex = "";
		int m = 256 + n;
		hex = Integer.toHexString(m);
		while (hex.length() < 2) {
			hex = "f" + hex;
		}
		return hex;
	}

	public String short_to_Hex(int n) {
		String hex = "";
		int m = 65536 + n;
		hex = Integer.toHexString(m);
		while (hex.length() < 4) {
			hex = "f" + hex;
		}
		return hex;
	}
	
	//输入8个字符转换为正确的8个字符
	public String float_neg_to_pos(String data){
		String new_data = "0x";
		if(data.length() != 8){
			System.err.println("error in completeData");
		}
		//16进制->2进制
		String bString = "", btmp;
		int i = 0;
	    for (i = 0; i < data.length(); i++)  
	    {  
	        btmp = "0000"  
	                    + Integer.toBinaryString(Integer.parseInt(data  
	                            .substring(i, i + 1), 16));  
	        bString += btmp.substring(btmp.length() - 4);  
	    }
	    //2进制取反+1
	    char[] temp = bString.toCharArray();
	    for(i=0;i<32;i++){
	    	if(temp[i] == '0'){
	    		temp[i] = '1';
	    	}
	    	else{
	    		temp[i] = '0';
	    	}
	    }
	    i = 31;
	    while(temp[i] == '1'){
	    	temp[i] = '0';
	    	i--;
	    }
	    temp[i] = '1';
	    bString = "";
	    for(i=0;i<32;i++){
	    	bString+=temp[i];
	    }
	    //2进制-> 16进制
	    StringBuffer htmp = new StringBuffer();  
        int iTmp = 0;  
        for (i = 0; i < bString.length(); i += 4)  
        {  
            iTmp = 0;  
            for (int j = 0; j < 4; j++)  
            {  
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);  
            }  
            htmp.append(Integer.toHexString(iTmp));  
        }
		
        new_data+=htmp.toString();
		return new_data;
	}
	
	//输入16bits
	public String long_neg_to_pos(String data){
		String new_data = "0x";
		if(data.length() != 16){
			System.err.println("error in completeData");
		}
		//16进制->2进制
		String bString = "", btmp;
		int i = 0;
	    for (i = 0; i < data.length(); i++)  
	    {  
	        btmp = "0000"  
	                    + Integer.toBinaryString(Integer.parseInt(data  
	                            .substring(i, i + 1), 16));  
	        bString += btmp.substring(btmp.length() - 4);  
	    }
	    //2进制取反+1
	    char[] temp = bString.toCharArray();
	    for(i=0;i<64;i++){
	    	if(temp[i] == '0'){
	    		temp[i] = '1';
	    	}
	    	else{
	    		temp[i] = '0';
	    	}
	    }
	    i = 63;
	    while(i>=0 && temp[i] == '1'){
	    	temp[i] = '0';
	    	i--;
	    }
	    temp[i] = '1';
	    bString = "";
	    for(i=0;i<64;i++){
	    	bString+=temp[i];
	    }
	    //2进制-> 16进制
	    StringBuffer htmp = new StringBuffer();  
        int iTmp = 0;  
        for (i = 0; i < bString.length(); i += 4)  
        {  
            iTmp = 0;  
            for (int j = 0; j < 4; j++)  
            {  
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);  
            }  
            htmp.append(Integer.toHexString(iTmp));  
        }
		
        new_data+=htmp.toString();
		return new_data;
	}

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
}
