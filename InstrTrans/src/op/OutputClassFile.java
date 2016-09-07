package op;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class OutputClassFile {
	static String ClassFileSavePath = "res/MainActivity.class";
	static final char[] HEX_CHAR_TABLE = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	static final byte[] HEX_TABLE = {0,0x1,0x2,0x3,0x4,0x5,0x6,0x7,0x8,0x9,0xA,0xB,0xC,0xD,0xE,0xF}; 
	
	public void print(String code) throws IOException{
		int i = 0;	
		byte[] buf = hexToByte(code);
        
        File file = new File(ClassFileSavePath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buf);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public byte[] hexToByte(String hexString){  
        if(hexString==null || hexString.length()==0)return null;  
        if(hexString.length()%2!=0)throw new RuntimeException();  
        byte[] data = new byte[hexString.length()/2];  
        char[] chars = hexString.toCharArray();  
        for(int i=0;i<hexString.length();i=i+2){  
            data[i/2] = (byte)(HEX_TABLE[getHexCharValue(chars[i])]<<4 | HEX_TABLE[getHexCharValue(chars[i+1])]);  
        }  
        return data;  
    }  
	
	private int getHexCharValue(char c){  
        int index = 0;  
        for(char c1: HEX_CHAR_TABLE){  
            if(c==c1){  
                return index;  
            }  
            index++;  
        }  
        return 0;  
    }  
}
