package op;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputByteCodeFile {
	static String byteCodeSavePath = "res/result.txt";
	
	
	public void print() throws IOException{
		File byteCodeFile = new File(byteCodeSavePath);
		FileWriter fw = new FileWriter(byteCodeFile.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		int outputTypeCodeNumber = 0;
		
		bw.write("Constant pool:"+"\n");
		for(int i=1;i<globalArguments.const_id;i++){
			bw.write("#"+i+" = "+globalArguments.const_id_type.get(i) +" "+globalArguments.const_id_value.get(i)+"\n");
		}
		bw.write("\n");
		bw.write("{"+"\n");
        for(;outputTypeCodeNumber<globalArguments.traTabByteCodePC;outputTypeCodeNumber++) {
        	bw.write(globalArguments.traTabByteCode.get(outputTypeCodeNumber) +"\n");
        }
        bw.write("}"+"\n");
        bw.close();
	}
}
