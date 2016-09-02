package transToClass;

import op.globalArguments;

public class field_info {
	int id = 0;
    //u2 access_flags = new u2();
    String access_flags = "0000";
	u2 name_index = new u2();
    u2 descriptor = new u2();
    u2 attributes_count = new u2();
//    attribute_info[] attributes = new attribute_info[attributes_count.get() - 1];

    @Override
    public String toString() {
//        String attributes_string = "";
//        for(int i = 0; i < attributes_count.get(); i++)
//            attributes_string += attributes[i].toString();
        return access_flags.toString()
                + name_index.toString()
                + descriptor.toString()
                + attributes_count.toString();
//                + attributes_string;
    }
    
    public void set_info(int field_id){
    	this.id = field_id;
    	access_flags = set_flag();
    	int n = 0;
    	n = globalArguments.fieldName_conpool_number.get(field_id);
    	name_index.set((short) n);
    	n = globalArguments.fieldType_conpool_number.get(field_id);
    	descriptor.set((short) n);
    	attributes_count.set((short) 0);
    	
    }
    
    public String set_flag(){
		String acc_flag = "0000";
		char[] bstr = "0000000000000000".toCharArray();
		int i = 0;
		for (i = 1; i < globalArguments.field_info.get(id).size()-1; i++) {
			switch (globalArguments.field_info.get(id).get(i)) {
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
			case "voilatie":
				bstr[9] = '1';
				break;
			case "transient":
				bstr[8] = '1';
				break;
			case "synthetic":
				bstr[3] = '1';
				break;
			case "annotation":
				bstr[2] = '1';
				break;
			case "enum":
				bstr[1] = '1';
				break;
			default:
				System.out.println("error in field/set_access_flags");
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
