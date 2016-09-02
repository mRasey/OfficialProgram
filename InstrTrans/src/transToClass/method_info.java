package transToClass;

import op.globalArguments;

import static op.globalArguments.method_codes;

public class method_info {
    //u2 access_flags = new u2();
	String access_flags = "0000";
    u2 name_index = new u2();
    u2 descriptor_index = new u2();
    u2 attributes_count = new u2((short) 1);
    attribute_info[] attributes = new attribute_info[1];

    int id = 0;
    
    @Override
    public String toString() {
        String attributes_string = "";
        for(int i = 0; i < attributes_count.get(); i++)
            attributes_string += attributes[i].toString();
        return access_flags.toString()
                + name_index.toString()
                + descriptor_index.toString()
                + attributes_count.toString()
                + attributes_string;
    }
    
    public void set_info(int method_id){
    	this.id = method_id;
    	access_flags = set_flag();
    	int n = 0;
    	n = globalArguments.methodName_conpool_number.get(method_id);
    	name_index.set((short) n);
    	n = globalArguments.methodType_conpool_number.get(method_id);
    	descriptor_index.set((short) n);
    	
    	
    	attributes_count.set((short) 1);
        //code 属性
        String codes = method_codes.get(method_id);
        u1[] u1s = new u1[codes.length()];
        //给u1数组赋值
        for(int i = 0; i < codes.length(); i = i + 2) {
            u1s[i] = new u1(Byte.parseByte(codes.charAt(i) + codes.charAt(i + 1) + ""));
        }
    	attributes[0] = new attribute_info(new u2((short) 3), new u4(codes.length() / 2), u1s);// code属性
    }
    
    public String set_flag(){
		String acc_flag = "0000";
		char[] bstr = "0000000000000000".toCharArray();
		int i = 0;
		for (i = 1; i < globalArguments.method_info.get(id).size()-1; i++) {
			//System.out.println(globalArguments.method_info.get(id).get(i));
			switch (globalArguments.method_info.get(id).get(i)) {
			case "public":
				bstr[15] = '1';
				break;
			case "private":
				bstr[14] = '1';
				break;
			case "protected":
				bstr[13] = '1';
				break;
			case "static":
				bstr[12] = '1';
				break;
			case "final":
				bstr[11] = '1';
				break;
			case "synchronized":
				bstr[10] = '1';
				break;
			case "bridge":
				bstr[9] = '1';
				break;
			case "varargs":
				bstr[8] = '1';
				break;
			case "native":
				bstr[7] = '1';
				break;
			case "abstract":
				bstr[5] = '1';
				break;
			case "strict":
				bstr[4] = '1';
				break;
			case "synthetic":
				bstr[3] = '1';
				break;
			default:
				System.out.println("error in method/set_access_flags");
				break;
			}
		}

		StringBuffer htmp = new StringBuffer();
		String temp = "";
		for (i = 0; i < 16; i++) {
			temp += bstr[i];
		}
		int iTmp = 0;
		for (i = 0; i < temp.length(); i += 4) {
			iTmp = 0;
			for (int j = 0; j < 4; j++) {
				iTmp += Integer.parseInt(temp.substring(i + j, i + j + 1)) << (4 - j - 1);
			}
			htmp.append(Integer.toHexString(iTmp));
		}
		acc_flag = htmp.toString();
		return acc_flag;
    }

    
}
